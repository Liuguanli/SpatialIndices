package com.unimelb.cis.node;

import com.unimelb.cis.geometry.Mbr;

import java.util.Arrays;

public class Point extends Node implements Comparable {

    private int index;

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
        dim = 2;
        this.location = new float[dim];
        location[0] = x;
        location[1] = y;
        locationOrder = new int[dim];
        mbr = Mbr.getMbrFromPoint(this);
    }

    public Point(float x, float y, int index) {
        this.index = index;
        dim = 2;
        this.location = new float[dim];
        location[0] = x;
        location[1] = y;
        locationOrder = new int[dim];
        mbr = Mbr.getMbrFromPoint(this);
    }

    public Point(float x, float y, float z) {
        this.dim = 3;
        this.location = new float[dim];
        location[0] = x;
        location[1] = y;
        location[2] = z;
        locationOrder = new int[dim];
        mbr = Mbr.getMbrFromPoint(this);
    }

    public Point(float x, float y, float z, int index) {
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
        return location[0];
    }


    public float getY() {
        return location[1];
    }

    public float getZ() {
        return location[2];
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getxIndex() {
        return locationOrder[0];
    }

    public void setxIndex(int xIndex) {
        locationOrder[0] = xIndex;
    }

    public int getyIndex() {
        return locationOrder[1];
    }

    public void setyIndex(int yIndex) {
        locationOrder[1] = yIndex;
    }

    public int getzIndex() {
        return locationOrder[2];
    }

    public void setzIndex(int zIndex) {
        locationOrder[2] = zIndex;
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

    public Point clone() {
        float[] copiedLocation = new float[this.location.length];
        System.arraycopy(location, 0, copiedLocation, 0, this.location.length);
        return new Point(copiedLocation);
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
