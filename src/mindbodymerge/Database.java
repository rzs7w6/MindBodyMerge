package mindbodymerge;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
/**
 *
 * @author Jonathon Lantsberger and Robert Spalding
 */
public class Database {
    private final String url = Credentials.url;
    private final String dbName = Credentials.dbName;
    private final String driver = Credentials.driver;
    private final String userName = Credentials.userName;
    private final String password = Credentials.password;
    
    public ObservableList getInfo() {
        ObservableList<String> items = FXCollections.observableArrayList();
        try{
            Class.forName(driver).newInstance();
            Connection conn = DriverManager.getConnection(url+dbName,userName,password);
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM items_list;");
            
            while(rs.next()){
                items.add(rs.getString("mocode") +  " - " + rs.getString("description"));
            }
            conn.close();
        }catch(ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e){
            e.printStackTrace();
        }
        return items;
    }
    public void deleteEntry(String mocode) {
        try{
            Class.forName(driver).newInstance();
            Connection conn = DriverManager.getConnection(url+dbName,userName,password);
            System.out.println(mocode);
            Statement stmt = conn.createStatement();
            stmt.execute("DELETE FROM items_list WHERE mocode='" + mocode + "';");
            conn.close();
        }catch(ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e){
            e.printStackTrace();
        }
    }
    
    public void addEntry(String mocode, String description) {
        try{
            Class.forName(driver).newInstance();
            Connection conn = DriverManager.getConnection(url+dbName,userName,password);
            
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("INSERT INTO items_list VALUES ('"+ mocode +"', '" + description + "');");
            
            conn.close();
        }catch(ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e){
            e.printStackTrace();
        }
    }
    
    public boolean addEntry(int id, String description, String alias) { //overloaded method to add an alias to a mocode
        String insertStmt = "INSERT INTO mocode_alias (mocode, alias) VALUES ((SELECT mocode FROM items_list WHERE description = ?), ?);";
        PreparedStatement insert = null;
        try{
            Class.forName(driver).newInstance();
            Connection conn = DriverManager.getConnection(url+dbName,userName,password);
            insert = conn.prepareStatement(insertStmt);
            
            insert.setString(1, description);
            insert.setString(2, alias);
            
            boolean checkReturn = insert.execute();
            
            conn.close();
            return checkReturn;
        }catch(ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e){
            e.printStackTrace();
        }
        
        return false;
    }
    
    public String getMocode(String description) {        
        String mocode = "";
        String selectStmt = "SELECT * FROM items_list WHERE description = ?";
        PreparedStatement mocodeSelect = null;
        try{
            Class.forName(driver).newInstance();
            Connection conn = DriverManager.getConnection(url+dbName,userName,password);
            mocodeSelect = conn.prepareStatement(selectStmt);
            
            mocodeSelect.setString(1, description);
            boolean check = mocodeSelect.execute();
            ResultSet rs = mocodeSelect.getResultSet();
            
            if(!rs.next()) {
                String harderLook = "SELECT items_list.mocode FROM items_list INNER JOIN mocode_alias ON items_list.mocode=mocode_alias.mocode WHERE alias = ?";
                PreparedStatement harderSelect = conn.prepareStatement(harderLook);
                harderSelect.setString(1, description);
                harderSelect.execute();
                ResultSet harderResultSet = harderSelect.getResultSet();
                
                if(harderResultSet.next()) {
                    return harderResultSet.getString("mocode"); //This is a mocode for a lot of different services in zouLIFE so it has to be manually set
                }
                else {
                    System.out.println("No record found for: " + description);
                    return "NOTFOUND: " + description;
                }
            }
            else {
                mocode = rs.getString("mocode");
                //System.out.println(description + " :");
                //System.out.println(mocode);
            }
            
            //Statement stmt = conn.createStatement();
            //ResultSet rs = stmt.executeQuery("SELECT mocode FROM items_list WHERE description='"+ description +"';");
            
            conn.close();
        }catch(ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e){
            e.printStackTrace();
        }
        
        return mocode;
    }
    
    public ObservableList<String> getAlias(String mocode){
        ObservableList<String> options = FXCollections.observableArrayList();
        String selectStmt = "SELECT * FROM mocode_alias WHERE mocode = ?";
        PreparedStatement select = null;
        
        try{
            Class.forName(driver).newInstance();
            Connection conn = DriverManager.getConnection(url+dbName,userName,password);
            select = conn.prepareStatement(selectStmt);
            select.setString(1, mocode);
            select.execute();
            
            ResultSet rs = select.getResultSet();
            
            while(rs.next()){
                options.add(rs.getString("alias"));
            }
            conn.close();
        }catch(ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e){
            e.printStackTrace();
        }
        
        return options;
    }
}
