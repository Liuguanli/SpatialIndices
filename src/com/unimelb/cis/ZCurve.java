package com.unimelb.cis;

import com.unimelb.cis.node.Point;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ZCurve extends Curve {

    public static List<Point> zCurve(List<Point> points) {
        int dimension = points.get(0).getDim();
        for (int i = 0; i < dimension; i++) {
            sortDimensiont(points, i);
        }
        int length = points.size();
        int bitNum = (int) (Math.log(length) / Math.log(2.0)) + 1;

        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            long result = getZcurve(point.getLocationOrder(), bitNum);
            point.setzCurveValue(result);
        }

        Collections.sort(points, new Comparator<Point>() {
            public int compare(Point o1, Point o2) {
                if (o1.getzCurveValue() > o2.getzCurveValue()) {
                    return 1;
                } else if (o1.getzCurveValue() < o2.getzCurveValue()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        return points;
    }

    public static long getZcurve(int[] locationOrder, int length) {
        long result = 0;
        for (int i = 0; i < length; i++) {
            long seed = (long) (Math.pow(2, i));
            long tempResult = 0;
            for (int j = 0; j < locationOrder.length; j++) {
                long temp = seed & locationOrder[j];
                temp = temp << (i + j);
                tempResult += temp;
            }
            result += tempResult;
        }
        return result;
    }

    public static long get2DZcurve(int x, int y, int length) {
        long result = 0;
        for (int i = 0; i < length; i++) {
            long seed = (long) (Math.pow(2, i));
            long tempX = seed & x;
            long tempY = seed & y;
            tempX = tempX << i;
            tempY = tempY << (i + 1);
            result += tempX + tempY;
        }
        return result;
    }

    public static float[] getlocation(long zValue, int length, int dimension) {
        float[] result = new float[dimension];
        for (int i = 0; i < length; i++) {

            for (int j = 0; j < dimension; j++) {
                long seed = 1L << (dimension * i + j);
                if ((zValue & seed) > 0) {
                    result[j] += Math.pow(2, i);
                }
            }

        }
        return result;
    }

    public static String get2Dlocation(long zValue, int length) {
        long x = 0;
        long y = 0;
        for (int i = 0; i < length; i++) {
            long xSeed = 1L << (2 * i + 0);
            long ySeed = 1L << (2 * i + 1);

            if ((zValue & xSeed) > 0) {
                x += Math.pow(2, i);
            }
            if ((zValue & ySeed) > 0) {
                y += Math.pow(2, i);
            }
        }
        String xx = Long.toBinaryString(x);
        String yy = Long.toBinaryString(y);
        return x + "_" + y;
    }

}
