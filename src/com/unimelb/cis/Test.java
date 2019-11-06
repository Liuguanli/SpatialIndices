package com.unimelb.cis;

import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.Point;
import com.unimelb.cis.structures.IRtree;
import com.unimelb.cis.structures.hrtree.HRRtree;
import com.unimelb.cis.structures.partitionmodel.PartitionModelByPred;
import com.unimelb.cis.structures.partitionmodel.PartitionModelRtree;
import com.unimelb.cis.structures.partitionmodel.RecursivePartition;
import com.unimelb.cis.structures.peanotree.PeanoRtree;
import com.unimelb.cis.structures.recursivemodel.OriginalRecursiveModel;
import com.unimelb.cis.structures.recursivemodel.RecursiveModelRtree;
import com.unimelb.cis.structures.unsupervisedPartition.UnsupervisedPartitionModel;
import com.unimelb.cis.structures.zrtree.ZRRtree;

import java.util.Arrays;
import java.util.List;

public class Test {


    public static void testZRtree(String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
        ZRRtree zRRtree = new ZRRtree(100);
        System.out.println("ZRRtree:");
        System.out.println("build finish:" + zRRtree.buildRtree(s).time);
        System.out.println("point query:" + zRRtree.pointQuery(zRRtree.getPoints()));
        System.out.println("window query:" + zRRtree.windowQuery(mbrs));
        System.out.println("knn query:" + zRRtree.knnQuery(knnPoints, k));
        System.out.println("insert:" + zRRtree.insert(insertedPoints));
    }

    public static void testHRtree(String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
        HRRtree hRRtree = new HRRtree(100);
        System.out.println("HRRtree:");
        System.out.println("build finish:" + hRRtree.buildRtree(s).time);
        System.out.println("point query:" + hRRtree.pointQuery(hRRtree.getPoints()));
        System.out.println("window query:" + hRRtree.windowQuery(mbrs));
        System.out.println("knn query:" + hRRtree.knnQuery(knnPoints, k));
        System.out.println("insert:" + hRRtree.insert(insertedPoints));
    }

    public static void testPeanoRtree(String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
        PeanoRtree peanoRtree = new PeanoRtree(100);
        System.out.println("PeanoRtree:");
        System.out.println("build finish:" + peanoRtree.buildRtree(s).time);
        System.out.println("point query:" + peanoRtree.pointQuery(peanoRtree.getPoints()));
        System.out.println("window query:" + peanoRtree.windowQuery(mbrs));
        System.out.println("knn query:" + peanoRtree.knnQuery(knnPoints, k));
        System.out.println("insert:" + peanoRtree.insert(insertedPoints));
    }

    public static void testPRegression(String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
        PartitionModelRtree PRegression = new PartitionModelRtree(10000, "H", 100, "LinearRegression");
        System.out.println("partition:" + "LinearRegression");
        System.out.println("build finish:" + PRegression.buildRtree(s).time);
        System.out.println("point query:" + PRegression.pointQuery(PRegression.getPoints()));
        System.out.println("window query:" + PRegression.windowQuery(mbrs));
        System.out.println("windowQueryByScanAll query:" + PRegression.windowQueryByScanAll(mbrs));
        System.out.println("knn query:" + PRegression.knnQuery(knnPoints, k));
        System.out.println("insert:" + PRegression.insert(insertedPoints));
    }

    public static void testPRegressionPcurve(String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
        PartitionModelRtree PRegression = new PartitionModelRtree(10000, "P", 100, "LinearRegression");
        System.out.println("partition:" + "LinearRegression" + " Pcurve");
        System.out.println("build finish:" + PRegression.buildRtree(s).time);
        System.out.println("point query:" + PRegression.pointQuery(PRegression.getPoints()));
        System.out.println("window query:" + PRegression.windowQuery(mbrs));
        System.out.println("windowQueryByScanAll query:" + PRegression.windowQueryByScanAll(mbrs));
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
        System.out.println("windowQueryByScanAll query:" + Pclassification.windowQueryByScanAll(mbrs));
        System.out.println("knn query:" + Pclassification.knnQuery(knnPoints, k));
        System.out.println("insert:" + Pclassification.insert(insertedPoints));
    }

    public static void testPclassificationPcurve(String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
        PartitionModelRtree Pclassification = new PartitionModelRtree(10000, "P", 100, "NaiveBayes");
//        System.out.println("partition:" + "NaiveBayes");
        System.out.println("partition:" + "NaiveBayes" + " Pcurve");
        System.out.println("build finish:" + Pclassification.buildRtree(s).time);
        System.out.println("point query:" + Pclassification.pointQuery(Pclassification.getPoints()));
        System.out.println("window query:" + Pclassification.windowQuery(mbrs));
        System.out.println("windowQueryByScanAll query:" + Pclassification.windowQueryByScanAll(mbrs));
        System.out.println("knn query:" + Pclassification.knnQuery(knnPoints, k));
        System.out.println("insert:" + Pclassification.insert(insertedPoints));
    }

    public static void testRRegression(String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
        RecursiveModelRtree RRegression = new RecursiveModelRtree(10000, "H", 100, "LinearRegression");
        System.out.println("Recursive:" + "LinearRegression");
        System.out.println("build finish:" + RRegression.buildRtree(s).time);
//        System.out.println("point query:" + RRegression.pointQuery(RRegression.getPoints()));
        System.out.println("window query:" + RRegression.windowQuery(mbrs));
//        System.out.println("knn query:" + RRegression.knnQuery(knnPoints, k));
//        System.out.println("insert:" + RRegression.insert(insertedPoints));
    }

    public static void testRclassification(String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
        RecursiveModelRtree Rclassification = new RecursiveModelRtree(10000, "H", 100, "NaiveBayes");
        System.out.println("Recursive:" + "NaiveBayes");
        System.out.println("build finish:" + Rclassification.buildRtree(s).time);
        System.out.println("point query:" + Rclassification.pointQuery(Rclassification.getPoints()));
//        System.out.println("window query:" + Rclassification.windowQuery(mbrs));
//        System.out.println("knn query:" + Rclassification.knnQuery(knnPoints, k));
//        System.out.println("insert:" + Rclassification.insert(insertedPoints));
    }

    public static void testOriginalRecursiveModel(String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
        System.out.println("OriginalRecursive");
        OriginalRecursiveModel originalRecursiveModel1 = new OriginalRecursiveModel(100, false, "Z", 2);
        System.out.println(originalRecursiveModel1.buildRtree(s));
        System.out.println("pointQuery:" + originalRecursiveModel1.pointQuery(originalRecursiveModel1.getPoints()));
        System.out.println("windowQuery:" + originalRecursiveModel1.windowQuery(mbrs));
        System.out.println("windowQueryByScanAll:" + originalRecursiveModel1.windowQueryByScanAll(mbrs));
        System.out.println("knn query:" + originalRecursiveModel1.knnQuery(knnPoints, k));
        System.out.println("insert:" + originalRecursiveModel1.insert(insertedPoints));
    }

    public static void testKMeans(String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
        UnsupervisedPartitionModel unsupervisedPartitionModel = new UnsupervisedPartitionModel(5000, "H", 100, "MultilayerPerceptron", 10);
        System.out.println("Partition:" + "KMeans");
        System.out.println("build finish:" + unsupervisedPartitionModel.buildRtree(s).time);
//        unsupervisedPartitionModel.visualize(600, 600, unsupervisedPartitionModel.getmbrFigures()).saveMBR("kmeans_160000.png");
        System.out.println("point query:" + unsupervisedPartitionModel.pointQuery(unsupervisedPartitionModel.getPoints()));
        System.out.println("window query:" + unsupervisedPartitionModel.windowQuery(mbrs));
        System.out.println("windowQueryByScanAll query:" + unsupervisedPartitionModel.windowQueryByScanAll(mbrs));
        System.out.println("knn query:" + unsupervisedPartitionModel.knnQuery(knnPoints, k));
        System.out.println("insert:" + unsupervisedPartitionModel.insert(insertedPoints));
    }

    public static void testKMeansPcurve(String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
        UnsupervisedPartitionModel unsupervisedPartitionModel = new UnsupervisedPartitionModel(5000, "P", 100, "MultilayerPerceptron", 10);
        System.out.println("Partition:" + "KMeans" + " Pcurve");
        System.out.println("build finish:" + unsupervisedPartitionModel.buildRtree(s).time);
//        unsupervisedPartitionModel.visualize(600, 600, unsupervisedPartitionModel.getmbrFigures()).saveMBR("kmeans_160000.png");
        System.out.println("point query:" + unsupervisedPartitionModel.pointQuery(unsupervisedPartitionModel.getPoints()));
        System.out.println("window query:" + unsupervisedPartitionModel.windowQuery(mbrs));
        System.out.println("windowQueryByScanAll query:" + unsupervisedPartitionModel.windowQueryByScanAll(mbrs));
        System.out.println("knn query:" + unsupervisedPartitionModel.knnQuery(knnPoints, k));
        System.out.println("insert:" + unsupervisedPartitionModel.insert(insertedPoints));
    }

    public static void testPartitionPred(String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
        PartitionModelByPred partitionModelByPred = new PartitionModelByPred(10000, "H", 100, "MultilayerPerceptron");
        System.out.println("Partition:" + "Pred");
        System.out.println("build finish:" + partitionModelByPred.buildRtree(s).time);
        partitionModelByPred.visualize(600, 600, partitionModelByPred.getmbrFigures()).saveMBR("testPartitionPred_1000000.png");
//        System.out.println("point query1:" + partitionModelByPred.pointQueryForExp());
        System.out.println("point query2:" + partitionModelByPred.pointQuery(partitionModelByPred.getPoints()));
//        System.out.println("window query:" + unsupervisedPartitionModel.windowQuery(mbrs));
//        System.out.println("knn query:" + unsupervisedPartitionModel.knnQuery(knnPoints, k));
//        System.out.println("insert:" + unsupervisedPartitionModel.insert(insertedPoints));
    }

    public static void testRecursivePartition(String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
        int maxPartitionNumEachDim = 256;  // LinearRegression
        RecursivePartition recursivePartition = new RecursivePartition("H", maxPartitionNumEachDim, 5000, "MultilayerPerceptron");
        System.out.println("Partition:" + "Recursive");
        System.out.println("build finish:" + recursivePartition.buildRtree(s).time);
        System.out.println("point query:" + recursivePartition.pointQuery(recursivePartition.getPoints()));
        System.out.println("window query:" + recursivePartition.windowQuery(mbrs));
        System.out.println("windowQueryByScanAll query:" + recursivePartition.windowQueryByScanAll(mbrs));
        System.out.println("knn query:" + recursivePartition.knnQuery(knnPoints, k));
        System.out.println("insert:" + recursivePartition.insert(insertedPoints));
    }

    public static void testIRtree(IRtree iRtree, String s, List<Point> insertedPoints, List<Mbr> mbrs, List<Point> knnPoints, int k) {
        System.out.println("------------------------------------------------");
//        int maxPartitionNumEachDim = 8;
//        RecursivePartition recursivePartition = new RecursivePartition(maxPartitionNumEachDim, 10000, "LinearRegression"); // time=93329793368 pageaccess=7911393
        System.out.println("Partition:" + "Pred");
        System.out.println("build finish:" + iRtree.buildRtree(s).time);
        System.out.println("point query:" + iRtree.pointQuery(iRtree.getPoints()));
        System.out.println("window query:" + iRtree.windowQuery(mbrs));
        System.out.println("knn query:" + iRtree.knnQuery(knnPoints, k));
        System.out.println("insert:" + iRtree.insert(insertedPoints));
    }


    static List<String> datasets = Arrays.asList(
//            "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_1000000_1_2_.csv"
//            "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_1000000_1_3_.csv",
//            "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_1000000_1_4_.csv",
//            "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_1000000_1_5_.csv",
//            "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_1000000_1_6_.csv"
//            "D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_3_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_4_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_5_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_6_.csv"
//            "D:\\datasets\\RLRtree\\raw\\uniform_2000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_4000000_1_2_.csv",
            "D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_2_.csv"
//            "D:\\datasets\\RLRtree\\raw\\skewed_8000000_9_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_16000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_32000000_1_2_.csv"
//            "D:\\datasets\\RLRtree\\raw\\uniform_64000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_100000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\real_east.csv"
//            "D:\\datasets\\RLRtree\\raw\\uniform_8000000_1_2_.csv"
//            "D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_2000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_4000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_8000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_16000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_32000000_1_2_.csv"
//            "D:\\datasets\\RLRtree\\raw\\uniform_64000000_1_2_.csv"
    );

    static List<String> datasets1 = Arrays.asList(
            "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_1000000_1_2_.csv"
//            "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_2000000_1_2_.csv"
//            "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_4000000_1_2_.csv"
//            "D:\\datasets\\RLRtree\\raw\\real_east.csv",
//              "D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_2_.csv"
//            "D:\\datasets\\RLRtree\\raw\\uniform_2000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_4000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_8000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_16000000_1_2_.csv",
//            "D:\\datasets\\RLRtree\\raw\\uniform_32000000_1_2_.csv"
//            "D:\\datasets\\RLRtree\\raw\\uniform_64000000_1_2_.csv"
//            "D:\\datasets\\RLRtree\\raw\\uniform_128000000_1_2_.csv"
    );

    public static void main(String[] args) {

        int k = 1;
        int dim = 2;

        List<Float> sides = Arrays.asList(0.04f);

        datasets.forEach(s -> {
            List<Point> knnPoints = Point.getPoints(10, 2);
            List<Point> insertedPoints = Point.getPoints(10000, 2);
            System.out.println(s);

            sides.forEach(aFloat -> {
                List<Mbr> mbrs = Mbr.getMbrs(aFloat, 10, 2);
//                    testPclassification(s, insertedPoints, mbrs, knnPoints, k);
//                    testZRtree(s, insertedPoints, mbrs, knnPoints, k);
//                testOriginalRecursiveModel(s, insertedPoints, mbrs, knnPoints, k);
//                    pointQuery:time=14455
//                    pageaccess=2.317

//                testZRtree(s, insertedPoints, mbrs, knnPoints, k);
//                testHRtree(s, insertedPoints, mbrs, knnPoints, k);
//                testPeanoRtree(s, insertedPoints, mbrs, knnPoints, k);
//                    testPartitionPred(s, insertedPoints, mbrs, knnPoints, k);
//                testKMeans(s, insertedPoints, mbrs, knnPoints, k);
//                testKMeansPcurve(s, insertedPoints, mbrs, knnPoints, k);
//                    point query:time=11847
//                    pageaccess=2.204226
//                testPRegression(s, insertedPoints, mbrs, knnPoints, k);
//                testPRegressionPcurve(s, insertedPoints, mbrs, knnPoints, k);

//                testPclassification(s, insertedPoints, mbrs, knnPoints, k);
//                testPclassificationPcurve(s, insertedPoints, mbrs, knnPoints, k);
//                    testRRegression(s, insertedPoints, mbrs, knnPoints, k);
//                    testRclassification(s, insertedPoints, mbrs, knnPoints, k);
//                    testKMeans(s, insertedPoints, mbrs, knnPoints, k);
                testRecursivePartition(s, insertedPoints, mbrs, knnPoints, k);
                // Partition  MLP
//                    point query:time=19549
//                    pageaccess=3.1358
//                     Partition NB
//                    point query:time=32108
//                    pageaccess=1.3873

            });
        });

//        for (int i = 1; i < datasets.size(); i++) {
//            String s = datasets.get(i);
//            List<Point> knnPoints = Point.getPoints(10, dim + i);
//            List<Point> insertedPoints = Point.getPoints(10000, dim + i);
//            List<Mbr> mbrs = Mbr.getMbrs(0.04f , 10, dim + i);
//            System.out.println(s);
////            testZRtree(s, insertedPoints, mbrs, knnPoints, k);
////            testHRtree(s, insertedPoints, mbrs, knnPoints, k);
////            testPRegression(s, insertedPoints, mbrs, knnPoints, k);
////            testPclassification(s, insertedPoints, mbrs, knnPoints, k);
//            testRRegression(s, insertedPoints, mbrs, knnPoints, k);
////            testRclassification(s, insertedPoints, mbrs, knnPoints, k);
//        }
    }
}
