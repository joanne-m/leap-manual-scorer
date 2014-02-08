package com.db;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Speakers model class
 * @version 1.0
 */
@DatabaseTable(tableName = "speakers")
public class Speaker {
	
	// for QueryBuilder to be able to find the fields
	public static final String SESSION_ID = "SessionID";
	public static final String ID_FIELD = "SpeakerID";
	public static final String NAME_FIELD = "SpeakerName";
	public static final String AGE_FIELD = "SpeakerAge";
	public static final String GENDER_FIELD = "SpeakerGender";
	public static final String DIALECT_FIELD = "SpekaerDialect";
	public static final String MOTHER_DIALECT_FIELD = "MotherDialect";
	public static final String FATHER_DIALECT_FIELD = "FatherDialect";
	public static final String PROFESSION_FIELD = "SpeakerProfession";
	public static final String COMMENTS_FIELD = "SpeakerComments";
	
	@DatabaseField(id = true, columnName = ID_FIELD)
	private String speakerId;
	
	@DatabaseField(columnName = NAME_FIELD, canBeNull = false)
	private String name;
	
	@DatabaseField(columnName = AGE_FIELD, canBeNull = false)
	private int age;
	
	@DatabaseField(columnName = GENDER_FIELD, canBeNull = false)
	private String gender;
	
	@DatabaseField(columnName = DIALECT_FIELD, canBeNull = false)
	private String dialect;
	
	@DatabaseField(columnName = MOTHER_DIALECT_FIELD)
	private String motherDialect;
	
	@DatabaseField(columnName = FATHER_DIALECT_FIELD)
	private String fatherDialect;
	
	@DatabaseField(columnName = PROFESSION_FIELD)
	private String profession;
	
	@DatabaseField(columnName = COMMENTS_FIELD)
	private String comment;
	
	/**
	 * No-arg constructor
	 */
	public Speaker() {
	}
	

	public Speaker(HashMap<String, String> speaker) {
		// TODO Auto-generated constructor stub
		this.speakerId = (speaker.get(SESSION_ID)+"."+speaker.get(ID_FIELD));
		this.name = speaker.get(NAME_FIELD);
		this.age = Integer.parseInt(speaker.get(AGE_FIELD));
		this.gender = speaker.get(GENDER_FIELD);
		this.dialect = speaker.get(DIALECT_FIELD);
		this.motherDialect = speaker.get(MOTHER_DIALECT_FIELD);
		this.fatherDialect = speaker.get(FATHER_DIALECT_FIELD);
		this.profession = speaker.get(PROFESSION_FIELD);
		this.comment = speaker.get(COMMENTS_FIELD);
		
	}
	
	public String[][] mapSpeakerInfo(){
		String[][] speakerInfo = {
				{ID_FIELD, this.speakerId},
				{NAME_FIELD, this.name},
				{AGE_FIELD, this.age+""},
				{GENDER_FIELD, this.gender},
				{DIALECT_FIELD, this.dialect},
				{MOTHER_DIALECT_FIELD, this.motherDialect},
				{FATHER_DIALECT_FIELD, this.fatherDialect},
				{PROFESSION_FIELD, this.profession},
				{COMMENTS_FIELD, this.comment}
		};
		return speakerInfo;
	}

	public String getSpeakerId() {
		return speakerId;
	}


	public void setSpeakerId(String speakerId) {
		this.speakerId = speakerId;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public int getAge() {
		return age;
	}


	public void setAge(int age) {
		this.age = age;
	}


	public String getGender() {
		return gender;
	}


	public void setGender(String gender) {
		this.gender = gender;
	}


	public String getDialect() {
		return dialect;
	}


	public void setDialect(String dialect) {
		this.dialect = dialect;
	}


	public String getMotherDialect() {
		return motherDialect;
	}


	public void setMotherDialect(String motherDialect) {
		this.motherDialect = motherDialect;
	}


	public String getFatherDialect() {
		return fatherDialect;
	}


	public void setFatherDialect(String fatherDialect) {
		this.fatherDialect = fatherDialect;
	}


	public String getProfession() {
		return profession;
	}


	public void setProfession(String profession) {
		this.profession = profession;
	}


	public String getComment() {
		return comment;
	}


	public void setComment(String comment) {
		this.comment = comment;
	}

	public int hashCode() {
		return speakerId.hashCode();
	}
	
	public boolean equals(Object o) {
		if (o == null || o.getClass() != getClass()) {
			return false;
		}
		return speakerId.equals(((Speaker) o).getSpeakerId());
	}
	

}