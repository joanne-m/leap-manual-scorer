package com.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Speakers model class
 * @version 1.0
 */
@DatabaseTable(tableName = "users")
public class User {

	public static final String ID_FIELD = "user_id";
	public static final String NAME_FIELD = "username";
	
	@DatabaseField(generatedId = true, columnName = ID_FIELD)
	private Integer userId;
	
	@DatabaseField(columnName = NAME_FIELD, canBeNull = false)
	private String name;

	public User(String username) {
		// TODO Auto-generated constructor stub
		this.name = username;
	}

	public User() {
		// TODO Auto-generated constructor stub
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
