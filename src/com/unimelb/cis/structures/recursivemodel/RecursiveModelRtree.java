package com.unimelb.cis.structures.recursivemodel;

import com.unimelb.cis.Curve;
import com.unimelb.cis.node.LeafModel;
import com.unimelb.cis.node.Model;
import com.unimelb.cis.node.NonLeafModel;
import com.unimelb.cis.node.Point;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.Logistic;

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
public class RecursiveModelRtree {

    int threshold;

    String curveType;

    int pageSize;

    Model root;

    public RecursiveModelRtree(int threshold, String curveType, int pageSize) {
        this.threshold = threshold;
        this.curveType = curveType;
        this.pageSize = pageSize;
    }

    /**
     * step 1
     *
     * @return
     */
    public void build(String path, String algorithmName) {
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
            root = new LeafModel(-1, algorithmName);
        } else {
            root = new NonLeafModel(-1, 100, algorithmName, threshold);
        }
        System.out.println("Root:" + root.getIndex());
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

    public void pointQuery(List<Point> points) {
        if(root != null) {
            root.pointQuery(points);
        }
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
    public static void main(String[] args) throws ParseException {
        List<String> all = new ArrayList<>();
        all.addAll(regs);
        all.addAll(clas);
        for (int i = 0; i < all.size(); i++) {
            System.out.println("---------------" + all.get(i) + "---------------");
            RecursiveModelRtree recursiveModelRtree = new RecursiveModelRtree(10000, "H", 100);
            recursiveModelRtree.build("D:\\datasets\\RLRtree\\raw\\normal_160000_1_2_.csv", all.get(i));
            recursiveModelRtree.pointQuery(recursiveModelRtree.root.getChildren());
            System.out.println(pageAccess);
            pageAccess = 0;
        }
    }

    public static List<String> clas = Arrays.asList("Logistic", "NaiveBayes", "MultilayerPerceptron");
    public static List<String> regs = Arrays.asList("LinearRegression");

    public static int pageAccess = 0;

}