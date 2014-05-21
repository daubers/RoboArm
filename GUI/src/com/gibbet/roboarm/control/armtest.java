package com.gibbet.roboarm.control;

/**
 * Created by matt on 21/05/14.
 */
public class armtest {
    public static void main(String args[]){
        try {
            Arm arm = new Arm();
            while (!arm.ready){
                Thread.sleep(1000);
            }
            arm.writeToArduino("41;");
            Thread.sleep(10000);
            arm.writeToArduino("0,0,200;");
            arm.writeToArduino("41;");
            Thread.sleep(10000);
            arm.close();
        }
        catch (Exception e){
            System.err.println(e.getMessage());
            System.err.println("Err");
        }

    }
}
