package com.unimelb.cis.structures.recursivemodel;

import com.unimelb.cis.Curve;
import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.*;
import com.unimelb.cis.structures.IRtree;
import com.unimelb.cis.utils.ExpReturn;
import weka.classifiers.Classifier;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static com.unimelb.cis.CSVFileReader.read;

public class OriginalRecursiveModel extends Model {

    String curveType;

    int pageSize;

    Model root;

    String algorithm;

    boolean rankspace;

    public OriginalRecursiveModel(int index, String name, boolean rankspace) {
        super(index, name);
        this.rankspace = rankspace;
    }


    /**
     * train the first model according to the length of stages.get(0)
     */

    public static void main(String[] args) {

        /**
         * 0.421375
         * 4.3944125
         *
         * 0.06645625
         * 40.6776125
         */
        String dataset = "/Users/guanli/Documents/datasets/RLRtree/raw/skewed_160000_9_2_.csv";
        OriginalRecursiveModel originalRecursiveModel = new OriginalRecursiveModel(0, "Z", true);
        originalRecursiveModel.curveType = "Z";
        originalRecursiveModel.buildRtree(dataset);
        System.out.println(originalRecursiveModel);
        originalRecursiveModel.points.forEach(point -> originalRecursiveModel.pointQuery(point));
        System.out.println(originalRecursiveModel.correctNum / 160000.0);
        System.out.println(originalRecursiveModel.pageaccess / 160000.0);
        System.out.println(originalRecursiveModel.maxErr);
        System.out.println(originalRecursiveModel.minErr);

        OriginalRecursiveModel originalRecursiveModel1 = new OriginalRecursiveModel(0, "Z", false);
        originalRecursiveModel1.curveType = "Z";
        originalRecursiveModel1.buildRtree(dataset);
        System.out.println(originalRecursiveModel1);
        originalRecursiveModel1.points.forEach(point -> originalRecursiveModel1.pointQuery(point));
        System.out.println(originalRecursiveModel1.correctNum / 160000.0);
        System.out.println(originalRecursiveModel1.pageaccess / 160000.0);
        System.out.println(originalRecursiveModel1.maxErr);
        System.out.println(originalRecursiveModel1.minErr);
    }

    List<List<List<Point>>> tmp_records = new ArrayList<>();
    List<Integer> stages = Arrays.asList(1, 100, 1600);  // the last value should be the number of pages
    String name = "MultilayerPerceptron";
    String type = "MDM";
    List<Point> points;
    List<List<Classifier>> index = new ArrayList<>();


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
        points = Curve.getPointByCurve(points, this.curveType, rankspace);

//        System.out.println("Root:" + root.getIndex());

        tmp_records.add(Arrays.asList(points));

        for (int i = 0; i < stages.size() - 1; i++) {
            int lengthOfStage = stages.get(i);

            int lengthOfNextStage = stages.get(i + 1);

            List<Classifier> indexForStages = new ArrayList<>();
            List<List<Point>> temp = new ArrayList<>();
            for (int k = 0; k < lengthOfNextStage; k++) {
                temp.add(new ArrayList<>());
            }
            for (int j = 0; j < lengthOfStage; j++) {
                List<Point> data = tmp_records.get(i).get(j);

                int denominator = points.size() / lengthOfNextStage + (points.size() % lengthOfNextStage == 0 ? 0 : 1);

                for (int k = 0; k < data.size(); k++) {
                    data.get(k).setIndex(data.get(k).getCurveValueOrder() / denominator);
                }
                Instances instances = getInstances("MultilayerPerceptron", data, "MDM");
                Classifier classifier = getModels("MultilayerPerceptron");

                indexForStages.add(classifier);

                train(classifier, instances);
                List<Double> results = getPredVals(classifier, instances);

                for (int k = 0; k < results.size(); k++) {
                    int predictedVAl = results.get(k).intValue();
                    if (predictedVAl < 0) {
                        predictedVAl = 0;
                    }
                    if (predictedVAl >= lengthOfNextStage) {
                        predictedVAl = lengthOfNextStage - 1;
                    }
                    temp.get(predictedVAl).add(data.get(k));
                }
            }
            tmp_records.add(temp);
            index.add(indexForStages);
        }
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }


    @Override
    public void build() {

    }

    int correctNum = 0;
    int pageaccess = 0;
    int maxErr = Integer.MIN_VALUE;
    int minErr = Integer.MAX_VALUE;

    @Override
    public ExpReturn pointQuery(Point point) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();

        List<Point> points = new ArrayList<>();
        points.add(point);
        Instances instances = getInstances(name, points, type);

        int predictedVal = 0;
        for (int i = 0; i < stages.size() - 1; i++) {
            List<Classifier> classifiers = index.get(i);
            Classifier classifier = classifiers.get(predictedVal);

            List<Double> results = getPredVals(classifier, instances);
            predictedVal = results.get(0).intValue();
            if (predictedVal < 0) {
                predictedVal = 0;
            }
            if (predictedVal >= stages.get(i + 1)) {
                predictedVal = stages.get(i + 1) - 1;
            }
        }
        if (point.getIndex() == predictedVal) {
            correctNum++;
        }
        if ((predictedVal - point.getIndex()) < minErr) {
            minErr = predictedVal - point.getIndex();
        }
        if ((predictedVal - point.getIndex()) > maxErr) {
            maxErr = predictedVal - point.getIndex();
        }

//        pageaccess += Math.log(Math.abs(predictedVal - point.getIndex()) * 2 + 1)/Math.log(2) + 1;
        pageaccess += Math.abs(predictedVal - point.getIndex()) * 2 + 1;

//        System.out.println(point + " " + predictedVal);
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn pointQuery(List<Point> points) {
        return null;
    }

    @Override
    public ExpReturn windowQuery(Mbr window) {
        return null;
    }

    @Override
    public ExpReturn windowQueryByScanAll(Mbr window) {
        return null;
    }

    @Override
    public ExpReturn insert(Point point) {
        return null;
    }

    @Override
    public ExpReturn insert(List<Point> points) {
        return null;
    }
}
