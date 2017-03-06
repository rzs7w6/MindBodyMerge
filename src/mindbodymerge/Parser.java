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
 * Takes in Sales and Membership data excel sheets, and a semester code, and creates array lists of the data formatted in the correct format
 * The format is 0<student number><myzou item type><variable number of 0's and a '-' sign if it's a credit><purchase amount><semester code>
 * Example: 018145185042200008000000019.00N4527
 * Example with a credit charge: 018142994042200009505-00015.00N4527
 * Example with a triple digit charge: 014235185042200004000000299.00N4527
 *
 * @author rzs7w6
 */
public class Parser {
    private File sales;
    private File membership;
    private String semesterCode;
    private ArrayList<String> conflictList = new ArrayList<>();
    
    /**
     * Parser object for compiling sales and membership data in the correct format.
     * 
     * @param sales
     * @param membership
     * @param semesterCode 
     */
    public Parser(File sales, File membership, String semesterCode) {
        this.sales = sales;
        this.membership = membership;
        this.semesterCode = semesterCode;
    }
    
    /**
     * Main method to handle all of the parsing that needs to be done.
     * None of this should probably have to change in case of changes in the format of the excel sheets or output text file.
     * 
     * @return Returns an Array List containing a compiled list of all data that was present.  Doesn't include items that did not 
        have a student number to match the student name in the membership file
    */
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
    
    /**
     * Parses the Sales data excel sheet that was passed in, and creates Array List populated with the member's names (memberList), the items the members
     * purchase (myzouItemList) and the price of the items that they paid (itemPriceList)
     * All array lists are indexed to the same person (i.e. memberList.get(0) bought myzouItemList.get(0) and paid itemPriceList.get(0))
     * Parsing of the excel files are hard coded to the columns that held the correct data fields, and will need to be changed if the format of the excel 
     * file changes
     *
     * @param memberList: list of members who purchased items
     * @param myzouItemList: list of what the members purchased
     * @param itemPriceList: list of how much the member paid
     * 
     * @return void
     *
    */
    private void parseSales(ArrayList<String> memberList, ArrayList<String> myzouItemList, ArrayList<String> itemPriceList) {
      
        try {
            //Variable to be used in determining which columns of the excel file should be read from used in the while loop
            int iterationCount = 0;
            
            //Used for parsing of the item price into an item string
            Double itemNumber;
            String itemString;
            
            //Holds mocode
            String mocode;
            
            //Uses Apache POI .XLSX file parser to get the data, as treating it like a normal .CSV will give weird data
            //sales is the sales data.xlsx file
            FileInputStream fis = new FileInputStream(sales);
            
            Database db = new Database();

            //Open WorkBook (.XLSX file)
            XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);
            //Open first sheet of workbook, shouldn't have to change unless the formatting of the sales data.xlsx file changes to a different sheet
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);

            //Iterator for moving through each row of the sheet
            Iterator<Row> rowIterator = mySheet.iterator();
            
            //This first call of the iterator is to move the iterator past the labels at the top of the columns in the excel sheet
            rowIterator.next();

            //Move through each row of the excel file
            while (rowIterator.hasNext()) {
                //Move to next row
                Row row = rowIterator.next();
                //Iterator for the cells in the row
                Iterator<Cell> cellIterator = row.cellIterator();

                //Reset the iterationCount to 0 so the while loop below knows what column we are in
                iterationCount = 0;

                while (cellIterator.hasNext()) {
                    //Move to the next cell
                    Cell cell = cellIterator.next(); 

                    //The second column (column B) holds the list of member names, so we read from that column to the memberList
                    if (iterationCount == 1) {
                        //Get rid of all the spaces so matching is easier
                        memberList.add(cell.getStringCellValue().replaceAll("\\s", ""));
                    }
                    //The fourth column (column D) holds the list of purchased items, so we read from the column to the memberList
                    else if (iterationCount == 3) {
                        mocode = db.getMocode(cell.getStringCellValue());
                        myzouItemList.add(mocode);
                    }
                    //The 17th column (column Q) holds the list of amount paid for the items with tax
                    //Make sure that you choose the column that holds the actual amount paid (e.g. the row with negative numbers showing credit charges and tax)
                    //number is taken in as a double, and formatted as a string to be added
                    else if (iterationCount == 16) {
                        itemNumber = cell.getNumericCellValue();
                        itemString = String.format("%.2f", itemNumber);
                        itemPriceList.add(itemString);
                    }

                    //Move counter to next cell
                    iterationCount++;
                }
            }
            //Test block for ensuring the lists are correct, the sizes should all be equal
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
    
    /**
     * Parses the membership.XLSX file to make a dictionary of student names and student numbers
     * Format is membershipDictionary.get(i) is the student number and membershipDictionary.get(i+1) is the student name for that student number
     * This is only because in the excel file the student number comes first, if this changes you will have to change the logic in the parseMemberList method
     * Almost the exact same logic as parseSales method, so look at that if this is confusing
     *
     * @param membershipDictionary: dictionary of student numbers/names where the student number membershipDictionary.get(i) correlates to the student name membershipDictionary.get(i+1)
    */
    private void parseMembership(ArrayList<String> membershipDictionary){
        //IOException
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

                    //This is the only part in which this is different from parseSales method
                    //In the current format of the excel sheet, there are some rows in the middle of the sheet which contains column headers, not the data we want
                        //In order to get rid of this data, we check if the data in column A is a number,
                        //If it isn't, it's column header data and we skip over it by setting the iterationCount to 2 (which is past any data we need)
                    if (iterationCount == 0) {
                        //Check if the cellType is numeric by comparing it to 0 because Cell.CELL_TYPE_NUMERIC == 0)
                        if (cell.getCellType() == 0) {
                            studentNumber = cell.getNumericCellValue();
                            studentString = String.format("%.0f", studentNumber);
                            membershipDictionary.add(studentString);
                        }
                        else 
                            iterationCount = 2;
                    }
                    //Get the student name that correlates to the student number we just parsed
                    else if (iterationCount == 1) {
                        //Have to replace some super weird unicode garbage from the data
                        membershipDictionary.add(cell.getStringCellValue().replaceAll("\u00a0", "").replaceAll(" ", ""));
                    }

                    iterationCount++;
                }
            }
            //Testing block, should have a student number followed by a student name for each person in the excel sheet
//            System.out.println(membershipDictionary.size());
//            System.out.println(membershipDictionary);
        } 
        catch(IOException e) {
            
        }
    }
    
    /**
     * Parse the memberList array list and replace the names in it with the correct student numbers from the membershipDictionary
     * Idea is to match the name in memberList.get(i) to something in membershipDictionary and replace memberList.get(i) with membershipDictionary.get(matchingIndex - 1)
     * If it doesn't exist in membershipDictionary, that means the membership excel sheet didn't have that person in it
     * We resolved this by outputting a list which has to be manually corrected, but that's handled in compileStrings
     * 
     * @param memberList: list of member names to be replaced with student numbers
     * @param membershipDictionary: mostly complete dictionary of student names and numbers where membershipDictionary.get(i) is the student number and membershipDictionary.get(i+1) is the student name for that student number
     * 
    */
    private void parseMemberList(ArrayList<String> memberList, ArrayList<String> membershipDictionary) {
        /*
        TODO : Works perfectly, except that some of the people aren't on the membership file so we don't have their student number lol
        RESOLVED-ish : We output a final list which has to be manually corrected
        */
        int i; 
        int index;

        //Loop through the memberList and replace with student numbers
        for (i = 0; i < memberList.size(); i++) {            
            index = membershipDictionary.indexOf(memberList.get(i));
            if(index == -1) {  
                //This will give you the names of people that weren't on the excel list if you need that for something
                //System.out.println(memberList.get(i));                   
            }   
            else {
                if (membershipDictionary.get(index - 1).length() == 8) {
                    memberList.set(i, membershipDictionary.get(index - 1));
                }
            }
        }       
        //Test block
//        System.out.println(memberList);
    }
    
    /**
     * Put the prices into the correct format which is <xxxxxx.xx>
     * Going from right to left, the first digits are the price so if they paid 15.00 it will be <xxxx15.00>
     * After that, if it is a credit charge like -15.00 then handle it so that it will look like <-xxx15.00>.
     * The rest of the x are replaced by 0's so <000015.00> or <-00015.00>
     * 
     * @param itemPriceList : list of prices to be correctly formatted
     */
    private void parsePriceList(ArrayList<String> itemPriceList) {
        /*
        TODO: Needs to look like <000015.00> or <00150.00> or <-00015.00> you get the picture pls
            So far it works, handles credit '-' signs like a champ wtf didn't even have to do anything special
        */
        int i;
        int j;
        
        //Blank item price string that can be filled in with correctly formatted data
        String item = "";
        
        //Starting length of the raw string
        int length;
        
        //number of digits left of the decimal place i.e. xxxxxx.00 the x's
        int zeroLength = 9;
        
        //Loop through
        for (i = 0; i < itemPriceList.size(); i++) {
            //Get length of raw string
            length = itemPriceList.get(i).length();
            //Add 0's to fill in x's left i.e. xxxx15.00 the x's here
            for (j = 0; j < zeroLength - length; j++) {
                item += "0";
            }
            //Check for a credit charge.  If it is, add the '-' in place of the first 0 in String item, and replace the '-' in the list with '0'
            if (itemPriceList.get(i).charAt(0) == '-') {
                itemPriceList.set(i, itemPriceList.get(i).replace('-', '0'));
                item = item.replaceFirst("0", "-");
            }
            
            //Add the item price so the string looks like 000015 or 000-15 or 000150 depending on charge
            item = item.concat(itemPriceList.get(i));
            
            //Replace the item with the correctly formatted string
            itemPriceList.set(i, item);
            
            //Reset the item string to be done again
            item = "";
        }
        //Test block
        System.out.println(itemPriceList);
    }
    
    /**
     * Compiles the completed array lists into the correct format to be outputted
     * If a row gets here and has a student name instead of a student number, that is added to a different array list conflictList to be handled later
     * The format is 0<student number><myzou item type><variable number of 0's and a '-' sign if it's a credit><purchase amount><semester code>
     * 
     * @param compiledList
     * @param memberList
     * @param myzouItemList
     * @param itemPriceList 
     */
    private void compileStrings(ArrayList<String> compiledList, ArrayList<String> memberList, ArrayList<String> myzouItemList, ArrayList<String> itemPriceList) {
        int i;
        String completedString;
        Double studentNumber;
        
        //Loop through
        for (i = 0; i < memberList.size(); i++) {
            //Here is where we check if the student name was not replaced by a student number
            //If it is correct, format it and add it to the compiledlist
            if (isNumeric(memberList.get(i)) && !myzouItemList.get(i).contains("NOTFOUND")) {
                completedString = "0" + memberList.get(i) + myzouItemList.get(i) + itemPriceList.get(i) + semesterCode;
                //System.out.println(myzouItemList.get(i) + " - " + i);
                compiledList.add(completedString);
            }
            //If it isn't correct, format it and add it to the conflictList
            else {
                completedString = "0" + memberList.get(i) + myzouItemList.get(i) + itemPriceList.get(i) + semesterCode;
                conflictList.add(completedString);
            }   
        }
    }
    
    /**
     * Getter for the conflict list from compileStrings
     * 
     * @return conflictList which holds the rows that didn't have a student number
     */
    public ArrayList<String> getConflictList() {
        return conflictList;
    }
    
    /**
     * Opens a save file dialog to save the output file
     * Calls export() with the compiledList and file name to write to the file
     * 
     * @param compiledList
     * @param stage
     * @param title 
     */
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
    
    /**
     * Simple output function to write the compiledList to a local @param file
     * 
     * @param compiledList
     * @param file 
     */
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
    
    /**
     * Function used in compile strings to determine if the student number was present
     * 
     * @param str Student number or name to check
     * @return True if numeric, false if stringy
     */
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
