package com.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.db.Question;
import com.db.Speaker;

public class CSVHelper {

	public static void main (String args[]){
		File x = new File(".//lib//PA_questions.csv");
		List<Question> speakerList = importToList(x, Question.class);
		for (Iterator iterator = speakerList.iterator(); iterator.hasNext();) {
			Question speaker = (Question) iterator.next();
			System.out.println(speaker.getQuestionId()+"|"+speaker.getText());
			
		}
	}
	
	public static <T> List<T> importToList(File csvFile, Class<T> class1) {
		
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",(?=([^\"]|\"[^\"]*\")*$)";
		String label[] = null;
		List<T> instance = new ArrayList<T>();
		try {
			HashMap<String, String> map = new HashMap<String, String>();
			br = new BufferedReader(new FileReader(csvFile));

			while ((line = br.readLine()) != null) {
				if(label == null){
					label = line.split(cvsSplitBy);
					for (int i = 0; i < label.length; i++) {
						label[i] = cleanText(label[i]);
					}
				}
				else{
					String[] property = line.split(cvsSplitBy);
					for (int i = 0; i < property.length; i++) {
						if(label[i].equalsIgnoreCase("text")) map.put(label[i], property[i].replaceAll("^\"|\"$", "").replaceAll("\"\"", "\"").trim());
						else map.put(label[i], cleanText(property[i]));
					}
					instance.add(class1.getConstructor(HashMap.class).newInstance(map));
				}
			}
		
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
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
		return instance;
	}
	
	static String cleanText(String s) {
		s = s.replace(". ", " ").replace(".'", "'");
		s = s.replace("<b>", "").replace("</b>", "").replace("<u>", "")
				.replace("</u>", "").replace("?", "")
				.replace(",", "").replace("!", "").replace(";", "")
				.replaceAll("\\.$", "").replaceAll("\\.{2,}", "")
				.replace(".\"", "\"").replace("8:00", "eight o'clock")
				.replace("24/7", "TWENTY FOUR SEVEN")
				.replace("9.95", "NINE-POINT-NINETY-FIVE").replace(":", "")
				.replace("\"", "").replace(" '", " ").replace(" - ", " ")
				.replace("' ", " ").replace("<c1>", "").replace("</c1>", "")
				.replace("<c2>", "").replace("</c2>", "").replace("<c3>", "")
				.replace("</c3>", "").replace("<c4>", "").replace("</c4>", "")
				.replace("<BR>", "").replace("<br>", "").replace("<br/>", "")
				.replace("<BR/>", "").replace("�", "").replace("�", "")
				.replace("�", "").replaceAll("  *", " ").replaceAll("^'", "")
				.replaceAll("'$", "").replace("U.S", "US");
		s = s.replace("._", "_").replace(". ", " ").replace(".$", "");
		return s;
	}
}
