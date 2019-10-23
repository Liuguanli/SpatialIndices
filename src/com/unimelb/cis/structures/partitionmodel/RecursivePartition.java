package com.unimelb.cis.structures.partitionmodel;

import com.unimelb.cis.Curve;
import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.NonLeafNode;
import com.unimelb.cis.node.Partition;
import com.unimelb.cis.node.Point;
import com.unimelb.cis.structures.IRtree;
import com.unimelb.cis.utils.ExpReturn;

import java.util.ArrayList;
import java.util.List;

import static com.unimelb.cis.CSVFileReader.read;

public class RecursivePartition extends IRtree {


    int partitionNum;

    Partition root;

    int threshold;

    public RecursivePartition(int partitionNum, int threshold) {
        this.partitionNum = partitionNum;
        this.threshold = threshold;
    }

    public static void main(String[] args) {
        RecursivePartition recursivePartition = new RecursivePartition(2, 10000);
        String dataset = "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_160000_1_2_.csv";
        System.out.println(recursivePartition.buildRtree(dataset));
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

        points = Curve.getPointByCurve(points, "Z", false);
        root = new Partition(0, partitionNum, threshold);
        root.setPoints(points);
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
        return null;
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
    public ExpReturn pointQuery(Point point) {
        return null;
    }

    @Override
    public ExpReturn insert(List<Point> points) {
        return null;
    }

    @Override
    public ExpReturn insert(Point point) {
        return null;
    }

    @Override
    public NonLeafNode buildRtreeAfterTuning(String path, int dim, int level) {
        return null;
    }
}
