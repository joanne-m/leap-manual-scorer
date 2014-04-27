package com;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.HashMap;

import javax.swing.JMenuItem;

import com.db.DbManager;
import com.db.User;
import com.util.Globals;

public class AutoScorer extends JMenuItem implements ActionListener{
	
	private static DbManager db;
	private String AUTOSCORER = "AUTOSCORER";
	
	public AutoScorer() {
		db = new DbManager();
		setText("Generate automated scores");

	}

	public void addDefaultActionListener() {
		addActionListener(this);
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		db.setCurrentUser(AUTOSCORER, Globals.CREATEIFNOTEXISTING);
		db.initScoringPanel(0);
		boolean remaining = true;
		while (remaining){
			try {
				String file = db.current_recording.getPath()+db.current_recording.getFilename();
				String question = db.current_question.getText();
				HashMap<String, Float> scores = autoScore(file, question);
			
				db.submitScore(scores);
				db.getNextToScore();
			} catch (NullPointerException e){
				remaining = false;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			Export.ExportUserScores(AUTOSCORER, db);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
	}

	private HashMap<String, Float> autoScore(String file, String question) {
		//code for scoring here!
		
		
		HashMap<String, Float> submit_score = new HashMap<>();
		
		submit_score.put(Globals.PA, (float) 0.00); //score for pronunciation and articulation
		submit_score.put(Globals.BF, (float) 0.50); //score for blending and fluency
		submit_score.put(Globals.UN, (float) 1.00); //score for understanding
				
		return submit_score;
	}

}
