package com.unimelb.cis.node;

import com.unimelb.cis.Curve;
import com.unimelb.cis.geometry.Boundary;
import com.unimelb.cis.geometry.Line;
import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.utils.ExpReturn;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

public class Partition extends Model {

    protected int maxPartitionNumEachDim = 8;

    protected Mbr mbr;

    protected int level;

    protected int index;

    protected int partitionNum;

    protected List<Partition> children;

    protected List<Point> points;

    protected int threshold;

    protected int dim;

    protected Boundary boundarys;

    private int maxPartitionNum;

    private int bitNum;

    Map<Integer, List<Point>> partitionPoints = new HashMap<>();

    Map<Integer, Model> partitionModels = new HashMap<>();

    public Partition(int index, int pageSize, String algorithm, int maxPartitionNumEachDim, int threshold, List<Point> points) {
        super(index, pageSize, algorithm);
        this.maxPartitionNumEachDim = maxPartitionNumEachDim;
        this.threshold = threshold;
        this.points = points;
//        maxPartitionNum = (int) Math.pow(maxPartitionNumEachDim, dim);
    }

    public void dataPartition(List<Point> points, int dim) {
        int num = points.size() / threshold + (points.size() % threshold == 0 ? 0 : 1);

        bitNum = (int) (Math.log(num) / Math.log(2));
        int length;
        if ((int) Math.pow(2, bitNum) < num) {
            length = (int) Math.pow(2, (bitNum / dim + 1));
        } else {
            length = (int) Math.pow(2, Math.max(bitNum / dim, 1));
        }
        length = Math.min(length, maxPartitionNumEachDim);

        int partitionNum = (int) Math.pow(length, dim);
        System.out.println(partitionNum);

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
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
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

    int classNum;

    @Override
    public void build() {
        dim = points.get(0).getDim();
        points.sort(getComparator(dim - 1));
        dataPartition(points, dim);

        List<Point> trainingSet = new ArrayList<>();

        partitionPoints.forEach((index, points) -> {
            points.forEach(point -> point.setIndex(index));
            trainingSet.addAll(points);
        });

        classNum = partitionPoints.keySet().size();

        Instances instances = getInstances(this.name, trainingSet, "LRM");
        classifier = getModels(this.name);
        try {
            classifier.buildClassifier(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }

        partitionPoints = new HashMap<>();
        List<Double> results = getPredVals(classifier, instances);
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
            System.out.println(integer + " " + points.size());
            partitionModels.put(integer, addPointsAndBuild(points));
        });
//        classNum = partitionModels.keySet().size();
    }

    private Model addPointsAndBuild(List<Point> points) {
        if (points.size() >= threshold * 2) {
            Partition partition = new Partition(0, 100, this.name, maxPartitionNumEachDim, threshold, points);
            partition.build();
            return partition;
        } else {
            points = Curve.getPointByCurve(points, "H", true);
            LeafModel leafModel = new LeafModel(level + 1, pageSize, this.name);
            leafModel.setChildren(points);
            leafModel.build();
            return leafModel;
        }
    }



    private int getModelIndex(Point point) {
        int pos = 0;
        try {
            Instances instances = prepareDataSetReg(Arrays.asList(point));
            Instance instance = instances.instance(0);
            pos = (int) classifier.classifyInstance(instance);
//            System.out.println("predict time:" + (end - begin));
//            System.out.println(pos + " " + point);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pos;
    }

    @Override
    public ExpReturn pointQuery(Point point) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        int modelIndex = getModelIndex(point);
//        modelIndex = Math.max(modelIndex, 0);
//        modelIndex = Math.min(modelIndex, classNum - 1);
        long end = System.nanoTime();
        Model model = partitionModels.get(modelIndex);
        if (model == null) {
            System.out.println("partitionModels:" + partitionModels);
            System.out.println("points.size()" + points.size());
            System.out.println("modelIndex" + modelIndex);
            System.out.println("classNum" + classNum);
        } else {
            ExpReturn eachExpReturn = model.pointQuery(point);
            expReturn.pageaccess += eachExpReturn.pageaccess;
            expReturn.time += eachExpReturn.time + end - begin;
        }
        return expReturn;
    }

    @Override
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
