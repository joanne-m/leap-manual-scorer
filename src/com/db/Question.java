package com.db;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Question model class
 * @version 1.0
 */
@DatabaseTable(tableName = "questions")
public class Question {
	
	// for QueryBuilder to be able to find the fields
	public static final String ID_FIELD = "question_id";
	public static final String TEXT_FIELD = "text";
	
	@DatabaseField(generatedId = true, columnName = ID_FIELD)
	private Integer questionId;
	
	@DatabaseField(columnName = TEXT_FIELD, canBeNull = false, unique = true)
	private String text;
	
	
	/**
	 * No-arg constructor
	 */
	public Question() {
	}
	

	public Question(String question) {
		// TODO Auto-generated constructor stub
		this.text = question;
	}


	/**
	 * @param questionId the questionId to set
	 */
	public void setQuestionId(Integer questionId) {
		this.questionId = questionId;
	}

	/**
	 * @return the questionId
	 */
	public Integer getQuestionId() {
		return questionId;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}


	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	public int hashCode() {
		return questionId.hashCode();
	}
	
	public boolean equals(Object o) {
		if (o == null || o.getClass() != getClass()) {
			return false;
		}
		return text.equals(((Question) o).getText());
	}
	

}
