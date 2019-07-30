package com.unimelb.cis.geometry;


import com.unimelb.cis.node.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mbr {

    private float x1;
    private float x2;
    private float y1;
    private float y2;

    public Mbr() {
        x1 = Float.MAX_VALUE;
        x2 = Float.MIN_VALUE;
        y1 = Float.MAX_VALUE;
        y2 = Float.MIN_VALUE;
    }

    public Mbr(float x1, float x2, float y1, float y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }

    public float getX1() {
        return x1;
    }

    public void setX1(float x1) {
        this.x1 = x1;
    }

    public float getX2() {
        return x2;
    }

    public void setX2(float x2) {
        this.x2 = x2;
    }

    public float getY1() {
        return y1;
    }

    public void setY1(float y1) {
        this.y1 = y1;
    }

    public float getY2() {
        return y2;
    }

    public void setY2(float y2) {
        this.y2 = y2;
    }

    public int hashCode() {
        int result = 1;
        result = 31 * result + Float.floatToIntBits(this.x1);
        result = 31 * result + Float.floatToIntBits(this.x2);
        result = 31 * result + Float.floatToIntBits(this.y1);
        result = 31 * result + Float.floatToIntBits(this.y2);
        return result;
    }

    public float area() {
        return (y2 - y1) * (x2 - x1);
    }

    public float peremeter() {
        return (y2 - y1 + x2 - x1) * 2;
    }

    public void merge(float x, float y) {
        x1 = x < x1 ? x : x1;
        y1 = y < y1 ? y : y1;
        x2 = x > x2 ? x : x2;
        y2 = y > y2 ? y : y2;
    }

    public Mbr getOverlap(Mbr another) {

        List<Float> latis = new ArrayList<>();
        List<Float> longis = new ArrayList<>();
        latis.add(this.getY1());
        latis.add(this.getY2());
        latis.add(another.getY1());
        latis.add(another.getY2());

        longis.add(this.getX1());
        longis.add(this.getX2());
        longis.add(another.getX1());
        longis.add(another.getX2());

        Collections.sort(latis);
        Collections.sort(longis);
        Mbr result = new Mbr(longis.get(1), longis.get(2), latis.get(1), latis.get(2));

        return result;
    }

    public boolean interact(Mbr mbr) {
        if (this.contains(new Point(mbr.x1, mbr.y1))
                || this.contains(new Point(mbr.x1, mbr.y2))
                || this.contains(new Point(mbr.x2, mbr.y1))
                || this.contains(new Point(mbr.x2, mbr.y2))) {
            return true;
        }

        if (mbr.contains(new Point(this.x1, this.y1))
                || mbr.contains(new Point(this.x1, this.y2))
                || mbr.contains(new Point(this.x2, this.y1))
                || mbr.contains(new Point(this.x2, this.y2))) {
            return true;
        }
        return false;
    }

    public boolean interact(Point point) {
        float x = point.getX();
        float y = point.getY();
        if (x >= this.getX1() && x <= this.getX2() && y >= this.getY1() && y <= this.getY2()) {
            return true;
        }
        return false;
    }

    public boolean contains(Point point) {
        if (x1 < point.getX() && x2 > point.getX() && y1 < point.getY() && y2 > point.getY()) {
            return true;
        }
        return false;
    }

    public boolean contains(Mbr mbr) {
        if (x1 < mbr.x1 && x2 > mbr.x2 && y1 < mbr.x1 && y2 > mbr.y2) {
            return true;
        }
        return false;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mbr mbr = (Mbr) o;
        return Float.compare(mbr.x1, x1) == 0 &&
                Float.compare(mbr.x2, x2) == 0 &&
                Float.compare(mbr.y1, y1) == 0 &&
                Float.compare(mbr.y2, y2) == 0;
    }

    public double calMINMAXDIST(Point point) {
        double distX = 0;
        double distY = 0;
        float midX = (this.getX1() + this.getX2()) / 2;
        float midY = (this.getY1() + this.getY2()) / 2;
        if (point.getX() < midX) {
            if (point.getY() < midY) {
                Point temp = new Point(this.getX1(), this.getY2());
                distX = point.calDist(temp);
            } else {
                Point temp = new Point(this.getX1(), this.getY1());
                distX = point.calDist(temp);
            }
        } else {
            if (point.getY() < midY) {
                Point temp = new Point(this.getX2(), this.getY2());
                distX = point.calDist(temp);
            } else {
                Point temp = new Point(this.getX2(), this.getY1());
                distX = point.calDist(temp);
            }
        }
        if (point.getY() < midY) {
            if (point.getX() < midX) {
                Point temp = new Point(this.getX2(), this.getY1());
                distY = point.calDist(temp);
            } else {
                Point temp = new Point(this.getX1(), this.getY1());
                distY = point.calDist(temp);
            }
        } else {
            if (point.getX() > midX) {
                Point temp = new Point(this.getX1(), this.getY2());
                distY = point.calDist(temp);
            } else {
                Point temp = new Point(this.getX2(), this.getY2());
                distY = point.calDist(temp);
            }
        }
        return distX < distY ? distX : distY;
    }

    public double calMINDIST(Point point) {
        double dist = 0;
        if (this.contains(point)) {
            return 0;
        } else {
            if (point.getX() < this.getX1()) {
                if (point.getY() < this.getY1()) {
                    Point temp = new Point(this.getX1(), this.getY1());
                    dist = point.calDist(temp);
                } else if (point.getY() > this.getY2()) {
                    Point temp = new Point(this.getX1(), this.getY2());
                    dist = point.calDist(temp);
                } else {
                    dist = this.getX1() - point.getX();
                }
            } else if (point.getX() > this.getX2()) {
                if (point.getY() < this.getY1()) {
                    Point temp = new Point(this.getX2(), this.getY1());
                    dist = point.calDist(temp);
                } else if (point.getY() > this.getY2()) {
                    Point temp = new Point(this.getX2(), this.getY2());
                    dist = point.calDist(temp);
                } else {
                    dist = point.getX() - this.getX2();
                }
            } else {
                if (point.getY() < this.getY1()) {
                    dist = this.getY1() - point.getY();
                } else if (point.getY() > this.getY2()) {
                    dist = point.getY() - this.getY2();
                } else {
                    dist = 0;
                }
            }
        }
        return dist;
    }

    public float calInteract(Mbr mbr) {
        return (this.getX2() + mbr.getX2() - this.getX1() - mbr.getX1()) * (this.getY2() + mbr.getY2() - this.getY1() - mbr.getY1());
    }

    public double claDist(Point point) {
        if (interact(point))
            return 0;
        float a1 = point.getX();
        float a2 = point.getX();
        float b1 = point.getY();
        float b2 = point.getY();
        boolean xyMostLeft = x1 < a1;
        float mostLeftX1 = xyMostLeft ? x1 : a1;
        float mostRightX1 = xyMostLeft ? a1 : x1;
        float mostLeftX2 = xyMostLeft ? x2 : a2;
        double xDifference = (double) Math.max(0.0F, mostLeftX1 == mostRightX1 ? 0.0F : mostRightX1 - mostLeftX2);
        boolean xyMostDown = y1 < b1;
        float mostDownY1 = xyMostDown ? y1 : b1;
        float mostUpY1 = xyMostDown ? b1 : y1;
        float mostDownY2 = xyMostDown ? y2 : b2;
        double yDifference = (double) Math.max(0.0F, mostDownY1 == mostUpY1 ? 0.0F : mostUpY1 - mostDownY2);
        return Math.sqrt(xDifference * xDifference + yDifference * yDifference);
    }

    public String printFormat() {
        return x1 + "," + x2 + "," + y1 + "," + y2;
    }

    public static Mbr genMbr(String s, String separator) {
        String[] xys = s.split(separator);
        return new Mbr(Float.valueOf(xys[0]), Float.valueOf(xys[1]), Float.valueOf(xys[2]), Float.valueOf(xys[3]));
    }

    @Override
    public String toString() {
        return "Mbr{" +
                "x1=" + x1 +
                ", x2=" + x2 +
                ", y1=" + y1 +
                ", y2=" + y2 +
                '}';
    }
}
