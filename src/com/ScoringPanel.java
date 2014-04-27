package com;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

import com.db.DbManager;
import com.util.Globals;
import com.util.Playback;

public class ScoringPanel extends JFrame implements ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3577503964371068047L;
	private JPanel playpanel;
	private JTextArea question;
	private JButton play;
	
	private JLabel errormsg;
	private JButton prev;
	private JButton next;
	private JLabel rem;
	
	private JPanel scorepanel;
	private ButtonGroup scoregroup[];
	private JRadioButton score[][];
	private String[] scoreDesc = {Globals.PA, Globals.BF, Globals.UN};
	private Playback audio;
	
	private static DbManager db;
	
	private String username;
	
	public ScoringPanel(DbManager database){
		
		db = database;
		
		while(username == null || username.isEmpty()){
			username = JOptionPane.showInputDialog("Enter username:");
		}
		db.setCurrentUser(username, Globals.CREATEIFNOTEXISTING);
		playpanel = new JPanel();
		audio = new Playback();
		
		JMenuBar menu = new JMenuBar();
		JMenu file = new JMenu("File");
		LogParser importLogs = new LogParser(db);
		importLogs.addActionListener(this);
		importLogs.addDefaultActionListener();
		importLogs.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				errormsg.setText("Loading...");
			}
		});
		
		WebParser importOnline = new WebParser(db);
		importOnline.addActionListener(this);
		importOnline.addDefaultActionListener();
		importOnline.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				errormsg.setText("Loading...");
			}
		});
		
		AutoScorer autoScore = new AutoScorer();
		autoScore.addActionListener(this);
		autoScore.addDefaultActionListener();
		autoScore.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				errormsg.setText("Generating automatic scores...");
			}
		});
		
		
		file.add(importLogs);
		file.add(importOnline);
		file.addSeparator();
		file.add(autoScore);
		menu.add(file);
		setJMenuBar(menu);
		
        play = new JButton("PLAY");
        play.addActionListener( new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
                if(audio.isNull()) play.setEnabled(false);
                else audio.run();
            }
        } );
        
        rem = new JLabel("Progress: ");
        
        question = new JTextArea(7, 40);
        question.setWrapStyleWord(true);
        question.setLineWrap(true);
        question.setEnabled(false);
        question.setDisabledTextColor(Color.BLACK);
        
        JPanel playstatus = new JPanel(new BorderLayout(5,5));
        playstatus.add(play, BorderLayout.CENTER);
        playstatus.add(rem, BorderLayout.SOUTH);
        playpanel.add(playstatus);
        playpanel.add(question);

        
        scorepanel = new JPanel(new GridLayout(1,scoreDesc.length));
        
        scoregroup = new ButtonGroup[scoreDesc.length];
        JPanel scoregrouppanel[] = new JPanel[scoreDesc.length];
        score = new JRadioButton[scoreDesc.length][];
        for(int i=0; i<scoreDesc.length; i++){
        	int numScore = 5;
        	double interval = 0.25;
        	score[i] = new JRadioButton[numScore];
        	scoregroup[i]  = new ButtonGroup();
        	scoregrouppanel[i] = new JPanel(new GridLayout(5,1));
        	scoregrouppanel[i].setBorder(BorderFactory.createTitledBorder(scoreDesc[i]));
        	scoregroup[i] = new ButtonGroup();
        	for(int j = 0; j<numScore; j++){
        		score[i][j] = new JRadioButton(""+(j*interval));
        		scoregroup[i].add(score[i][j]);
        		scoregrouppanel[i].add(score[i][j]);
        	}
        	scorepanel.add(scoregrouppanel[i]);
        	
        }
        
        JButton export = new JButton("Export");
        export.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					Export.ExportUserScores(username, db);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
        
        errormsg = new JLabel("");
        errormsg.setForeground(Color.RED);
        
        prev = new JButton("PREVIOUS");
        prev.addActionListener( new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
            	
            	//play.setEnabled(false);
            	submitScore(false);
            	db.getPrevToScore();
            	updateScoringPanel(true);
    	        //play.setEnabled(true);
            	
	            
            }
        } );
        
        next = new JButton("NEXT");
        next.addActionListener( new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
            	try{
            		//play.setEnabled(false);
            		if(submitScore(true)){
            			db.getNextToScore();
            			updateScoringPanel(true);
                    	
            		}
            		
    	           // play.setEnabled(true);
            	}
            	catch(NullPointerException e){
            		updateScoringPanel(false);
            	}
	            
            }
        } );
        
        JPanel bottom = new JPanel(new BorderLayout(10,10));
        JPanel bottom_left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel login = new JLabel("Logged in as: "+username);
        
        bottom_left.add(login);
        bottom_left.add(export);
        
        JPanel bottom_right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom_right.add(errormsg);
        bottom_right.add(prev);
        bottom_right.add(next);
        
        bottom.add(bottom_left, BorderLayout.WEST);
        bottom.add(bottom_right, BorderLayout.EAST);
        
        
        //bottom.add(rem);
        
        add(playpanel, BorderLayout.NORTH);
        add(scorepanel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        
        
        initScoringPanel();
        
        setResizable(false);
		setTitle("Manual Scorer");
		setSize(650, 350);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public ScoringPanel() {
		this(db);
	}

	
	public static void main(String[] args) {
		db = new DbManager();
		new ScoringPanel();	        
        
	}
	
	private void initScoringPanel(){
		//init
        db.initScoringPanel();
        
        if (db.counter <= 0) prev.setEnabled(false);
        if(db.counter < db.total && db.current_recording != null){
        	updateScoringPanel(true);
        }
        else{
        	updateScoringPanel(false);
        }
	}
	
	private void updateScoringPanel(boolean existing){
		if (existing){
			audio.setFile(db.current_recording.getPath()+db.current_recording.getFilename());
        	question.setText(db.current_question.getText());
        	if(db.current_score != null) setScore(db.current_score.getScores());
        	rem.setText("Progress: "+(db.counter+1) +"/"+db.total);
        	
    		if (db.counter > 0) prev.setEnabled(true);
    		else prev.setEnabled(false);
    		
    		if(db.counter < db.total) next.setEnabled(true);
        	scorepanel.setVisible(true);
        	        	
        	play.setEnabled(true);
		}
		else{
			question.setText("No more recordings to score.");
			if (db.counter <db.total) db.counter++;
			rem.setText("Progress: "+(db.counter) +"/"+db.total);
    		play.setEnabled(false);
    		next.setEnabled(false);
    		scorepanel.setVisible(false);
    		
		}
	}
	
	private void setScore(HashMap<String, Float> scores){
		Float scoreValue = new Float(0);
		scorepanel.setVisible(true);
		double interval = 0.25;
		
		for(int i=0; i<scoreDesc.length; i++){
			
			scoreValue = scores.get(scoreDesc[i]);
			
        	score[i][(int) (scoreValue/interval)].setSelected(true);
		}
		
		
	}
	
	private boolean submitScore(boolean next){
		//submit_score1 = new String[score.length];
		//System.out.println("Submitscore"+db.current_score.getScores().toString());
		HashMap<String, Float> submit_score = new HashMap<>();
		for (int i = 0; i < score.length; i++) {
			for (int j = 0; j < score[i].length; j++) {
				if(score[i][j].isSelected()){
					submit_score.put(scoreDesc[i], Float.parseFloat(score[i][j].getText()));
					//submit_score1[i] = score[i][j].getText();
				}				
			}
			if(!submit_score.containsKey(scoreDesc[i])) {
				if (next) errormsg.setText("Please enter score for all categories.");
				else errormsg.setText("");
				return false;
			}
		}
		
		if (db.current_score ==null || !submit_score.equals(db.current_score.getScores())){
			//System.out.println("Submitting score...");
			try{
				db.submitScore(submit_score);
			}
			catch(SQLException e){
				errormsg.setText("Database error");
				e.printStackTrace();
				return false;
			}
		}
		//else System.out.println("No score change");
		for (int i = 0; i < score.length; i++) {
			scoregroup[i].clearSelection();
		}		
		errormsg.setText(" ");
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		errormsg.setText("");
		for (int i = 0; i < score.length; i++) {
			scoregroup[i].clearSelection();
		}
		initScoringPanel();
	}

}
