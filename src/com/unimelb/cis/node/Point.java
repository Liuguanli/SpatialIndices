package com.unimelb.cis.node;

import com.unimelb.cis.geometry.Mbr;

import java.util.Arrays;

public class Point extends Node implements Comparable {

    private float x;

    private float y;

    private float z;

    private int index;

    private int xIndex;

    private int yIndex;

    private int zIndex;

    private long zCurveValue;


    private int[] locationOrder;

    public int[] getLocationOrder() {
        return locationOrder;
    }

    public Point(float... location) {
        this.location = location;
        this.dim = location.length;
        locationOrder = new int[dim];
        mbr = Mbr.getMbrFromPoint(this);
    }

    public Point(int index, float... location) {
        this.index = index;
        this.location = location;
        this.dim = location.length;
        locationOrder = new int[dim];
        mbr = Mbr.getMbrFromPoint(this);
    }

    public Point(String line) {
        String[] items = line.split(",");
        float[] location = new float[items.length - 1];
        for (int j = 0; j < items.length - 1; j++) {
            location[j] = Float.parseFloat(items[j]);
        }
        this.index = Integer.parseInt(items[items.length - 1]);
        this.location = location;
        this.dim = location.length;
        locationOrder = new int[dim];
        mbr = Mbr.getMbrFromPoint(this);
    }

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
        this.location = new float[dim];
        location[0] = x;
        location[1] = y;
        locationOrder = new int[dim];
        mbr = Mbr.getMbrFromPoint(this);
    }

    public Point(float x, float y, int index) {
        this.x = x;
        this.y = y;
        this.index = index;
        this.location = new float[dim];
        location[0] = x;
        location[1] = y;
        locationOrder = new int[dim];
        mbr = Mbr.getMbrFromPoint(this);
    }

    public Point(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = 3;
        this.location = new float[dim];
        location[0] = x;
        location[1] = y;
        location[2] = z;
        locationOrder = new int[dim];
        mbr = Mbr.getMbrFromPoint(this);
    }

    public Point(float x, float y, float z, int index) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.index = index;
        this.dim = 3;
        this.location = new float[dim];
        location[0] = x;
        location[1] = y;
        location[2] = z;
        locationOrder = new int[dim];
        mbr = Mbr.getMbrFromPoint(this);
    }


    public int compareTo(Object o) {
        Point p = (Point) o;
        for (int i = 0; i < dim; i++) {
            if (this.location[i] > p.location[i]) {
                return 1;
            } else if (this.location[i] < p.location[i]) {
                return -1;
            } else {
                continue;
            }
        }
        return 0;
    }

    @Override
    public boolean isFull() {
        return true;
    }

    public double calDist(Point point) {
        double result = 0;
        for (int i = 0; i < dim; i++) {
            result += (point.location[i] - this.location[i]) * (point.location[i] - this.location[i]);
        }
        return Math.sqrt(result);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getxIndex() {
        return xIndex;
    }

    public void setxIndex(int xIndex) {
        this.xIndex = xIndex;
    }

    public int getyIndex() {
        return yIndex;
    }

    public void setyIndex(int yIndex) {
        this.yIndex = yIndex;
    }

    public int getzIndex() {
        return zIndex;
    }

    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public long getzCurveValue() {
        return zCurveValue;
    }

    public void setzCurveValue(long zCurveValue) {
        this.zCurveValue = zCurveValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point that = (Point) o;
        return Arrays.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(location);
    }

    @Override
    public String toString() {
        return "Point{" +
                "zCurveValue=" + zCurveValue +
                ", location=" + Arrays.toString(location) +
                ", locationOrder=" + Arrays.toString(locationOrder) +
                '}';
    }

    public String getOutPutString(Node root) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < location.length; i++) {
            if (i == 0) {
                builder.append(location[i]);
            } else {
                builder.append(",").append(location[i]);
            }
        }
        
        Node temp = this.getParent();
        while (temp != root) {
            builder.append(",").append(temp.getOrderInLevel());
            temp = temp.getParent();
        }
        if (temp.getOrderInLevel() == 0) {
            builder.append(",").append(0);
        }
        return builder.toString();
    }
}
