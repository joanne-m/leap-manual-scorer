package com.util;

public class Globals {
	public static String NO_RECORDING = "NOT_RECORDED";
	
	public static String DATABASE_URL = "jdbc:sqlite:database.db";
	public static final String IMPORT_QUESTIONS_URL = ".\\lib\\PA_questions.csv";
	//public static String LIB_URL = ".\\lib\\PC01_Day2\\PC03_Day2";
	
	public static String PA = "Pronunciation & Articulation";
	public static String BF = "Blending & Fluency";
	public static String UN = "Understandability";
	
	public static int CREATEIFNOTEXISTING = 0;
	public static int DISREGARDIFNOTEXISTING = 1;
	public static final boolean INSTALLER = false; // set this to true before
	// creating installer

}
