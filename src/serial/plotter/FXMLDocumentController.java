/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serial.plotter;


import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;


/**
 *
 * @author torben
 */
public class FXMLDocumentController implements Initializable {

    DataHandler dh;
    @FXML
    private Label connectionStatus;
    @FXML
    private ComboBox PortLB;
    @FXML
    private TextField BaudrateTB;
    @FXML
    private Button StartStopBT;
    @FXML
    private TextField channelDelimiterTB;
    @FXML
    private TextField channelCountTB;
    @FXML
    private TextField blockDelimiterTB;
      
    @FXML
    private AnchorPane chartPane;
    
    private NumberAxis xAxis;
    
    
    private static final int MAX_DATA_POINTS = 1000;
    private int xSeriesData = 0;
    LineChart<Number, Number> lineChart;
    ObservableList seriesList= FXCollections.observableArrayList();;
    
    int channelCount=0;
    
    
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
                
                
                channelCount = Integer.getInteger(channelCountTB.getText(), 1);
                channelCountTB.setText(""+channelCount);
               
                String baudrate = BaudrateTB.getText();
                int baudrateNum = Integer.getInteger(baudrate, 115200);
                BaudrateTB.setText(""+baudrateNum);
                connected=dh.connect(selectedPort+1, baudrateNum, channelCount);
                
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
        
        
        xAxis = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS / 10);
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickVisible(false);

        NumberAxis yAxis = new NumberAxis();
        
        // Create a LineChart
        lineChart = new LineChart<Number, Number>(xAxis, yAxis) {
            // Override to remove symbols on each data point
            @Override
            protected void dataItemAdded(XYChart.Series<Number, Number> series, int itemIndex, XYChart.Data<Number, Number> item) {
            }
        };
        lineChart.setData(seriesList);
        AnchorPane.setBottomAnchor(lineChart, 0.0);
        AnchorPane.setTopAnchor(lineChart, 0.0);
        AnchorPane.setLeftAnchor(lineChart, 0.0);
        AnchorPane.setRightAnchor(lineChart, 0.0);
        chartPane.getChildren().add(lineChart);
        
        
        prepareTimeline();
    }
    
    
    void updateGui(){
        if(connected){
            StartStopBT.setText("Stop");
            connectionStatus.setText("connected");
              seriesList.clear();
            for(int i =0; i<channelCount; i++){
                seriesList.add(new XYChart.Series<Number, Number>());
            }
          
            
            
            
        }else{
            StartStopBT.setText("Start");
            connectionStatus.setText("not connected");
          
            
        }
    }
    
    
    
      //-- Timeline gets called in the JavaFX Main thread
    private void prepareTimeline() {
        // Every frame to take any data from queue and add to chart
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                addDataToSeries();
            }
        }.start();
    }

    private void addDataToSeries() {
        ArrayList<ConcurrentLinkedQueue<Number>> dataContainer = dh.getDataContainer();
        
        for (int i = 0; i < 20; i++) { //-- add 20 numbers to the plot+
            if(dataContainer.isEmpty()) break;
            if (dataContainer.get(0).isEmpty()) break;
            int k=0;
            for (ConcurrentLinkedQueue<Number> dataContainer1 : dataContainer) {
                Series<Number, Number> series = (Series<Number, Number>) seriesList.get(k+1);
                series.getData().add(new XYChart.Data<>(xSeriesData++, dataContainer1.remove()));
                k++;
                if (series.getData().size() > MAX_DATA_POINTS) {
                    series.getData().remove(0, series.getData().size() - MAX_DATA_POINTS);
                }
            }
        }
        
    /*
        // remove points to keep us at no more than MAX_DATA_POINTS
        if (series1.getData().size() > MAX_DATA_POINTS) {
            series1.getData().remove(0, series1.getData().size() - MAX_DATA_POINTS);
        }
        if (series2.getData().size() > MAX_DATA_POINTS) {
            series2.getData().remove(0, series2.getData().size() - MAX_DATA_POINTS);
        }
        if (series3.getData().size() > MAX_DATA_POINTS) {
            series3.getData().remove(0, series3.getData().size() - MAX_DATA_POINTS);
        }

*/
        // update
        xAxis.setLowerBound(xSeriesData - MAX_DATA_POINTS);
        xAxis.setUpperBound(xSeriesData - 1);
    }


}
