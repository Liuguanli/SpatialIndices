package com.unimelb.cis.structures.recursivemodel;

import com.unimelb.cis.Curve;
import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.LeafNode;
import com.unimelb.cis.node.Model;
import com.unimelb.cis.node.NonLeafNode;
import com.unimelb.cis.node.Point;
import com.unimelb.cis.structures.IRtree;
import com.unimelb.cis.utils.ExpReturn;
import weka.classifiers.Classifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static com.unimelb.cis.CSVFileReader.read;
import static com.unimelb.cis.ZCurve.getZcurve;

public class OriginalRecursiveModel extends IRtree {

    String curveType;

    Model root;

    String algorithm;

    boolean rankspace;

    List<Integer> stages;  // the last value should be the number of pages

    List<LeafNode> leafNodes = new ArrayList<>();

    public OriginalRecursiveModel(int pagesize, boolean rankspace, List<Integer> stages, String curveType) {
        super(pagesize);
        this.rankspace = rankspace;
        this.stages = stages;
        this.curveType = curveType;
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
        String dataset = "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_160000_1_2_.csv";
        OriginalRecursiveModel originalRecursiveModel = new OriginalRecursiveModel(100, true, Arrays.asList(1, 100, 1600),"Z");
        System.out.println(originalRecursiveModel.buildRtree(dataset));
        System.out.println(originalRecursiveModel.pointQuery(originalRecursiveModel.points));

//        time=301473632
//        pageaccess=247383

        OriginalRecursiveModel originalRecursiveModel1 = new OriginalRecursiveModel(100, false, Arrays.asList(1, 100, 1600), "Z");
        System.out.println(originalRecursiveModel1.buildRtree(dataset));
        System.out.println(originalRecursiveModel1.pointQuery(originalRecursiveModel1.points));

    }

    List<List<List<Point>>> tmp_records = new ArrayList<>();
    String name = "MultilayerPerceptron";
    String type = "MDM";
    List<List<Classifier>> index = new ArrayList<>();

    List<Integer> maxErrors = new ArrayList<>();
    List<Integer> minErrors = new ArrayList<>();


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
        dim = points.get(0).getDim();
        LeafNode leafNode = null;
        for (int i = 0; i < points.size(); i++) {
            if (i % pagesize == 0) {
                leafNode = new LeafNode(pagesize, dim);
                leafNodes.add(leafNode);
            }
            Point point = points.get(i);
            leafNode.add(point);
            point.setParent(leafNode);
            point.setOrderInLevel(i);
            point.setIndex(i / pagesize);
        }

        tmp_records.add(Arrays.asList(points));

        int N = stages.get(stages.size() - 1);

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
//                int denominator = points.size() / lengthOfNextStage + (points.size() % lengthOfNextStage == 0 ? 0 : 1);
//                for (int k = 0; k < data.size(); k++) {
//                    data.get(k).setIndex(data.get(k).getCurveValueOrder() / denominator);
//                }
                Instances instances = prepareDataSetRegMDM(data);
                Classifier classifier = new MultilayerPerceptron();

                indexForStages.add(classifier);
                try {
                    classifier.buildClassifier(instances);
                } catch (Exception e) {
//                    e.printStackTrace();
                }
                List<Double> results = getPredVals(classifier, instances);
                int maxErr = 0;
                int minErr = 0;
                for (int k = 0; k < results.size(); k++) {

                    int predictedVAl = results.get(k).intValue() * lengthOfNextStage / N;

//                    System.out.println("predictedVAl:" + predictedVAl);

                    if (predictedVAl < 0) {
                        predictedVAl = 0;
                    }
                    if (predictedVAl >= lengthOfNextStage) {
                        predictedVAl = lengthOfNextStage - 1;
                    }
                    temp.get(predictedVAl).add(data.get(k));

                    if (i == stages.size() - 2) {
//                        System.out.println("predictedVAl:" + predictedVAl + " real index:" + data.get(k).getIndex());
                        if (predictedVAl == data.get(k).getIndex())
                            continue;
                        int error = data.get(k).getIndex() - predictedVAl;
                        if (error > 0) {
                            maxErr = Math.max(maxErr, error);
                        } else {
                            minErr = Math.min(minErr, error);
                        }
                    }
                }
                if (i == stages.size() - 2) {
//                    System.out.println(maxErr + " " + minErr);
                    maxErrors.add(maxErr);
                    minErrors.add(minErr);
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
    public ExpReturn buildRtree(List<Point> points) {
        return null;
    }

    public Instances prepareDataSetRegMDM(List<Point> points) {
        FastVector atts = new FastVector();
        atts.addElement(new Attribute("att" + 1));
        atts.addElement(new Attribute("index"));
        Instances dataSet = new Instances("tree", atts, 0);
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            double[] vals = new double[dataSet.numAttributes()];
            vals[0] = point.getCurveValue();
            vals[1] = point.getIndex();
            dataSet.add(new Instance(1.0, vals));
        }
        dataSet.setClassIndex(dataSet.numAttributes() - 1);
        return dataSet;
    }

    public List<Double> getPredVals(Classifier classifier, Instances instances) {
        List<Double> results = new ArrayList<>();
        try {
            for (int i = 0; i < instances.numInstances(); i++) {
                Instance instance = instances.instance(i);
                if (classifier == null) {
                    results.add(0.0);
                } else {
                    double value = classifier.classifyInstance(instance);
                    results.add(value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    @Override
    public ExpReturn pointQuery(Point point) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();

        int width = points.size();
        int dimension = point.getDim();
        for (int i = 0; i < dimension; i++) {
            point.getLocationOrder()[i] = (long) (point.getLocation()[i] * width);
        }

        int bitNum = (int) (Math.log(width) / Math.log(2.0)) + 1;

        Instances instances = prepareDataSetRegMDM(Arrays.asList(point));

        long zValue = getZcurve(point.getLocationOrder(), bitNum);
        point.setCurveValue(zValue);

        int maxErr = 0;
        int minErr = 0;
        int predictedVal = 0;
        int N = stages.get(stages.size() - 1);
        for (int i = 0; i < stages.size() - 1; i++) {
            List<Classifier> classifiers = index.get(i);
            Classifier classifier = classifiers.get(predictedVal);
//            System.out.println("stages:" + i + " predictedVal:" + predictedVal + " classifier:" + classifier);
            List<Double> results = getPredVals(classifier, instances);
            int lengthOfNextStage = stages.get(i + 1);
            predictedVal = results.get(0).intValue() * lengthOfNextStage / N;


            if (predictedVal < 0) {
                predictedVal = 0;
            }
            if (predictedVal >= stages.get(i + 1)) {
                predictedVal = stages.get(i + 1) - 1;
            }
            if (i == stages.size() - 2) {
                expReturn.minErr = minErr;
                expReturn.maxErr = maxErr;
                expReturn.pageaccess++;
                if (leafNodes.get(predictedVal).getChildren().contains(point)) {
                    expReturn.index = predictedVal;
                } else {
//                    System.out.println(minErr + " " + maxErr);
                    int front = Math.max(0, predictedVal + minErr);
                    int back = Math.min(stages.get(i + 1) - 1, predictedVal + maxErr);
                    int mid = (front + back) / 2;


                    while (front <= back) {
//                        System.out.println("missed " + front + " " + back);
                        LeafNode leafNode = leafNodes.get(mid);
                        if (leafNode.getMbr().contains(point)) {
                            if (leafNode.getChildren().contains(point)) {
                                expReturn.pageaccess++;
                                break;
                            }
                        }

                        if (leafNode.getChildren().get(0).getCurveValue() < point.getCurveValue()) {
                            front = mid + 1;
                        } else {
                            back = mid - 1;
                        }
                        mid = (front + back) / 2;
                    }
                }
            } else {
                maxErr = maxErrors.get(predictedVal);
                minErr = minErrors.get(predictedVal);
            }
        }
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn pointQuery(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        points.forEach(point -> {
            ExpReturn temp = pointQuery(point);
            expReturn.plus(temp);
        });
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn knnQuery(Point point, int k) {
        return null;
    }

    @Override
    public ExpReturn knnQuery(List<Point> points, int k) {
        return null;
    }

    @Override
    public ExpReturn windowQuery(Mbr window) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        List<Point> vertexes = window.getCornerPoints();

        List<Integer> results = new ArrayList<>();
        vertexes.forEach(point -> {
            ExpReturn temp = pointQuery(point);
            results.add(temp.index + temp.minErr);
            results.add(temp.index + temp.maxErr);
            expReturn.pageaccess += temp.pageaccess;
        });

        results.sort(Integer::compareTo);
        int indexLow = Math.max(0, results.get(0));
        int indexHigh = Math.min(leafNodes.size(), results.get(results.size() - 1));

        for (int i = indexLow; i < indexHigh; i++) {
            if (window.interact(leafNodes.get(i).getMbr())) {
                expReturn.pageaccess++;
                leafNodes.get(i).getChildren().forEach(point -> {
                    if (window.contains(point)) {
                        expReturn.result.add(point);
                    }
                });
            }
        }
        long end = System.nanoTime();
        expReturn.time = end - begin;
//        System.out.println("windowQuery:" + expReturn.result.size());
        return expReturn;
    }

    @Override
    public ExpReturn windowQuery(List<Mbr> windows) {
        ExpReturn expReturn = new ExpReturn();
        windows.forEach(mbr -> {
            ExpReturn temp = windowQuery(mbr);
            expReturn.time += temp.time;
            expReturn.pageaccess += temp.pageaccess;
        });
        expReturn.time /= windows.size();
        expReturn.pageaccess /= windows.size();
        return expReturn;
    }

    public ExpReturn windowQueryByScanAll(Mbr window) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        leafNodes.forEach(leafNode -> {
            if (leafNode.getMbr().interact(window)) {
                expReturn.pageaccess++;
                leafNode.getChildren().forEach(point -> expReturn.result.add(point));
            }
        });
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn insert(Point point) {
        return null;
    }

    @Override
    public NonLeafNode buildRtreeAfterTuning(String path, int dim, int level) {
        return null;
    }

    @Override
    public ExpReturn insert(List<Point> points) {
        return null;
    }
}
