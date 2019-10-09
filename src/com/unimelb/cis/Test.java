package com.unimelb.cis;

import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.Point;
import com.unimelb.cis.structures.IRtree;
import com.unimelb.cis.structures.hrtree.HRtree;
import com.unimelb.cis.structures.partitionmodel.PartitionModelRtree;
import com.unimelb.cis.structures.recursivemodel.RecursiveModelRtree;
import com.unimelb.cis.structures.zrtree.ZRtree;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class Test {


    public static void testZRtree(String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
        ZRtree zRtree = new ZRtree(100);
        System.out.println("ZRtree:");
        System.out.println("build finish:" + zRtree.buildRtree(s).time);
        System.out.println("point query:" + zRtree.pointQuery(zRtree.getPoints()));
        System.out.println("window query:" + zRtree.windowQuery(mbrs));
        System.out.println("knn query:" + zRtree.knnQuery(knnPoints, k));
        System.out.println("insert:" + zRtree.insert(insertedPoints));
    }

    public static void testHRtree(String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
        HRtree hRtree = new HRtree(100);
        System.out.println("HRtree:");
        System.out.println("build finish:" + hRtree.buildRtree(s).time);
        System.out.println("point query:" + hRtree.pointQuery(hRtree.getPoints()));
        System.out.println("window query:" + hRtree.windowQuery(mbrs));
        System.out.println("knn query:" + hRtree.knnQuery(knnPoints, k));
        System.out.println("insert:" + hRtree.insert(insertedPoints));
    }

    public static void testPRegression(String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
        PartitionModelRtree PRegression = new PartitionModelRtree(10000, "H", 100, "LinearRegression");
        System.out.println("partition:" + "LinearRegression");
        System.out.println("build finish:" + PRegression.buildRtree(s).time);
        System.out.println("point query:" + PRegression.pointQuery(PRegression.getPoints()));
        System.out.println("window query:" + PRegression.windowQuery(mbrs));
        System.out.println("knn query:" + PRegression.knnQuery(knnPoints, k));
        System.out.println("insert:" + PRegression.insert(insertedPoints));
    }

    public static void testPclassification(String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
        PartitionModelRtree Pclassification = new PartitionModelRtree(10000, "H", 100, "NaiveBayes");
//        System.out.println("partition:" + "NaiveBayes");
        System.out.println("partition:" + "NaiveBayes");
        System.out.println("build finish:" + Pclassification.buildRtree(s).time);
        System.out.println("point query:" + Pclassification.pointQuery(Pclassification.getPoints()));
        System.out.println("window query:" + Pclassification.windowQuery(mbrs));
        System.out.println("knn query:" + Pclassification.knnQuery(knnPoints, k));
        System.out.println("insert:" + Pclassification.insert(insertedPoints));
    }

    public static void testRRegression(String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
        RecursiveModelRtree RRegression = new RecursiveModelRtree(10000, "H", 100, "LinearRegression");
        System.out.println("Recursive:" + "LinearRegression");
        System.out.println("build finish:" + RRegression.buildRtree(s).time);
        System.out.println("point query:" + RRegression.pointQuery(RRegression.getPoints()));
        System.out.println("window query:" + RRegression.windowQuery(mbrs));
        System.out.println("knn query:" + RRegression.knnQuery(knnPoints, k));
        System.out.println("insert:" + RRegression.insert(insertedPoints));
    }

    public static void testRclassification(String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
        RecursiveModelRtree Rclassification = new RecursiveModelRtree(10000, "H", 100, "NaiveBayes");
        System.out.println("Recursive:" + "NaiveBayes");
        System.out.println("build finish:" + Rclassification.buildRtree(s).time);
        System.out.println("point query:" + Rclassification.pointQuery(Rclassification.getPoints()));
        System.out.println("window query:" + Rclassification.windowQuery(mbrs));
        System.out.println("knn query:" + Rclassification.knnQuery(knnPoints, k));
        System.out.println("insert:" + Rclassification.insert(insertedPoints));
    }

    static List<String> datasets = Arrays.asList(
//            "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_1000000_1_2_.csv",
//            "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_1000000_1_3_.csv",
//            "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_1000000_1_4_.csv",
//            "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_1000000_1_5_.csv",
//            "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_1000000_1_6_.csv"
            "D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_2_.csv",
            "D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_3_.csv",
            "D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_4_.csv",
            "D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_5_.csv",
            "D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_6_.csv"
//            "D:\\datasets\\RLRtree\\raw\\uniform_2000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_4000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_8000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_16000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_32000000_1_2_.csv"
//            "D:\\datasets\\RLRtree\\raw\\uniform_64000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_100000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\real_east.csv"
//            "D:\\datasets\\RLRtree\\raw\\uniform_80000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_2000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_4000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_8000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_16000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_32000000_1_2_.csv"
//            "D:\\datasets\\RLRtree\\raw\\uniform_64000000_1_2_.csv"
    );

    static List<String> datasets1 = Arrays.asList(
            "D:\\datasets\\RLRtree\\raw\\real_east.csv",
            "D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_2_.csv",
            "D:\\datasets\\RLRtree\\raw\\uniform_2000000_1_2_.csv",
            "D:\\datasets\\RLRtree\\raw\\uniform_4000000_1_2_.csv",
            "D:\\datasets\\RLRtree\\raw\\uniform_8000000_1_2_.csv",
            "D:\\datasets\\RLRtree\\raw\\uniform_16000000_1_2_.csv",
            "D:\\datasets\\RLRtree\\raw\\uniform_32000000_1_2_.csv"
//            "D:\\datasets\\RLRtree\\raw\\uniform_64000000_1_2_.csv"
    );

    public static void main(String[] args) {

        int k = 25;
        int dim = 2;

        datasets1.forEach(s -> {
            List<Point> knnPoints = Point.getPoints(10, 2);
            List<Point> insertedPoints = Point.getPoints(10000, 2);
            List<Mbr> mbrs = Mbr.getMbrs(0.04f , 10, 2);
            System.out.println(s);
            testZRtree(s, insertedPoints, mbrs, knnPoints, k);
            testHRtree(s, insertedPoints, mbrs, knnPoints, k);
            testPRegression(s, insertedPoints, mbrs, knnPoints, k);
            testPclassification(s, insertedPoints, mbrs, knnPoints, k);
            testRRegression(s, insertedPoints, mbrs, knnPoints, k);
            testRclassification(s, insertedPoints, mbrs, knnPoints, k);
        });

        for (int i = 3; i < datasets.size(); i++) {
            String s = datasets.get(i);
            List<Point> knnPoints = Point.getPoints(10, dim + i);
            List<Point> insertedPoints = Point.getPoints(10000, dim + i);
            List<Mbr> mbrs = Mbr.getMbrs(0.04f , 10, dim + i);
            System.out.println(s);
            testZRtree(s, insertedPoints, mbrs, knnPoints, k);
            testHRtree(s, insertedPoints, mbrs, knnPoints, k);
            testPRegression(s, insertedPoints, mbrs, knnPoints, k);
            testPclassification(s, insertedPoints, mbrs, knnPoints, k);
            testRRegression(s, insertedPoints, mbrs, knnPoints, k);
            testRclassification(s, insertedPoints, mbrs, knnPoints, k);
        }
    }
}
