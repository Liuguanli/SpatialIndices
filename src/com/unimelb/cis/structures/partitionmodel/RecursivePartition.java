package com.unimelb.cis.structures.partitionmodel;

import com.unimelb.cis.Curve;
import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.NonLeafNode;
import com.unimelb.cis.node.Partition;
import com.unimelb.cis.node.Point;
import com.unimelb.cis.structures.IRtree;
import com.unimelb.cis.utils.ExpReturn;
import weka.classifiers.functions.SMOreg;

import java.util.ArrayList;
import java.util.List;

import static com.unimelb.cis.CSVFileReader.read;

public class RecursivePartition extends IRtree {

    Partition root;

    int threshold;

    String algorithm;

    int maxPartitionNumEachDim;

    public RecursivePartition(int maxPartitionNumEachDim, int threshold, String algorithm) {
        this.maxPartitionNumEachDim = maxPartitionNumEachDim;
        this.threshold = threshold;
        this.algorithm = algorithm;
    }

    public static void main(String[] args) {
        int maxPartitionNumEachDim = 8;
        RecursivePartition recursivePartition = new RecursivePartition(maxPartitionNumEachDim, 10000, "MultilayerPerceptron");
//        RecursivePartition recursivePartition = new RecursivePartition(maxPartitionNumEachDim, 10000, "LinearRegression"); // time=93329793368 pageaccess=7911393
        String dataset = "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_4000000_1_2_.csv";
        System.out.println(recursivePartition.buildRtree(dataset));
        System.out.println(recursivePartition.pointQuery(recursivePartition.getPoints()));
//        recursivePartition.visualize(600, 600, recursivePartition.root.getmbrFigures()).saveMBR("recursivePartition_uniform_1000000.png");
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
        root = new Partition(0, 100, algorithm, maxPartitionNumEachDim, threshold, points);
        root.build();

        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn buildRtree(List<Point> points) {
        return null;
    }

    @Override
    public ExpReturn windowQuery(Mbr window) {
        return null;
    }

    @Override
    public ExpReturn windowQuery(List<Mbr> windows) {
        return null;
    }

    @Override
    public ExpReturn pointQuery(List<Point> points) {
        return root.pointQuery(points);
    }

    @Override
    public ExpReturn knnQuery(Point point, int k) {
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
    public ExpReturn pointQuery(Point point) {
        return root.pointQuery(point);
    }

    @Override
    public ExpReturn insert(List<Point> points) {
        return root.insert(points);
    }

    @Override
    public ExpReturn insert(Point point) {
        return root.insert(point);
    }

    @Override
    public NonLeafNode buildRtreeAfterTuning(String path, int dim, int level) {
        return null;
    }
}
