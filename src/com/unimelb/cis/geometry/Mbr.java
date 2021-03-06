package com.unimelb.cis.geometry;


import com.unimelb.cis.node.Point;

import java.util.*;
import java.util.function.Consumer;

public class Mbr {

    static Random random = new Random(42);

    static Map<Integer, Map<Float, List<Mbr>>> mbrCache = new HashMap<>();

    private int dim;

    private float[] location;

    public Mbr(Point point) {
        dim = point.getDim();
        location = new float[dim * 2];
        for (int i = 0; i < dim; i++) {
            location[i] = point.getLocation()[i];
            location[i + dim] = point.getLocation()[i];
        }
    }

    public Mbr(int dim) {
        location = new float[dim * 2];
        this.dim = dim;
        for (int i = 0; i < dim; i++) {
            location[i] = Float.MAX_VALUE;
            location[i + dim] = Float.MIN_VALUE;
        }
    }

    public Mbr(float... location) {
        this.location = location;
        dim = location.length / 2;
    }

    public static Mbr getMbrFromPoint(Point point) {
        int pointDim = point.getDim();
        float[] tempLocation = new float[pointDim * 2];
        for (int i = 0; i < pointDim; i++) {
            tempLocation[i] = point.getLocation()[i];
            tempLocation[i + pointDim] = point.getLocation()[i];
        }
        return new Mbr(tempLocation);
    }

    public float[] getLocation() {
        return location;
    }

    public void setLocation(float[] location) {
        this.location = location;
    }

    public float getX1() {
        return location[0];
    }

    public float getX2() {
        return location[dim];
    }

    public float getY1() {
        return location[1];
    }

    public float getY2() {
        return location[1 + dim];
    }

    public int hashCode() {
        int result = 1;
        for (int i = 0; i < location.length; i++) {
            result = 31 * result + Float.floatToIntBits(location[i]);
        }
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

    public boolean mutualInteract(Mbr mbr) {
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
        return false;
    }

    public boolean interact(Mbr mbr) {
        return this.mutualInteract(mbr) || mbr.mutualInteract(this);
    }

    public boolean interact(Point point) {
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
            if (point.getLocation()[i] < this.location[i] || point.getLocation()[i] > this.location[i + dim])
                return false;
            else
                continue;
        }
        return true;
    }

//    public boolean contains(Mbr mbr) {
//        if (x1 < mbr.x1 && x2 > mbr.x2 && y1 < mbr.x1 && y2 > mbr.y2) {
//            return true;
//        }
//        return false;
//    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mbr mbr = (Mbr) o;
        for (int i = 0; i < location.length; i++) {
            if (this.location[i] != mbr.location[i]) {
                return false;
            }
        }
        return true;
    }

    public double calMINMAXDIST(Point queryPoint) {
        if (contains(queryPoint)) {
            return 0;
        }
        float minResult = Float.MAX_VALUE;
        for (int i = 0; i < dim; i++) {
            float result = 0;
            for (int j = 0; j < dim; j++) {
                if (i == j) {
                    float temp = 0;
                    if (queryPoint.getLocation()[j] < (this.getLocation()[j + dim] + this.getLocation()[j]) / 2) {
                        temp = this.getLocation()[j];
                    } else {
                        temp = this.getLocation()[j + dim];
                    }
                    result += (queryPoint.getLocation()[j] - temp) * (queryPoint.getLocation()[j] - temp);
                } else {
                    float temp = 0;
                    if (queryPoint.getLocation()[j] > (this.getLocation()[j + dim] + this.getLocation()[j]) / 2) {
                        temp = this.getLocation()[j + dim];
                    } else {
                        temp = this.getLocation()[j];
                    }
                    result += Math.abs(queryPoint.getLocation()[j] - temp);
                }
            }
            if (result < minResult) {
                minResult = result;
            }
        }
        return Math.sqrt(minResult);
    }

    public double calMINDIST(Point queryPoint) {
        if (this.contains(queryPoint)) {
            return 0;
        }

        float[] newLocations = new float[dim];
        boolean flag = true;
        for (int i = 0; i < dim; i++) {
            if (queryPoint.getLocation()[i] <= this.getLocation()[i + dim] && queryPoint.getLocation()[i] >= this.getLocation()[i]) {
                newLocations[i] = queryPoint.getLocation()[i];
                flag = false;
            } else {
                if (Math.abs(queryPoint.getLocation()[i] - this.getLocation()[i + dim]) < Math.abs(queryPoint.getLocation()[i] - this.getLocation()[i])) {
                    newLocations[i] = this.getLocation()[i + dim];
                } else {
                    newLocations[i] = this.getLocation()[i];
                }
            }
        }
        if (flag) {
            List<Point> points = getAllPoints();
            List<Double> result = new ArrayList<>();
            // queryPoint to each Vertexs
            points.forEach(point -> result.add(point.getDist(queryPoint)));

            result.sort(Double::compareTo);
            return result.get(0);
        } else {
            return new Point(newLocations).getDist(queryPoint);
        }
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
        if (interact(point)) {
            return 0;
        } else {
            double sum = 0;
            for (int i = 0; i < dim; i++) {
                float a1 = point.getLocation()[i];
                boolean xyMostLeft = location[i] < a1;
                float mostLeftX1 = xyMostLeft ? location[i] : a1;
                float mostRightX1 = xyMostLeft ? a1 : location[i];
                float mostLeftX2 = xyMostLeft ? location[i + dim] : a1;
                double xDifference = (double) Math.max(0.0F, mostLeftX1 == mostRightX1 ? 0.0F : (mostRightX1 - mostLeftX2));
                sum = xDifference * xDifference;
            }
            return Math.sqrt(sum);
        }
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
                return new Mbr(x - side, y - side, x + side, y + side);
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
        for (int i = 0; i < num; i++) {
            mbrs.add(getMbr(side, dim));
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

    public float getLambdaByAxis(int axis) {
        return location[axis + dim] - location[axis];
    }

    public float getPerimMax() {
        float lambdaMin = Float.MAX_VALUE;
        float sum = 0;
        for (int i = 0; i < dim; i++) {
            float lambdaD = location[i + dim] - location[i];
            if (lambdaD < lambdaMin) {
                lambdaMin = lambdaD;
            }
            sum += lambdaD;
        }
        sum = sum * 2 - lambdaMin;
        return sum;

    }

    public float getCenterByAxis(int axis) {
        return (location[axis] + location[axis + dim]) / 2;
    }

    public float getProjectionByAxis(int axis) {
        return location[axis + dim] + location[axis];
    }

    public double getDistToCenter(Point point) {
        float result = 0;
        for (int i = 0; i < dim; i++) {
            result += Math.pow(((location[i] + location[i + dim]) / 2 - point.getLocation()[i]), 2);
        }
        return Math.sqrt(result);
    }

    public Point getPointLow() {
        float[] location = new float[dim];
        for (int i = 0; i < dim; i++) {
            location[i] = this.location[i];
        }
        return new Point(location);
    }

    public Point getPointHigh() {
        float[] location = new float[dim];
        for (int i = 0; i < dim; i++) {
            location[i] = this.location[i + dim];
        }
        return new Point(location);
    }

    public List<Point> getCornerPoints() {
        return Arrays.asList(getPointLow(), getPointHigh());
    }

    public List<Point> getAllPoints() {
        List<Point> points = new ArrayList<>();
        getAllVertexs(this, 0, dim, new float[dim], points);
        return points;
    }

    public static Mbr getMbr(Point point, float side) {
        int dim = point.getDim();
        float[] location = new float[dim * 2];
        for (int i = 0; i < dim; i++) {
            location[i] = Math.max(0, point.getLocation()[i] - side);
            location[i + dim] = Math.min(1, point.getLocation()[i] + side);
        }
        return new Mbr(location);
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
