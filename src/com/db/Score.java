package com.db;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.util.Globals;

/**
 * Scores model class
 * @version 1.0
 */
@DatabaseTable(tableName = "scores")
public class Score {
	
	// for QueryBuilder to be able to find the fields
	public static final String ID_FIELD = "score_id";
	public static final String RECORDING_FIELD = "recording_id";
	public static final String USER_FIELD = "user_id";
	public static final String SCORE_FIELD1 = "score_pa";
	public static final String SCORE_FIELD2 = "score_bf";
	public static final String SCORE_FIELD3 = "score_un";
	
	@DatabaseField(generatedId = true, columnName = ID_FIELD)
	private Integer scoreId;
	
	@DatabaseField(columnName = RECORDING_FIELD, canBeNull = false, foreign = true)
	private Recording recording;
	
	@DatabaseField(columnName = USER_FIELD, canBeNull = false, foreign = true)
	private User user;
	
	@DatabaseField(columnName = SCORE_FIELD1, dataType = DataType.FLOAT)
	private float score_pron;
	
	@DatabaseField(columnName = SCORE_FIELD2, dataType = DataType.FLOAT)
	private float score_blen;
	
	@DatabaseField(columnName = SCORE_FIELD3, dataType = DataType.FLOAT)
	private float score_und;
	
	
	/**
	 * No-arg constructor
	 */
	public Score() {
	}


	public Integer getScoreId() {
		return scoreId;
	}


	public void setScoreId(Integer scoreId) {
		this.scoreId = scoreId;
	}


	

	public Recording getRecording() {
		return recording;
	}


	public void setRecording(Recording recording) {
		this.recording = recording;
	}


	public User getUser() {
		return user;
	}


	public void setUser(User user) {
		this.user = user;
	}


	public double getScore_pron() {
		return score_pron;
	}


	public void setScore_pron(float score_pron) {
		this.score_pron = score_pron;
	}


	public float getScore_blen() {
		return score_blen;
	}


	public void setScore_blen(float score_blen) {
		this.score_blen = score_blen;
	}


	public float getScore_und() {
		return score_und;
	}


	public void setScore_und(float score_und) {
		this.score_und = score_und;
	}


	/*public void setScores(String[] scores) {
		// TODO Auto-generated method stub
		setScore_pron(Float.parseFloat(scores[0]));
		setScore_blen(Float.parseFloat(scores[1]));
		setScore_und(Float.parseFloat(scores[2]));
	}*/
	
	public void setScores(HashMap<String, Float> scores){
		setScore_pron(scores.get(Globals.PA));
		setScore_blen(scores.get(Globals.BF));
		setScore_und(scores.get(Globals.UN));
	}

	public HashMap<String, Float> getScores(){
		HashMap<String, Float> scores = new HashMap<>();
		scores.put(Globals.PA, score_pron);
		scores.put(Globals.BF, score_blen);
		scores.put(Globals.UN, score_und);
		return scores;
	}
	

}
