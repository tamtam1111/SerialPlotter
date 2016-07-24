/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serial.plotter;


import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author torben
 */
public class FXMLDocumentController implements Initializable {

    DataHandler dh;
    @FXML
    private Label connectionStatus;
    @FXML
    private ChoiceBox PortLB;
    @FXML
    private TextField BaudrateTB;
    @FXML
    private Button StartStopBT;

    boolean connected=false;
    int selectedPort=0;
    @FXML
    private void onStartStop(ActionEvent event) {
        System.out.println("Start!");
        connectionStatus.setText("Start!");
        if(connected){
            connected=false;
        }else{
            if(dh.getPorts().size()>0){
                
               
                String baudrate = BaudrateTB.getText();
                int baudrateNum = Integer.getInteger(baudrate, 115200);
                BaudrateTB.setText(""+baudrateNum);
                connected=dh.connect(selectedPort, baudrateNum);
            }else{
                
                System.out.println("no ports available");
            }
        }
        updateGui();
    }

    @FXML
    private void onLoggingSelect(ActionEvent event) {
        System.out.println("Logging!");
        connectionStatus.setText("Logging!");

    }
  
    

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO

        dh = DataHandler.getInstance();
        PortLB.setItems(dh.getPorts());
        PortLB.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                selectedPort=newValue.intValue();
            }
            
        });
    }
    
    
    void updateGui(){
        if(connected){
            StartStopBT.setText("Stop");
            connectionStatus.setText("connected");
            
        }else{
            StartStopBT.setText("Start");
            connectionStatus.setText("not connected");
        }
    }

}
