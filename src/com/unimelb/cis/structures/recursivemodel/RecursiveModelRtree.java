package com.unimelb.cis.structures.recursivemodel;

import com.unimelb.cis.curve.Curve;
import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.*;
import com.unimelb.cis.structures.IRtree;
import com.unimelb.cis.utils.ExpReturn;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.unimelb.cis.CSVFileReader.read;

/**
 * first we get ZRRtree or only the points with Z value and cal the last level index
 * <p>
 * then Use the model to classify to sub models. Then generate new points with new index!!!
 * <p>
 * All the model in this part should be stored to later prediction and insertion.
 * <p>
 * The model stop Until the sub data set less than a threshold.
 */
public class RecursiveModelRtree extends IRtree {

    int threshold;

    String curveType;

    int pageSize;

    Model root;

    String algorithm;

    public RecursiveModelRtree(int threshold, String curveType, int pageSize, String algorithm) {
        this.threshold = threshold;
        this.curveType = curveType;
        this.pageSize = pageSize;
        this.algorithm = algorithm;
    }

    /**
     * step 1
     *
     * @return
     */
    public void build(String path) {
        List<String> lines = read(path);
        List<Point> points = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Point point = new Point(line);
            points.add(point);
        }
        points = Curve.getPointByCurve(points, this.curveType, true);
        int classNum = points.size() / threshold;
        if (classNum <= 1) {
            root = new LeafModel(-1, pageSize, algorithm);
        } else {
            root = new NonLeafModel(-1, pageSize, algorithm, threshold);
        }
//        System.out.println("Root:" + root.getIndex());
        root.setChildren(points);
        root.build();
    }

//    /**
//     * step 3
//     */
//    public Classifier getModels(String name) {
//        Classifier classifier = null;
//        switch (name) {
//            case "Logistic":
//                classifier = new Logistic();
//                break;
//            case "LinearRegression":
//                classifier = new LinearRegression();
//                break;
//        }
//        return classifier;
//    }

    public ExpReturn pointQuery(List<Point> points) {
        if (root != null) {
            return root.pointQuery(points);
        }
        return null;
    }

    @Override
    public ExpReturn knnQuery(Point point, int k) {
        // 4 side * side = 4k/data set size
        float knnquerySide = (float) Math.sqrt((float) k / points.size());
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        while (true) {
            Mbr window = Mbr.getMbr(point, knnquerySide);
            ExpReturn tempExpReturn = windowQuery(window);
            List<Point> tempResult = tempExpReturn.result;
            if (tempResult.size() >= k) {
                tempResult.sort((o1, o2) -> {
                    double d1 = point.getDist(o1);
                    double d2 = point.getDist(o2);
                    if (d1 > d2) {
                        return 1;
                    } else if (d1 < d2) {
                        return -1;
                    } else {
                        return 0;
                    }
                });
                if (tempResult.get(k - 1).getDist(point) <= knnquerySide) {
                    expReturn.result = tempResult.subList(0, k);
                    expReturn.pageaccess += tempExpReturn.pageaccess;
                    break;
                }
            }
            knnquerySide = knnquerySide * 2;
        }
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn pointQuery(Point point) {
        return pointQuery(Arrays.asList(point));
    }

    @Override
    public ExpReturn buildRtree(String path) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        List<String> lines = read(path);
        points = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Point point = new Point(line);
            points.add(point);
        }
        dim = points.get(0).getDim();
        points = Curve.getPointByCurve(points, this.curveType, true);
        int classNum = points.size() / threshold;
        if (classNum <= 1) {
            root = new LeafModel(0, pageSize, algorithm);
        } else {
            root = new NonLeafModel(0, pageSize, algorithm, threshold);
        }
//        System.out.println("Root:" + root.getIndex());
        root.setChildren(points);
        root.build();
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn buildRtree(List<Point> res) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        this.points = res;
        int classNum = points.size() / threshold;
        dim = points.get(0).getDim();
        if (classNum <= 1) {
            root = new LeafModel(-1, pageSize, algorithm);
        } else {
            root = new NonLeafModel(-1, pageSize, algorithm, threshold);
        }
//        System.out.println("Root:" + root.getIndex());
        root.setChildren(points);
        root.build();
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    public ExpReturn windowQueryByScanAll(Mbr window) {
        if (root != null) {
            return root.windowQueryByScanAll(window);
        }
        return null;
    }

    @Override
    public ExpReturn windowQuery(List<Mbr> windows) {
        ExpReturn expReturn = new ExpReturn();
        windows.forEach(mbr -> {
            ExpReturn temp = windowQuery(mbr);
            expReturn.time += temp.time;
            expReturn.pageaccess += temp.pageaccess;
            expReturn.accuracy += temp.accuracy;
        });
        expReturn.time /= windows.size();
        expReturn.pageaccess /= windows.size();
        expReturn.accuracy /= windows.size();
        return expReturn;
    }

    @Override
    public ExpReturn knnQuery(List<Point> points, int k) {
        ExpReturn expReturn = new ExpReturn();
        points.forEach(point -> {
            ExpReturn temp = knnQuery(point, k);
            expReturn.time += temp.time;
            expReturn.pageaccess += temp.pageaccess;
        });
        expReturn.time /= points.size();
        expReturn.pageaccess /= points.size();
        return expReturn;
    }

    public ExpReturn windowQuery(Mbr window) {
        if (root != null) {
            ExpReturn expReturn = root.windowQuery(window);
            ExpReturn accurate = windowQueryByScanAll(window);
            expReturn.accuracy = (double) expReturn.result.size() / accurate.result.size();
            return expReturn;
        }
        return null;
    }

    @Override
    public ExpReturn insert(Point point) {
        return insert(Arrays.asList(point));
    }

    @Override
    public NonLeafNode buildRtreeAfterTuning(String path, int dim, int level) {
        return null;
    }

    @Override
    public ExpReturn insert(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        points.forEach(point -> {
            expReturn.pageaccess += root.insert(point).pageaccess;
        });
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }


    /**
     * add data
     * https://waikato.github.io/weka-wiki/formats_and_processing/creating_arff_file/
     * <p>
     * <p>
     * TODO run deom
     * TODO test different data size  10000, 160,000  100 0000  200 0000
     * TODO MBR
     *
     * @param args
     * @throws ParseException
     */
    public static void main(String[] args) {
        long begin = System.nanoTime();
        RecursiveModelRtree recursiveModelRtree = new RecursiveModelRtree(10000, "H", 100, "NaiveBayes");
        recursiveModelRtree.buildRtree("/Users/guanli/Documents/datasets/RLRtree/raw/uniform_10000_1_2_.csv");
//            recursiveModelRtree.buildRtree("D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_2_.csv");
//            recursiveModelRtree.build("D:\\datasets\\RLRtree\\raw\\normal_160000_1_2_.csv", all.get(i));
        ExpReturn expReturn = recursiveModelRtree.pointQuery(recursiveModelRtree.root.getChildren());
        System.out.println("knn query:" + recursiveModelRtree.knnQuery(new Point(0.5f, 0.5f), 1));
//            ExpReturn expReturn1 = recursiveModelRtree.windowQuery(new Mbr(0.1f, 0.1f, 0.6f, 0.6f));
//            long end = System.nanoTime();
//            System.out.println(end - begin);
        System.out.println(expReturn);
//            System.out.println(expReturn1);
        System.out.println("insert" + recursiveModelRtree.insert(new Point(0.5f, 0.5f)));
//            break;

//        PrintStream var10000 = System.out;
//        String var10001 = "MultilayerPerceptron";
//        var10000.println("---------------" + (String)var10001 + "---------------");
//        RecursiveModelRtree recursiveModelRtree = new RecursiveModelRtree(10000, "H", 100, var10001);
//        recursiveModelRtree.buildRtree("D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_2_.csv");
//        var10000 = System.out;
//        System.out.println("build finish");
//        System.out.println("point query"+recursiveModelRtree.pointQuery(recursiveModelRtree.points));
//        ExpReturn var6 = recursiveModelRtree.insert(new Point(0.5F, 0.5F));
//        var10000.println("insert" + var6);

    }


}
