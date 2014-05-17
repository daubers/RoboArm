package com.gibbet.roboarm.control;

/**
 * Created by matt on 17/05/14.
 */
public class Servo {
    private int position;
    private int max;
    private int min;
    private int upright;
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getUpright() {
        return upright;
    }

    public void setUpright(int upright) {
        this.upright = upright;
    }
}
