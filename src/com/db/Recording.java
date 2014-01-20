package com.db;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.util.Globals;

/**
 * Recordings model class
 * @version 1.0
 */
@DatabaseTable(tableName = "recordings")
public class Recording {
	
	// for QueryBuilder to be able to find the fields
	public static final String ID_FIELD = "recording_id";
	public static final String SPEAKER_FIELD = "speaker_id";
	public static final String QUESTION_FIELD = "question_id";
	public static final String PATH_FIELD = "path";
	public static final String FILENAME_FIELD = "filename";
	
	
	
	@DatabaseField(generatedId = true, columnName = ID_FIELD)
	private Integer recordingId;
	
	@DatabaseField(columnName = SPEAKER_FIELD, canBeNull = false, foreign = true, uniqueCombo = true)
	private Speaker speaker;
	
	@DatabaseField(columnName = QUESTION_FIELD, canBeNull = false, foreign = true, uniqueCombo = true)
	private Question question;
	
	@DatabaseField(columnName = PATH_FIELD)
	private String path;
	
	@DatabaseField(columnName = FILENAME_FIELD, canBeNull = false)
	private String filename;
	
	
	/**
	 * No-arg constructor
	 */
	public Recording() {
	}
	
	

	public Integer getRecordingId() {
		return recordingId;
	}



	public void setRecordingId(Integer recordingId) {
		this.recordingId = recordingId;
	}



	public Speaker getSpeaker() {
		return speaker;
	}

	public void setSpeaker(Speaker speaker) {
		this.speaker = speaker;
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @param text the text to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}


	/**
	 * @return the text
	 */
	public String getFilename() {
		return filename;
	}
	
	public boolean withRecording(){
		return (filename.equalsIgnoreCase(Globals.NO_RECORDING));
	}

}
