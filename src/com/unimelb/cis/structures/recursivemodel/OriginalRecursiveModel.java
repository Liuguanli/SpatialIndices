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

    public OriginalRecursiveModel(int index, String name) {
        super(index, name);
    }


    /**
     * train the first model according to the length of stages.get(0)
     */
    public void init() {


        for (int i = 0; i < stages.size(); i++) {


        }
    }

    public static void main(String[] args) {
        String dataset = "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_160000_1_2_.csv";
        OriginalRecursiveModel originalRecursiveModel = new OriginalRecursiveModel(0, "Z");
        originalRecursiveModel.curveType = "Z";
        originalRecursiveModel.buildRtree(dataset);
        System.out.println(originalRecursiveModel);
        originalRecursiveModel.points.forEach(point -> originalRecursiveModel.pointQuery(point));
    }


    List<List<List<Point>>> tmp_records = new ArrayList<>();
    List<Integer> stages = Arrays.asList(1, 50, 1600);  // the last value should be the number of pages
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
        points = Curve.getPointByCurve(points, this.curveType);

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
        System.out.println(point + " " + predictedVal);
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
