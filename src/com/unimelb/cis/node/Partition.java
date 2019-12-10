package com.unimelb.cis.node;

import com.unimelb.cis.curve.Curve;
import com.unimelb.cis.geometry.Boundary;
import com.unimelb.cis.geometry.Line;
import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.utils.ExpReturn;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

public class Partition extends Model {

    protected int maxPartitionNumEachDim;

    protected int level;

    protected int index;

    protected int threshold;

    protected int dim;

    private int bitNum;

    private String curve;

    String modelName = "LRM";

    Map<Integer, List<Point>> partitionPoints = new HashMap<>();

    public Map<Integer, Model> partitionModels = new HashMap<>();

    public Partition(String curve, int index, int pageSize, String algorithm, int maxPartitionNumEachDim, int threshold, List<Point> points) {
        super(index, pageSize, algorithm);
        this.curve = curve;
        this.maxPartitionNumEachDim = maxPartitionNumEachDim;
        this.threshold = threshold;
        this.children = points;
//        maxPartitionNum = (int) Math.pow(maxPartitionNumEachDim, dim);
    }

    public void dataPartition(List<Point> points, int dim) {
        int num = points.size() / (2 * threshold) + (points.size() % (2 * threshold) == 0 ? 0 : 1);

        bitNum = (int) (Math.log(num) / Math.log(2));
        int length;
        if ((int) Math.pow(2, bitNum) < num) {
            length = (int) Math.pow(2, (bitNum / dim + 1));
        } else {
            length = (int) Math.pow(2, Math.max(bitNum / dim, 1));
        }
        length = Math.min(length, maxPartitionNumEachDim);

        int partitionNum = (int) Math.pow(length, dim);
//        System.out.println(partitionNum);

        int capacity = points.size() / partitionNum;

        // If it's not divisible boundry
        getPartition(points, capacity, dim, length, null);
    }

    public void getPartition(List<Point> points, int capacity, int dim, int length, Boundary indexorders) {
        if (dim == 0) {
            int index = (int) indexorders.getHCurveValue(bitNum + 1);
            partitionPoints.put(index, points);
            return;
        }
        points.sort(getComparator(dim - 1));
        Boundary boundary = new Boundary(dim);
        for (int i = 0; i < length; i++) {
            if (indexorders == null) {
                indexorders = new Boundary(dim);
            }
            int begin = i * capacity * (int) Math.pow(length, dim - 1);
            int end = Math.min((i + 1) * capacity * (int) Math.pow(length, dim - 1), points.size());
            if (begin >= end) {
                break;
            }
            boundary.addBoundry(new Line(points.get(begin).getLocation()[dim - 1], points.get(end - 1).getLocation()[dim - 1]));

            indexorders.setDimOrder(dim, i);

            List<Point> temp = points.subList(begin, end);
            getPartition(temp, capacity, dim - 1, length, indexorders);

        }
    }

    public List<Mbr> getmbrFigures() {
        List<Mbr> mbrFigures = new ArrayList<>();
        partitionModels.forEach((integer, model) -> mbrFigures.add(model.getMbr()));
        return mbrFigures;
    }

    public List<Point> getPoints() {
        return children;
    }

    public void setPoints(List<Point> points) {
        this.children = points;
    }

    public Mbr getMbr() {
        return mbr;
    }

    public void setMbr(Mbr mbr) {
        this.mbr = mbr;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public void build() {
        dim = children.get(0).getDim();
        children.sort(getComparator(dim - 1));
        dataPartition(children, dim);

        List<Point> trainingSet = new ArrayList<>();

        partitionPoints.forEach((index, points) -> {
            points.forEach(point -> point.setIndex(index));
            trainingSet.addAll(points);
        });

        classNum = partitionPoints.keySet().size();

        Instances instances = getInstances(this.name, trainingSet, modelName);
        classifier = getModels(this.name);
        try {
            classifier.buildClassifier(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }

        partitionPoints = new HashMap<>();
        List<Double> results = getPredVals(classifier, instances).predictResults;
        for (int i = 0; i < results.size(); i++) {
            int pos = results.get(i).intValue();
//            pos = Math.min(pos, classNum - 1);
//            pos = Math.max(pos, 0);
            if (!partitionPoints.containsKey(pos)) {
                partitionPoints.put(pos, new ArrayList<>());
            }
            partitionPoints.get(pos).add(trainingSet.get(i));
        }

        partitionPoints.forEach((integer, points) -> {
//            System.out.println(integer + " " + points.size());
            partitionModels.put(integer, addPointsAndBuild(points));
        });
//        classNum = partitionModels.keySet().size();
    }

    private Model addPointsAndBuild(List<Point> points) {
        if (points.size() >= threshold * 2) {
            Partition partition = new Partition(curve, 0, 100, this.name, maxPartitionNumEachDim, threshold, points);
            partition.setChildren(points);
            partition.build();
//            System.out.println(partition.getMbr());
            return partition;
        } else {
            points = Curve.getPointByCurve(points, curve, true);
            LeafModel leafModel = new LeafModel(level + 1, pageSize, this.name);
//            LeafModel leafModel = new LeafModel(level + 1, pageSize, "NaiveBayes");
//            LeafModel leafModel = new LeafModel(level + 1, pageSize, "Logistic");
            leafModel.setChildren(points);
            leafModel.build();
            return leafModel;
        }
    }

    private List<Integer> getModelIndex(List<Point> points, Instances instances) {
        List<Integer> result = new ArrayList<>();
        try {
            for (int i = 0; i < points.size(); i++) {
                Instance instance = instances.instance(i);
                result.add((int) classifier.classifyInstance(instance));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    private int getModelIndex(Point point) {
        int pos = 0;
        try {
            Instances instances = getInstances(this.name, Arrays.asList(point), modelName);
//            Instances instances = prepareDataSetReg(Arrays.asList(point));
            Instance instance = instances.instance(0);
            pos = (int) classifier.classifyInstance(instance);
//            System.out.println("predict time:" + (end - begin));
//            System.out.println(pos + " " + point);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pos;
    }

    private int getModelIndex(Instance instance) {
        int pos = 0;
        try {
            pos = (int) classifier.classifyInstance(instance);
//            System.out.println("predict time:" + (end - begin));
//            System.out.println(pos + " " + point);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pos;
    }

    public int getIndex(Point point) {
        Instances instances = getInstances(this.name, Arrays.asList(point), modelName);
        Instance instance = instances.instance(0);
        int modelIndex = getModelIndex(instance);
        return modelIndex;
    }

    @Override
    public ExpReturn pointQuery(Point point) {
        ExpReturn expReturn = new ExpReturn();
        Instances instances = getInstances(this.name, Arrays.asList(point), modelName);
//        Instances instances = getInstances(Arrays.asList(point));
        Instance instance = instances.instance(0);
        long begin = System.nanoTime();
        int modelIndex = getModelIndex(instance);
//        modelIndex = Math.max(modelIndex, 0);
//        modelIndex = Math.min(modelIndex, classNum - 1);
        long end = System.nanoTime();
//        System.out.println("level:" + level);
//        System.out.println("getModelIndex time:" + (end - begin));
        long gap = end - begin;
        Model model = partitionModels.get(modelIndex);
        if (model == null) {
//            System.out.println("partitionModels:" + partitionModels);
//            System.out.println("points.size()" + children.size());
//            System.out.println("modelIndex" + modelIndex);
//            System.out.println("classNum" + classNum);
        } else {
            ExpReturn eachExpReturn = model.pointQuery(point);
            expReturn.pageaccess += eachExpReturn.pageaccess;
            expReturn.time += eachExpReturn.time + gap;
        }
        return expReturn;
    }

    @Override
    public ExpReturn pointQuery(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        Instances instances = getInstances(this.name, points, modelName);
//        Instances instances = prepareDataSetReg(points);
        long begin = System.nanoTime();
        List<Integer> results = getModelIndex(points, instances);
        long end = System.nanoTime();
        long gap = end - begin;
        for (int i = 0; i < results.size(); i++) {
            Model model = partitionModels.get(results.get(i));
            if (model != null) {
                ExpReturn eachExpReturn = model.pointQuery(points.get(i));
                expReturn.plus(eachExpReturn);
            }
        }
//        points.forEach(point -> {
//            ExpReturn eachExpReturn = pointQuery(point);
//            expReturn.pageaccess += eachExpReturn.pageaccess;
//            expReturn.time += eachExpReturn.time;
//        });
        System.out.println("gap:" + gap);
        return expReturn;
    }

    @Override
    public ExpReturn windowQuery(Mbr window) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();

        List<Integer> results = new ArrayList<>();
        window.getAllPoints().forEach(point -> {
            int modelIndex = getModelIndex(point);
            results.add(modelIndex);
        });
        results.sort(Integer::compareTo);
        int indexLow = results.get(0);
        int indexHigh = results.get(results.size() - 1);
        for (int i = indexLow; i <= indexHigh; i++) {
            if (partitionModels.keySet().contains(i)) {
                Model model = partitionModels.get(i);
                ExpReturn eachExpReturn = model.windowQuery(window);
                expReturn.plus(eachExpReturn);
            }
        }
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn windowQueryOpt(Mbr window) {
        ExpReturn expReturn = new ExpReturn();
        partitionModels.forEach((integer, model) -> {
            if (model.getMbr().interact(window)) {
                ExpReturn eachExpReturn = model.windowQueryOpt(window);
                expReturn.plus(eachExpReturn);
            }
        });
        return expReturn;
    }

    @Override
    public ExpReturn windowQueryByScanAll(Mbr window) {
        ExpReturn expReturn = new ExpReturn();
        partitionModels.forEach((integer, model) -> {
            if (model.getMbr().interact(window)) {
                ExpReturn eachExpReturn = model.windowQueryByScanAll(window);
                expReturn.plus(eachExpReturn);
            }
        });
        return expReturn;
    }

    @Override
    public ExpReturn insert(Point point) {
        ExpReturn expReturn = new ExpReturn();
        int modelIndex = getModelIndex(point);
        Model model = partitionModels.get(modelIndex);
        if (model != null) {
            return model.insert(point);
        }
        return expReturn;
    }

    @Override
    public ExpReturn insert(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        points.forEach(point -> expReturn.pageaccess += insert(point).pageaccess);
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn insertByLink(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        points.forEach(point -> {
            int modelIndex = getModelIndex(point);
            Model model = partitionModels.get(modelIndex);
            if (model != null) {
                ExpReturn temp = model.insert(point);
                expReturn.plus(temp);
            }
        });
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn delete(Point point) {
        ExpReturn expReturn = new ExpReturn();
        Instances instances = getInstances(this.name, Arrays.asList(point), modelName);
//        Instances instances = getInstances(Arrays.asList(point));
        Instance instance = instances.instance(0);
        long begin = System.nanoTime();
        int modelIndex = getModelIndex(instance);
//        modelIndex = Math.max(modelIndex, 0);
//        modelIndex = Math.min(modelIndex, classNum - 1);
        long end = System.nanoTime();
//        System.out.println("level:" + level);
//        System.out.println("getModelIndex time:" + (end - begin));
        long gap = end - begin;
        Model model = partitionModels.get(modelIndex);
        if (model == null) {
//            System.out.println("partitionModels:" + partitionModels);
//            System.out.println("points.size()" + children.size());
//            System.out.println("modelIndex" + modelIndex);
//            System.out.println("classNum" + classNum);
        } else {
            ExpReturn eachExpReturn = model.delete(point);
            expReturn.pageaccess += eachExpReturn.pageaccess;
            expReturn.time += eachExpReturn.time + gap;
        }
        return expReturn;
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
}
