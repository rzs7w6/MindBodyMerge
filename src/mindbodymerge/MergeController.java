/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mindbodymerge;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author Jonathon Lantsberger and Robert Spalding
 */
public class MergeController implements Initializable {
    
    @FXML
    private TextField purchaseData;
    
    @FXML
    private TextField studentData;
    
    @FXML
    private TextField semesterCode;
    
    private Scene myzou;
    private Stage stage;
    private MyZouItemsController controller;
    
    void ready(Stage stage) {
        this.stage = stage;
        this.stage.setTitle("MindBody Merger");
    }
    
    @FXML
    private void handlePurchaseBrowse(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel Sheet (*.xlsx)", "*.xlsx");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        purchaseData.setText(file.getAbsolutePath());
    }
    
    @FXML
    private void handleStudentBrowse(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel Sheet (*.xlsx)", "*.xlsx");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        studentData.setText(file.getAbsolutePath());
    }
    
    @FXML
    private void handleMerge(ActionEvent event) {
        File sales = new File(purchaseData.getText());
        File membership = new File(studentData.getText());
        String sCode = semesterCode.getText();
        if (sCode.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error");
            alert.setContentText("Please enter a semester code.");
            alert.showAndWait();
            return;
        }
        
        Parser parse = new Parser(sales, membership, sCode);
        ArrayList<String> compiledList = null;
        
        try {
            compiledList = parse.handleParsing();
            //System.out.println(compiledList.size());
            //System.out.println(compiledList);

            parse.saveFile(compiledList, stage, "Saving the Good List");
            parse.saveFile(parse.getConflictList(), stage, "Saving the Conflict List");

            //clearing the text fields
            purchaseData.clear();
            studentData.clear();
            semesterCode.clear();

            //showing completion window
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Merge Complete");
            alert.setContentText("The files have been merged. There were " + compiledList.size() + " good records, and " + parse.getConflictList().size() + " conflicts that need to be resolved.");
            alert.showAndWait();
        }            
        catch (IllegalStateException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error");
            alert.setContentText("Please check input files." + e);
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleSwitchMyZou(ActionEvent event){
        try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("myZouItems.fxml"));
                Parent root = loader.load();
                controller = loader.getController();
                System.out.println(controller);
                myzou = new Scene(root);
                stage.setScene(myzou);
                controller.ready(stage);
                return;
            } catch (Exception ex) {
                System.out.println("Unable to load MyZou UI. Error: " + ex);
                return;
            }
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
