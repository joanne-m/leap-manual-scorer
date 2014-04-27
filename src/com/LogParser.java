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

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.db.DbManager;
import com.db.Speaker;
import com.util.Globals;

public class LogParser extends JMenuItem implements ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static DbManager db;
	private static String read;
	
	public LogParser(DbManager db2){
		this.db = db2;
		setText("Import log files (from rec tool)");
		//addActionListener(this);
		
	}
	
	public LogParser() {
		this(db);
	}
	
	public void addDefaultActionListener(){
		addActionListener(this);
	}
	
	/*public static void main(String[] args) {
		
		db = new DbManager();
		JFrame frame = new JFrame("Manual Scorer");
		LogParser p = new LogParser();
		frame.getContentPane().add(p);
		frame.setSize(400, 100);
		frame.setVisible(true);
		//parseLogsInDir(new File(Globals.LIB_URL));
	}*/
	
	public void actionPerformed(ActionEvent e) {
	    read = "Successfully imported the following files:";
	        
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
	      parseLogsInDir(chooser.getSelectedFile());
	      
	      }
	    else {
	      System.out.println("No Selection ");
	      }
	    if(!read.equals("Successfully imported the following files:")) JOptionPane.showMessageDialog(null, read);
	 }
	
	public static void parseLogsInDir(File dir){
		
		FilenameFilter textFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				File f = new File(dir.getPath()+File.separator+name);
				if (lowercaseName.endsWith(".log") || f.isDirectory()) {
					return true;
				} else {
					return false;
				}
			}
		};

		File[] files = dir.listFiles(textFilter);
		for (File file : files) {
			if (file.isFile()) {
				parse(file);
			}
			else if (file.isDirectory()){
				parseLogsInDir(file);
			}			
		}
	}
	
	public static void parse(File csvFile) {
		 
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = " = ";
	 
		try {
			HashMap<String, String> map = new HashMap<String, String>();
			br = new BufferedReader(new FileReader(csvFile));

			while ((line = br.readLine()) != null) {
				String[] property = line.split(cvsSplitBy);
				if(property.length <2) break;
				if (property[0].contains("SessionID")) property[0] = "SessionID";
				map.put(property[0], property[1].replaceAll("^\"|\"$", "").trim());
				//System.out.println("label= " + property[0] + " , value=" + property[1].replaceAll("^\"|\"$", "").trim() + "]");
	 
			}
			Speaker speaker = new Speaker(map);
			
			do{
				//System.out.println(line);
				String[] split = line.split("\\s+\"\\S+\"\\s+");
				//System.out.println("label= " + split[0] + " , value=" + split[1].replaceAll("^\"|\"$", "").trim() + "]");
				String filename = split[0];
				String path = "";
				if(!(split[0].equalsIgnoreCase(Globals.NO_RECORDING))) path = csvFile.getParent()+File.separator;
				
				String question = split[1].substring(1, split[1].length()-1).trim();
				//System.out.println(split[1]);
				db.updateDatabase(path, filename, question, speaker);
			} while ((line = br.readLine()) != null);
			
		read = read + "\n- "+ csvFile.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
