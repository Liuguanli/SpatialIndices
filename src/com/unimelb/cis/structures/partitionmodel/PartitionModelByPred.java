package com.unimelb.cis.structures.partitionmodel;

import com.unimelb.cis.curve.Curve;
import com.unimelb.cis.geometry.Boundary;
import com.unimelb.cis.geometry.Line;
import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.*;
import com.unimelb.cis.structures.IRtree;
import com.unimelb.cis.utils.ExpReturn;
import weka.classifiers.Classifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.unimelb.cis.CSVFileReader.read;

public class PartitionModelByPred extends IRtree {

    private int threshold;

    private String curve;

    private int pageSize;

    private String algorithm;

    private Boundary boundary;

    Map<Integer, Model> partitionModels = new HashMap<>();
    Map<Integer, List<Point>> partitionPoints = new HashMap<>();

    Classifier classifier;

    public PartitionModelByPred(int threshold, String curve, int pageSize, String algorithm) {
        this.threshold = threshold;
        this.curve = curve;
        this.pageSize = pageSize;
        this.algorithm = algorithm;
    }

    public Comparator<Point> getComparator(int index) {
        return (o1, o2) -> {
            if (o1.getLocation()[index] > o2.getLocation()[index]) {
                return 1;
            } else if (o1.getLocation()[index] < o2.getLocation()[index]) {
                return -1;
            } else {
                return 0;
            }
        };
    }

    int modelIndex = 0;

    private LeafModel addPointsAndBuild(List<Point> points) {
        points = Curve.getPointByCurve(points, curve, true);
        LeafModel leafModel = new LeafModel(-1, pageSize, algorithm);
        leafModel.setChildren(points);
        leafModel.build();
        return leafModel;
    }

    public Boundary getPartition(List<Point> points, int dim, int length) {
        if (dim == 0) {
            Boundary boundary = new Boundary(modelIndex, dim - 1);
//            boundary.addBoundry(new Line(points.get(0).getLocation()[dim - 1], points.get(points.size() - 1).getLocation()[dim - 1]));
//            partitionModels.put(modelIndex++, addPointsAndBuild(points));
            partitionPoints.put(modelIndex++, points);
            return boundary;
        }
        Boundary boundary = new Boundary(dim);
        points.sort(getComparator(dim - 1));
        for (int i = 0; i < length; i++) {
            int begin = i * threshold * (int) Math.pow(length, dim - 1);
            int end = Math.min((i + 1) * threshold * (int) Math.pow(length, dim - 1), points.size());
            if (begin >= end) {
                break;
            }
            boundary.addBoundry(new Line(points.get(begin).getLocation()[dim - 1], points.get(end - 1).getLocation()[dim - 1]));
            List<Point> temp = points.subList(begin, end);
            Boundary boundary1 = getPartition(temp, dim - 1, length);
            boundary.addChild(boundary1);
        }
        return boundary;
    }


    public void dataPartition(List<Point> points, int dim) {
        int num = points.size() / threshold + (points.size() % threshold == 0 ? 0 : 1);
        int length = (int) Math.pow(num, 1.0 / dim);
        int partitionNum = (int) Math.pow(length, dim);

        // If it's not divisible boundry
        boundary = getPartition(points.subList(0, partitionNum * threshold), dim, length);
        if (partitionNum * threshold != points.size()) {
            Boundary temp = getPartition(points.subList(partitionNum * threshold, points.size()), dim, length);
            boundary.addBoundary(temp);
        }
    }

    public int getModelIndex(Boundary boundary, Point point, int dim) {
        // first get LeafModel
        if (dim == 0) {
            return boundary.getIndex();
        } else {
            int result = 0;
            int begin = 0;
            int end = boundary.getBoundries().size();
            List<Line> boundries = boundary.getBoundries();
            while (begin <= end) {
                int mid = (begin + end) / 2;
                if (mid >= boundries.size()) {
                    // out of boundary!!! However, we can not change the boundary
                    result = boundries.size() - 1;
                    break;
                }

//                System.out.println(point);
                if (boundary.getBoundries().get(mid).isContains(point.getLocation()[dim - 1])) {
//                    if (boundary.getChildren().size() == 0) {
//                        result = boundary.getIndex();
//                    } else {
                    result = getModelIndex(boundary.getChildren().get(mid), point, dim - 1);
//                    }
                    break;
                } else {
                    if (boundary.getBoundries().get(mid).isLeft(point.getLocation()[dim - 1])) {
                        end = mid - 1;
                    } else {
                        begin = mid + 1;
                    }
                }
            }
            return result;
        }
    }

    public ExpReturn pointQueryForExp() {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        partitionModels.forEach((integer, leafModel) -> expReturn.pageaccess += leafModel.pointQuery(leafModel.getChildren()).pageaccess);
        long end = System.nanoTime();
        expReturn.time += end - begin;
        return expReturn;
    }

    public ExpReturn pointQuery(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        points.forEach(point -> {
            ExpReturn eachExpReturn = pointQuery(point);
            expReturn.pageaccess += eachExpReturn.pageaccess;
            expReturn.time += eachExpReturn.time;
        });
        return expReturn;
    }

    @Override
    public ExpReturn windowQueryByScanAll(List<Mbr> windows) {
        return null;
    }

    @Override
    public ExpReturn pointQuery(Point point) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        long end = 0;
        int pos = 0;
        try {
            Instances instances = prepareDataSetReg(Arrays.asList(point));
            Instance instance = instances.instance(0);
            begin = System.nanoTime();
            pos = (int) classifier.classifyInstance(instance);
            end = System.nanoTime();
//            System.out.println("predict time:" + (end - begin));
//            System.out.println(pos + " " + point);
        } catch (Exception e) {
            e.printStackTrace();
        }
        begin = System.nanoTime();
        LeafModel model = (LeafModel) partitionModels.get(pos);
        end = System.nanoTime();
//        System.out.println("get model time:" + (end - begin));
        begin = System.nanoTime();
        ExpReturn eachExpReturn = model.pointQuery(point);
        end = System.nanoTime();
//        System.out.println("sub model pointQuery 1:" + (end - begin));
//        System.out.println("sub model pointQuery 2:" + eachExpReturn.time);
//        expReturn.time = eachExpReturn.time;
        expReturn.time = end - begin;
        expReturn.pageaccess = eachExpReturn.pageaccess;
        return expReturn;
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

    @Override
    public ExpReturn knnQuery(Point point, int k) {
        // 4 side * side = 4k/data set size
        float knnquerySide = (float) Math.sqrt((float) k / points.size());
        ExpReturn expReturn = new ExpReturn();
        while (true) {
            Mbr window = Mbr.getMbr(point, knnquerySide);
            ExpReturn tempExpReturn = windowQuery(window);
            List<Point> tempResult = tempExpReturn.result;
            expReturn.time += tempExpReturn.time;
            long begin = System.nanoTime();
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
            long end = System.nanoTime();
            expReturn.time += end - begin;
        }
        return expReturn;
    }

    @Override
    public ExpReturn insert(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();

        points.forEach(point -> {
            int modelIndex = getModelIndex(boundary, point, point.getDim());
            LeafModel model = (LeafModel) partitionModels.get(modelIndex);
            expReturn.pageaccess += model.insert(point).pageaccess;
        });

        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn insert(Point point) {
        return insert(Arrays.asList(point));
    }

    @Override
    public ExpReturn insertByLink(List<Point> points) {
        return insert(points);
    }

    @Override
    public ExpReturn delete(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
//        long begin = System.nanoTime();
//        points.forEach(new Consumer<Point>() {
//            @Override
//            public void accept(Point point) {
//                root.delete(point);
//            }
//        });
//        this.getPoints().removeAll(points);
//        long end = System.nanoTime();
//        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public NonLeafNode buildRtreeAfterTuning(String path, int dim, int level) {
        return null;
    }

    public Instances prepareDataSetReg(List<Point> points) {
        int dim = points.get(0).getDim();
        FastVector atts = new FastVector();
        for (int i = 0; i < dim; i++) {
            atts.addElement(new Attribute("att" + (i + 1)));
        }
        atts.addElement(new Attribute("index"));
        Instances dataSet = new Instances("tree", atts, 0);
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            double[] vals = new double[dataSet.numAttributes()];
            for (int j = 0; j < vals.length - 1; j++) {
                vals[j] = point.getLocation()[j];
            }
            vals[vals.length - 1] = point.getIndex();
            dataSet.add(new Instance(1.0, vals));
        }
        dataSet.setClassIndex(dataSet.numAttributes() - 1);
        return dataSet;
    }

    public Instances prepareDataSetCla(List<Point> points, int classNum) {
        int dim = points.get(0).getDim();
        FastVector atts = new FastVector();
        FastVector attVals = new FastVector();
        for (int i = 0; i < dim; i++) {
            atts.addElement(new Attribute("att" + (i + 1)));
        }
        for (int i = 0; i < classNum; i++) {
            attVals.addElement("" + i);
        }
        atts.addElement(new Attribute("index", attVals));
        Instances dataSet = new Instances("tree", atts, 0);
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            double[] vals = new double[dataSet.numAttributes()];
            for (int j = 0; j < vals.length - 1; j++) {
                vals[j] = point.getLocation()[j];
            }
            int index = attVals.indexOf("" + point.getIndex());
            vals[vals.length - 1] = index;
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
        points.sort(getComparator(dim - 1));
        dataPartition(points, dim);

        List<Point> trainingSet = new ArrayList<>();
        // Partition finish
        partitionPoints.forEach(new BiConsumer<Integer, List<Point>>() {
            @Override
            public void accept(Integer index, List<Point> points) {
                points.forEach(new Consumer<Point>() {
                    @Override
                    public void accept(Point point) {
                        point.setIndex(index);
                    }
                });
                trainingSet.addAll(points);
            }
        });
        //
//        Instances instances = prepareDataSetCla(trainingSet, partitionPoints.keySet().size());
        Instances instances = prepareDataSetReg(trainingSet);
//        classifier = new NaiveBayes();
        classifier = new MultilayerPerceptron();
        try {
            classifier.setOptions(Utils.splitOptions("-L 0.3 -M 0.2 -N 500 -V 0 -S 0 -E 20 -H a"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            classifier.buildClassifier(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }
        partitionPoints = new HashMap<>();
        List<Double> results = getPredVals(classifier, instances);
        for (int i = 0; i < results.size(); i++) {
            int pos = results.get(i).intValue();
            if (!partitionPoints.containsKey(pos)) {
                partitionPoints.put(pos, new ArrayList<>());
            }
            partitionPoints.get(pos).add(trainingSet.get(i));
        }

        partitionPoints.forEach((integer, points) -> {
            System.out.println(integer + " " + points.size());
            partitionModels.put(integer, addPointsAndBuild(points));
        });

        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn buildRtree(List<Point> res) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        this.points = res;
        dim = points.get(0).getDim();
        points.sort(getComparator(dim - 1));
        dataPartition(points, dim);
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    /**
     * This is for linear scan not binary search
     *
     * @param window
     */
    public ExpReturn windowQuery(Mbr window) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        partitionModels.forEach((integer, leafModel) -> {
            if (leafModel.getMbr().interact(window)) {
                ExpReturn eachExpReturn = leafModel.windowQuery(window);
                expReturn.result.addAll(eachExpReturn.result);
                expReturn.pageaccess += eachExpReturn.pageaccess;
            }
        });
        long end = System.nanoTime();
        ExpReturn accurate = windowQueryByScanAll(window);
        expReturn.accuracy = (double) expReturn.result.size() / accurate.result.size();
        expReturn.time = end - begin;
        return expReturn;
    }

    public ExpReturn windowQueryByScanAll(Mbr window) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        partitionModels.forEach((integer, leafModel) -> {
            if (leafModel.getMbr().interact(window)) {
                ExpReturn eachExpReturn = leafModel.windowQueryByScanAll(window);
                expReturn.result.addAll(eachExpReturn.result);
                expReturn.pageaccess += eachExpReturn.pageaccess;
            }
        });
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    public List<Mbr> getmbrFigures() {
        List<Mbr> mbrFigures = new ArrayList<>();
        partitionModels.forEach((integer, model) -> mbrFigures.add(model.getMbr()));
        return mbrFigures;
    }

    public static void main(String[] args) {

        PartitionModelByPred partitionModelByPred = new PartitionModelByPred(10000, "H", 100, "MultilayerPerceptron");
        partitionModelByPred.buildRtree("/Users/guanli/Documents/datasets/RLRtree/raw/uniform_160000_1_2_.csv");
//        point querytime=658977043
//        pageaccess=316829
//        partitionModelByPred.buildRtree("D:\\datasets\\RLRtree\\raw\\uniform_160000_1_2_.csv");
        System.out.println("build finish");
//        partitionModelByPred.visualize(600, 600, partitionModelByPred.getmbrFigures()).saveMBR("partition_pred_uniform_2000000.png");
        System.out.println(partitionModelByPred.pointQueryForExp());
        System.out.println("point query" + partitionModelByPred.pointQuery(partitionModelByPred.points));


//        PartitionModelRtree partitionModelRtree = new PartitionModelRtree(10000, "H", 100, "NaiveBayes");
////            partitionModelRtree.buildRtree("/Users/guanli/Documents/datasets/RLRtree/raw/uniform_1000000_1_2_.csv");
////        partitionModelRtree.buildRtree("/Users/guanli/Documents/datasets/RLRtree/raw/uniform_8000000_1_2_.csv");
//        partitionModelRtree.buildRtree("D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_2_.csv");
//        System.out.println("build finish");
//        System.out.println(partitionModelRtree.pointQueryForExp());
////        partitionModelRtree.visualize(600, 600, partitionModelRtree.getmbrFigures()).saveMBR("partition_uniform_2000000.png");
//        System.out.println("point query" + partitionModelRtree.pointQuery(partitionModelRtree.getPoints()));

    }

}
