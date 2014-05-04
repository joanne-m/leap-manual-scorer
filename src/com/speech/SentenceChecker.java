/**
 * ==============================================================================
 * File:			SentenceChecker.java
 * Package:			org.isip.states.speech
 * Created:			2013/09/04
 * Last Changed:	2013/09/04
 * Author:			Reginald Almonte
 * ==============================================================================
 * This code is copyright (c) 2013 Learning English Application for Pinoys (LEAP)
 * 
 * History:
 * -- add revision history here --
 * 
 */

package com.speech;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


import com.util.Constants;
//import com.dostproject7.util.GameFlow;

/**
 * Computes score for sentence/paragraph exercises. It uses word recognition
 * engine and trained logistic regression systems to compute the score of the
 * recorded file based on the 7 criteria: pronunciation and articulation,
 * fluency and blending, understandability, intonation, phrasing, stress and
 * emphasis. {@link com.dostproject7.speech.SentenceChecker SentenceChecker}.
 * 
 * @author Ayvee Mallare
 * @version 1.0
 * 
 */

public class SentenceChecker {


	/** the level of accepted "correct" pronunciation and articulation **/
	private static double scoreThreshold = 0.80;

	/**
	 * the parameters set by the speech recognizer. The variables in comment are
	 * parameters that are not yet used but are also available from the speech
	 * recognizer
	 */
	private static String[] wordASR = null;
	// private static String decoded = "";
	private static String errIndx = "";
	private static String[] mispron = null;
	private static String misIndx = "";
	private static String[] wer_stat = new String[7];
	private static double wer = 0.0;
	// private static int hit = 0;
	// private static int sub = 0;
	// private static int mis = 0;
	// private static int del = 0;
	// private static int ins = 0;
	private static String[] scores = new String[7];
	private static double decscore = 0.0;
	// private static double hypscore = 0.0;
	// private static double hypAscore = 0.0;
	// private static double hypLscore = 0.0;
	// private static double corscore = 0.0;
	// private static double corAscore = 0.0;
	// private static double corLscore = 0.0;
	private static String[] spkRate = new String[3];
	// private static int nWords = 0;
	private static int nPhns = 0;
	private static int nFrms = 0;
	private static String[] silRate = new String[2];
	// private static int nSil = 0;
	private static int totSil = 0;
	// private static String detail = "";

	/** the computed init score **/
	private static double initScore = 0.0;
	/** the computed pronunciation and articulation score **/
	private static float pronAndArtScore = (float) 0.0;
	/** fluency and blending score **/
	private static float fluencyAndBlendScore = (float) 0.0;
	/** understandability score **/
	private static float undScore = (float) 0.0;
	/** stress score **/
	private static float stressScore = (float) 0.0;
	/** phrasing score **/
	private static float phrasingScore = (float) 0.0;
	/** emphasis score **/
	private static float emphasisScore = (float) 0.0;
	/** intonation score **/
	private static float intonationScore = (float) 0.0;
	/** scores for all the criteria */
	private static float[] critScores;

	/** parameters for computing initScore and other scores **/
	private final static double wer_mean = 13.048951;
	private final static double wer_max = 90.00;
	private final static double dec_mean = 23368.527943;
	private final static double dec_max = 65652.523400;
	private final static double[] theta0 = { 2.3552, -0.9000, 7.9392 };
	private final static double alpha0 = 0.1;
	private final static double totSil_mean = 24.724832;
	private final static double totSil_max = 705.000000;
	private final static double spkrate_mean = 0.108654;
	private final static double spkrate_max = 0.167102;
	private static final double alpha1 = 0.75;
	private static final double[] theta1 = { 2.2668, -22.0344, -5.4722 };
	private static Integer[] scoreWeight = { 2, 2, 1, 0, 0, 0, 0 };

	/**
	 * Default empty constructor
	 */
	private SentenceChecker() {

	}

	/**
	 * Returns the total score of the recorded wav file for each criterion
	 * 
	 * @param lesson
	 *            the lesson containing the wav file
	 * @param itemscore
	 *            the total points per item
	 * @return the computed score
	 */
	public static double checkSentence(String lesson, Integer itemscore, double delta) {
		double sentenceScore = 0.0;
		boolean withStress = false;
		Integer baseScore = 5;
		String[] ans = new String[1];

		System.out.println("*** Check Sentence ***");

		/** 1. Initialize variables */

		if (critScores == null) {
			critScores = new float[7];
		} else {
			for (int i = 0; i < critScores.length; i++) {
				critScores[i] = 0;
			}
		}
		for (int i = 0; i < scoreWeight.length; i++) {
			scoreWeight[i] = 0;
		}
		try {
			ans = SpeechScorer.answers.split(" ");
		} catch (Exception e) {
			System.out.println("Unable to initialize String array \"answers\".\n"
					+ "Score might be insconstent");
		}

		/** 2. Get the speech parameters from the speech recognizer */
		if (SpeechScorer.scoringType.equals(Constants.SP_SC_CHECKSTRESS)) {
			withStress = true;
		}
		try {
			System.out.println("Calling getWordASR()");
			System.out.println("> Reference Words = " + SpeechScorer.refWords);
			System.out.println("> Wave File = " + SpeechScorer.testWav);
			System.out.println("> Lesson = " + lesson);
			System.out.println("> With Stress = " + withStress);
			wordASR = getWordASR(SpeechScorer.testWav, lesson, withStress);
		} catch (Exception e) {
			System.out.println(
					"Failed to compute value for wordASR using getWordASR(). Return score = 0.");
			return 0;
		}

		/**
		 * 3. Set up the values of variables from the extracted parameters from
		 * the speech recognizer. NOTE: the variables in comment are the other
		 * available parameters that are not yet used
		 * */
		if (wordASR[0] == null) {
			System.out.println("Failed to recognize speech in getWordASR(). Assume file is invalid. Return score = 0.");
			int i = 0;
			SpeechScorer.err = "0";
			for (i = 1; i < SpeechScorer.refWords.split(" ").length; i++) {
				SpeechScorer.err = SpeechScorer.err + "," + String.valueOf(i);
			}

			System.out.println("OUTPUT: SpeechScoring.err = " + SpeechScorer.err);
			System.out.println("OUTPUT: Score = 0");
			return 0;
		}

		errIndx = wordASR[1];
		misIndx = wordASR[2];
		if (wordASR[3].indexOf(";") > -1)
			mispron = wordASR[3].split(";");
		else {
			mispron = new String[1];
			mispron[0] = wordASR[3];
		}
		wer_stat = wordASR[4].split(" ");
		scores = wordASR[5].split(" ");
		spkRate = wordASR[6].split(" ");
		silRate = wordASR[7].split(" ");
		String corrPath = wordASR[8];

		SpeechScorer.decoded = wordASR[0];
		SpeechScorer.err = wordASR[1].replace(" ", ",");
		SpeechScorer.recogScore = 100.0 - Double.valueOf(wer_stat[0]);

		/**
		 * SpeechScorer.feat: 
		 *  0 wer - word error rate
		 *  1 hit 
		 *  2 sub 
		 *  3 mis 
		 *  4 del 
		 *  5 ins 
		 *  6 decscore 
		 *  7 hypscore 
		 *  8 hypAscore 
		 *  9 hypLscore 
		 *  10 corscore 
		 *  11 corAscore 
		 *  12 corLscore 
		 *  13 nWrds 
		 *  14 nPhns 
		 *  15 nFrms 
		 *  16 nSil 
		 *  17 totSil
		 * 18 errIndx 
		 * 19 misIndx
		 * */

		SpeechScorer.feat = new String[20];
		SpeechScorer.feat[0] = wer_stat[0];
		SpeechScorer.feat[1] = wer_stat[1];
		SpeechScorer.feat[2] = wer_stat[2];
		SpeechScorer.feat[3] = wer_stat[3];
		SpeechScorer.feat[4] = wer_stat[4];
		SpeechScorer.feat[5] = wer_stat[5];
		SpeechScorer.feat[6] = scores[0];
		SpeechScorer.feat[7] = scores[1];
		SpeechScorer.feat[8] = scores[2];
		SpeechScorer.feat[9] = scores[3];
		SpeechScorer.feat[10] = scores[4];
		SpeechScorer.feat[11] = scores[5];
		SpeechScorer.feat[12] = scores[6];
		SpeechScorer.feat[13] = spkRate[0];
		SpeechScorer.feat[14] = spkRate[1];
		SpeechScorer.feat[15] = spkRate[2];
		SpeechScorer.feat[16] = silRate[0];
		SpeechScorer.feat[17] = silRate[1];
		SpeechScorer.feat[18] = wordASR[1];
		SpeechScorer.feat[19] = wordASR[2];

		SpeechScorer.refLen = Integer.valueOf(spkRate[0]);

		SpeechScorer.err = errIndx.concat(" ")
				.concat(misIndx.replace(";", " "));

		wer = Double.valueOf(wer_stat[0]);
		// int hit = Integer.valueOf(wer_stat[1]);
		// int sub = Integer.valueOf(wer_stat[2]);
		// int mis = Integer.valueOf(wer_stat[3]);
		// int del = Integer.valueOf(wer_stat[4]);
		// int ins = Integer.valueOf(wer_stat[5]);

		decscore = Double.valueOf(scores[0]);
		// double hypscore = Double.valueOf(scores[1]);
		// double hypAscore = Double.valueOf(scores[2]);
		// double hypLscore = Double.valueOf(scores[3]);
		// double corscore = Double.valueOf(scores[4]);
		// double corAscore = Double.valueOf(scores[5]);
		// double corLscore = Double.valueOf(scores[6]);

		// int nWords = Integer.valueOf(spkRate[0]);
		nPhns = Integer.valueOf(spkRate[1]);
		nFrms = Integer.valueOf(spkRate[2]);

		// int nSil = Integer.valueOf(silRate[0]);
		totSil = Integer.valueOf(silRate[1]);

		/** 3. Organize the errors: put the key index in front of the array */
		for (int i = 0; i < ans.length; i++) {
			if (SpeechScorer.err.indexOf(ans[i]) > -1) {
				SpeechScorer.err = SpeechScorer.err.replace(ans[i], "");
				SpeechScorer.err = ans[i] + " " + SpeechScorer.err;
				SpeechScorer.err = SpeechScorer.err.replace("  ", " ");
				SpeechScorer.keyErrLen++;
				SpeechScorer.err = SpeechScorer.err.trim();
			}
		}
		SpeechScorer.err = SpeechScorer.err.replace(" ", ",");

		/**
		 * 4. Get the seven scores and set the weights of each score depending
		 * on the exercise and scoring type
		 */

		getInitScore(lesson);
		pronAndArtScore = (float) (getArtScore(ans, delta) + getPronScore(ans, delta))
				/ (float) 2;
		fluencyAndBlendScore = (float) getFluencyAndBlendScore(lesson, delta);
		undScore = (float) getUndScore();

		scoreWeight[0] = 1;
		scoreWeight[1] = 1;
		scoreWeight[2] = 1;
		stressScore = 0;

		if (SpeechScorer.scoringType.equals("")) {
			scoreWeight[0] = 2;
			scoreWeight[1] = 2;
		}
		if (SpeechScorer.scoringType.equals(Constants.SP_SC_CHECKSTRESS)
				|| SpeechScorer.scoringType.equals(Constants.SP_SC_CHECKALL)) {
			stressScore = pronAndArtScore;
			scoreWeight[3] = 2;
		}

		if (SpeechScorer.scoringType.equals(Constants.SP_SC_CHECKPHRASING)
				|| SpeechScorer.scoringType.equals(Constants.SP_SC_CHECKALL)) {
			phrasingScore = Float.valueOf(wordASR[9]);
			if (phrasingScore < 0) {
				phrasingScore = (float) fluencyAndBlendScore;
			}
			scoreWeight[4] = 2;
		}
		if (SpeechScorer.scoringType.equals(Constants.SP_SC_CHECKEMPHASIS)
				|| SpeechScorer.scoringType.equals(Constants.SP_SC_CHECKALL)) {
			emphasisScore = Float.valueOf(wordASR[10]);
			if (emphasisScore < 0) {
				emphasisScore = (float) pronAndArtScore;
			}
			scoreWeight[5] = 2;
		}
		if (SpeechScorer.scoringType.equals(Constants.SP_SC_CHECKINTONATION)
				|| SpeechScorer.scoringType.equals(Constants.SP_SC_CHECKALL)) {
			if (SpeechScorer.refIntonation != null
					&& SpeechScorer.refIntonation.length() > 0) {
				System.out.println("*** Intonation Score ***");
				System.out.println("Creating Instance of SpeechUtterance.");
				System.out.println("> wavfile: " + SpeechScorer.testWav);
				System.out.println("> reference utterance: "
						+ SpeechScorer.refUtterance);
				System.out.println("> reference intonation: "
						+ SpeechScorer.refIntonation);
				System.out.println("> forced alignment: " + corrPath);
				try {
					IntonationDetector su = IntonationDetector.createInstance(
							SpeechScorer.testWav, SpeechScorer.refUtterance,
							SpeechScorer.refIntonation, corrPath);
					intonationScore = (float) su.checkIntonation();
					System.out.println("RESULT: Intonation Score = "
							+ intonationScore);
				} catch (IOException e) {
					intonationScore = 0;
					System.out.println("Failed to compute intonation score.");
				} catch (ArrayIndexOutOfBoundsException e) {
					intonationScore = 0;
				}
			} else {
				System.out.println("Unable to find reference intonation from the database");
			}
			scoreWeight[6] = 2;
		}

		if (SpeechScorer.exerciseType.equals(Constants.SP_CALLSIMULATION)) {
			baseScore = 15;
			scoreWeight[0] = 2;
			scoreWeight[1] = 2;
			scoreWeight[2] = 1;
			scoreWeight[3] = 1;
			scoreWeight[4] = 3;
			scoreWeight[5] = 3;
			scoreWeight[6] = 3;
		} else if (SpeechScorer.exerciseType.equals(Constants.SP_CONVERSATION)) {
			baseScore = 10;
			scoreWeight[3] = 1;
		}

		/** 5. Compute the final score based on the baseScore */
		System.out.println("*** Final Sentence Score ***");
		System.out.println("> initScore: " + initScore);
		System.out.println("> pronAndArtScore and weight: " + pronAndArtScore + ", "
				+ scoreWeight[0]);
		System.out.println("> fluencyAndBlendScore and weight: "
				+ fluencyAndBlendScore + ", " + scoreWeight[1]);
		System.out.println("> undScore and weight: " + undScore + ", "
				+ scoreWeight[2]);
		System.out.println("> stressScore and weight: " + stressScore + ", "
				+ scoreWeight[3]);
		System.out.println("> phrasingScore and weight: " + phrasingScore + ", "
				+ scoreWeight[4]);
		System.out.println("> emphasisScore and weight: " + emphasisScore + ", "
				+ scoreWeight[5]);
		System.out.println("> intonationScore and weight: " + intonationScore + ", "
				+ scoreWeight[6]);
		System.out.println("> normalization factor: " + (itemscore / baseScore));

		sentenceScore = ((pronAndArtScore * scoreWeight[0])
				+ (fluencyAndBlendScore * scoreWeight[1])
				+ (undScore * scoreWeight[2]) + (stressScore * scoreWeight[3])
				+ (phrasingScore * scoreWeight[4])
				+ (emphasisScore * scoreWeight[5]) + (intonationScore * scoreWeight[6]))
				* (double) (itemscore / baseScore);
		SpeechScorer.normFactor = (itemscore / baseScore);
		
		/**
		 * 6. Set the values of critScore
		 * 
		 * Note: critScore[3] is score for proper stressing of sound. 
		 * it is not used for now but its value is added to critScore[0] 
		 */

		critScores[0] = (float) pronAndArtScore * scoreWeight[0];
		/*
		if (SpeechScorer.scoringType.equals(Constants.SP_SC_CHECKSTRESS)) {
			critScores[3] = (float) stressScore * scoreWeight[3];
			critScores[0] += critScores[3];
		}
		*/
		critScores[1] = (float) fluencyAndBlendScore * scoreWeight[1];
		critScores[2] = (float) undScore * scoreWeight[2];

		if (SpeechScorer.scoringType.equals(Constants.SP_SC_CHECKPHRASING)) {
			critScores[3] = (float) phrasingScore * scoreWeight[4];
		} else if (SpeechScorer.scoringType
				.equals(Constants.SP_SC_CHECKEMPHASIS)) {
			critScores[3] = (float) emphasisScore * scoreWeight[5];
		} else if (SpeechScorer.scoringType
				.equals(Constants.SP_SC_CHECKINTONATION)) {
			critScores[3] = (float) intonationScore * scoreWeight[6];
		} else if (SpeechScorer.scoringType.equals(Constants.SP_SC_CHECKALL)) {
			critScores[3] = (float) phrasingScore * scoreWeight[4];
			critScores[4] = (float) emphasisScore * scoreWeight[5];
			critScores[5] = (float) intonationScore * scoreWeight[6];
			critScores[6] = (float) stressScore * scoreWeight[3];
		}

		/** 6. Adjust error in pronunciation and articulation depending on score */
		if (pronAndArtScore == 1)
			SpeechScorer.err = "";

		System.out.println("OUTPUT: Sentence Score = " + sentenceScore);
		System.out.println("OUTPUT: Score per Criteria = " + critScores[0] + " "
				+ critScores[1] + " " + critScores[2] + " " + critScores[3]
				+ " " + critScores[4] + " " + critScores[5]);
		System.out.println("OUTPUT: Error Index = " + SpeechScorer.err);
		System.out.println("*** DONE: Check Sentence ***");
		return Math.round(sentenceScore);
	}

	/**
	 * Returns the score related to the probability that the pronunciation and
	 * articulation of the recorded file is correct, computed using logistic
	 * regression trained beforehand
	 * 
	 * @param lesson
	 *            the lesson containing the wav file
	 */
	private static void getInitScore(String lesson) {
		/**
		 * initScore is the score related to the probability that the
		 * pronunciation and articulation in the recorded file is correct. it is
		 * computed using a logistic regression, trained beforehand.
		 * */
		System.out.println("*** Init Score ***");
		try {
			System.out.println("Init Score Features:");
			System.out.println("> wer: " + wer);
			System.out.println("> decscore: " + decscore);

			/** 1. Normalize features */
			System.out.println("Normalization:");
			System.out.println("> wer_mean: " + wer_mean);
			System.out.println("> wer_max: " + wer_max);
			System.out.println("> dec_mean: " + dec_mean);
			System.out.println("> dec_max: " + dec_max);
			wer = (wer - wer_mean) / wer_max;
			decscore = (decscore - dec_mean) / dec_max;
			System.out.println("COMPUTED: Normalized wer = " + wer);
			System.out.println("COMPUTED: Normalized decscore = " + decscore);

			/** 2. Compute init score using the trained logistic regression */
			System.out.println("Logistic Regression Parameters:");
			System.out.println("> theta0 = " + theta0[0]);
			System.out.println("> theta1 (*wer) = " + theta0[1]);
			System.out.println("> theta2 (*decscore) = " + theta0[2]);

			double x = theta0[0] + theta0[1] * wer + theta0[2] * decscore;
			initScore = 1 / (1 + Math.exp(-x));

			System.out.println("COMPUTED: x = theta0 + theta1*wer + theta2*decscore= "
					+ x);
			System.out.println("COMPUTED: initScore = 1 / (1 + Math.exp(-x)) = "
					+ initScore);
			if (lesson.equals("lesson07")) {
				if (initScore < 0.8) {
					initScore = initScore + alpha0;
					System.out.println("Lesson7 initScore adjustment:");
					System.out.println("> alpha = " + alpha0);
					System.out.println("COMPUTED: new initScore =  initScore+alpha = "
							+ initScore);
				}
			}
		} catch (Exception e) {
			initScore = 0;
			System.out.println("Unable to compute initScore. Variable was set to a default value = 0.");
		}
		System.out.println("OUTPUT: init Score: " + initScore);
	}

	/**
	 * Returns the fluency and blending score, the probability that the fluency
	 * and blending of the recorded file is correct, computed using logistic
	 * regression trained beforehand
	 * 
	 * @param lesson
	 *            the lesson containing the wav file
	 * @return the fluency and blending score
	 */
	private static double getFluencyAndBlendScore(String lesson, double delta) {
		/**
		 * Fluency and Blending Score is the score related to the probability
		 * that the fluency and blending of the speech is correct. it is also
		 * computed using the logistic regression, trained beforehand.
		 * */

		System.out.println("*** Fuency and Blending Score ***");

		double totSilence = totSil;
		double spkRate = 0.0;
		System.out.println("> number of phones decoded = " + nPhns);
		System.out.println("> number of frames recorded = " + nFrms);
		if (nFrms != 0) {
			spkRate = (double) nPhns / (double) nFrms;
		}

		System.out.println("Fluency And BlendScore Features:");
		System.out.println("> COMPUTED: Speaking Rate = " + spkRate);
		System.out.println("> Number of Pauses (in Frames) = " + totSilence);

		/** 1 Normalize features */
		System.out.println("Normalization:");
		System.out.println("> totSil_mean = " + totSil_mean);
		System.out.println("> totSil_max = " + totSil_max);
		System.out.println("> spkrate_mean = " + spkrate_mean);
		System.out.println("> spkrate_max = " + spkrate_max);
		spkRate = (spkRate - spkrate_mean) / spkrate_max;
		totSilence = (totSil - totSil_mean) / totSil_max;
		System.out.println("> COMPUTED: Normalized totSilence = " + totSilence);
		System.out.println("> COMPUTED: Normalized spkRate = " + spkRate);

		/** 2. Compute FandBScore using the trained logistic regression */
		System.out.println("Logistic Regression Parameters:");
		System.out.println("> theta0 = " + theta1[0]);
		System.out.println("> theta1 (*totSilence) = " + theta1[1]);
		System.out.println("> theta2 (*spkRate) = " + theta1[2]);

		double x = theta1[0] + theta1[1] * totSilence + theta1[2] * spkRate;
		double FandBscore = 1 / (1 + Math.exp(-x));

		System.out.println("COMPUTED: x = = theta0 + theta1*totSilence + theta2*spkRate = "
				+ x);
		System.out.println("COMPUTED: FandB score = " + FandBscore);
		if (lesson.equals("lesson07")) {
			FandBscore = FandBscore + alpha1;
			System.out.println("Lesson7 score adjustment:");
			System.out.println("> alpha = " + alpha1);
		}
		if (FandBscore > (scoreThreshold-delta)) {
			FandBscore = 1;
		} else if (FandBscore < 0) {
			FandBscore = 0;
		}
		System.out.println("OUTPUT: FandB Score = " + FandBscore);
		return FandBscore;
	}

	/**
	 * Returns the pronunciation score
	 * 
	 * @param ans
	 *            the keywords
	 * @return the pronunciation score
	 */
	private static double getPronScore(String[] ans, double delta) {
		/**
		 * Pronunciation score computation: decision boundary of error is equal
		 * to the set scoreThreshold. so that if InitScore is >= scoreThreshold,
		 * the detected error is disregarded else, check if error is coming from
		 * the key words and compute the score based on the number of key words
		 * pronounced erroneously. if error is not coming from the key words,
		 * return the value of InitScore
		 */
		double pronScore = 0.0;

		System.out.println("*** Pronunciation Score ***");
		System.out.println("> initScore = " + initScore);
		System.out.println("> scoreThreshold = " + (scoreThreshold-delta));
		if (initScore >= (scoreThreshold-delta)) {
			System.out.println("NOTE: Pronunciation was classified perfect.");
			System.out.println("OUTPUT: Pronunciation Score = 1");
			return 1;
		}

		double nKeys = 0;
		int nErr = 0;

		System.out.println("> Index of key words: " + ans);
		System.out.println("> Index of all words where detected errors occured: "
				+ SpeechScorer.err);
		for (int i = 0; i < ans.length; i++) {
			nKeys++;
			if (SpeechScorer.err.indexOf(ans[i]) > -1) {
				nErr++;
			}
		}
		System.out.println("COMPUTED: Number of key words: " + nKeys);
		System.out.println("COMPUTED: Number of mispronounced key words: " + nErr);
		if (nErr > 0) {
			pronScore = 1.0 - (nErr / nKeys);
			System.out.println("COMPUTED: Pronunciation Score = 1.0 - nErr / nKeys = "
					+ pronScore);
		} else {
			pronScore = initScore;
			System.out.println("COMPUTED: Pronunciation Score = initScore = "
					+ initScore);
		}
		System.out.println("OUTPUT: Pronunciation Score = " + initScore);
		return pronScore;
	}

	/**
	 * Returns the articulation score
	 * 
	 * @param ans
	 *            the keywords
	 * @return the articulation score
	 */
	private static double getArtScore(String[] ans, double delta) {
		/**
		 * articulation score: Score depends on initScore, as it is in the
		 * Pronunciation score and on the key phoneme of the exercise
		 */
		double ArtScore = 0.0;
		System.out.println("*** Articulation Score ***");
		System.out.println("> initScore = " + initScore);
		System.out.println("> scoreThreshold = " + (scoreThreshold-delta));
		if (initScore >= (scoreThreshold-delta)) {
			System.out.println("NOTE: Articulation was classified perfect.");
			System.out.println("OUTPUT: Articulation Score = 1");
			return 1;
		}

		/** check if key phones in key words are mispronounced */
		double nKeys = 0;
		int nErr = 0;
		String[] key = null;

		System.out.println("> Key Phone(s): " + SpeechScorer.key);
		if (SpeechScorer.key != null) {
			if (SpeechScorer.key.indexOf(" ") > -1
					|| SpeechScorer.key.indexOf("_") > -1)
				key = SpeechScorer.key.replace("0", "").replace("1", "")
						.replace("2", "").replace("_", " ").split(" ");
			else {
				key = new String[1];
				key[0] = SpeechScorer.key.replace("0", "").replace("1", "")
						.replace("2", "");
			}
			String keys = key[0];
			String answers = ans[0];
			int i;
			for (i = 1; i < key.length; i++) {
				keys = keys + " " + key[i];
			}
			for (i = 1; i < ans.length; i++) {
				answers = answers + " " + ans[i];
			}
			System.out.println("> Reformatted Key Phone(s): " + keys);
			System.out.println("> Index of key words: " + answers);
			System.out.println("> Index of words where detected mispronunciation occured: "
					+ misIndx);
			System.out.println("> Index of words where substitution or deletion occured: "
					+ errIndx);

			for (i = 0; i < ans.length; i++) {
				nKeys++;
				if (misIndx.indexOf(ans[i]) > -1) {
					/** mispronounced words: check if key phone is mispornounced */
					int realIndx = misIndx.indexOf(ans[i])
							- misIndx.substring(0, misIndx.indexOf(ans[i]))
									.replaceAll(";", "").length();
					for (int j = 0; j < key.length; j++) {
						if (mispron[realIndx].indexOf(key[j]) > -1) {
							nErr++;
							j = key.length; // stop for loop - avoiding to have
											// nErr > nKeys
						}
					}
				} else if (errIndx.indexOf(ans[i]) > -1) {
					/** un-decoded words: no need to check for key phone */
					nErr++;
				}
			}
			System.out.println("COMPUTED: Number of key words: " + nKeys);
			System.out.println("COMPUTED: Number of key words mispronounced in the key phonemes: "
					+ nErr);
			if (nErr > 0) {
				ArtScore = 1.0 - nErr / nKeys;
				System.out.println("COMPUTED: Articulation Score = 1.0 - nErr/nKeys = "
						+ ArtScore);
			} else {
				System.out.println("NOTE: nErr = 0.");
			}
		}
		System.out.println("COMPUTE: Articulation Score = initScore = " + initScore);
		ArtScore = initScore;
		System.out.println("OUTPUT: Articulation Score = " + initScore);
		return ArtScore;
	}

	/**
	 * Returns the understandability score computed from the combined
	 * pronunciation and art score, and combined fluency and blending score.
	 * 
	 * @return the understandability score
	 */
	private static double getUndScore() {
		/**
		 * Understandability is related to pronAndArtScore and
		 * fluencyAndBlendScore as computed as below
		 */
		System.out.println("*** Understandability Score ***");
		System.out.println("OUTPUT: Understandability Score = "
				+ "average(pronAndArtScore+fluencyAndBlendScore) = "
				+ ((pronAndArtScore + fluencyAndBlendScore) / 2.0));
		return (pronAndArtScore + fluencyAndBlendScore) / 2.0;
	}

	/**
	 * Returns the decoded speech of the recorded wav file
	 * 
	 * @param testWav
	 *            the recorded wav file
	 * @param lesson
	 *            the lesson containing the wav file
	 * @param withStress
	 *            whether the reference phonemes to be used has stress or not
	 * @return the parameters set by the recognizer including the decoded speech
	 */
	public static String[] getWordASR(String testWav, String lesson,
			boolean withStress) {
		/**
		 * Call Speech recognizer to compute the parameters used in scoring the
		 * speech file
		 */
		String[] line = new String[15];
		int timeout = 0;
		int trial = 0;
		int i = 0;

		//if (!Globals.installer){
		//	testWav = "../../" + testWav.replaceAll("\"", "");
		//}
		
		String j_wrdcmd = "janus -f janus/words/getUttFeat.tcl 0 " + testWav
				+ " \"" + SpeechScorer.refWords.toUpperCase() + "\" " + lesson;
		/**
		 * Note: This is for future use. For now, with stress and with no stress
		 * are treated the same
		 */
		if (withStress) {
			j_wrdcmd = "janus -f janus/words/getUttFeat.tcl 0 " + testWav
					+ " \"" + SpeechScorer.refPhones2.toUpperCase() + "\" "
					+ lesson;
		}

		if (SpeechScorer.scoringType.equals(Constants.SP_SC_CHECKPHRASING)
				|| SpeechScorer.scoringType.equals(Constants.SP_SC_CHECKALL)
				|| SpeechScorer.exerciseType.equals(Constants.SP_LONGPARAGRAPH)
				|| SpeechScorer.exerciseType
						.equals(Constants.SP_SHORTPARAGRAPH)
				|| SpeechScorer.exerciseType.equals(Constants.SP_QANDA)) {
			j_wrdcmd = j_wrdcmd + " checkphrasing";
		}
		if (SpeechScorer.scoringType.equals(Constants.SP_SC_CHECKEMPHASIS)
				|| SpeechScorer.scoringType.equals(Constants.SP_SC_CHECKALL)) {
			j_wrdcmd = j_wrdcmd + " checkemphasis";
		}
		SpeechScorer.janusCommand = j_wrdcmd;

		System.out.println("Creating speech recognition process to decode given speech file "
				+ "using the following command:");
		System.out.println(j_wrdcmd);
		try {
			do {
				if (trial > 0) {
					System.out.println("Failed to decode speech in trial #" + trial);
					timeout = timeout + 10;
				}
				Runtime r = Runtime.getRuntime();
				Process p = r.exec(j_wrdcmd);
				Thread.sleep(timeout);
				BufferedReader b = new BufferedReader(new InputStreamReader(
						p.getInputStream()));
				i = 0;
				while ((line[i] = b.readLine()) != null) {
					if (line[i].indexOf('*') == 0) {
						p.destroy();
					}
					i++;
				}
				trial++;
			} while (line[0] == null && trial < 4);
		} catch (Exception e) {
			System.out.println(
					"Failed to create process for speech recognition engine.");
		}
		if (line[0] == null) {
			System.out.println("Failed to recognize sample wav file.");
			System.out.println("OUTPUT: null");
		} else {
			for (i = 0; i < line.length; i++) {
				if (line[i] != null)
					System.out.println("OUTPUT: " + i + " - " + line[i]);
			}
		}
		return line;
	}
	
	/**
	 * Returns the scores for all the criteria
	 * 
	 * @return the scores for all the criteria
	 */
	public static Integer[] getScoreWeights() {
		return scoreWeight;
	}
	
	/**
	 * Returns the scores for all the criteria
	 * 
	 * @return the scores for all the criteria
	 */
	public static float[] getCritScores() {
		return critScores;
	}
	
}
