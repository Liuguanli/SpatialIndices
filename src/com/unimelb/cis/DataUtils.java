//package com.unimelb.cis;
//
//import com.leo.r_tree_rxjava.Point;
//import com.leo.r_tree_rxjava.curve_tree.Point;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//
//public class DataUtils {
//
//    static float maxLati = Float.MIN_VALUE;
//    static float maxLongi = Float.MIN_VALUE;
//    static float minLati = Float.MAX_VALUE;
//    static float minLongi = Float.MAX_VALUE;
//
//    public static List<List<Point>> getPartition(List<Point> points, int size) {
//
//        List<List<Point>> result = new ArrayList<>();
//        int num = (int) Math.ceil(((double) points.size()) / size);
//
//        int width = (int) Math.sqrt(num);
//        int length = num / width;
//
//        int remainder = num - length * (width - 1);
//        System.out.println(width);
//        System.out.println(length);
//        System.out.println(remainder);
//        for (int i = 0; i < width - 1; i++) {
//            List<Point> temp = points.subList((0 + i) * size * length, (1 + i) * size * length);
//            sortLongitude(temp);
//            for (int j = 0; j < length; j++) {
//                result.add(temp.subList((0 + j) * size, (1 + j) * size));
//            }
//        }
//
//        if (remainder != 0) {
//            List<Point> temp = points.subList((width - 1) * length * size, points.size());
//            for (int j = 0; j < remainder - 1; j++) {
//                result.add(temp.subList((0 + j) * size, (1 + j) * size));
//            }
//            result.add(temp.subList((remainder - 1) * size, temp.size()));
//
//        }
//
//        return result;
//    }
//
//    private static void init(List<Float> latis, List<Float> longis) {
//        for (int i = 0; i < latis.size(); i++) {
//            float lati = latis.get(i);
//            float longi = longis.get(i);
//            if (lati > maxLati) {
//                maxLati = lati;
//            }
//            if (lati < minLati) {
//                minLati = lati;
//            }
//            if (longi > maxLongi) {
//                maxLongi = longi;
//            }
//            if (longi < minLongi) {
//                minLongi = longi;
//            }
//        }
//    }
//
//    public static void normalizeData(List<Float> latis, List<Float> longis, int featureRange) {
//        init(latis, longis);
//        if (latis == null || latis.size() == 0 || longis == null || longis.size() == 0 || latis.size() != longis.size() || featureRange <= 0)
//            return;
//
//        float latiInterval = maxLati - minLati;
//        float longiInterval = maxLongi - minLongi;
//        for (int i = 0; i < latis.size(); i++) {
//            latis.set(i, (latis.get(i) - minLati) * featureRange / latiInterval);
//            longis.set(i, (longis.get(i) - minLongi) * featureRange / longiInterval);
//        }
//    }
//
//    public static void normalizeData1(List<Float> latis, List<Float> longis, int featureRange, float lowerLati, float upperLati, float lowerLongi, float upperLongi) {
//        if (latis == null || latis.size() == 0 || longis == null || longis.size() == 0 || latis.size() != longis.size() || featureRange <= 0)
//            return;
//
//        float latiInterval = upperLati - lowerLati;
//        float longiInterval = upperLongi - lowerLongi;
//        for (int i = 0; i < latis.size(); i++) {
//            latis.set(i, (latis.get(i) - lowerLati) * featureRange / latiInterval);
//            longis.set(i, (longis.get(i) - lowerLongi) * featureRange / longiInterval);
//        }
//    }
//
//    public static List<Point> sortLatitude(List<Point> points) {
//        Collections.sort(points, new Comparator<Point>() {
//            public int compare(Point p1, Point p2) {
//                if (p1.getLatitude() > p2.getLatitude()) {
//                    return 1;
//                } else if (p1.getLatitude() < p2.getLatitude()) {
//                    return -1;
//                } else {
//                    if (p1.getLongitude() > p2.getLongitude()) {
//                        return 1;
//                    } else if (p1.getLongitude() < p2.getLongitude()) {
//                        return -1;
//                    } else {
//                        return 0;
//                    }
//                }
//            }
//        });
//        return points;
//    }
//
//    public static List<Point> sortLongitude(List<Point> points) {
//        Collections.sort(points, new Comparator<Point>() {
//            public int compare(Point p1, Point p2) {
//                if (p1.getLongitude() > p2.getLongitude()) {
//                    return 1;
//                } else if (p1.getLongitude() < p2.getLongitude()) {
//                    return -1;
//                } else {
//                    if (p1.getLatitude() > p2.getLatitude()) {
//                        return 1;
//                    } else if (p1.getLatitude() < p2.getLatitude()) {
//                        return -1;
//                    } else {
//                        return 0;
//                    }
//                }
//            }
//        });
//        return points;
//    }
//
//}
