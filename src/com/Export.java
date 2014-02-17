package com;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import com.db.DbManager;
import com.db.Speaker;
import com.db.User;
import com.j256.ormlite.dao.GenericRawResults;
import com.util.ExcelHandler;
import com.util.Globals;

public class Export {
	static ExcelHandler excel;
	
	static DbManager db;
	
	static String[] headers = {"WAV File", "Question Text", "Pronunciation & Articulation", "Blending & Fluency", "Understanding"};
	private static int[] columnWidth;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			String username = JOptionPane.showInputDialog("Enter username");
			ExportUserScores(username);
					
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	private static void ExportUserScores(String username) throws Exception {
		DbManager db = new DbManager();
		ExportUserScores(username, db);
		// TODO Auto-generated method stub
		
	}

	public static void ExportUserScores(String username, DbManager database) throws Exception{
		excel = new ExcelHandler("Manual Scoring ("+username +")");
		db = database;
		db.setUser(username, Globals.DISREGARDIFNOTEXISTING);
		List<Speaker> speakerList = db.getScoredSpeakers(username);
		if(speakerList.isEmpty()) {
			JOptionPane.showMessageDialog(null, "No stored scores for user "+username);
			return;
		}
		columnWidth = new int[]{30, 100, 20, 20, 20};
		for (Iterator<Speaker> iterator = speakerList.iterator(); iterator.hasNext();) {
			Speaker speaker = (Speaker) iterator.next();
			createSheet(speaker);
		}
		cleanup();
	}
	
	private static void createSheet(Speaker speaker){
		excel.newSheet(speaker.getName());
		
		
		
		
		writeScores(speaker);
		writeSpeakerInfo (speaker);
		excel.setColumnWidth(columnWidth);
		excel.freezeHeaderRow();
	}
	
	private static void writeScores(Speaker speaker){
		try{
			excel.writeRow(headers);
			GenericRawResults<String[]> results = db.getScores(speaker);
			for (String[] scoreRow : results) {
				excel.writeRow(scoreRow);
			}
		} catch(SQLException e){
			System.out.println("Error writing scores.");
			e.printStackTrace();
		}
	}

	private static void writeSpeakerInfo(Speaker speaker){
		try{
			excel.writeRow(new String[1]);
			String[][] speakerinfo = speaker.mapSpeakerInfo();
			for (int i = 0; i < speakerinfo.length; i++) {
				
				excel.writeRow(speakerinfo[i]);			
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error writing speaker info.");
			e.printStackTrace();
		}

	}

	private static void cleanup() throws IOException{
		excel.closeFile();
		
	}
	

}
