/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serial.plotter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import jssc.SerialPort;
import jssc.SerialPortException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortList;

/**
 *
 * @author torben
 */
public class DataHandler {

    private static DataHandler singleton = new DataHandler();
    private ObservableList ports = FXCollections.observableArrayList();
    private Timer portUpdateTimer;
    private SerialPort serialPort;
    private Thread serialReaderThread;
    
    private ArrayList<ConcurrentLinkedQueue<Number>> dataContainer = new ArrayList<>();

    public ArrayList<ConcurrentLinkedQueue<Number>> getDataContainer() {
        return dataContainer;
    }
    boolean connected=false;
    boolean running=true;
    int channelCount=0;
    /* A private Constructor prevents any other 
    * class from instantiating.
     */
    private DataHandler() {

        portUpdateTimer = new Timer();
        portUpdateTimer.schedule(
                new TimerTask() {

            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    public void run() {
                        //getPorts();
                    }
                });

            }
        }, 0, 1000);
        
        serialReaderThread=new Thread(new Runnable() {
            @Override
            public void run() {
                
                while(running){
                    if(connected){
                        for(int i=0; i<channelCount; i++){
                            double d = Math.random();
                            dataContainer.get(i).add(d);
                            System.out.println("series "+i+ " val: "+d);
                            
                        }
                        try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }
                    }
                }
            }
        });
        serialReaderThread.start();
    }

    /* Static 'instance' method */
    public static DataHandler getInstance() {
        return singleton;
    }

    public ObservableList getPorts() {

        String[] portNames = SerialPortList.getPortNames();
        ports.clear();
        for (int i = 0; i < portNames.length; i++) {
            System.out.println(portNames[i]);
            ports.add(portNames[i]);
        }

        return this.ports;
    }
    


    public void Cleanup() {
        portUpdateTimer.cancel();
        portUpdateTimer.purge();
        if (serialPort != null) {
            try {
                serialPort.closePort();//Close serial port
            } catch (SerialPortException ex) {
                Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if(serialReaderThread.isAlive()){
            running=false;
            try {
                serialReaderThread.join(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public boolean connect(int index, int baudrate, int channelCount) {
        this.channelCount=channelCount;
        if (index-1 < ports.size()) {
            try {
                
                serialPort = new SerialPort((String)ports.get(index));
                serialPort.openPort();//Open serial port
                serialPort.setParams(baudrate,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);
                int mask = SerialPort.MASK_RXCHAR;//Prepare mask
                connected=true;
                dataContainer.clear();
                for(int i=0; i<channelCount; i++){
                    dataContainer.add(new ConcurrentLinkedQueue<Number>());
                }
                /*
                serialPort.setEventsMask(mask);//Set mask
                serialPort.addEventListener(new SerialPortEventListener() {
                    @Override
                    public void serialEvent(SerialPortEvent event) {
                      
                            //Read data, if 10 bytes available 
                            try {
                                byte buffer[] = serialPort.readBytes(10);
                                System.out.println(""+new String(buffer, StandardCharsets.UTF_8));
                                
                                
                                
                            } catch (SerialPortException ex) {
                                System.out.println(ex);
                            }
                        
                    }

                });//Add SerialPortEventListener
                */
            } catch (SerialPortException ex) {
                System.out.println(ex);
            }
            return true;
        } else {
            return false;
        }

    }
    
    
    public void disconnect(){
        connected=false;
        if(serialPort!=null){
            try {
                serialPort.closePort();
            } catch (SerialPortException ex) {
                Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }

}
