package com.db;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;





import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.table.TableUtils;
import com.util.CSVHelper;
import com.util.Globals;

public class DbManager {
	public static JdbcConnectionSource connectionSource;
	public static Dao<User, Integer> userDao;
	public static Dao<Question, Integer> questionDao;
	public static Dao<Recording, Integer> recordingDao;
	public static Dao<Speaker, Integer> speakerDao;
	public static Dao<Score, Integer> scoreDao;
	
	
	static List<Recording> recordingList;
	static List<Score> scoreList;

	private User current_user;
	public int total;
	public int counter;
	
	public Recording current_recording;
	public Question current_question;
	public Score current_score;

	public DbManager() {
		setupDatabase();
		
	}

	/**
	 * Initialize database DAOs
	 */
	private static void setupDatabase() {
		try {
			System.out.println("Initiating database connection located at "+ Globals.DATABASE_URL);
			connectionSource = new JdbcConnectionSource(Globals.DATABASE_URL);

			System.out.println("Connected to database. Start creating DAO for database models");

			userDao = DaoManager.createDao(connectionSource,
					User.class);
			System.out.println("DAO for "+User.class+" created");
			
			questionDao = DaoManager.createDao(connectionSource,
					Question.class);
			System.out.println("DAO for "+Question.class+" created");
			
			
			speakerDao = DaoManager.createDao(connectionSource,
					Speaker.class);
			System.out.println("DAO for "+Speaker.class+" created");
			
			recordingDao = DaoManager.createDao(connectionSource,
					Recording.class);
			System.out.println("DAO for "+Recording.class+" created");
			
			
			scoreDao = DaoManager.createDao(connectionSource,
					Score.class);
			System.out.println("DAO for "+Score.class+" created");
			
			TableUtils.createTableIfNotExists(connectionSource, User.class);
			TableUtils.createTableIfNotExists(connectionSource, Speaker.class);
			TableUtils.createTableIfNotExists(connectionSource, Question.class);
			TableUtils.createTableIfNotExists(connectionSource, Recording.class);
			TableUtils.createTableIfNotExists(connectionSource, Score.class);
			
			System.out.println("Database setup complete");
		} catch (SQLException e) {
			System.out.println("Failed to complete database setup."+e);
			e.printStackTrace();
		} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("Failed to initialize database."+e);
		} finally {
			if (connectionSource != null) {
				try {
					connectionSource.close();
					System.out.println("Closed database connection");
				} catch (SQLException e) {
					System.out.println("Failed to close database connection"+ e);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		new DbManager();
		
	}
	
	public void setCurrentUser(String username, int mode){
		QueryBuilder<User, Integer> userQb = userDao.queryBuilder();
		SelectArg username_arg = new SelectArg(username.toLowerCase());
		List<User> userList;
		try {
			userList = userQb.selectColumns(User.ID_FIELD).where().eq(User.NAME_FIELD, username_arg).query();
		
			if (userList.isEmpty() && mode == Globals.CREATEIFNOTEXISTING) {
				User user = new User(username_arg.toString());
				current_user = userDao.createIfNotExists(user);
			}
			else if (userList.isEmpty() && mode == Globals.DISREGARDIFNOTEXISTING) {
				current_user = new User();
				current_user.setUserId(-1);
			}
			else current_user = userList.get(0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateDatabase(String path, String filename, String question, Speaker speaker) {
		// TODO Auto-generated method stub

		try{
			Question q = new Question(question);
			q = newQuestionEntry(q);
			
			QueryBuilder<Speaker, Integer> speakerQb = speakerDao.queryBuilder();
			List<Speaker> speakerList = speakerQb.where().eq(Speaker.ID_FIELD, speaker.getSpeakerId()).query();
			if(speakerList.isEmpty()) speakerDao.createOrUpdate(speaker);
			newRecordingEntry(path, filename, q, speaker);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	public void updateDatabase(List<Speaker> speakerList){
		try{
			for (Iterator iterator = speakerList.iterator(); iterator.hasNext();) {
				Speaker speaker = (Speaker) iterator.next();
				speakerDao.createOrUpdate(speaker);
			}
		}catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	public boolean updateDatabase(String path, String filename, int questionId, String speakerId) {
		// TODO Auto-generated method stub

		try{
			Question q = questionDao.queryForId(questionId);
			
			List<Speaker> speakerList = speakerDao.queryForEq(Speaker.ID_FIELD, speakerId);
			Speaker speaker = speakerList.get(0);
			if(q != null && speaker != null) {
				newRecordingEntry(path, filename, q, speaker);
				return true;
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return false;
	}
	
private void newRecordingEntry(String path, String filename, Question question, Speaker speaker) throws SQLException{
		// TODO Auto-generated method stub
		Recording r = new Recording();
		r.setPath(path);
		r.setFilename(filename);
		r.setQuestion(question);
		r.setSpeaker(speaker);
		
		QueryBuilder<Recording, Integer> recordingQb = recordingDao.queryBuilder();
		List<Recording> recordingList = recordingQb.where().eq(Recording.SPEAKER_FIELD, speaker).and().eq(Recording.QUESTION_FIELD, question).query();
		
		if (recordingList.isEmpty()) {
			recordingDao.createIfNotExists(r);
		}
		
	}

public void initQuestionsTable(){
	try{
		if (questionDao.countOf() == 0){				
			final List<Question> questionsToInsert = CSVHelper.importToList(new File(Globals.IMPORT_QUESTIONS_URL), Question.class);
			questionDao.callBatchTasks(new Callable<Void>() {
			    public Void call() throws Exception {
			        for (Question question : questionsToInsert) {
			        	questionDao.createOrUpdate(question);
			        }
			        return null;
			    }
			});
		}
	}catch(SQLException e){
		e.printStackTrace();
	}catch (Exception e) {
		e.printStackTrace();
	}
}

private Question newQuestionEntry(Question q) throws SQLException{
		
		List<Question> questionList = questionDao.queryForEq(Question.TEXT_FIELD, new SelectArg(q.getText()));
		
		if (questionList.isEmpty()) {
			Question newquestion =  questionDao.createIfNotExists(q);
			return newquestion;
		}
		else return questionList.get(0);
	}

private void initIterator() throws SQLException{
	
		QueryBuilder<Recording, Integer> recordingQb = recordingDao.queryBuilder();
		
		QueryBuilder<Score, Integer> scoreQb = scoreDao.queryBuilder();
		
		recordingQb.where().not().eq(Recording.FILENAME_FIELD, Globals.NO_RECORDING);
		
		recordingList = recordingQb.query();
		//System.out.println(recordingQb.prepareStatementString());
		

		//scoreQb.where().eq(Score.USER_FIELD, current_user).or().isNull(Score.USER_FIELD);
		scoreList = scoreDao.queryForEq(Score.USER_FIELD, current_user);
		counter = scoreList.size();
		/*where.and();
		where.notIn(Recording.ID_FIELD, scoreDao.queryBuilder().selectColumns(Score.RECORDING_FIELD).where().eq(Score.USER_FIELD, current_user).prepare());
		System.out.println(recordingQb.prepareStatementString());
		Recording firstNotScored = where.queryForFirst();
		System.out.println(firstNotScored.getFilename());*/
		//current_recording = recordingList.get(counter);
		//iterator.set(firstNotScored);
		total = recordingList.size();
		
	
	
}

private void updatePointers() throws SQLException{
	current_recording = recordingList.get(counter);
	current_question = questionDao.queryForId(current_recording.getQuestion().getQuestionId());
	scoreList =  scoreDao.queryBuilder().where().eq(Score.RECORDING_FIELD, current_recording).and().eq(Score.USER_FIELD, current_user).query();
	if(scoreList.isEmpty()){
		current_score = null;
	}
	else current_score = scoreList.get(0);
	//System.out.println(counter + ":"+ total);
}

private void resetPointers(){
	current_recording = null;
	current_question = null;
	current_score = null;
}

public void initScoringPanel(){
	try {
		initIterator();
		if (!recordingList.isEmpty() && counter < total) updatePointers();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}

public void initScoringPanel(int counter){
	try {
		initIterator();
		this.counter = counter;
		if (!recordingList.isEmpty() && counter < total) updatePointers();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}

public void getPrevToScore(){
	try {
		if(recordingList == null) initIterator();
		
		while(counter>0) {
			counter--;
			updatePointers();
			//update scores
			return;
		}
	}
	
	 catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	resetPointers();
	return;
	
}

public void getNextToScore(){
	try {
		if(recordingList == null) initIterator();
		
		while(counter<total-1 && counter>=0) {
			counter++;
			updatePointers();
			
			//System.out.println(total+current_recording.getFilename()+current_question.getText());
			return;
		}
	}
	/*
	
		if(iterator == null) {
			//iterator contains entries in recording table with no score yet

			QueryBuilder<Recording, Integer> recordingQb = recordingDao.queryBuilder();
			QueryBuilder<Score, Integer> scoreQb = scoreDao.queryBuilder();
			scoreQb.selectColumns(Score.RECORDING_FIELD).where().eq(Score.USER_FIELD, current_user);//.and().eq(Score.RECORDING_FIELD, rec)
			Where<Recording, Integer> where = recordingQb.where();
			where.and(where.not().in(Recording.ID_FIELD, scoreQb),where.not().eq(Recording.FILENAME_FIELD, Globals.NO_RECORDING));
					
			List <Recording> recordingList= recordingQb.query();
			total = recordingList.size();
			counter = 0;
			iterator = recordingList.listIterator();//use .listiterator for previous button
		}
		while(iterator.hasNext()) {

			current_recording = (Recording) iterator.next();
			current_question = questionDao.queryForId(current_recording.getQuestion().getQuestionId());
			counter++;
			
			//System.out.println(total+current_recording.getFilename()+current_question.getText());
			return;
		}
	}	*/
	 catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	resetPointers();
	return;
	
}

public void submitScore(HashMap<String, Float> scores) throws SQLException{
	// TODO Auto-generated method stub
	
		Score s = scoreDao.queryBuilder().where().eq(Score.USER_FIELD, current_user).and().eq(Score.RECORDING_FIELD, current_recording).queryForFirst();
		if(s==null){
			s = new Score();
			s.setRecording(current_recording);
			s.setScores(scores);
			s.setUser(current_user);
			scoreDao.create(s);
			//System.out.println("Created new score entry!!");
		}
		else{
			s.setScores(scores);
			scoreDao.update(s);
			//System.out.println("Updated existing score entry!!");
			
		}
	
}

public List<Speaker> getScoredSpeakers(String username) throws SQLException{
	List <Speaker> speakerList;
	setCurrentUser(username, Globals.DISREGARDIFNOTEXISTING);
	QueryBuilder<Speaker, Integer> speakerQb = speakerDao.queryBuilder();
	QueryBuilder<Recording, Integer> recordingQb = recordingDao.queryBuilder();
	QueryBuilder<Score, Integer> scoreQb = scoreDao.queryBuilder();
	speakerQb.distinct();
	scoreQb.where().eq(Score.USER_FIELD, current_user);
	recordingQb = recordingQb.join(scoreQb);
	speakerList = speakerQb.join(recordingQb).query();
	

	return speakerList;
}

public GenericRawResults<String[]> getScores(Speaker speaker) throws SQLException{
	GenericRawResults<String[]> rawResults =
			  scoreDao.queryRaw("select filename, text, score_pa, score_bf, score_un from questions join recordings using (question_id) left join scores using (recording_id) where speaker_id = '"+new SelectArg(speaker.getSpeakerId()) + "' and (user_id = "+current_user.getUserId()+" or (user_id is null and filename = 'NOT_RECORDED'))");
	return rawResults;
}

	public void importScores(String wavFilename, double score_pa, double score_bf) throws SQLException{
		if(!wavFilename.equalsIgnoreCase(Globals.NO_RECORDING)){
			List<Recording> rec = recordingDao.queryForEq(Recording.FILENAME_FIELD, wavFilename);
			
			if(!rec.isEmpty()){
				Score s = new Score();
				s.setRecording(rec.get(0));
				s.setUser(current_user);
				s.setScore_pron((float) score_pa);
				s.setScore_blen((float)score_bf);
				s.setScore_und(0);
				
				scoreDao.createOrUpdate(s);
			}
		}
	//if filename != NOT_RECORDED {select recording_id from recordings where filename = wavfilename
	//create score (recording_id, userid = 1, score_pa, score_bf, score_un = 0.0)
	}
}
