package com.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.WorkbookUtil;
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
	
	private static DbManager db;
	
	public ExcelHandler(String filename) throws Exception{
		//file = new FileInputStream(new File(filename));
        this.filename = filename;
        //Create Workbook instance holding reference to .xlsx file
        workbook = new XSSFWorkbook();
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
		for(int i=0; i< data.length; i++){
			Cell cell = row.createCell(i);
			
			if(data[i]!= null && data[i].matches("[-+]?\\d+(\\.\\d+)?")) cell.setCellValue(Double.parseDouble(data[i]));
			else cell.setCellValue(data[i]);
		}
		currRow++;
	}
	
	public void autoColumnWidth(){
		for (int i = 0; i < maxColumn; i++) {
			currSheet.autoSizeColumn(i);
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
