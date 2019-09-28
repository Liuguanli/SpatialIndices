package com.unimelb.cis.structures.recursivemodel;

import com.unimelb.cis.Curve;
import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.LeafModel;
import com.unimelb.cis.node.Model;
import com.unimelb.cis.node.NonLeafModel;
import com.unimelb.cis.node.Point;
import com.unimelb.cis.structures.IRtree;
import com.unimelb.cis.utils.ExpReturn;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.unimelb.cis.CSVFileReader.read;

/**
 * first we get ZRtree or only the points with Z value and cal the last level index
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
        points = Curve.getPointByCurve(points, this.curveType);
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
    public ExpReturn pointQuery(Point point) {
        return pointQuery(Arrays.asList(point));
    }

    @Override
    public boolean buildRtree(String path) {
        List<String> lines = read(path);
        points = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Point point = new Point(line);
            points.add(point);
        }
        points = Curve.getPointByCurve(points, this.curveType);
        int classNum = points.size() / threshold;
        if (classNum <= 1) {
            root = new LeafModel(-1, pageSize, algorithm);
        } else {
            root = new NonLeafModel(-1, pageSize, algorithm, threshold);
        }
//        System.out.println("Root:" + root.getIndex());
        root.setChildren(points);
        root.build();
        return true;
    }

    @Override
    public boolean buildRtree(List<Point> res) {
        this.points = res;
        int classNum = points.size() / threshold;
        if (classNum <= 1) {
            root = new LeafModel(-1, pageSize, algorithm);
        } else {
            root = new NonLeafModel(-1, pageSize, algorithm, threshold);
        }
//        System.out.println("Root:" + root.getIndex());
        root.setChildren(points);
        root.build();
        return true;
    }

    public ExpReturn windowQuery(Mbr window) {
        if (root != null) {
            return root.windowQuery(window);
        }
        return null;
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
        List<String> all = new ArrayList<>();
        all.addAll(regs);
        all.addAll(clas);

        for (int i = 0; i < all.size(); i++) {
            long begin = System.nanoTime();
            System.out.println("---------------" + all.get(i) + "---------------");
            RecursiveModelRtree recursiveModelRtree = new RecursiveModelRtree(10000, "H", 100, all.get(i));
            recursiveModelRtree.buildRtree("/Users/guanli/Documents/datasets/RLRtree/raw/uniform_10000_1_2_.csv");
//            recursiveModelRtree.build("D:\\datasets\\RLRtree\\raw\\normal_160000_1_2_.csv", all.get(i));
            ExpReturn expReturn = recursiveModelRtree.pointQuery(recursiveModelRtree.root.getChildren());
            ExpReturn expReturn1 = recursiveModelRtree.windowQuery(new Mbr(0.1f, 0.1f, 0.6f, 0.6f));
            long end = System.nanoTime();
            System.out.println(end - begin);
            System.out.println(expReturn);
            System.out.println(expReturn1);
//            break;
        }
    }

    public static List<String> clas = Arrays.asList("NaiveBayes", "MultilayerPerceptron");
    public static List<String> regs = Arrays.asList("LinearRegression");

}
