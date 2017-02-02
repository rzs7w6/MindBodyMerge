/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mindbodymerge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 *
 * @author rzs7w6
 */
public class Parser {
    private File sales;
    private File membership;
    private String semesterCode;
    private ArrayList<String> conflictList = new ArrayList<>();
    
    public Parser(File sales, File membership, String semesterCode) {
        this.sales = sales;
        this.membership = membership;
        this.semesterCode = semesterCode;
    }
    
    public ArrayList<String> handleParsing() {
        ArrayList<String> memberList = new ArrayList<>();
        ArrayList<String> myzouItemList = new ArrayList<>();
        ArrayList<String> itemPriceList = new ArrayList<>();
        ArrayList<String> membershipDictionary = new ArrayList<>();
        ArrayList<String> compiledList = new ArrayList<>();
        
        parseSales(memberList, myzouItemList, itemPriceList);
        parseMembership(membershipDictionary);
        parseMemberList(memberList, membershipDictionary);
        parsePriceList(itemPriceList);
        
        compileStrings(compiledList, memberList, myzouItemList, itemPriceList);
        
        return compiledList;
    }
    
    private void parseSales(ArrayList<String> memberList, ArrayList<String> myzouItemList, ArrayList<String> itemPriceList) {
      
        try {
            int iterationCount = 0;
            Double itemNumber;
            String itemString;
            String mocode;
            
            FileInputStream fis = new FileInputStream(sales);
            Database db = new Database();

            XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);

            Iterator<Row> rowIterator = mySheet.iterator();
            rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();

                iterationCount = 0;

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next(); 

                    if (iterationCount == 1) {
                        memberList.add(cell.getStringCellValue().replaceAll("\\s", ""));
                    }
                    else if (iterationCount == 3) {
                        mocode = db.getMocode(cell.getStringCellValue());
                        myzouItemList.add(mocode);
                    }
                    else if (iterationCount == 11) {
                        itemNumber = cell.getNumericCellValue();
                        itemString = String.format("%.0f", itemNumber);
                        itemPriceList.add(itemString);
                    }

                    iterationCount++;
                }
            }

//            System.out.println(memberList.size());
//            System.out.println(myzouItemList.size());
//            System.out.println(itemPriceList.size());
//            
//            System.out.println(memberList);
//            System.out.println(myzouItemList);
//            System.out.println(itemPriceList);
        } 
        catch(IOException e) {
            
        }
    }
    
    private void parseMembership(ArrayList<String> membershipDictionary){
        try {
            int iterationCount;
            Double studentNumber;
            String studentString;
            
            FileInputStream fis = new FileInputStream(membership);

            XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);

            Iterator<Row> rowIterator = mySheet.iterator();
            rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();

                iterationCount = 0;

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next(); 

                    if (iterationCount == 0) {
                            if (cell.getCellType() == 0) {
                                studentNumber = cell.getNumericCellValue();
                                studentString = String.format("%.0f", studentNumber);
                                membershipDictionary.add(studentString);
                            }
                            else 
                                iterationCount = 2;
                    }
                    else if (iterationCount == 1) {
                        membershipDictionary.add(cell.getStringCellValue().replaceAll("\u00a0", "").replaceAll(" ", ""));
                    }

                    iterationCount++;
                }
            }

//            System.out.println(membershipDictionary.size());
//            System.out.println(membershipDictionary);
        } 
        catch(IOException e) {
            
        }
    }
    
    private void parseMemberList(ArrayList<String> memberList, ArrayList<String> membershipDictionary) {
        /*
        TODO : Works perfectly, except that some of the people aren't on the membership file so we don't have their student number lol
        */
        int i; 
        int index;

        for (i = 0; i < memberList.size(); i++) {            
            index = membershipDictionary.indexOf(memberList.get(i));
            if(index == -1) {  
                //System.out.println(memberList.get(i));                   
            }   
            else {
                if (membershipDictionary.get(index - 1).length() == 8) {
                    memberList.set(i, membershipDictionary.get(index - 1));
                }
            }
        }       
//        System.out.println(memberList);
    }
    
    private void parsePriceList(ArrayList<String> itemPriceList) {
        /*
        TODO: Needs to look like <000015.00> or <00150.00> or <-00015.00> you get the picture pls
            So far it works, handles credit '-' signs like a champ wtf didn't even have to do anything special
        */
        int i;
        int j;
        String item = "";
        int length;
        int zeroLength = 6;
        
        for (i = 0; i < itemPriceList.size(); i++) {
            length = itemPriceList.get(i).length();
            for (j = 0; j < zeroLength - length; j++) {
                item += "0";
            }
            item = item.concat(itemPriceList.get(i));
            item = item.concat(".00");
            itemPriceList.set(i, item);
            item = "";
        }
        //System.out.println(itemPriceList);
    }
    
    private void compileStrings(ArrayList<String> compiledList, ArrayList<String> memberList, ArrayList<String> myzouItemList, ArrayList<String> itemPriceList) {
        int i;
        String completedString;
        Double studentNumber;
        
        for (i = 0; i < memberList.size(); i++) {
            if (isNumeric(memberList.get(i)) && !myzouItemList.get(i).contains("NOTFOUND")) {
                completedString = "0" + memberList.get(i) + myzouItemList.get(i) + itemPriceList.get(i) + semesterCode;
                //System.out.println(myzouItemList.get(i) + " - " + i);
                compiledList.add(completedString);
            }
            else {
                completedString = "0" + memberList.get(i) + myzouItemList.get(i) + itemPriceList.get(i) + semesterCode;
                conflictList.add(completedString);
                //System.out.println(conflictList);
            }   
        }
    }
    
    public ArrayList<String> getConflictList() {
        return conflictList;
    }
    
    public void saveFile(ArrayList<String> compiledList, Stage stage, String title) {
        FileChooser fileChooser = new FileChooser();
  
        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle(title);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(stage);

        if(file != null){
            export(compiledList, file);
        }
    }
    
    public void export(ArrayList<String> compiledList, File file) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            for(String str: compiledList) {
                writer.write(str);
                writer.write("\r\n");
            }   writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static boolean isNumeric(String str)  
    {  
      try  
      {  
        double d = Double.parseDouble(str);  
      }  
      catch(NumberFormatException nfe)  
      {  
        return false;  
      }  
      return true;  
    }
}
