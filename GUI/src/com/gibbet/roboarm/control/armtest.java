package com.gibbet.roboarm.control;

/**
 * Created by matt on 21/05/14.
 */
public class armtest {
    public static void main(String args[]){
        try {
            System.err.println("Yaboo");
            Arm arm = new Arm();
            //Thread.sleep(30000);
            while (!arm.ready){
                Thread.sleep(1000);
            }
            arm.writeToArduino("41;");
            Thread.sleep(10000);
            //System.err.println("Yaboo");
            arm.close();
        }
        catch (Exception e){
            System.err.println(e.getMessage());
            System.err.println("Err");
        }

    }
}
