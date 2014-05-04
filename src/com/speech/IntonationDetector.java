/**
 * ==============================================================================
 * File:			IntonationDetector.java
 * Created:			2013/09/04
 * Last Changed:	2014/01/15
 * Author:			Gay Figueroa
 * ==============================================================================
 * This code is copyright (c) 2013 Learning English Application for Pinoys (LEAP)
 * 
 * History:
 * 
 * 01/15/2014 - Added code for incrementing partial points, getting pitch of last 3 words per IP
 * 01/28/2014 gay - refactored
 * 
 */

package com.speech;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Extracts the intonation of a signal per intonational phrase (IP) by analyzing
 * the pitch contour of the speech data
 * {@link com.dostproject7.speech.IntonationDetector IntonationDetector}
 * 
 * @author Gay Figueroa
 * @version 1.0
 * 
 */

public class IntonationDetector {
	/** Id of utterance based on filename */
	private String filename;

	/** Reference text or the utterance **/
	private String referenceText;

	/** Intonational phrases of the utterance **/
	private List<IntonationalPhrase> intonationalPhrases;

	/** Word boundaries of the utterance **/
	private List<WordBoundary> wordBoundaries;

	/** Pitch contour of speech file **/
	private List<Pitch> pitchContour;

	/** Pitch boundary speech file **/
	private Pitch[] pitchBoundary;

	/** Types of intonation **/
	private final static String UNCLASSIFIED = "Unclassified";
	private final static String RISING = "Rising";
	private final static String UPPER = "Upper";
	private final static String FALLING = "Falling";
	private final static String LOWER = "Lower";

	/** Pitch levels **/
	private final static String HIGH_PITCH = "H";
	private final static String NEUTRAL_PITCH = "N";
	private final static String LOW_PITCH = "L";
	
	/** Edge tones per type of intonation **/
	private static String[] risingEdgeTone = new String[] { "H-L-H", "L-L-H", "L-H-H",
			"L-H-N", "L-N-N", "N-H-H", "N-H-N", "N-L-H", "H-H-N", "L-H-H",
			"L-N-H", "H-N-H", "N-N-H", "N-L-N" };
	private static String[] fallingEdgeTone = new String[] { "H-N-L", "H-L-L", "L-H-L",
			"L-L-N", "N-N-L", "L-N-L", "N-L-L", "H-H-L", "N-H-L", "L-H-L",
			"H-L-N", "H-N-N", };
	private static String[] upperEdgeTone = new String[] { "H-H-H" };
	private static String[] lowerEdgeTone = new String[] { "N-N-N", "L-L-L" };
	
	/** Pitch boundary names **/
	private final static int H2 = 3; // upper boundary for high pitch
	private final static int H1 = 2; // lower boundary for high pitch
	private final static int L2 = 1; // upper boundary for low pitch
	private final static int L1 = 0; // lower boundary for low pitch

	private final static Logger LOGGER = LogManager.getLogger();

	/**
	 * Constructs an new instance of utterance
	 * 
	 * @param filename
	 *            the path of speech file to be processed
	 * @param referenceText
	 *            the reference text
	 */
	public IntonationDetector(String filename, String referenceText) {
		this.filename = filename;
		this.referenceText = referenceText;
	}

	/**
	 * Returns the path of the speech file to be processed
	 * 
	 * @return the path of the speech file to be processed
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Sets the path of speech file to be processed
	 * 
	 * @param filename
	 *            the path of speech file to be processed
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * Returns the utterance or reference text
	 * 
	 * @return the utterance or reference text
	 */
	public String referenceText() {
		return referenceText;
	}

	/**
	 * Sets the utterance or reference text
	 * 
	 * @param referenceText
	 *            the utterance or reference text
	 */
	public void referenceText(String referenceText) {
		this.referenceText = referenceText;
	}

	/**
	 * Returns the parsed intonational phrases of the utterance
	 * 
	 * @return the intonational phrases of the utterance
	 */
	private List<IntonationalPhrase> getIntonationalPhrases() {
		return intonationalPhrases;
	}

	/**
	 * Sets the intonational phrases of the utterance
	 * 
	 * @param intonationalPhrases
	 *            the intonational phrases of the utterance
	 */
	private void setIntonationalPhrases(List<IntonationalPhrase> intonationalPhrases) {
		this.intonationalPhrases = intonationalPhrases;
	}

	/**
	 * Returns the word boundaries of the decoded speech
	 * 
	 * @return the word boundaries of the decoded speech
	 */
	@SuppressWarnings("unused")
	private List<WordBoundary> getWordBoundaries() {
		return wordBoundaries;
	}

	/**
	 * Sets the word boundaries of the utterance
	 * 
	 * @param wordBoundaries
	 *            the word boundaries of the utterance
	 */
	private void setWordBoundaries(List<WordBoundary> wordBoundaries) {
		this.wordBoundaries = wordBoundaries;
	}

	/**
	 * Returns the pitch contour of the speech file
	 * 
	 * @return the pitch contour of the speech file
	 */
	@SuppressWarnings("unused")
	private List<Pitch> getPitchContour() {
		return pitchContour;
	}

	/**
	 * Sets the pitch contour of the speech file
	 * 
	 * @param pitchContour
	 *            the pitch contour of the speech file
	 */
	private void setPitchContour(List<Pitch> pitchContour) {
		this.pitchContour = pitchContour;
	}
	
	/**
	 * Returns the pitch boundary (H2, H1, L2, L1) of the wav file
	 * @return the pitch boundary (H2, H1, L2, L1) of the wav file
	 */
	private Pitch[] getPitchBoundary() {
		return this.pitchBoundary;
	}
	
	/**
	 * Sets the pitch boundary (H2, H1, L2, L1) of the wav file
	 * @param pitchBoundary the pitch boundary (H2, H1, L2, L1) of the wav file
	 */
	private void setPitchBoundary(Pitch[] pitchBoundary) {
		this.pitchBoundary = pitchBoundary;
	}

	/**
	 * Parses the reference text of the utterance into intonational phrases (IP)
	 * based on word boundaries from the decoded text and evaluates the
	 * intonation of each IP based on the pitch contour of the speech file
	 * 
	 * @param speechFilename
	 *            the path of speech file to be processed
	 * @param refText
	 *            the reference text or utterance
	 * @param uttIntonation
	 *            the list of expected intonation for all IPs in the utterance
	 * @param corrPath
	 *            the decoded speech from the speech file
	 * @return the {@link com.dostproject7.speech.IntonationDetector
	 *         SpeechUtterance} object
	 * @throws IOException
	 *             if reference text cannot be parsed, speech file was not
	 *             decoded or pitch contour of speech file was not computed
	 */
	public static IntonationDetector createInstance(String speechFilename,
			String refText, String uttIntonation, String corrPath)
			throws IOException, ArrayIndexOutOfBoundsException {
		
		LOGGER.debug("Cleaning reference text: {}", refText);
		String cleanText = cleanReferenceText(refText);
		LOGGER.info("Clean reference text: {}", cleanText);
		
		IntonationDetector utterance = new IntonationDetector(speechFilename,
				cleanText);

		LOGGER.debug("Getting word boundaries from the correct path",
				cleanText);
		List<WordBoundary> uttWordBoundaries = utterance.parseWordBoundaries(corrPath);
		utterance.setWordBoundaries(uttWordBoundaries);
		
		LOGGER.debug("Extracting pitch contour of speech file {}",
				speechFilename.replace("\"", ""));
		speechFilename = speechFilename.replace("\"", "");
		List<Pitch> pitchContour = Pitch.computePitch(new File(
				speechFilename));
		utterance.setPitchContour(pitchContour);
		LOGGER.debug("Extracted signal from pitch contour");

		LOGGER.debug("Identifying pitc boundary of wav file");
		Pitch[] pitchBoundary = utterance.findPitchBoundary();
		utterance.setPitchBoundary(pitchBoundary);
		LOGGER.info("> identified H2 = {}\tH1 = {}\tL2 = {}\tL1 = {}", 
				pitchBoundary[H2].getValue(), pitchBoundary[H1].getValue(), 
				pitchBoundary[L2].getValue(), pitchBoundary[L1].getValue());		

		/**
		 * Parse the utterance to intonational phrases based on sentence structure, 
		 * and get the word boundaries and its corresponding pitch of IP. 
		 * For each IP, get its edge tone and the corresponding intonation
		 */

		Pattern ipPattern = Pattern.compile("([-\\s\\w']+)([\\w\\.,\\?;!])");
		Matcher ipMatcher = ipPattern.matcher(utterance.referenceText());

		LOGGER.debug("Parsing reference to intonational phrases based on punctuation marks");
		String[] intonationStringPerIP = uttIntonation.split("\\s+");
		List<IntonationalPhrase> ips = new ArrayList<IntonationalPhrase>();
		int ipStartWordIndex = 0;
		int ipIndex = 0;
		
		while (ipMatcher.find()) {
			String ipString = ipMatcher.group(1).trim(); // text before punctuation mark
			String ipMark = ipMatcher.group(2); // punctuation mark
			String ipIntonation = ipIndex < intonationStringPerIP.length ? intonationStringPerIP[ipIndex] : "";
			String[] allWords = ipString.split("\\s+");
			int ipEndWordIndex = ipStartWordIndex + allWords.length - 1;
			int edgeWordStartIndex = allWords.length >= 3 ? ipEndWordIndex - 2 : ipEndWordIndex - allWords.length + 1;  
			
			IntonationalPhrase ip = utterance.new IntonationalPhrase(ipIndex,
					ipMark, ipString, ipIntonation.split("\\|"));
			
			LOGGER.info("Created IP with mark: {}\tstring: {}", ipMark, ipString);
//			ip.printWords();
			
			// Get start and last sample of IP in the wav file
			LOGGER.info("Getting word boundaries of IP - word(s) {} to {}",
					ipStartWordIndex, ipEndWordIndex);
			WordBoundary edgeStartWord = utterance.getSampleFromWordIndex(edgeWordStartIndex);
			WordBoundary edgeLastWord = utterance.getSampleFromWordIndex(ipEndWordIndex);
			LOGGER.debug(
					"Edge tone is from sample {}-{} to {}-{}",
					edgeStartWord.getWordString(), edgeStartWord.getStartingSample(), edgeLastWord.getWordString(), edgeLastWord.getEndSample());
			
			// Determine the critical pitch values of last word for this IP based on its samples
			Pitch[] criticalPitch = utterance.findCriticalPitch(edgeStartWord.getStartingSample(), edgeLastWord.getEndSample());
			ip.setCriticalPitch(criticalPitch);
			LOGGER.debug("Min pitch={}\tMax pitch={}\tFinal pitch={}", criticalPitch[IntonationalPhrase.MIN_PITCH],
					criticalPitch[IntonationalPhrase.MAX_PITCH], criticalPitch[IntonationalPhrase.FINAL_PITCH]);

			LOGGER.debug("Getting edge tone of wav file");
			ip.setDetectedEdgeTone(IntonationDetector.detectEdgeTone(ip,
					utterance));

			LOGGER.debug("Getting intonation of wav file");
			ip.setDetectedIntonation(IntonationDetector.detectIntonation(ip,
					utterance));

			ips.add(ip);

			LOGGER.info(
					"Correct intonation:{}\tDetected Intonation:{}\tEdge tone:{}\t\tPitch Values:{}\tIP: {}",
					ip.getPossibleIntonations()[0], ip.getDetectedIntonation(),
					ip.getDetectedEdgeTone(),
					IntonationDetector.detectEdgeTonePitch(ip, utterance),
					ipString);

			// Update IP count and word boundary index
			ipIndex++;
			ipStartWordIndex = ipEndWordIndex + 1;
		}

		utterance.setIntonationalPhrases(ips);

		return utterance;
	}
	
	/**
	 * Cleans the text by removing unnecessary characters for parsing into intonational phrases
	 * @param rawText the text to be cleaned
	 * @return the text to be parsed
	 */
	public static String cleanReferenceText(String rawText) {
		return rawText.replace("; || <br>", ";")
		.replace(".... <br>", ".").replace(", <br>", ",")
		.replace(". <br>", ".").replace(" <br>", ".")
		.replace("�", "'").replace("...", ",").replace(".'", ".")
		.replace(" (Pause) ", " ").replace(" (Silence) ", " ")
		.replace("|", "").replace("(", "").replace(")", "")
		.replace("  ", " ").replace("..", ",").replaceAll("^'", "")
		.replace("..", ",").replace("Mrs.", "Mrs").replace("Mr.", "Mr")
		.replace("Ms.", "Ms").replace("8:00", "eight o'clock")
		.replace("24/7", "TWENTY FOUR SEVEN")
		.replace("9.95", "NINE-POINT-NINETY-FIVE")
		.replace("800-PASSENGER", "EIGHT-HUNDRED-PASSENGER")
		.replace("&", "AND").replace("U.S", "US").replace("(", "")
		.replace(")", "");
	}
	
	/**
	 * Reads and returns the correct path output from the command line
	 * 
	 * @return the correct path
	 */
	public static String readCorrPath(String command) throws IOException,
			InterruptedException {
		Runtime r = Runtime.getRuntime();
		Process p = r.exec(command);
		Thread.sleep(3000);
		BufferedReader b = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		int i = 0;
		String[] templ = new String[20];
		while ((templ[i] = b.readLine()) != null) {
			if (templ[i].indexOf('*') == 0) {
				p.destroy();
			}
			i++;
		}
		return templ[8];
	}
	
	/**
	 * Returns the word boundaries parsed from the correct path
	 * @param corrPath the correct path
	 * @return word boundaries
	 * @throws IOException if it fails to parse the correct path with the specified format
	 */
	public List<WordBoundary> parseWordBoundaries(String corrPath) throws IOException {
		List<WordBoundary> parsedWordBoundaries = new ArrayList<WordBoundary>();
		
		if (corrPath != null && corrPath.contains("} {")) {
			int wordIndex = 0;
			String[] decoded = corrPath.substring(1, corrPath.length() - 1)
					.split("\\} \\{");
			
			for (String rawDecodedLine : decoded) {
				String[] splitLine = rawDecodedLine.split(" ");
				String wordString = splitLine[0].trim();
				if (wordString.indexOf("(") > 0) {
					wordString = wordString.substring(0,
							wordString.indexOf("("));
				}
				int startingSample = Integer.valueOf(splitLine[1].trim());
				int endSample = Integer.valueOf(splitLine[2].trim());

				WordBoundary wb = new WordBoundary(wordIndex,
						wordString, startingSample, endSample);
				parsedWordBoundaries.add(wb);
				LOGGER.debug("WB:" + wb.getWordIndex() + "\t"
						+ wb.getWordString() + "\t" + wb.getStartingSample()
						+ "\t" + wb.getEndSample());

				if (!wb.isSIL()) {
					wordIndex++;
				}
			}
			 
		} else {
			LOGGER.error(
					"Failed to parse reference text in {}. Decoded speech is empty",
					IntonationDetector.class.getSimpleName());
			throw new IOException("Decoded speech is empty");
		}
		
		return parsedWordBoundaries;
	}
	
	/**
	 * Returns the upper and lower boundary for high pitch and low pitch
	 * @param pitchContour the pitch contour of the wav file
	 * @return the pitch boundary for the high and low levels of pitch of this wav file
	 * @throws IOException if it fails to extract the pitch boundary
	 */
	public Pitch[] findPitchBoundary() throws IOException {
		Pitch[] pitchBoundary = new Pitch[4];
		Pitch h2 = new Pitch();
		Pitch h1 = new Pitch();
		Pitch l2 = new Pitch();
		Pitch l1 = new Pitch();

		for (Pitch f0i1 : this.pitchContour) {
			// 4.1 update the upper boundary for high pitch of the contour
			if (f0i1.getValue() > 0
					&& (h2.isEmpty() || f0i1.getValue() > h2.getValue())) {
				h2 = f0i1;
			}
			// 4.2 update the lower boundary for low pitch of the contour
			if (f0i1.getValue() > 0
					&& (l1.isEmpty() || f0i1.getValue() < l1.getValue())) {
				l1 = f0i1;
			}
		}

		// 4.3 compute the lower boundary for high pitch and the higher boundary for low pitch based on the highest and the lowest pitch value
		if (!l1.isEmpty() && !h2.isEmpty()) {
			double gap = h2.getValue() - l1.getValue();
			int buffer = gap > 100 ? (int) Math.floor(.20 * gap) : (int) Math
					.floor(.30 * gap);

			LOGGER.info("fixed range buffer={}", buffer);
			l2 = new Pitch(l1.getValue() + buffer);
			h1 = new Pitch(h2.getValue() - buffer);
		} else {
			LOGGER.debug("Failed to extract H2 and L1 from pitch contour");
			throw new IOException(
					"No H2 and L1 extracted from the pitch contour!");
		}
		
		pitchBoundary[L1] = l1;
		pitchBoundary[L2] = l2;
		pitchBoundary[H1] = h1;
		pitchBoundary[H2] = h2;
		
		return pitchBoundary;
	}
	
	/**
	 * Returns the word boundary object of the word at index
	 * @param index the word index 
	 * @return the word boundary at the given index
	 */
	public WordBoundary getSampleFromWordIndex(int index) {
		WordBoundary word = new WordBoundary();
		for (WordBoundary wb : this.wordBoundaries) {
			if (wb.getWordIndex() == index && wb.getWordString()==null)
				System.out.println("THIS IS NIL\t"+wb.getWordIndex());
			else if (wb.getWordIndex() == index && !wb.isSIL())
				word = wb;
		}
		return word;
	}
	
	/**
	 * Returns the min, max and final pitch of the pitch contour between the given sample indices
	 * @param edgeStartSample the starting sample index
	 * @param edgeLastSample the end sample index
	 * @return the min, max and final pitch
	 */
	public Pitch[] findCriticalPitch(int edgeStartSample, int edgeLastSample) {
		Pitch finalPitch = new Pitch();
		Pitch min = new Pitch(1000);
		Pitch max = new Pitch();
		Pitch[] criticalPitch = new Pitch[3];
		
		List<Pitch> edgeTone = this.pitchContour.subList(
				edgeStartSample, edgeLastSample + 1);
		
		for (Pitch pitchItem : edgeTone) {
			if (finalPitch.isEmpty() || pitchItem.getValue() > 0) {
				finalPitch = pitchItem;
			}
		}

		for (Pitch pitchItem : edgeTone) {
			double pVal = pitchItem.getValue();
			int pIndex = pitchItem.getSampleIndex();
			if (pVal > 0 && pVal < min.getValue()
					&& pIndex < finalPitch.getSampleIndex()) {
				min = pitchItem;
			}
			if (max.isEmpty() || (pVal > 0 && pVal > max.getValue())
					&& pIndex < finalPitch.getSampleIndex()) {
				max = pitchItem;
			}
		}

		// if no possible min pitch > 0, choose the first pitch
		if (min.getValue() == 1000)
			min = edgeTone.get(0); 
		
		criticalPitch[IntonationalPhrase.MIN_PITCH] = min;
		criticalPitch[IntonationalPhrase.MAX_PITCH] = max;
		criticalPitch[IntonationalPhrase.FINAL_PITCH] = finalPitch;
		
		return criticalPitch;
	}

	/**
	 * Returns the total points counted for each IP with matching intonation
	 * over the total number of IPs
	 * 
	 * @return the total points for matching intonation
	 */
	public double checkIntonation() {
		double partialPoints = this.getPartialPoints();
		double correctIntonationCount = 0;
		List<IntonationalPhrase> ipList = this.getIntonationalPhrases();

		for (IntonationalPhrase ip : ipList) {
			String actualPosInt = ip.getPossibleIntonations()[0];
			String technicallyPosInt = actualPosInt.equals(FALLING) ? LOWER
					: (actualPosInt.equals(RISING) ? UPPER : (actualPosInt
							.equals(LOWER) ? FALLING : (actualPosInt
							.equals(UPPER) ? RISING : "")));

			if (ip.getDetectedIntonation().equals(actualPosInt)) {
				correctIntonationCount += 1;
			} else if (ip.getDetectedIntonation().equals(technicallyPosInt)) {
				correctIntonationCount += partialPoints;
			}
		}
//		System.out.println(newDBIntonation.trim()+"\t"+edgeTone.trim()+"\t"+edgePitch.trim());
		
		return ((double) correctIntonationCount / ipList.size());
	}
	
	/**
	 * Returns the default or adjusted partial points for users who repeated the exercise 
	 * @return the default or adjusted partial points
	 */
	public double getPartialPoints() {
		double partialPoints = 0.5;
		
		try {
			double currDelta = 0.0;
			partialPoints = currDelta < 0.5 ? currDelta + partialPoints
					: partialPoints;
			LOGGER.debug(
					"Currdelta is {}. Partial points for intonation scoring is {}",
					currDelta, partialPoints);

		} catch (Exception e) {
			LOGGER.error("Failed to get unitscore for unit x user x.");
		} 
		
		return partialPoints;
	}

	/**
	 * Returns the intonation of the IP determined by its edge tone
	 * 
	 * @return the detected intonation
	 */
	public static String detectIntonation(IntonationalPhrase ip,
			IntonationDetector utterance) throws IOException {
		String intonation;
		if (ip.matchesEdgeTone(risingEdgeTone)) {
			intonation = RISING;
		} else if (ip.matchesEdgeTone(fallingEdgeTone)) {
			intonation = FALLING;
		} else if (ip.matchesEdgeTone(upperEdgeTone)) {
			intonation = UPPER;
		} else if (ip.matchesEdgeTone(lowerEdgeTone)) {
			intonation = LOWER;
		} else {
			intonation = UNCLASSIFIED;
		}
		
		return intonation;
	}

	/**
	 * Returns the pitch level based on critical pitch points of the IP
	 * 
	 * @return the pitch level
	 */
	public static String getPitchLevel(Pitch thisPitch,
			IntonationDetector utterance) {
		double pVal = thisPitch.getValue();
		Pitch[] pitchBoundary = utterance.getPitchBoundary();
		
		if (pitchBoundary[L1].getValue() <= pVal
				&& pVal <= pitchBoundary[L2].getValue()) {
			return IntonationDetector.LOW_PITCH;
		} else if (pitchBoundary[L2].getValue() < pVal
				&& pVal < pitchBoundary[H1].getValue()) {
			return IntonationDetector.NEUTRAL_PITCH;
		} else if (pitchBoundary[H1].getValue() <= pVal
				&& pVal <= pitchBoundary[H2].getValue()) {
			return IntonationDetector.HIGH_PITCH;
		} else { // probably unvoiced
			return IntonationDetector.LOW_PITCH;
		}
	}

	/**
	 * Returns the edge tone of an IP based on critical pitch points of the
	 * speech file
	 * 
	 * @return the detected edge tone
	 **/
	private static String detectEdgeTone(IntonationalPhrase ip,
			IntonationDetector utterance) throws IOException {
		Pitch[] criticalPitch = ip.getCriticalPitch();
		Pitch ipMin = criticalPitch[IntonationalPhrase.MIN_PITCH];
		Pitch ipMax = criticalPitch[IntonationalPhrase.MAX_PITCH];
		Pitch ipFinal = criticalPitch[IntonationalPhrase.FINAL_PITCH];
		String edgeTone;

		if (ipMin.isEmpty() || ipMax.isEmpty() || ipMax.isEmpty()) {
			edgeTone = "X-X-X";
		} else {
			boolean minFirst = ipMin.getSampleIndex() < ipMax.getSampleIndex();
			String minLevel = IntonationDetector
					.getPitchLevel(ipMin, utterance);
			String maxLevel = IntonationDetector
					.getPitchLevel(ipMax, utterance);
			String finalLevel = IntonationDetector.getPitchLevel(ipFinal,
					utterance);
			edgeTone = minFirst ? minLevel + "-" + maxLevel + "-" + finalLevel
					: maxLevel + "-" + minLevel + "-" + finalLevel;
		}
		return edgeTone;
	}

	/**
	 * Returns the pitch values of the edge tone of an IP based on critical
	 * pitch points of the speech file
	 * 
	 * @return the detected edge tone
	 **/
	private static String detectEdgeTonePitch(IntonationalPhrase ip,
			IntonationDetector utterance) {
		Pitch[] criticalPitch = ip.getCriticalPitch();
		Pitch ipMin = criticalPitch[IntonationalPhrase.MIN_PITCH];
		Pitch ipMax = criticalPitch[IntonationalPhrase.MAX_PITCH];
		Pitch ipFinal = criticalPitch[IntonationalPhrase.FINAL_PITCH];
		
		String edgeTone;

		if (ipMin.isEmpty() || ipMax.isEmpty() || ipFinal.isEmpty()) {
			edgeTone = "X-X-X";
		} else {
			boolean minFirst = ipMin.getSampleIndex() < ipMax.getSampleIndex();
			edgeTone = minFirst ? (int) ipMin.getValue() + "-"
					+ (int) ipMax.getValue() + "-" + (int) ipFinal.getValue()
					: (int) ipMax.getValue() + "-" + (int) ipMin.getValue()
							+ "-" + (int) ipFinal.getValue();
		}
		return edgeTone;
	}

	/**
	 * Contains a portion of the reference text, its word boundaries and audio
	 * information needed to detect its intonation
	 */
	private class IntonationalPhrase {

		/** Index of this intonational phrase within the utterance **/
		private int ipIndex;		

		/** Punctuation mark of the IP **/
		private String punctuation;

		/** Part of utterance included in this IP **/
		private String ipString;

		/** Other possible or acceptable intonation for this IP **/
		private String[] possibleIntonations;

		/** Detected intonation of the IP based on speech file **/
		private String detectedIntonation;		

		/**
		 * Minimum, maximum and final pitch from the wav file of this IP
		 **/
		private Pitch[] criticalPitch;

		/** Detected edge tone of the wav file for this IP **/
		private String detectedEdgeTone;

		/** Critical pitch per IP **/
		public final static int MIN_PITCH = 0;
		public final static int MAX_PITCH = 1;
		public final static int FINAL_PITCH = 2;
		
		/**
		 * Constructs a new intonational phrase without detected intonation
		 */
		public IntonationalPhrase(int ipIndex, String punctuation, String ipString,
				String[] possibleIntonations) {
			this(ipIndex, punctuation,
					ipString, possibleIntonations, "");
		}

		/**
		 * Constructs a new intonational phrase
		 */
		public IntonationalPhrase(int ipIndex, String punctuation, String ipString,
				String[] possibleIntonations, String detectedIntonation) {
			this.ipIndex = ipIndex;
			this.punctuation = punctuation;
			this.ipString = ipString;
			this.possibleIntonations = possibleIntonations;
			this.detectedIntonation = detectedIntonation;
		}

		@SuppressWarnings("unused")
		public int getIpIndex() {
			return ipIndex;
		}

		@SuppressWarnings("unused")
		public void setIpIndex(int ipIndex) {
			this.ipIndex = ipIndex;
		}
		
		@SuppressWarnings("unused")
		public String getPunctuation() {
			return punctuation;
		}

		@SuppressWarnings("unused")
		public void setPunctuation(String punctuation) {
			this.punctuation = punctuation;
		}

		@SuppressWarnings("unused")
		public String getIpString() {
			return ipString;
		}

		@SuppressWarnings("unused")
		public void setIpString(String ipString) {
			this.ipString = ipString;
		}

		public String[] getPossibleIntonations() {
			return possibleIntonations;
		}

		@SuppressWarnings("unused")
		public void setPossibleIntonations(String[] possibleIntonations) {
			this.possibleIntonations = possibleIntonations;
		}

		public String getDetectedIntonation() {
			return detectedIntonation;
		}

		public void setDetectedIntonation(String detectedIntonation) {
			this.detectedIntonation = detectedIntonation;
		}
		
		public Pitch[] getCriticalPitch() {
			return this.criticalPitch;
		}
		
		public void setCriticalPitch(Pitch[] criticalPitch) {
			this.criticalPitch = criticalPitch;
		}

		public String getDetectedEdgeTone() {
			return detectedEdgeTone;
		}

		public void setDetectedEdgeTone(String detectedEdgeTone) {
			this.detectedEdgeTone = detectedEdgeTone;
		}
		
		/**
		 * Prints each word on a separate line
		 */
		@SuppressWarnings("unused")
		public void printWords() {
			 int xs = 0;
			 String[] allWords = this.ipString.split("\\s+");
			 for(String sss: allWords) {
			 System.out.println(xs++ + "\t[" + sss+"]");
			 }
		}
		
		/**
		 * Returns <code>true</code> if the detected edge tone is included in the
		 * expected edge tones
		 * 
		 * @return <code>true</code> if the detected edge tone is included in the
		 *         expected edge tones; <code>false</code> otherwise
		 **/
		public boolean matchesEdgeTone(String[] expectedEdgeTones) {
			for (String edgeTone : expectedEdgeTones) {
				if (this.detectedEdgeTone.equalsIgnoreCase(edgeTone)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Contains a word and its index in the utterance, and indices of its first
	 * and last samples in the speech file
	 */
	public class WordBoundary {

		/** Index of the word within the utterance **/
		private int wordIndex;

		/** the word **/
		private String wordString;

		/** the starting sample index of the word in the speech file **/
		private int startingSample;

		/** the last sample index of the word in the speech file **/
		private int endSample;

		/**
		 * Empty constructor
		 */
		public WordBoundary() {
			
		}
		
		/**
		 * Constructs a new word boundary
		 */
		public WordBoundary(int wordIndex, String wordString,
				int startingSample, int endSample) {
			this.wordIndex = wordIndex;
			this.wordString = wordString;
			this.startingSample = startingSample;
			this.endSample = endSample;
		}

		public int getWordIndex() {
			return wordIndex;
		}

		public void setWordIndex(int wordIndex) {
			this.wordIndex = wordIndex;
		}

		public String getWordString() {
			return wordString;
		}

		public void setWordString(String wordString) {
			this.wordString = wordString;
		}

		public int getStartingSample() {
			return startingSample;
		}

		public void setStartingSample(int startingSample) {
			this.startingSample = startingSample;
		}

		public int getEndSample() {
			return endSample;
		}

		public void setEndSample(int endSample) {
			this.endSample = endSample;
		}

		public boolean isSIL() {
			return this.wordString.equalsIgnoreCase("SIL");
		}
	}
}
