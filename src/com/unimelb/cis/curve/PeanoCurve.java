package com.unimelb.cis.curve;

import com.unimelb.cis.node.Point;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PeanoCurve extends Curve {

    public static List<Point> peanoCurve(List<Point> points) {
        return peanoCurve(points, true);
    }

    public static List<Point> peanoCurve(List<Point> points, boolean ranksapce) {
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
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            long result = getPeanoValue(point.getLocationOrder());
            point.setCurveValue(result);
        }
        Collections.sort(points, (o1, o2) -> {
            if (o1.getCurveValue() > o2.getCurveValue()) {
                return 1;
            } else if (o1.getCurveValue() < o2.getCurveValue()) {
                return -1;
            } else {
                return 0;
            }
        });
        for (int i = 0; i < points.size(); i++) {
            points.get(i).setCurveValueOrder(i);
        }
        return points;
    }



    public static long[] rotate(int n, long[] locations, int s) {
        long[] result = new long[locations.length];
        if (n == 1) {
            return locations;
        }
        n = n - 1;
        switch (s) {
            case 0:
            case 2:
            case 6:
            case 8:
                return locations;
            case 1:
            case 7:
                result[0] = n - locations[0];
                result[1] = locations[1];
                return result;
            case 3:
            case 5:
                result[0] = locations[0];
                result[1] = n - locations[1];
                return result;
            case 4:
                result[0] = n - locations[0];
                result[1] = n - locations[1];
                return result;
            default:
                return result;
        }
    }

    public static long[] map(int t, int N) {
        long x = 0L;
        long y = 0L;

        if (t >= N * N) {
            return null;
        }

        for (int i = 1; i < N; i *= 3) {
            int s = t % 9;

            int rx = s / 3;
            int ry = s % 3;
            if (rx == 1) {
                ry = 2 - ry;
            }

            if (i > 1) {
                long[] temp = rotate(i, new long[]{x, y}, s);
                x = temp[0];
                y = temp[1];
            }
            x += rx * i;
            y += ry * i;

            t = t / 9;
        }

        return new long[]{x, y};
    }

    private static Long getIndex(long x, long y) {
        if (x == 0) {
            if (y == 0) {
                return 0L;
            } else if (y == 1) {
                return 1L;
            } else {
                return 2L;
            }
        } else if (x == 1) {
            if (y == 0) {
                return 5L;
            } else if (y == 1) {
                return 4L;
            } else {
                return 3L;
            }
        } else {
            if (y == 0) {
                return 6L;
            } else if (y == 1) {
                return 7L;
            } else {
                return 8L;
            }
        }
    }

    public static long getPeanoValue(long[] locations) {
        long x = locations[0];
        long y = locations[1];

        int xDegree = (int) (Math.log(x + 1) / Math.log(3));
        int yDegree = (int) (Math.log(y + 1) / Math.log(3));
        List<Long> xbits = new ArrayList<>();
        List<Long> ybits = new ArrayList<>();

        for (int i = xDegree; i >= 0; i--) {
            int denominator = (int) Math.pow(3, i);
            long index = x / denominator;
            x = x % denominator;
            xbits.add(index);
        }

        for (int i = yDegree; i >= 0; i--) {
            int denominator = (int) Math.pow(3, i);
            Long index = y / denominator;
            y = y % denominator;
            ybits.add(index);
        }
        Collections.reverse(xbits);
        Collections.reverse(ybits);
        int length = Math.max(xbits.size(), ybits.size());
        if (xbits.size() < length) {
            for (int i = xbits.size(); i < length; i++) {
                xbits.add(0L);
            }
        }
        if (ybits.size() < length) {
            for (int i = ybits.size(); i < length; i++) {
                ybits.add(0L);
            }
        }
        Collections.reverse(xbits);
        Collections.reverse(ybits);
        int result = 0;
        List<Long> rotates = new ArrayList<>();
        rotates.add(0L);
        for (int i = 0; i < length; i++) {
            long[] temp = new long[]{xbits.get(i), ybits.get(i)};
            for (int j = 0; j < rotates.size(); j++) {
                temp = rotate(3, temp, rotates.get(j).intValue());
            }
            long index = getIndex(temp[0], temp[1]);
            rotates.add(index);
            result += Math.pow(9, length - i - 1) * index;
        }
        return result;
    }

    public static void main(String[] args) {

        for (int i = 0; i < 81 * 81 * 9; i++) {
            long[] result = map(i, 9*9*9);
//            System.out.println(result[0] + " " + result[1] + " " + i);
            if (i != getPeanoValue(result)) {
                System.out.println(result[0] + " " + result[1] + " " + i);
                System.out.println(getPeanoValue(result) + " !!!");
            }
        }

    }

}
