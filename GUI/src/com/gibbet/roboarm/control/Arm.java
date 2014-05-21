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
import org.apache.commons.lang3.ArrayUtils;
import com.gibbet.roboarm.control.Servo;

public class Arm implements SerialPortEventListener{

    public static SerialPort serial_port;
    public static InputStream input;
    public static OutputStream output;
    private Servo[] servos = new Servo[5];
    public boolean ready = false;
    public String outdata;

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
        Thread.sleep(3000);
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
                this.outdata = this.outdata + data;
                //System.out.print(data);
                if (data.contains("ready")){
                    this.ready = true;
                    this.outdata = "";
                }
                //System.out.println(data.substring(Math.max(data.length() - 2, 0)));
                if (this.outdata.endsWith("tt")){
                    //We have a complete data line to process
                    //How are we going to process these... state machine or add a shadowed
                    //command form on the arduino side to deal with machine commands?
                    //the first byte is the command that was called
                    if (this.outdata.startsWith("1")){
                        //This is the current settings for each of the servos!
                        //update our internal mappings to make stuff be happy
                        this.outdata = this.outdata.substring(1,this.outdata.length()-2);
                        String[] lines = this.outdata.split("\t");
                        for (String line: lines){
                            String[] thisData = line.split(",");
                            if (this.servos[Integer.valueOf(thisData[0])] == null){
                                this.servos[Integer.valueOf(thisData[0])] = new Servo();
                            }
                            this.servos[Integer.valueOf(thisData[0])].setPosition(Integer.valueOf(thisData[1]));
                            this.servos[Integer.valueOf(thisData[0])].setMin(Integer.valueOf(thisData[2]));
                            this.servos[Integer.valueOf(thisData[0])].setMax(Integer.valueOf(thisData[3]));
                            this.servos[Integer.valueOf(thisData[0])].setUpright(Integer.valueOf(thisData[4]));
                        }
                    }
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
        while (portList.hasMoreElements()) {
            port = (CommPortIdentifier) portList.nextElement();
            System.err.println(port.getName());
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
                portId = CommPortIdentifier.getPortIdentifier("/dev/ttyACM0"); //linux FTDI for now
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
