package com.unimelb.cis.node;

import com.unimelb.cis.geometry.Mbr;
import weka.core.Instance;

import java.util.*;

public class Point extends Node implements Comparable {

    private int index;

    private long curveValue;

    private long[] locationOrder;

    public long[] getLocationOrder() {
        return locationOrder;
    }

    public Point(float... location) {
        this.location = location;
        this.dim = location.length;
        locationOrder = new long[dim];
        mbr = Mbr.getMbrFromPoint(this);
    }

    public Point(int index, float... location) {
        this.index = index;
        this.location = location;
        this.dim = location.length;
        locationOrder = new long[dim];
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
        locationOrder = new long[dim];
        mbr = Mbr.getMbrFromPoint(this);
    }

    public Point(float x, float y) {
        dim = 2;
        this.location = new float[dim];
        location[0] = x;
        location[1] = y;
        locationOrder = new long[dim];
        mbr = Mbr.getMbrFromPoint(this);
    }

    public Point(float x, float y, int index) {
        this.index = index;
        dim = 2;
        this.location = new float[dim];
        location[0] = x;
        location[1] = y;
        locationOrder = new long[dim];
        mbr = Mbr.getMbrFromPoint(this);
    }

    public Point(float x, float y, float z) {
        this.dim = 3;
        this.location = new float[dim];
        location[0] = x;
        location[1] = y;
        location[2] = z;
        locationOrder = new long[dim];
        mbr = Mbr.getMbrFromPoint(this);
    }

    public Point(float x, float y, float z, int index) {
        this.index = index;
        this.dim = 3;
        this.location = new float[dim];
        location[0] = x;
        location[1] = y;
        location[2] = z;
        locationOrder = new long[dim];
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

    @Override
    public void adjust() {
        Node parent = this.getParent();
        while (parent != null) {
            parent.adjust();
            parent = parent.getParent();
        }
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

    public long getxIndex() {
        return locationOrder[0];
    }

    public void setxIndex(int xIndex) {
        locationOrder[0] = xIndex;
    }

    public long getyIndex() {
        return locationOrder[1];
    }

    public void setyIndex(int yIndex) {
        locationOrder[1] = yIndex;
    }

    public long getzIndex() {
        return locationOrder[2];
    }

    public void setzIndex(int zIndex) {
        locationOrder[2] = zIndex;
    }

    public long getCurveValue() {
        return curveValue;
    }

    public void setCurveValue(long curveValue) {
        this.curveValue = curveValue;
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
                "curveValue=" + curveValue +
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

    public Instance pointToInstance() {
        return null;
    }

    public static Point InstanceToPoint(Instance instance) {
        int dim = instance.numAttributes() - 1;
        float[] location = new float[dim];
        return new Point(location);
    }

    static Random random = new Random(42);

    static Map<Integer, Map<Integer, List<Point>>> pointCache = new HashMap<>();

    public static List<Point> generatePoints(int num, int dim) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            float[] locations = new float[dim];
            for (int j = 0; j < dim; j++) {
                locations[i] = random.nextFloat();
            }
            points.add(new Point(locations));
        }
        return points;
    }

    public static List<Point> getPoints(int num, int dim) {
        List<Point> points;
        if (pointCache.containsKey(dim)) {
            if (pointCache.get(dim).containsKey(num)) {
                points = pointCache.get(dim).get(num);
                return points;
            } else {
                points = generatePoints(num, dim);
                pointCache.get(dim).put(num, points);
            }
        } else {
            points = generatePoints(num, dim);
            Map<Integer, List<Point>> item = new HashMap<>();
            item.put(num, points);
            pointCache.put(dim, item);
        }
        return points;
    }

}
