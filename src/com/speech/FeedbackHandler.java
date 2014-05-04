package com.speech;

/**
 * ==============================================================================
 * File:			SpeechFeedback.java
 * Created:			2013/09/04
 * Last Changed:	2013/09/04
 * Author:			Edric Solis
 * ==============================================================================
 * This code is copyright (c) 2013 Learning English Application for Pinoys (LEAP)
 * 
 * History:
 * -- add revision history here --
 * 
 */


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Change the color of the mispronounced word or the letter (phoneme)
 * 
 * {@link com.dostproject7.speech.FeedbackHandler FeedbackHandler}.
 * 
 * @author Edric Solis
 * @version 1.0
 * 
 */

public class FeedbackHandler {

	private static ArrayList<Integer> store = new ArrayList<Integer>();
	private final static Logger LOGGER = LogManager.getLogger();

	public static String result(String input, String text) {
		LOGGER.debug("Editing the text to show error");
		LOGGER.debug("text = {}", text);
		LOGGER.debug("index of error = {}", input);

		StringBuffer result = new StringBuffer();
		Pattern p = Pattern.compile("[0-9]+");
		Matcher m = p.matcher(input);

		store.clear();

		while (m.find()) {
			int n = Integer.parseInt(m.group());
			store.add(n);
		}
		// sentences
		if (text.contains(" ")) {

			String[] words = text.split(" ");
			String[] combine = new String[words.length];

			ArrayList<String> wordList = new ArrayList<String>();

			for (String splitWords : words) {
				wordList.add(splitWords);
			}

			for (int i = 0; i < words.length; i++) {
				if (store.contains(i)) {
					combine[i] = wordList.get(i).replace(wordList.get(i),
							"<c>" + wordList.get(i) + "</c>");
				} else {
					combine[i] = wordList.get(i);
				}
			}

			for (int i = 0; i < combine.length; i++) {
				if (i != 0) {
					result.append(" " + combine[i]);
				} else {
					result.append(combine[i]);
				}
			}

			// words
		} else {

			String s;
			String[] combine = new String[text.length()];

			for (int i = 0; i < text.length(); i++) {
				s = text.substring(i, i + 1);
				if (store.contains(i)) {
					combine[i] = s.replace(s, "<c>" + s + "</c>");
				} else {
					combine[i] = s;
				}
			}

			// Combine strings inside array
			for (int i = 0; i < combine.length; i++) {
				result.append(combine[i]);
			}
		}
		LOGGER.debug("new text = {}", result.toString());
		LOGGER.debug("Successfully edited the text to show error");
		return result.toString();

	}

	public ArrayList<Integer> feedbackIndex() {
		return store;
	}

	public static String getFeedbackIndex(String err, String[] mapping) {
		LOGGER.debug("Mapping the phoneme index to its corresponding letter index in the word");
		LOGGER.debug("phoneme index with error = {}", err);
		
		String[] arr = err.split(",");
		String fbindex = "";

		for (int i = 0; i < arr.length; i++) {
			String map = mapping[i];
			fbindex += map + ",";
		}
		LOGGER.debug("Successfully Mapped the phoneme index: {}", fbindex);
		return fbindex;
	}

}
