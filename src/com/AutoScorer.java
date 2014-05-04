package com;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import javax.swing.JMenuItem;
import javax.swing.SwingWorker;

import com.db.DbManager;
import com.speech.SpeechScorer;
import com.util.Constants;
import com.util.Globals;

public class AutoScorer extends SwingWorker<Void, Void>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static DbManager db;
	private String AUTOSCORER = "AUTOSCORER";
	private static double itemscore;

	public AutoScorer() {
		db = new DbManager();
	}

	public Void doInBackground() {
		// TODO Auto-generated method stub
		db.setCurrentUser(AUTOSCORER, Globals.CREATEIFNOTEXISTING);
		db.initScoringPanel(0);
		boolean remaining = true;
		while (remaining) {
			try {
				String file = db.current_recording.getPath()
						+ db.current_recording.getFilename();
				String question = db.current_question.getText();
				HashMap<String, Float> scores = autoScore(file, question);

				db.submitScore(scores);
				db.getNextToScore();
				setProgress(db.counter*100/db.total );
			} catch (NullPointerException e) {
				remaining = false;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		setProgress(100);
		try {
			Export.ExportUserScores(AUTOSCORER, db);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	private HashMap<String, Float> autoScore(String file, String question)
			throws IOException, InterruptedException {
		// code for scoring here!

		HashMap<String, Float> submit_score = new HashMap<>();

		/** TODO: Put janus code here */
		SpeechScorer.setParameters("", cleanText(question), "", "", "", "",
				file.replaceAll("\\\\", "/"), Constants.SP_REPEATSENTENCE, "");

		itemscore = SpeechScorer.computescore(5, 0.0);
		float[] CritScore = SpeechScorer.getScores();
		float PA = CritScore[0] / 2;
		float BF = CritScore[1] / 2;
		float UN = CritScore[2];
		System.out.println("score: " + itemscore);

		submit_score.put(Globals.PA, PA); // score for pronunciation
													// and articulation
		submit_score.put(Globals.BF, BF); // score for blending and
													// fluency
		submit_score.put(Globals.UN, UN); // score for understanding

		return submit_score;
	}

	/**
	 * Check answer/submitted recording
	 * 
	 * @throws SlickException
	 * @throws IOException
	 * @throws InterruptedException
	 */

	static String cleanText(String s) {
		s = s.replace(". ", " ").replace(".'", "'");
		s = s.replace("<b>", "").replace("</b>", "").replace("<u>", "")
				.replace("</u>", "").replace("?", "").replace(",", "")
				.replace("!", "").replace(";", "").replaceAll("\\.$", "")
				.replaceAll("\\.{2,}", "").replace(".\"", "\"")
				.replace("8:00", "eight o'clock")
				.replace("24/7", "TWENTY FOUR SEVEN")
				.replace("9.95", "NINE-POINT-NINETY-FIVE").replace(":", "")
				.replace("\"", "").replace(" '", " ").replace(" - ", " ")
				.replace("' ", " ").replace("<c1>", "").replace("</c1>", "")
				.replace("<c2>", "").replace("</c2>", "").replace("<c3>", "")
				.replace("</c3>", "").replace("<c4>", "").replace("</c4>", "")
				.replace("<BR>", "").replace("<br>", "").replace("<br/>", "")
				.replace("<BR/>", "").replace("�", "").replace("�", "")
				.replace("�", "").replaceAll("  *", " ").replaceAll("^'", "")
				.replaceAll("'$", "").replace("U.S", "US");
		s = s.replace("._", "_").replace(". ", " ").replace(".$", "");
		return s;
	}

}
