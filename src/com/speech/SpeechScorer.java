/**
 * ==============================================================================
 * File:			SpeechScoring.java
 * Created:			2013/09/04
 * Last Changed:	2013/09/04
 * Author:			Ayvee Mallare
 * ==============================================================================
 * This code is copyright (c) 2013 Learning English Application for Pinoys (LEAP)
 * 
 * History:
 * -- add revision history here --
 * 
 */

package com.speech;

import java.io.IOException;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.newdawn.slick.SlickException;

import com.util.Constants;
//import com.dostproject7.util.GameFlow;
//import com.dostproject7.util.Globals;
//import com.dostproject7.util.StreamGobbler;

/**
 * Organizes the speech scoring parameters, such as recorded wave files,
 * exercise type, scoring type and etc. Based on the exercise and scoring type,
 * it calls either WordChecker or SentenceChecker to compute score.
 * {@link com.dostproject7.speech.SpeechScorer SpeechScorer}.
 * 
 * @author Ayvee Mallare
 * @version 1.0
 * 
 */

public class SpeechScorer {

	/** 1. Parameters computed in Speech Recognizer */

	/** detected word chosen by user from multiple choice or sayword */
	protected static String saidWord;
	/** decoded phones or words */
	protected static String decoded;
	/** speech features extracted from the speech recognizer */
	protected static String[] feat;
	/** Number of silence pts */
	protected static int nSil;
	/** Total number of silence pts */
	protected static int totSil;
	/** recognition score */
	protected static double recogScore;

	/** 2. Parameters computed in LEAP - Speech Package */

	/** decoded text showing where the ins, del, and sub occurs */
	protected static String editSentence;
	/** number of phones or words in the reference text */
	protected static int refLen;
	/** alignment score */
	protected static double alignmentScore;
	/** index of word or phones where errors were detected */
	public static String err;
	/** number of phones or words in err */
	protected static int errLen;
	/** number of key phones/words in err */
	protected static int keyErrLen;
	/** additional remarks in scoring */
	public static String comment = "";

	/** 3. Parameters to set when using Speech Scoring */
	/** System to use */
	protected static String system = "002";
	/** Reference WAV file */
	protected static String refWav;
	/** Reference words */
	protected static String refWords;
	/** Reference phones */
	protected static String refPhones;
	/** Reference phones with stress */
	protected static String refPhones2;
	/** Key sound for checkword */
	protected static String key;
	/** Index of keywords (sentences) or phone mappings (words) */
	protected static String answers;
	/** WAV file to be tested or scored */
	protected static String testWav;
	/** Type of current exercise */
	protected static String exerciseType;
	/** Type of scoring to be done */
	public static String scoringType;
	/** Intonation of reference wav file **/
	protected static String refIntonation;
	/** Reference utterance **/
	protected static String refUtterance;
	/** Word choices for find the word exercise */
	protected static String choices;
	protected static String lesson;
	/** for debugging purposes */
	protected static String janusCommand = "";
	protected static int normFactor = 1;

	/** Logger variable */
	private final static Logger LOGGER = LogManager.getLogger();

	/**
	 * Non-arg constructor
	 */
	private SpeechScorer() {
	}

	/**
	 * Default method to be called for setting parameters
	 * 
	 * @param refWav
	 * @param refWords
	 * @param refPhones
	 * @param refPhones2
	 * @param answers
	 * @param testWav
	 * @param exerciseType
	 * @param scoringType
	 */
	public static void setParameters(String refWav, String refWords,
			String refPhones, String refPhones2, String key, String answers,
			String testWav, String exerciseType, String scoringType) {
		LOGGER.info("Executing Speech Scoring: Set parameters (1)");
		try {
			SpeechScorer.refWav = refWav;
			SpeechScorer.refWords = refWords;
			SpeechScorer.refPhones = refPhones;
			SpeechScorer.refPhones2 = refPhones2;
			SpeechScorer.key = key;
			SpeechScorer.answers = answers;
			SpeechScorer.testWav = "\"" + testWav + "\"";
			SpeechScorer.exerciseType = exerciseType;
			SpeechScorer.scoringType = scoringType;
			err = "";
			comment = "";
			lesson = "";
		} catch (Exception e) {
			LOGGER.error(
					"ERROR: Failed to set parameters for Speech Scoring (1)", e);
		}
		LOGGER.info("DONE. Speech Scoring: Set parameters (1)");
	}

	public static void setParameters(String refWav, String refWords,
			String answers, String testWav, String exerciseType,
			String scoringType) {
		LOGGER.info("Executing Speech Scoring: Set parameters (2)");
		try {
			SpeechScorer.refWav = refWav;
			SpeechScorer.refWords = refWords;
			SpeechScorer.answers = answers;
			SpeechScorer.testWav = "\"" + testWav + "\"";
			SpeechScorer.exerciseType = exerciseType;
			SpeechScorer.scoringType = scoringType;
			err = "";
			comment = "";
		} catch (Exception e) {
			LOGGER.error(
					"ERROR: Failed to set parameters for Speech Scoring (2)", e);
		}
		LOGGER.info("DONE. Speech Scoring: Set parameters (2)");
	}

	/**
	 * Sets the reference words
	 * 
	 * @param refWords
	 *            the reference words
	 */
	public static void setRefWords(String refWords) {
		SpeechScorer.refWords = refWords;
	}

	/**
	 * Sets choices for "sayword" exercises
	 * 
	 * @param choices
	 */
	public static void setChoices(String choices) {
		LOGGER.info("Executing Speech Scoring: Set parameters (3)");
		SpeechScorer.choices = choices;
		LOGGER.info("DONE. choices = " + SpeechScorer.choices);
	}

	/**
	 * Sets sil parameters for sentence exercises
	 * 
	 * @param nSil
	 * @param totSil
	 */
	public static void setSil(String nSil, String totSil) {
		SpeechScorer.nSil = Integer.parseInt(nSil);
		SpeechScorer.totSil = Integer.parseInt(totSil);
	}

	/**
	 * Sets the reference utterance and intonation
	 * 
	 * @param refUttIntonation
	 */
	public static void setRefUttIntonation(String[] refUttIntonation) {
		LOGGER.info("Executing Speech Scoring: Set parameters (4)");
		SpeechScorer.refUtterance = refUttIntonation[0];
		SpeechScorer.refIntonation = refUttIntonation[1];
		LOGGER.info("DONE. refIntonation = " + refIntonation);
	}

	/**
	 * Resets the scores and parameters
	 * 
	 */
	private static void resetResults() {
		feat = null;
		recogScore = 0;
		alignmentScore = 0;
		keyErrLen = 0;
		errLen = 0;
		refLen = 0;
		decoded = "";
		editSentence = "";
		err = "";
		comment = "";
		saidWord = "";
	}

	/**
	 * Returns the score of recorded wav file
	 * 
	 * @param itemscore
	 *            the total points per item
	 * @return the score of recorded wav file
	 * @throws SlickException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static double computescore(Integer itemscore, double delta)
			throws IOException, InterruptedException {
		resetResults();

		/** change the delimiter of choices to spaces */
		if (choices != null) {
			choices = choices.replace(",", " ");
			LOGGER.debug("Choices: " + choices);
		}

		/** Prepare data to be tested */
		/**
		 * 1. get the lesson number (lesson) - used by speech recognition system
		 */
		int underscore = refWav.indexOf('_');
		lesson = refWav.trim();
		if (underscore != -1) {
			lesson = "lesson" + refWav.substring(0, underscore);
		} else {
			lesson = "preassessment";
		}

		/**
		 * 2. In debug mode, convert the sample wav file to ogg put the
		 * converted file to temp.ogg change the value of 'testWav' (the test
		 * file) to temp.ogg
		 */

		/**
		 * 3. Scoring: use exerciseType and scoringType to determine which type
		 * of scoring has to be used
		 */

		LOGGER.debug("> Reference Words: " + refWords);
		if (exerciseType.equals(Constants.SP_REPEATWORD)) {
			LOGGER.debug("Speech Scoring: Use \"Check Word Scoring\"");
			return WordChecker.checkWord(lesson, delta);
		} else if (exerciseType.equals(Constants.SP_SAYWORD)) {
			LOGGER.debug("Speech Scoring: Use \"Say Word Scoring\"");
			return WordChecker.checkWord(lesson, delta);
		} else {
			LOGGER.debug("Speech Scoring: Use \"Check Sentence Scoring\"");
			return SentenceChecker.checkSentence(lesson, itemscore, delta);
		}

	}

	/**
	 * Returns the scores for all criteria
	 * 
	 * @return the scores for all criteria
	 */
	public static float[] getScores() {
		return SentenceChecker.getCritScores();
	}

	/**
	 * Returns the weights of the scores
	 * 
	 * @return the scores for all criteria
	 */
	public static Integer[] getWeights() {
		return SentenceChecker.getScoreWeights();
	}

	/**
	 * Returns the weights of the scores
	 * 
	 * @return the scores for all criteria
	 */
	public static Integer getChoiceScore() {
		return WordChecker.getChoiceScore();
	}

	/**
	 * Returns the alignment score computed from the initial alignment score and
	 * decoding score
	 * 
	 * @param recogResult
	 *            the initial alignment score and the decoding score
	 * @return the computed alignment score
	 */

	protected static int getAlignmentScore(String[] result) {
		int alignmentScore = 0;
		LOGGER.debug("*** Alignment Score *** ");
		LOGGER.debug("> alignment score = " + result[0]);
		LOGGER.debug("> best alignment score = " + result[1]);
		LOGGER.debug("> decoding score = " + result[2]);

		if (result[0] != "0.0" && result[1] != "0.0") {
			decoded = result[3];
			Double scoreDiff = Double.valueOf(result[1])
					- Double.valueOf(result[0]);
			LOGGER.debug("> score difference = " + scoreDiff);
			if (scoreDiff >= 50) {
				alignmentScore = 100;
				LOGGER.info(testWav + " aligned with the text accordingly");
			} else if (scoreDiff >= -50) {
				alignmentScore = scoreDiff.intValue() + 50;
				LOGGER.info(testWav + " aligned with the text accordingly");
			} else { // if (scoreDiff <= -50) {
				alignmentScore = 0;
				LOGGER.info(testWav + " did not align very well");
			}
		} else {
			decoded = "+ERROR+";
			alignmentScore = -100;
		}

		LOGGER.debug("> OUTPUT: alignment score = " + alignmentScore);
		LOGGER.debug("> OUTPUT: decoded text = " + decoded);
		return alignmentScore;
	}

	/**
	 * Returns the indices of the phonemes where errors were detected
	 * 
	 * @return the indices of the phonemes where errors were detected
	 */
	protected static Vector<Integer> getError() {
		/*
		 * Use LDistance to compare the decoded phonemes with the reference
		 * phonemes and determine where substitutions, insertions or deletions
		 * occurred if there are the decoded does not align with the reference
		 */
		LOGGER.debug("*** get error indeces ***");
		LOGGER.debug("Creating new instance of LDistance.");
		LOGGER.info("reference phonemes = " + refWords);
		LOGGER.info("decoded phonemes = " + decoded);
		LDistance ld = new LDistance();
		Vector<Integer> wer_err = new Vector<Integer>();
		int NErr = 0;
		int NPhonemes = 0;
		try {
			/**
			 * 1. process the Levenshtein distance of the reference and the
			 * decoded phonemes
			 */
			ld.processLD(refWords, decoded);
			/**
			 * 2. edit decoded phonemes, identifying where the detected errors
			 * occurred
			 */
			editSentence = ld.getLDString(ld.getLDMatrix(), ld.getSArr(),
					ld.getTArr());
			LOGGER.debug("COMPUTED: edit sentence = " + editSentence);

			/**
			 * 3. Get number of errors in the decoded phonemes and the number of
			 * phonemes in the reference phonemes
			 */
			wer_err = ld.getLDStringStats(ld.getLDString(ld.getLDMatrix(),
					ld.getSArr(), ld.getTArr()));
			NErr = wer_err.get(0);
			NPhonemes = wer_err.get(1);
			LOGGER.debug("> num of errors in decoded phonemes = " + NErr);
			LOGGER.debug("> num of phonemes in reference phonemes = "
					+ NPhonemes);

		} catch (Exception e) {
			LOGGER.error(
					"ERROR: Failed to process Levenshtein Distance of reference and decoded phonemes.",
					e);
		}

		/** 4. Compute Recognition Score */
		recogScore = 100.0 - ((float) NErr / (float) NPhonemes * 100.0);
		LOGGER.debug("COMPUTED: Recognition Score = " + recogScore);

		/** 5. Based on Recognition Score, set the comment */
		if (recogScore >= 90)
			comment = "Excellent";
		else if (recogScore >= 75)
			comment = "Very Good";
		else if (recogScore >= 50)
			comment = "Good";
		else if (recogScore >= 30)
			comment = "Try harder";
		else
			comment = "Practice more";
		/**
		 * 6. Remove the number of errors and the number of phonemes from
		 * wer_err. This will leave indexes of the phonemes where the errors are
		 * detected.
		 */
		wer_err.remove(0);
		wer_err.remove(0);

		/** 7. Return wer_err */
		LOGGER.debug("OUTPUT: Indexes of Errors: " + wer_err);
		return wer_err;

	}

	/**
	 * Returns the index of word or phones where errors were detected
	 * 
	 * @return the index of word or phones with errors
	 */
	public static String getErr() {
		return err;
	}

	/**
	 * Returns additional remarks or feedback in scoring
	 * 
	 * @return feedback in scoring
	 */
	public static String getComment() {
		return comment;
	}

	/**
	 * Returns the decoded phones or word
	 * 
	 * @return the decoded phones or word
	 */
	public static String getDecoded() {
		return decoded;
	}

	/**
	 * Returns the text that shows where ins, del or sub occurred
	 * 
	 * @return the text that shows where ins, del or sub occurred
	 */
	public static String getEditSentence() {
		return editSentence;
	}

	/**
	 * Returns the recognition score
	 * 
	 * @return the recognition score
	 */
	public static double getRecogScore() {
		return recogScore;
	}

	/**
	 * Returns the alignment score
	 * 
	 * @return the alignment score
	 */
	public static double getAlignmentScore() {
		return alignmentScore;
	}

	/**
	 * Returns the speech features extracted from the speech recognizer
	 * 
	 * @return the extracted speech features
	 */
	public static String[] getFeat() {
		if (feat == null)
			return new String[1];
		return feat;
	}

	/**
	 * Returns the key
	 * 
	 * @return the key
	 */
	public static String getKey() {
		return key;
	}

	/**
	 * Returns the key
	 * 
	 * @return the key
	 */
	public static String getAnswers() {
		String ans[] = new String[1];
		String ref[] = new String[1];
		String keywords = "";

		if (refWords.indexOf("|") > -1)
			return "";
		if (answers == null || answers == "")
			return "";
		if (exerciseType.equals(Constants.SP_SAYWORD))
			return "";
		ref = refWords.split(" ");
		ans = answers.split(" ");
		try {
			for (int i = 0; i < ans.length; i++) {
				keywords = keywords + ref[Integer.valueOf(ans[i])] + " ";
			}
		} catch (NumberFormatException e) {
			return "";
		}
		return answers;
	}

	/**
	 * Returns the raw score or the average of the recognition score and the
	 * alignment score
	 * 
	 * @return the raw score
	 */
	public static double getRawScore() {
		return (recogScore + alignmentScore) / 2.0;
	}

	/**
	 * Returns the number of words or phones where errors occurred
	 * 
	 * @return the number of words or phones where errors occurred
	 */
	public static int getErrLen() {
		return errLen;
	}

	/**
	 * Returns the number of key words or phones where errors occurred
	 * 
	 * @return the number of key words or phones
	 */
	public static int getKeyErrLen() {
		return keyErrLen;
	}

	/**
	 * Returns the number of words or phones in the reference text
	 * 
	 * @return the number of words or phones
	 */
	public static int getRefLen() {
		return refLen;
	}

	/**
	 * Returns the detected word chosen by the user for multiple choice or
	 * sayword exercises
	 * 
	 * @return the detected word chosen by the user
	 */
	public static String getReadWord() {
		return saidWord;
	}

	/**
	 * Returns the janus command used to decode the sample
	 * 
	 * @return the janus command use
	 */
	public static String getJanusCommand() {
		return janusCommand;
	}

	/**
	 * Returns the janus command used to decode the sample
	 * 
	 * @return the janus command use
	 */
	public static int getNormFactor() {
		return normFactor;
	}



}
