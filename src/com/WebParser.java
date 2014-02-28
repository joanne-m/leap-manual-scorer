package com;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.db.DbManager;
import com.db.Speaker;
import com.util.CSVHelper;
import com.util.Globals;

public class WebParser extends JMenuItem implements ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static DbManager db;
	private static String read;
	
	private static Pattern p;
	private static int counter;
	
	public WebParser(DbManager db2){
		this.db = db2;
		setText("Import wav files (from dostproject7.com)");
		p = Pattern.compile("(\\w+)_(\\d+).wav");
		//addActionListener(this);
		
	}
	
	public WebParser() {
		this(db);
	}
	
	public void addDefaultActionListener(){
		addActionListener(this);
	}
	
	public static void main(String[] args) {
		
		db = new DbManager();
		JFrame frame = new JFrame("Manual Scorer");
		WebParser p = new WebParser();
		p.addDefaultActionListener();
		frame.getContentPane().add(p);
		frame.setSize(400, 100);
		frame.setVisible(true);
		//parseLogsInDir(new File(Globals.LIB_URL));
	}
	
	public void actionPerformed(ActionEvent e) {
		counter = 0;
	        
	    JFileChooser chooser = new JFileChooser(); 
	    chooser.setCurrentDirectory(new java.io.File("."));
	    chooser.setDialogTitle("Select directory containing log files");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    //
	    // disable the "All files" option.
	    //
	    chooser.setAcceptAllFileFilterUsed(false);
	    //    
	    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
	      //System.out.println("getCurrentDirectory(): " +  chooser.getCurrentDirectory());
	      System.out.println("getSelectedFile() : " 
	         +  chooser.getSelectedFile());
	      parseSpeakerCsv(chooser.getSelectedFile().getAbsolutePath()+File.separator+"speakers.csv");
	      parseWavsInDir(chooser.getSelectedFile());
	      
	      }
	    else {
	      System.out.println("No Selection ");
	      }
	    JOptionPane.showMessageDialog(null, "Successfully imported "+counter+" files.");
	 }
	
	private void parseSpeakerCsv(String speakerCsv) {
		// TODO Auto-generated method stub
		File file = new File(speakerCsv);
		List<Speaker> speakerList = CSVHelper.importToList(file, Speaker.class);
		db.updateDatabase(speakerList);
	}

	public static void parseWavsInDir(File dir){
				
		FilenameFilter wavTextFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				File f = new File(dir.getPath()+File.separator+name);
				if (lowercaseName.endsWith(".wav") || f.isDirectory()) {
					return true;
				} else {
					return false;
				}
			}
		};

		File[] files = dir.listFiles(wavTextFilter);
		for (File file : files) {
			if (file.isFile()) {
				String filename = file.getName();
				
				Matcher m = p.matcher(filename);
				if(m.matches()){
					String speakerId = m.group(1);
					int questionId = Integer.parseInt(m.group(2));
					
					String path = file.getParent()+File.separator;
				
					//System.out.println(speakerId+"|"+filename+"|"+path+"|"+questionId);
					if (db.updateDatabase(path, filename, questionId, speakerId)) counter++;
				}
			}
			else if (file.isDirectory()){
				parseWavsInDir(file);
			}			
		}
	}
	
}
