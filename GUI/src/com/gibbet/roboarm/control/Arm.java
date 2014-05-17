package com.gibbet.roboarm.control;

/**
 * Created by matt on 17/05/14.
 * Class that abstracts the serial control of the arm
 */

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;

public class Arm implements SerialPortEventListener{

    public static SerialPort serial_port;
    public static InputStream input;
    public static OutputStream output;
    private ArrayList<Servo> servos;

    public void refreshServoPositions(){
        try {
            this.writeToArduino("3;");
        }
        catch (Exception e){
            System.err.println(e.getMessage());
        }
    }

    public Arm() throws Exception{
        init();
        serial_port.addEventListener((SerialPortEventListener) this);
        serial_port.notifyOnDataAvailable(true);
        //wait for rxtx to work away
        Thread.sleep(1500);
    }

    public Arm(String serialport) throws Exception{
        init(serialport);
        serial_port.addEventListener((SerialPortEventListener) this);
        serial_port.notifyOnDataAvailable(true);
        //wait for rxtx to work away
        Thread.sleep(1500);
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        //mucho mucho bulk of logic goes here!
        if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                int available = input.available();
                byte chunk[] = new byte[available];
                input.read(chunk, 0, available);
                String data = new String(chunk);
                System.out.print(data);
                if (data.endsWith("\n")){
                    //We have a complete data line to process
                    //How are we going to process these... state machine or add a shadowed
                    //command form on the arduino side to deal with machine commands?
                }
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }

    public static ArrayList<CommPortIdentifier> getSerialPorts() throws Exception {
        ArrayList<CommPortIdentifier> ports = new ArrayList<CommPortIdentifier>();
        CommPortIdentifier port;
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        System.out.println("Get List");
        while (portList.hasMoreElements()) {
            port = (CommPortIdentifier) portList.nextElement();
            if (port.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                ports.add(port);
            }
        }
        return ports;
    }

    public void writeToArduino(String str) throws Exception {
        output.write(str.getBytes());
    }

    public static void init(String serialport) throws Exception {

        CommPortIdentifier portId = null;
        if (serialport == null) {
            ArrayList<CommPortIdentifier> ports = getSerialPorts();
            if (ports.isEmpty()) {
                portId = CommPortIdentifier.getPortIdentifier("ttyUSB0"); //linux FTDI for now
            } else {
                portId = ports.get(0); //grab the first one.
            }

        } else {
            portId = CommPortIdentifier.getPortIdentifier(serialport);
        }

        System.out.println("Setup SerialToIR on port " + portId.getName()); //this will fail if any issues arise and be caught in the WS layer.

        serial_port = (SerialPort) portId.open("Arduino", 2000);
        serial_port.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        input = serial_port.getInputStream();
        output = serial_port.getOutputStream();

    }

    public static void init() throws Exception {
        init(null);
    }

    public synchronized void close() {
        if (serial_port != null) {
            serial_port.removeEventListener();
            serial_port.close();
        }
    }
}