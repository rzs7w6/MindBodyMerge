package mindbodymerge;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Jonathon Lantsberger and Robert Spalding
 */
public class MyZouItemsController implements Initializable {

    private Stage stage;
    private ObservableList<String> items;
    private ObservableList<String> aliases;
    private Scene main;
    private MergeController controller;
    
    @FXML
    private ListView<String> list;
    
    @FXML
    private TextField description;
    
    @FXML
    private TextField itemNum; //alias for mocode
    
    @FXML
    private TextField newAlias;
    
    @FXML
    private Label aliasDescription;
    
    @FXML
    private ComboBox aliasList;
    
    public void ready(Stage stage) {
        this.stage = stage;
        Database database = new Database();
        items = database.getInfo();
        System.out.println(items);
        list.setItems(items);
        this.stage.setTitle("Manage MyZou Item Types");
    }
    
    @FXML
    private void handleDelete(ActionEvent event) {
        Database database = new Database();
        String mocode = list.getSelectionModel().getSelectedItem();
        String[] parsedMocode = mocode.split(" ");
        database.deleteEntry(parsedMocode[0]);
        refresh();
    }
    
    @FXML
    private void handleAddMocode(ActionEvent event) {
        Database database = new Database();
        String mocode = itemNum.getText();
        String descript = description.getText();
        database.addEntry(mocode, descript);
        refresh();
        itemNum.clear();
        description.clear();
    }
    
    @FXML
    private void handleReturn(ActionEvent event) {
        try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("merge.fxml"));
                Parent root = loader.load();
                controller = loader.getController();
                System.out.println(controller);
                main = new Scene(root);
                stage.setScene(main);
                controller.ready(stage);
                return;
            } catch (Exception ex) {
                System.out.println("Unable to load Main UI. Error: " + ex);
                return;
            }
    }
    
    @FXML
    private void handleAddAlias(ActionEvent event) {
        String descript = "";
        Database database = new Database();
        String mocode = list.getSelectionModel().getSelectedItem();
        String[] parsedMocode = mocode.split(" ");
        for(int i = 2; i < parsedMocode.length; i++) { //cutting off the mocode and putting the description back together (the description starts at index 2)
            descript += parsedMocode[i] + " ";
        }
        String alias = newAlias.getText();
        boolean result = database.addEntry(0, descript.substring(0, descript.length() - 1) , alias); //using the substring method to remove the tailing space left over from reassembling the string above
    }
    
    @FXML
    private void handleViewAlias(MouseEvent event) {
        String descript = "";
        Database database = new Database();
        String mocode = list.getSelectionModel().getSelectedItem();
        String[] parsedMocode = mocode.split(" ");
        aliases = database.getAlias(parsedMocode[0]);
        aliasDescription.setText("Viewing Aliases for: " + parsedMocode[0]);
        
        aliasList.getItems().clear();
        aliasList.getItems().addAll(aliases);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    private void refresh() {
        Database database = new Database();
        list.getItems().removeAll(items);
        items = database.getInfo();
        list.setItems(items);
    }
}
