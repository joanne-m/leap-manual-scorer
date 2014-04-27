package com.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.db.DbManager;

public class ExcelHandler {
	//private FileInputStream file;
	private String filename;
	private XSSFWorkbook workbook;
	private XSSFSheet currSheet;
	private int currRow;
	private int maxColumn;
	private XSSFCellStyle headerStyle;
	private XSSFCellStyle noRecStyle;
	
	private static DbManager db;
	
	public ExcelHandler(String filename) throws Exception{
		//file = new FileInputStream(new File(filename));
        this.filename = filename;
        //Create Workbook instance holding reference to .xlsx file
        workbook = new XSSFWorkbook();
        
        XSSFFont headerFont= workbook.createFont();
	    headerFont.setFontHeightInPoints((short)10);
	    headerFont.setFontName("Arial");
	    headerFont.setColor(IndexedColors.WHITE.getIndex());
	    headerFont.setBold(true);
	    headerFont.setItalic(false);
	    
	    headerStyle = workbook.createCellStyle();
	    headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
	    headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
	    headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
	    headerStyle.setFont(headerFont);
	    
	    XSSFFont noRecFont= workbook.createFont();
	    noRecFont.setFontHeightInPoints((short)10);
	    noRecFont.setFontName("Arial");
	    noRecFont.setColor(IndexedColors.RED.getIndex());
	    noRecFont.setBold(true);
	    noRecFont.setItalic(false);
	    
	    noRecStyle = workbook.createCellStyle();
	    noRecStyle.setFont(noRecFont);
	    
	}
	
	public void newSheet(String sheetname){
		sheetname = WorkbookUtil.createSafeSheetName(sheetname);
		int i = 0;
		while (workbook.getSheet(sheetname) != null){
			sheetname += i;
			i++;
		}
		currSheet = workbook.createSheet(sheetname);
		currRow = 0;
	}
	
	
	public void setSheet(int sheetNum){
		workbook.setActiveSheet(sheetNum);
		currSheet = workbook.getSheetAt(sheetNum);
		//rowIterator = currSheet.iterator();
	}
	
	public void writeRow (String[] data){
		Row row = currSheet.createRow(currRow);
		maxColumn = Math.max(maxColumn, data.length);
		XSSFCellStyle currCellStyle= null;
		if (row.getRowNum() == 0) currCellStyle = headerStyle;
		else if(data[0] !=null && data[0].equals(Globals.NO_RECORDING)) currCellStyle = noRecStyle;
		for(int i=0; i< data.length; i++){
			Cell cell = row.createCell(i);
			
			if(data[i]!= null && data[i].matches("[-+]?\\d+(\\.\\d+)?")) cell.setCellValue(Double.parseDouble(data[i]));
			else cell.setCellValue(data[i]);
			
			cell.setCellStyle(currCellStyle);
			
		}
		currRow++;
	}
	
	public void setColumnWidth(int[] columnWidth){
		for (int i = 0; i < maxColumn; i++) {
			if(i<columnWidth.length) currSheet.setColumnWidth(i, columnWidth[i]*256);
			else currSheet.autoSizeColumn(i);
		}
	}
	
	public void autoColumnWidth(){
		for (int i = 0; i < maxColumn; i++) {
			currSheet.autoSizeColumn(i);
		}
	}
	
	public void freezeHeaderRow(){
		currSheet.createFreezePane( 0, 1, 0, 1 );
	}
	
	public static void parseResults(String filename, String username) throws IOException, SQLException{
		FileInputStream fis = new FileInputStream(filename);
		XSSFWorkbook w = new XSSFWorkbook(fis);
		DbManager db = new DbManager();
		db.setCurrentUser(username, Globals.CREATEIFNOTEXISTING);
		for(int i=0; i<w.getNumberOfSheets(); i++){
			XSSFSheet currSheet = w.getSheetAt(i);
			System.out.println(i+currSheet.getSheetName());
			int rowNum = 2;
			XSSFRow currRow;
			String wavFilename;
			while((currRow = currSheet.getRow(rowNum)) != null){
				wavFilename = currRow.getCell(0).getStringCellValue();
				double score_pa = (currRow.getCell(4).getNumericCellValue())/2;
				double score_bf = (currRow.getCell(5).getNumericCellValue())/2;
				System.out.println(wavFilename+": "+score_pa +" "+score_bf);
				db.importScores(wavFilename, score_pa, score_bf);
				rowNum++;
			}
		}
	}
	
	public static void main(String args[]){
		try {
			parseResults("test.xlsx", "alainandrew");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*public RowClass getNextRow(){
		if (rowIterator.hasNext()){
			Row row = rowIterator.next();
			Iterator<Cell> cellIterator = row.cellIterator();
			Object[] array = new Object[5];
			int x=0;
			while (cellIterator.hasNext() && x < 5) 
            {
				Cell cell = cellIterator.next();
                						
				//Check the cell type and format accordingly
                switch (cell.getCellType()) 
                {
                    case Cell.CELL_TYPE_NUMERIC:
                        array [x] = (int)(cell.getNumericCellValue());
                        break;
                    case Cell.CELL_TYPE_STRING:
                    	try{
                    		array[x] = cell.getHyperlink().getAddress();
            			}
                    	catch(NullPointerException e){
                    		array[x] = cell.getStringCellValue();
                    	}
                        break;
                }
                
                
                x++;
            }
			RowClass currentRow = new RowClass(array);
			return currentRow;
		}
		else return null;
	}*/
	
	public void closeFile() throws IOException{
		FileOutputStream fileOut = new FileOutputStream(filename+".xlsx");
	    workbook.write(fileOut);
	    fileOut.close();
	    JOptionPane.showMessageDialog(null,"Scores successfully exported to \""+filename+".xlsx\".");
	}
}
