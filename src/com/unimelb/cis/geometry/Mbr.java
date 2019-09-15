package com.unimelb.cis.geometry;


import com.unimelb.cis.node.Point;

import java.util.*;

public class Mbr {

    static Random random = new Random(42);

    static Map<Integer, Map<Float, List<Mbr>>> mbrCache = new HashMap<>();

    private float x1;
    private float x2;
    private float y1;
    private float y2;
    private float z1;
    private float z2;

    private float[] location;

//    public Mbr() {
//        x1 = Float.MAX_VALUE;
//        x2 = Float.MIN_VALUE;
//        y1 = Float.MAX_VALUE;
//        y2 = Float.MIN_VALUE;
//        location = new float[4];
//        location[0] = x1;
//        location[1] = y1;
//        location[2] = x2;
//        location[3] = y2;
//    }

//    public Mbr(float x1, float x2, float y1, float y2, float z1, float z2) {
//        this.x1 = x1;
//        this.x2 = x2;
//        this.y1 = y1;
//        this.y2 = y2;
//        this.z1 = z1;
//        this.z2 = z2;
//        location = new float[6];
//        location[0] = x1;
//        location[1] = y1;
//        location[2] = z1;
//        location[3] = x2;
//        location[4] = y2;
//        location[5] = z2;
//    }

    public Mbr(int dim) {
        location = new float[dim * 2];
        if (dim == 2) {
            x1 = Float.MAX_VALUE;
            x2 = Float.MIN_VALUE;
            y1 = Float.MAX_VALUE;
            y2 = Float.MIN_VALUE;
            location[0] = x1;
            location[1] = y1;
            location[2] = x2;
            location[3] = y2;
        } else if (dim == 3) {
            this.x1 = Float.MAX_VALUE;
            this.x2 = Float.MIN_VALUE;
            this.y1 = Float.MAX_VALUE;
            this.y2 = Float.MIN_VALUE;
            this.z1 = Float.MAX_VALUE;
            this.z2 = Float.MIN_VALUE;
            location[0] = x1;
            location[1] = y1;
            location[2] = z1;
            location[3] = x2;
            location[4] = y2;
            location[5] = z2;
        } else {
            for (int i = 0; i < location.length; i++) {
                location[i] = Float.MAX_VALUE;
                location[i + dim] = Float.MIN_VALUE;
            }
        }
    }

//    public Mbr(float x1, float x2, float y1, float y2) {
//        this.x1 = x1;
//        this.x2 = x2;
//        this.y1 = y1;
//        this.y2 = y2;
//        location = new float[4];
//        location[0] = x1;
//        location[1] = y1;
//        location[2] = x2;
//        location[3] = y2;
//    }

    public Mbr(float... location) {
        if (location.length == 4) {
            this.x1 = location[0];
            this.y1 = location[1];
            this.x2 = location[2];
            this.y2 = location[3];
            this.location = location;
        } else if (location.length == 6) {
            this.x1 = location[0];
            this.y1 = location[1];
            this.z1 = location[2];
            this.x2 = location[3];
            this.y2 = location[4];
            this.z2 = location[5];
            this.location = location;
        } else {
            this.location = location;
        }
    }

    public float[] getLocation() {
        return location;
    }

    public void setLocation(float[] location) {
        this.location = location;
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

    public float volume() {
        float volume = 1f;
        int dim = location.length / 2;
        for (int i = 0; i < dim; i++) {
            volume *= (location[dim + i] - location[i]);
        }
        return volume;
    }

    public float perimeter() {
        float perimeter = 0;
        int dim = location.length / 2;
        for (int i = 0; i < dim; i++) {
            perimeter += (location[dim + i] - location[i]);
        }
        // perimeter is not the real result because there must be a parameter n to multiple perimeter.
        // While n is not that important.
        return perimeter;
    }

    public void merge(float x, float y) {
        x1 = x < x1 ? x : x1;
        y1 = y < y1 ? y : y1;
        x2 = x > x2 ? x : x2;
        y2 = y > y2 ? y : y2;
    }

    public void getAllVertexs(Mbr mbr, int index, int dim, float[] locations, List<Point> points) {

        if (index == dim) {
            points.add(new Point(locations.clone()));
            return;
        }

        locations[index] = mbr.location[index];

        getAllVertexs(mbr, index + 1, dim, locations, points);

        locations[index] = mbr.location[index + dim];

        getAllVertexs(mbr, index + 1, dim, locations, points);

    }

    public boolean interact(Mbr mbr) {
        float[] mbrLocations = mbr.getLocation();
        int dim = mbrLocations.length / 2;

        float[] locations1 = new float[dim];
        float[] locations2 = new float[dim];

        for (int i = 0; i < dim; i++) {
            locations1[i] = mbrLocations[i];
            locations2[i] = mbrLocations[i + dim];
        }

        List<Point> points = new ArrayList<>();
        getAllVertexs(mbr, 0, dim, new float[dim], points);

        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            if (interact(point))
                return true;
            else
                continue;
        }
//        if (this.contains(new Point(mbr.x1, mbr.y1))
//                || this.contains(new Point(mbr.x1, mbr.y2))
//                || this.contains(new Point(mbr.x2, mbr.y1))
//                || this.contains(new Point(mbr.x2, mbr.y2))) {
//            return true;
//        }
//
//        if (mbr.contains(new Point(this.x1, this.y1))
//                || mbr.contains(new Point(this.x1, this.y2))
//                || mbr.contains(new Point(this.x2, this.y1))
//                || mbr.contains(new Point(this.x2, this.y2))) {
//            return true;
//        }
        return false;
    }

    public boolean interact(Point point) {
//        float x = point.getX();
//        float y = point.getY();
//        if (x >= this.getX1() && x <= this.getX2() && y >= this.getY1() && y <= this.getY2()) {
//            return true;
//        }
        int dim = this.location.length / 2;
        for (int i = 0; i < dim; i++) {
            if (point.getLocation()[i] < this.location[i] || point.getLocation()[i] > this.location[i + dim])
                return false;
            else
                continue;
        }

        return true;
    }

    public boolean contains(Point point) {
        int dim = this.location.length / 2;
        for (int i = 0; i < dim; i++) {
            if (point.getLocation()[i] <= this.location[i] || point.getLocation()[i] >= this.location[i + dim])
                return false;
            else
                continue;
        }

        return true;
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

    public float getOverlapVol(Mbr mbr) {
        if (interact(mbr)) {
            float overlap = 1;
            int dim = location.length / 2;
            for (int i = 0; i < dim; i++) {
                overlap *= (Math.min(this.location[i + dim], mbr.location[i + dim]) - Math.max(this.location[i], mbr.location[i]));
            }
            return overlap;
        } else {
            return 0;
        }
    }

    public float getOverlapPerim(Mbr mbr) {
        if (interact(mbr)) {
            float overlap = 0;
            int dim = location.length / 2;
            for (int i = 0; i < dim; i++) {
                overlap += (Math.min(this.location[i + dim], mbr.location[i + dim]) - Math.max(this.location[i], mbr.location[i]));
            }
            return overlap;
        } else {
            return 0;
        }
    }

//    public Mbr getOverlap(Mbr another) {
//
//        List<Float> latis = new ArrayList<>();
//        List<Float> longis = new ArrayList<>();
//        latis.add(this.getY1());
//        latis.add(this.getY2());
//        latis.add(another.getY1());
//        latis.add(another.getY2());
//
//        longis.add(this.getX1());
//        longis.add(this.getX2());
//        longis.add(another.getX1());
//        longis.add(another.getX2());
//
//        Collections.sort(latis);
//        Collections.sort(longis);
//        Mbr result = new Mbr(longis.get(1), longis.get(2), latis.get(1), latis.get(2));
//
//        return result;
//    }

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
                "location=" + Arrays.toString(location) +
                '}';
    }

    public static Mbr getMbr(float side, int dim) {
        side = side / 2;

        float[] locations = new float[dim];
        float[] result = new float[dim * 2];
        for (int i = 0; i < dim; i++) {
            locations[i] = random.nextFloat();
            if (locations[i] - side > 0 && locations[i] + side < 1) {
                continue;
            } else {
                i--;
            }
        }
        for (int i = 0; i < dim; i++) {
            result[i] = locations[i] - side;
            result[i + dim] = locations[i] + side;
        }
        return new Mbr(result);
    }

    public static Mbr getMbr2D(float side) {
        side = side / 2;
        float x = random.nextFloat();
        float y = random.nextFloat();
        while (true) {
            if (x - side > 0 && x + side < 1 && y - side > 0 && y + side < 1) {
                return new Mbr(x - side, x + side, y - side, y + side);
            } else {
                x = random.nextFloat();
                y = random.nextFloat();
            }
        }
    }

    public static Mbr getMbr3D(float side) {
        side = side / 2;
        float x = random.nextFloat();
        float y = random.nextFloat();
        float z = random.nextFloat();
        while (true) {
            if (x - side > 0 && x + side < 1 && y - side > 0 && y + side < 1 && z - side > 0 && z + side < 1) {
                return new Mbr(x - side, x + side, y - side, y + side, z - side, z + side);
            } else {
                x = random.nextFloat();
                y = random.nextFloat();
                z = random.nextFloat();
            }
        }
    }

    public static List<Mbr> getMbrs(float side, int num, int dim) {
        List<Mbr> mbrs = new ArrayList<>(num);
        if (mbrCache.containsKey(dim)) {
            if (mbrCache.get(dim).containsKey(side)) {
                List<Mbr> temp = mbrCache.get(dim).get(side);
                if (temp != null && temp.size() >= num) {
                    mbrs = temp.subList(0, num);
                    return mbrs;
                }
            }
        }
        if (dim == 2) {
            for (int i = 0; i < num; i++) {
                mbrs.add(getMbr2D(side));
            }
        } else if (dim == 3) {
            for (int i = 0; i < num; i++) {
                mbrs.add(getMbr3D(side));
            }
        } else {
            for (int i = 0; i < num; i++) {
                mbrs.add(getMbr(side, dim));
            }
        }
        Map temp = new HashMap<>();
        temp.put(side, mbrs);
        mbrCache.put(dim, temp);
        return mbrs;
    }

    public void updateMbr(Point point, int dim) {
        for (int i = 0; i < dim; i++) {
            float val = point.getLocation()[i];
            if (this.getLocation()[i] > val) {
                this.getLocation()[i] = val;
            }
            if (this.getLocation()[i + dim] < val) {
                this.getLocation()[i + dim] = val;
            }
        }
    }

    public Mbr clone() {
        float[] copiedLocation = new float[this.location.length];
        System.arraycopy(location, 0, copiedLocation, 0, this.location.length);
        return new Mbr(copiedLocation);
    }

    //    public static List<Mbr> getMbrs(float[] sides, int dim) {
//        List<Mbr> mbrs = new ArrayList<>(sides.length);
//        if (dim == 2) {
//            for (int i = 0; i < sides.length; i++) {
//                mbrs.add(getMbr2D(sides[i]));
//            }
//        } else if (dim == 3) {
//            for (int i = 0; i < sides.length; i++) {
//                mbrs.add(getMbr3D(sides[i]));
//            }
//        } else {
//            for (int i = 0; i < sides.length; i++) {
//                mbrs.add(getMbr(sides[i], dim));
//            }
//        }
//        return mbrs;
//    }

}
