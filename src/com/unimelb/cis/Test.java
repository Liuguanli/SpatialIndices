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

    static int k = 25;
    static List<Point> knnPoints = Point.getPoints(10, 2);
    static List<Point> insertedPoints = Point.getPoints(10000, 2);
    static List<Mbr> mbrs = Mbr.getMbrs(0.04f , 10, 2);

    public static void testZRtree(String s) {
        System.out.println("------------------------------------------------");
        ZRtree zRtree = new ZRtree(100);
        System.out.println("ZRtree:");
        System.out.println("build finish:" + zRtree.buildRtree(s).time);
        System.out.println("point query:" + zRtree.pointQuery(zRtree.getPoints()));
        System.out.println("insert:" + zRtree.insert(insertedPoints));
        System.out.println("window query:" + zRtree.windowQuery(mbrs));
        System.out.println("knn query:" + zRtree.knnQuery(knnPoints, k));
    }

    public static void testHRtree(String s) {
        System.out.println("------------------------------------------------");
        HRtree hRtree = new HRtree(100);
        System.out.println("HRtree:");
        System.out.println("build finish:" + hRtree.buildRtree(s).time);
        System.out.println("insert:" + hRtree.insert(insertedPoints));
        System.out.println("point query:" + hRtree.pointQuery(hRtree.getPoints()));
        System.out.println("window query:" + hRtree.windowQuery(mbrs));
        System.out.println("knn query:" + hRtree.knnQuery(knnPoints, k));
    }

    public static void testPRegression(String s) {
        System.out.println("------------------------------------------------");
        PartitionModelRtree PRegression = new PartitionModelRtree(10000, "H", 100, "LinearRegression");
        System.out.println("partition:" + "LinearRegression");
        System.out.println("build finish:" + PRegression.buildRtree(s).time);
        System.out.println("point query:" + PRegression.pointQuery(PRegression.getPoints()));
        System.out.println("window query:" + PRegression.windowQuery(mbrs));
        System.out.println("knn query:" + PRegression.knnQuery(knnPoints, k));
        System.out.println("insert:" + PRegression.insert(insertedPoints));
    }

    public static void testPclassification(String s) {
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

    public static void testRRegression(String s) {
        System.out.println("------------------------------------------------");
        RecursiveModelRtree RRegression = new RecursiveModelRtree(10000, "H", 100, "LinearRegression");
        System.out.println("Recursive:" + "LinearRegression");
        System.out.println("build finish:" + RRegression.buildRtree(s).time);
        System.out.println("point query:" + RRegression.pointQuery(RRegression.getPoints()));
        System.out.println("window query:" + RRegression.windowQuery(mbrs));
        System.out.println("knn query:" + RRegression.knnQuery(knnPoints, k));
        System.out.println("insert:" + RRegression.insert(insertedPoints));
    }

    public static void testRclassification(String s) {
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
//            "D:\\datasets\\RLRtree\\raw\\uniform_10000_1_2_.csv"
//            "D:\\datasets\\RLRtree\\raw\\uniform_2000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_4000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_8000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_16000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_32000000_1_2_.csv"
//            "D:\\datasets\\RLRtree\\raw\\uniform_64000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_100000000_1_2_.csv",
            "D:\\datasets\\RLRtree\\raw\\real_east.csv"
//            "D:\\datasets\\RLRtree\\raw\\uniform_80000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_2000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_4000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_8000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_16000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_32000000_1_2_.csv"
//            "D:\\datasets\\RLRtree\\raw\\uniform_64000000_1_2_.csv"
    );

    public static void main(String[] args) {
        datasets.forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                System.out.println(s);
//                testZRtree(s);
//                testHRtree(s);
                testPRegression(s);
                testPclassification(s);
                testRRegression(s);
                testRclassification(s);
            }
        });
    }
}
