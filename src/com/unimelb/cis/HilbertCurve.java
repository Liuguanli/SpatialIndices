package com.unimelb.cis;

import com.unimelb.cis.node.Point;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HilbertCurve extends Curve {

    public static List<Point> hilbertCurve(List<Point> points) {
        int length = points.size();
        int bitNum = (int) (Math.log(length) / Math.log(2.0)) + 1;

//        System.out.println(bitNum);
//        for (int i = 0; i < points.size(); i++) {
//            Point point = points.get(i);
//            int result = get2DZcurve(point.getxIndex(), point.getyIndex(), bitNum);
//            point.setzCurveValue(result);
//        }
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            long result = get2DHilbertCurve(point.getxIndex(), point.getyIndex(), bitNum);
            point.setzCurveValue(result);
//            if (i % 10000 == 0) {
//                System.out.println("cal ing");
//            }
        }
        System.out.println("cal HilbertValue finish");
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

    public static String getlocation(long zValue, int length) {
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

    public static void main(String args[]) {
        System.out.println(get2DHilbertCurve(5, 2, 16));
    }

}
