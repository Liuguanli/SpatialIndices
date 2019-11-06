package com.unimelb.cis.curve;

import com.unimelb.cis.node.Point;

import java.math.BigInteger;
import java.util.*;

public class HilbertCurve extends Curve {



    public static List<Point> hilbertCurve(List<Point> points, boolean ranksapce) {
        int dimension = points.get(0).getDim();
        int width = points.size();
        if (ranksapce) {
            for (int i = 0; i < dimension; i++) {
                sortDimensiont(points, i);
            }
        } else {
            for (int i = 0; i < dimension; i++) {
                for (int j = 0; j < width; j++) {
                    Point point = points.get(j);
                    point.getLocationOrder()[i] = (long) (point.getLocation()[i] * width);
                }
            }
        }
        int length = points.size();
        int bitNum = (int) (Math.log(length) / Math.log(2.0)) + 1;

//        System.out.println(bitNum);
//        for (int i = 0; i < points.size(); i++) {
//            Point point = points.get(i);
//            int result = get2DZcurve(point.getxIndex(), point.getyIndex(), bitNum);
//            point.setCurveValue(result);
//        }
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            long result = getHilbertValue(bitNum, point.getLocationOrder());
            point.setCurveValue(result);
//            if (i % 10000 == 0) {
//                System.out.println("cal ing");
//            }
        }

        Collections.sort(points, new Comparator<Point>() {
            public int compare(Point o1, Point o2) {
                if (o1.getCurveValue() > o2.getCurveValue()) {
                    return 1;
                } else if (o1.getCurveValue() < o2.getCurveValue()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        for (int i = 0; i < points.size(); i++) {
            points.get(i).setCurveValueOrder(i);
        }
        return points;
    }


    public static List<Point> hilbertCurve(List<Point> points) {
        return hilbertCurve(points, true);
    }

    public static long f(int n, int x, int y) {
        if (n == 0) return 1;
        int m = 1 << (n - 1);//2的n-1次方
        if (x <= m && y <= m) {
            return f(n - 1, y, x);
        }
        if (x > m && y <= m) {
            return 3L * m * m + f(n - 1, m - y + 1, m * 2 - x + 1); // 3LL表示long long 类型的3
        }
        if (x <= m && y > m) {
            return 1L * m * m + f(n - 1, x, y - m);
        }
        if (x > m && y > m) {
            return 2L * m * m + f(n - 1, x - m, y - m);
        }
        return 0;
    }


    public static long get2DHilbertCurve(int x, int y, int length) {
        long result = 0;
        int quadX = 0;
        int quadY = 0;
        for (int i = length - 1; i > -1; i--) {
            result = result << 2;
            int mask = 1 << i;
            if ((x & mask) != 0) {
                quadX = 1;
            } else {
                quadX = 0;
            }
            if ((y & mask) != 0) {
                quadY = 1;
            } else {
                quadY = 0;
            }
            mask = mask - 1;
            x = x & mask;
            y = y & mask;
            int tempPosition = 0;
            if (quadX == 0) {
                if (quadY == 0) {
                    tempPosition = 0;
                } else {
                    tempPosition = 1;
                }
            } else {
                if (quadY == 0) {
                    tempPosition = 3;
                } else {
                    tempPosition = 2;
                }
            }
            result = result | tempPosition;
        }
        return result;
    }

    public static long[] get2Dlocation(long hValue, int length) {
        long x = 0;
        long y = 0;
        for (int i = 0; i < length; i++) {
            long xSeed = 1L << (2 * i + 0);
            long ySeed = 1L << (2 * i + 1);

            if ((hValue & xSeed) > 0) {
                x += Math.pow(2, i);
            }
            if ((hValue & ySeed) > 0) {
                y += Math.pow(2, i);
            }
        }
        long[] result = new long[2];
        result[0] = x;
        result[1] = y;
        return result;
    }

    // the following codes are copied from https://github.com/davidmoten/hilbert-curve/blob/master/src/main/java/org/davidmoten/hilbert/HilbertCurve.java



    public static long getHilbertValue(int bits, long... point) {
        HilbertCurveLib c =
                HilbertCurveLib.bits(bits).dimensions(point.length);
        BigInteger index = c.index(point);
        return index.longValue();
    }

    public static long[] getlocation(long hValue, int length, int dim) {
        HilbertCurveLib c =
                HilbertCurveLib.bits(length).dimensions(dim);
        return c.point(hValue);
    }


    public static void main(String args[]) {
//        System.out.println(get2DHilbertCurve(5, 2, 16));
//
//        int length = 8;
//        for (int i = 0; i < 8; i++) {
//            for (int j = 0; j < 8; j++) {
//                long h1 = get2DHilbertCurve(i, j, length) + 1;
//                long h2 = getHilbertValue(length, i, j) + 1;
//                System.out.println("(" + i + "," + j + ") " + "h1:" + h1 + " h2:" + h2);
//            }
//        }
//
//        List<Long> xs = new ArrayList<>();
//        List<Long> ys = new ArrayList<>();
//        for (int i = 0; i < 64; i++) {
//            long[] locations1 = get2Dlocation(i, length);
//            long[] locations3 = getlocation(i, length, 2);
//            xs.add(locations3[0]);
//            ys.add(locations3[1]);
//        }
//        System.out.println(xs);
//        System.out.println(ys);

    }

}
