/**
 * ==============================================================================
 * File:			LDistance.java
 * Created:			2013/09/04
 * Last Changed:	2013/09/04
 * Author:			Reg Almonte
 * ==============================================================================
 * This code is copyright (c) 2013 Learning English Application for Pinoys (LEAP)
 * 
 * History:
 * -- add revision history here --
 * 
 */

package com.speech;

import java.util.Vector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Computes Levenshtein distance of a given string from its target string to get the
 * statistics on the number of hits, deletions, substitutions and insertions.
 * {@link com.dostproject7.speech.LDistance LDistance}
 * 
 * @author Reg Almonte
 * @version 1.0
 * 
 */

public class LDistance {

	int d[][]; 		/** distance matrix */
	String[] sarr; 	/** source sentence array */
	String[] tarr; 	/** target sentence array */
	private final static Logger LOGGER = LogManager.getLogger();

	/**
	 * Returns the smallest of three integer values
	 * @param a integer value to be compared
	 * @param b another integer value to be compared
	 * @param c another integer value to be compared
	 * @return the smallest of a, b and c
	 */
	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	/**
	 * Processes the Levenshtein matrix and initializes the
	 * other variables such as the string array versions of the source and
	 * target sentence strings. Code obtained from
	 * http://www.merriampark.com/ld.htm.
	 * 
	 * @param s
	 *            - source string or the decoded Text
	 * @param t
	 *            - target String or the reference Text
	 */

	public void processLD(String s, String t) {

		String sclean = s.trim();
		String tclean = t.trim();
		sarr = sclean.split(" ");
		tarr = tclean.split(" ");

		int n; // length of s
		int m; // length of t
		int i; // iterates through s
		int j; // iterates through t

		n = sarr.length;
		m = tarr.length;

		d = new int[n + 1][m + 1];

		for (i = 0; i <= n; i++)
			d[i][0] = i;
		for (j = 0; j <= m; j++)
			d[0][j] = j;
		for (i = 1; i <= n; i++)
			for (j = 1; j <= m; j++) {
				d[i][j] = minimum(d[i - 1][j] + 1, d[i][j - 1] + 1,
						d[i - 1][j - 1]
								+ ((sarr[i - 1].equals(tarr[j - 1])) ? 0 : 1));

			}
		LOGGER.debug("");
	}

	/**
	 * Returns the Levenshtein distance using the generated
	 * matrices that was initialized in the processLD() method.
	 * 
	 * @return ldistance the Levenshtein distance
	 */
	public int getLDistance() {

		int n = sarr.length;
		int m = tarr.length;

		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}

		return d[n][m];
	}

	/**
	 * Returns the matrix containing the values obtained during the Levenshtein
	 * process.
	 * 
	 * @return the matrix that was generated during the Levenshtein process
	 */
	public int[][] getLDMatrix() {
		return d;
	}

	/**
	 * Returns the string array needed for the getLDString() method.
	 * 
	 * @return the string array of the source sentence
	 */
	public String[] getSArr() {
		return sarr;
	}

	/**
	 * This method returns the string array needed for the getLDString() method.
	 * 
	 * @return the String Array of the target sentence
	 */
	public String[] getTArr() {
		return tarr;
	}

	/**
	 * Returns the various hit words, insertions, deletions and substitutions performed to obtain
	 * the target sentence from the source sentence. It processes EDIT TEXT String by using the
	 * information obtained during the Levenshtein process 
	 * 
	 * @param d
	 *            - value returned by getLDMatrix()
	 * @param sarr
	 *            - value returned by getSArr()
	 * @param tarr
	 *            - value returned by getTArr()
	 * @return the EDIT TEXT String
	 */
	public String getLDString(int[][] d, String[] sarr, String[] tarr) {
		String finalsent = "";

		int i = d.length - 1;
		int j = d[1].length - 1;

		while (i != 0 || j != 0) {
			if (i > 0 && j > 0) {
				if (d[i][j] - d[i - 1][j - 1] == 0
						&& sarr[i - 1].equals(tarr[j - 1])) {
					finalsent = sarr[i - 1] + " " + finalsent;
					j--;
					i--;
				}
			}
			if (i > 0)
				if (d[i][j] - d[i - 1][j] == 1) {
					finalsent = "<del> " + finalsent;
					i--;
				}
			if (j > 0) {
				if (d[i][j] - d[i][j - 1] == 1) {
					finalsent = "<ins> " + finalsent;
					j--;
				}
			}
			if (i > 0 && j > 0) {
				if (d[i][j] - d[i - 1][j - 1] == 1) {
					finalsent = "<sub> " + finalsent;
					j--;
					i--;
				}
			}
		}
		return finalsent;
	}

	/**
	 * 
	 * Creates a list of the results of the different indices, ignoring the
	 * insertions that occured. The best statistic (hit, del, ins, sub) will be
	 * also displayed.
	 * 
	 * @param LDString text that shows where insertions, deletions and substitutions occurred in the target sentence
	 * @return positions of the deletions and substitutions that occurred
	 */
	public Vector<Integer> getLDStringStats(String LDString) {
		String[] ldarr = LDString.split(" ");
		Vector<String> revisedString = new Vector<String>();
		Vector<Integer> errorlist = new Vector<Integer>();
		int hit = 0;
		int del = 0;
		int ins = 0;
		int sub = 0;

		for (int i = 0; i < ldarr.length; i++) {
			if (!ldarr[i].equals("<ins>")) {
				revisedString.add(ldarr[i]);
			}
			if (ldarr[i].equals("<ins>")) {
				ins++;
			} else if (ldarr[i].equals("<del>")) {
				del++;
			} else if (ldarr[i].equals("<sub>")) {
				sub++;
			} else {
				hit++;
			}
		}

		errorlist.add(sub + del);
		errorlist.add(sub + del + hit);

		for (int i = 0; i < revisedString.size(); i++) {
			if (revisedString.get(i).equals("<del>")) {
				errorlist.add(i);
			} else if (revisedString.get(i).equals("<sub>")) {
				errorlist.add(i);
			}
		}

		LOGGER.debug("OUTPUT: Best Stat: wer=" + errorlist.firstElement()
				+ "; hit=" + hit + "; del" + del + "; insert=" + ins + "; sub="
				+ sub);

		return errorlist;
	}

}
