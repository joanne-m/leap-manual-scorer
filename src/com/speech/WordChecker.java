/**
 * ==============================================================================
 * File:			WordChecker.java
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import com.dostproject7.speech.test.SpeechTester;
import com.util.Constants;
import com.util.Globals;

/**
 * Compute score for word exercises. it uses phoneme recognition engine and
 * trained linear regression system to compute the score of the recorded file
 * {@link com.dostproject7.speech.WordChecker WordChecker}.
 * 
 * @author Ayvee Mallare
 * @version 1.0
 * 
 */

public class WordChecker {
	private final static double W_ONES = 0.88801;
	private final static double W_RECOG = 0.00016193;
	private final static double W_FA = -0.00042086;
	private final static double W_DECODING = 0.00043;
	private final static double W_REFLEN = -0.011896;
	private final static double W_ERRLEN = 0.004732;
	private final static double W_KEY = 0.012754;
	private final static double W_THRESHOLD1 = 0.80;
	private final static double W_THRESHOLD2 = 0.90;
	private final static Logger LOGGER = LogManager.getLogger();
	private static Integer choiceScore = 0;

	/**
	 * Returns the computed score of the recorded file using a phoneme
	 * recognition engine and trained linear regression
	 * 
	 * @param lesson
	 *            the lesson that contains the word exercise
	 * @return the score of the recorded file
	 */
	public static double checkWord(String lesson, double delta) {

		/** 1. Initialize scores and other variables */
		double wordScore = 0.0;
		String[] phonemeASR = null;
		boolean success = true;
		LOGGER.debug("*** Check Word ***");
		int i = (SpeechScorer.refPhones.length()
				- SpeechScorer.refPhones.replaceAll(" ", "").length() + 1) * 2 + 10;

		/** 2. Get the speech parameters from the speech recognizer */
		SpeechScorer.feat = null;
		SpeechScorer.feat = new String[4];
		try {
			if (SpeechScorer.exerciseType.equals(Constants.SP_REPEATWORD)) {
				LOGGER.debug("Calling getPhonemeASR()");
				//LOGGER.debug("> number of output lines = " + i);
				LOGGER.debug("> reference words = "
						+ SpeechScorer.refWords.toLowerCase());
				LOGGER.debug("> reference phones = " + SpeechScorer.refPhones);
				LOGGER.debug("> Wave File = " + SpeechScorer.testWav);
				LOGGER.debug("> Lesson = " + lesson);
				phonemeASR = getPhonemeASR(SpeechScorer.refWords.toLowerCase(),
						SpeechScorer.refPhones, SpeechScorer.testWav, lesson);
			} else if (SpeechScorer.exerciseType.equals(Constants.SP_SAYWORD)) {
				LOGGER.debug("Calling getPhonemeASR2()");
				LOGGER.debug("> number of output lines = " + i);
				LOGGER.debug("> reference words = "
						+ SpeechScorer.refWords.toLowerCase());
				LOGGER.debug("> reference phones = " + SpeechScorer.refPhones);
				LOGGER.debug("> Wave File = " + SpeechScorer.testWav);
				LOGGER.debug("> Lesson = " + lesson);
				LOGGER.debug("> Choices = " + SpeechScorer.choices);
				SpeechScorer.normFactor = 2;
				phonemeASR = getPhonemeASR2(i,
						SpeechScorer.refWords.toLowerCase(),
						SpeechScorer.refPhones, SpeechScorer.testWav, lesson,
						SpeechScorer.choices);
			}

		} catch (Exception e) {
			LOGGER.error(
					"Failed to compute value for phonemeASR using getPhonemeASR()/getPhonemeASR2()",
					e);
			LOGGER.error("Return Score = 0");
			return 0;
		}

		if (phonemeASR[0] == null) {
			LOGGER.warn("Unable to recognize the speech. Features will be set to default values.");
			LOGGER.warn("Computation of final score might not be accurate.");
			success = false;
		}

		

		/** 3. Set up features from the speech recognizer */
		if (success) {
			if (phonemeASR[0].indexOf(":") > -1) {
				SpeechScorer.feat = phonemeASR[0].split(":");
				if (SpeechScorer.feat[0] == "" || SpeechScorer.feat[0] == null) {
					SpeechScorer.feat[0] = "0.0";
				}
				if (SpeechScorer.feat[1] == "" || SpeechScorer.feat[1] == null) {
					SpeechScorer.feat[1] = "0.0";
				}
				if (SpeechScorer.feat[2] == "" || SpeechScorer.feat[0] == null) {
					SpeechScorer.feat[2] = "0.0";
				}
				if (SpeechScorer.feat[3] == null) {
					SpeechScorer.feat[3] = "";
				}
			} else {
				SpeechScorer.feat[0] = "0.0";
				SpeechScorer.feat[1] = "0.0";
				SpeechScorer.feat[2] = "0.0";
				SpeechScorer.feat[3] = "";
			}
		} else {
			SpeechScorer.feat[0] = "0.0";
			SpeechScorer.feat[1] = "0.0";
			SpeechScorer.feat[2] = "0.0";
			SpeechScorer.feat[3] = "";
		}

		/** 5. Set up the additional score for multiple choice "say word" */
		if (SpeechScorer.exerciseType.equals(Constants.SP_SAYWORD)) {
			LOGGER.debug("*** Choice Score ***");
			if (phonemeASR[1] == null) {
				LOGGER.warn("Unable to recognize speech. Setting chosen word to blank.");
				LOGGER.warn("Computation of final score might not be accurate.");
				phonemeASR[1] = "";
			}
			SpeechScorer.saidWord = phonemeASR[1];
			LOGGER.debug("Detected User's Choice = " + phonemeASR[1]);
			LOGGER.debug("Correct Answer = " + SpeechScorer.refWords);
			if (phonemeASR[1].equalsIgnoreCase(SpeechScorer.refWords)) {
				choiceScore = 1;
			} else {
				choiceScore = 0;
			}
			LOGGER.debug("COMPUTED: Choice Score = ", +choiceScore);
		}

		/** 6. Compute alignment score */
		SpeechScorer.alignmentScore = SpeechScorer
				.getAlignmentScore(SpeechScorer.feat);

		/**
		 * 7. Get index of errors by comparing reference and decoded phones
		 * through getError()
		 */
		SpeechScorer.refWords = SpeechScorer.refPhones;
		Vector<Integer> wer_err = SpeechScorer.getError();

		/** 8. Compute final score */
		/**
		 * NOTE: err_length is the number of errors to be shown show 3/4 of the
		 * errors if number of error is > 3
		 */
		int err_length = wer_err.size();
		SpeechScorer.errLen = err_length;
		LOGGER.debug("> Number of Original errors = " + err_length);
		if (success) {
			if (err_length > 3) {
				err_length = (int) (0.75 * err_length);
			}
			SpeechScorer.err = "";
			/** NOTE: this was the previous computation of word score: */
			LOGGER.debug("> Recognition Score = " + SpeechScorer.recogScore);
			LOGGER.debug("> Alignment Score = " + SpeechScorer.alignmentScore);
			wordScore = (SpeechScorer.recogScore + SpeechScorer.alignmentScore) / 2;
			LOGGER.debug("> COMPUTED: Average (old computation of score) = "
					+ wordScore + "/100");

			/**
			 * Parameters used in Current computation of word score: ans =
			 * indexes of the key phonemes in the reference phones_arr = array
			 * of reference phonemes phones2_arr = array of reference phonemes
			 * with stress marks key_arr = list of the key phonemes
			 * */
			LOGGER.debug("*** Computing the Pronunciation and the Final Score ***");
			LOGGER.debug("> reference phonemes = " + SpeechScorer.refPhones);
			String[] phones_arr = SpeechScorer.refPhones.split(" ");
			SpeechScorer.refLen = phones_arr.length;
			String[] phones2_arr = null;
			if (SpeechScorer.refPhones2 != null) {
				LOGGER.debug("> reference phonemes 2 = "
						+ SpeechScorer.refPhones2);
				phones2_arr = SpeechScorer.refPhones2.split(" ");
			}
			LOGGER.debug("> indexes of key phonemes = " + SpeechScorer.answers);
			String[] ans = SpeechScorer.answers.split(" ");
			String[] key_arr = null;
			if (SpeechScorer.key != null) {
				LOGGER.debug("> key phonemes = " + SpeechScorer.key);
				key_arr = SpeechScorer.key.split(" ");
			} else {
				LOGGER.debug("> key phonemes = " + SpeechScorer.refPhones);
				key_arr = phones_arr;
			}
			wordScore = computeScore(wer_err, ans, phones_arr, phones2_arr,
					key_arr, delta);
			LOGGER.debug("> COMPUTED: Word Pronunciation Score = " + wordScore);
			LOGGER.debug("> Score Comment = " + SpeechScorer.comment);

			if (SpeechScorer.exerciseType.equals("sayword")) {
				LOGGER.debug("> COMPUTED: Choice Score = " + choiceScore);
				if (choiceScore == 1) {
					wordScore += choiceScore;
				} else {
					wordScore = 0;
				}
			}
		} else { /* no need to compute score */
			LOGGER.warn("Unable to decode the speech. Score is set to 0.");
			LOGGER.warn("Computation of final score might not be accurate.");
			wordScore = 0;
			SpeechScorer.err = wer_err.toString();
			SpeechScorer.err = SpeechScorer.err.replace("[", "").replace("]",
					"");
		}
		LOGGER.info("OUPUT: Word Score = " + wordScore);
		LOGGER.info("OUPUT: Error Indexes = " + SpeechScorer.err);
		LOGGER.debug("*** DONE: Check Word ***");
		return wordScore;
	}

	/**
	 * Returns the score computed using the trained Linear Regression
	 * 
	 * @param wer_err
	 *            the indices where the ins, del and sub occurred
	 * @param ans
	 *            the indices of key phonemes
	 * @param phones_arr
	 *            the reference phonemes without stress
	 * @param phones2_arr
	 *            the reference phonemes with stress
	 * @param key_arr
	 *            the key phonemes
	 * @return the recomputed score using weights from Matlab
	 * @param wer_err
	 *            the indices where the ins, del and sub occurred
	 * @param ans
	 *            the indices of key phonemes
	 * @param phones_arr
	 *            the reference phonemes without stress
	 * @param phones2_arr
	 *            the reference phonemes with stress
	 * @param key_arr
	 *            the key phonemes
	 * @return
	 */
	private static double computeScore(Vector<Integer> wer_err, String[] ans,
			String[] phones_arr, String[] phones2_arr, String[] key_arr,
			double delta) {

		int err_length = wer_err.size();
		int i = 0;
		SpeechScorer.keyErrLen = 0;

		while (!wer_err.isEmpty() && i < err_length) {
			if (SpeechScorer.err != "") {
				SpeechScorer.err = SpeechScorer.err + ",";
			}
			int tmp = wer_err.get(0);
			wer_err.remove(0);
			String err_phone = phones_arr[tmp];

			boolean isKey = true;
			for (String key : key_arr) {
				/** Check if key contains stress */
				if (key.contains("1") || key.contains("0")) {
					err_phone = phones2_arr[tmp]; // replace with phones2
				}

				/** Check if key contains compound phonemes */
				if (key.contains("_")) {
					isKey = false;
					String[] l6_key = key.split("_");
					/** Check if previous phoneme is part of the key */
					if (tmp - 1 > -1) {
						isKey = l6_key[0].equals(phones_arr[tmp - 1]);
						key = l6_key[1];
					}
					/** Check if next phoneme is part of the key */
					if ((tmp + 1 < phones_arr.length) && !isKey) {
						isKey = l6_key[1].equals(phones_arr[tmp + 1]);
						key = l6_key[0];
					}
				}

				if (key.equals(err_phone) && isKey) {
					SpeechScorer.keyErrLen++;
					break;
				}
			}
			if (SpeechScorer.exerciseType.equals(Constants.SP_REPEATWORD)) {
				SpeechScorer.err = SpeechScorer.err + ans[tmp];
			}
			i++;
		}
		LOGGER.debug("COMPUTED: No. of Mispronounced Key Phonemes = "
				+ SpeechScorer.keyErrLen);

		LOGGER.debug("Linear Regression Parameters:");
		LOGGER.debug("> Theta0 = " + W_ONES);
		LOGGER.debug("> Theta1 = " + W_RECOG);
		LOGGER.debug("> (Theta1*) Recog Score = "
				+ Double.valueOf(SpeechScorer.recogScore));
		LOGGER.debug("> Theta2 = " + W_FA);
		LOGGER.debug("> (Theta2*) Alignment Score = "
				+ Double.valueOf(SpeechScorer.feat[0]));
		LOGGER.debug("> Theta3 = " + W_DECODING);
		LOGGER.debug("> (Theta3*) Decoding Score = "
				+ Double.valueOf(SpeechScorer.feat[1]));
		LOGGER.debug("> Theta4 = " + W_REFLEN);
		LOGGER.debug("> (Theta4*) Reference Length = "
				+ Double.valueOf(SpeechScorer.refLen));
		LOGGER.debug("> Theta5 = " + W_ERRLEN);
		LOGGER.debug("> (Theta5*) No. of Mispronounced Phonemes = "
				+ Double.valueOf(SpeechScorer.errLen));
		LOGGER.debug("> Theta6 = " + W_KEY);
		LOGGER.debug("> (Theta6*) No. of Mispronounced Key Phonemes = "
				+ Double.valueOf(SpeechScorer.keyErrLen));

		double result = W_ONES + W_RECOG
				* Double.valueOf(SpeechScorer.recogScore) + W_FA
				* Double.valueOf(SpeechScorer.feat[0]) + W_DECODING
				* Double.valueOf(SpeechScorer.feat[1]) + W_REFLEN
				* Double.valueOf(SpeechScorer.refLen) + W_ERRLEN
				* Double.valueOf(SpeechScorer.errLen) + W_KEY
				* Double.valueOf(SpeechScorer.keyErrLen);

		LOGGER.debug("COMPUTED: Linear Regression Score = " + result);

		LOGGER.debug("Determining Score according to LR Score.");
		if (SpeechScorer.recogScore == 100) {
			LOGGER.debug("> Score Boundary (1) = " + (W_THRESHOLD1 - delta));
			if (result > (W_THRESHOLD1 - delta)) {
				SpeechScorer.comment = "Excellent!";
				return 1;
			} else {
				SpeechScorer.comment = "Practice makes perfect. :)";
				return 0;
			}
		} else {
			LOGGER.debug("> Score Boundary (2) = " + (W_THRESHOLD2 - delta));
			if (result > (W_THRESHOLD2 - delta)) {
				SpeechScorer.comment = "Excellent!";
				return 1;
			} else {
				SpeechScorer.comment = "Practice makes perfect. :)";
				return 0;
			}
		}
	}

	/**
	 * Gets phoneme ASR for "checkword" using janus
	 * 
	 * @param i
	 *            the index of the line to be returned from the janus output
	 * @param refWords
	 *            the reference text
	 * @param refPhones
	 *            the reference phonemes
	 * @param testWav
	 *            the wav file to be decoded
	 * @param lesson
	 *            the lesson containing the "checkword" exercise
	 * @return the phoneme ASR for "checkword"
	 */

	public static String[] getPhonemeASR(String refWords, String refPhones,
			String testWav, String lesson) {
		String[] line = new String[3];
		int timeout = 0;
		int trial = 0;
		// if (!Globals.installer){
		// testWav = "../../" + testWav.replaceAll("\"", "");
		// }
		String j_phcmd = "janus -f janus/phonemes/getUttFeat.tcl " + testWav
				+ " " + refWords.toLowerCase() + " \"" + refPhones + "\"";

		SpeechScorer.janusCommand = j_phcmd;
		int i = 0;
		LOGGER.debug("Creating speech recognition process to decode given speech file "
				+ "using the following command:");
		LOGGER.debug(j_phcmd);
		try {
			
			do {
				if (trial > 0) {
					LOGGER.error("Failed to decode speech in trial #" + trial
							+ ".\n" + "Trying to decode again.");
					timeout = timeout + 10;
				}
				Runtime r = Runtime.getRuntime();
				Process p = r.exec(j_phcmd);

				BufferedReader b = new BufferedReader(new InputStreamReader(
						p.getInputStream()));
				i = 0;
				while ((line[i] = b.readLine()) != null) {
					if (line[i].equals("*")) {
						p.destroy();
					}
					i++;
				}
				trial++;
			} while (line[0] == null && trial < 3);
		} catch (Exception e) {
			LOGGER.error(
					"Failed to create process for speech recognition engine.",
					e);
		}
		if (line[0] == null) {
			LOGGER.error("Failed to recognize sample wav file.");
			LOGGER.debug("OUTPUT: null");
		} else {
			for (i = 0; i < line.length; i++) {
				if (line[i] != null)
					LOGGER.debug("OUTPUT: " + i + " - " + line[i]);
			}
		}
		return line;
	}

	/**
	 * Gets phoneme asr for "sayword" using janus
	 * 
	 * @param i
	 *            the index of the line to be returned from the janus output
	 * @param refWords
	 *            the reference text
	 * @param refPhones
	 *            the reference phonemes
	 * @param testWav
	 *            the wav file to be decoded
	 * @param lesson
	 *            the lesson containing the "checkword" exercise
	 * @param choices
	 *            word choices for find the word exercise
	 * @return the phoneme ASR for "sayword"
	 */
	public static String[] getPhonemeASR2(int i, String refWords,
			String refPhones, String testWav, String lesson, String choices) {
		String[] line = new String[i];
		int timeout = 0;
		int trial = 0;
		// if (!Globals.installer){
		// testWav = "../../" + testWav.replaceAll("\"", "");
		// }
		String j_phcmd = "janus -f janus/sayword/sayword.tcl " + testWav + " "
				+ choices + " \"" + refPhones + "\"";

		SpeechScorer.janusCommand = j_phcmd;
		LOGGER.debug("Creating speech recognition process to decode given speech file "
				+ "using the following command:");
		LOGGER.debug(j_phcmd);
		try {
			do {
				if (trial > 0) {
					LOGGER.error("Failed to decode speech in trial #" + trial
							+ ".\n" + "Trying to decode again.");
					timeout = timeout + 10;
				}
				Runtime r = Runtime.getRuntime();
				Process p = r.exec(j_phcmd);

				BufferedReader b = new BufferedReader(new InputStreamReader(
						p.getInputStream()));
				i = 0;
				while ((line[i] = b.readLine()) != null) {
					if (line[i].equals("*")) {
						p.destroy();
					}
					i++;
				}
				trial++;
			} while (line[0] == null && trial < 3);
		} catch (Exception e) {
			LOGGER.error(
					"Failed to create process for speech recognition engine.",
					e);
		}
		if (line[0] == null) {
			LOGGER.error("Failed to recognize sample wav file.");
			LOGGER.debug("OUTPUT: null");
		} else {
			for (i = 0; i < line.length; i++) {
				if (line[i] != null)
					LOGGER.debug("OUTPUT: " + i + " - " + line[i]);
			}
		}
		return line;
	}

	public static Integer getChoiceScore() {
		return choiceScore;
	}

}
