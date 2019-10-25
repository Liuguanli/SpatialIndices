package com.unimelb.cis.node;

import com.unimelb.cis.Curve;
import com.unimelb.cis.ZCurve;
import com.unimelb.cis.geometry.Mbr;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Partition {

    protected Mbr mbr;

    protected int level;

    protected int index;

    protected int partitionNum;

    List<Partition> children;

    List<Point> points;

    int threshold;

    public Partition(int level, int partitionNum, int threshold) {
        this.level = level;
        this.partitionNum = partitionNum;
        this.threshold = threshold;
        children = new ArrayList<>();
        points = new ArrayList<>();
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

    public List<Partition> getChildren() {
        return children;
    }

    public void setChildren(List<Partition> children) {
        this.children = children;
    }

    public void getCurveValues(int partitionNum, int num, int dim, List<Long> temp, List<long[]> result) {
        if (dim == 0) {
            long[] orders = new long[temp.size()];
            for (int i = 0; i < temp.size(); i++) {
                orders[i] = temp.get(i);
            }
            result.add(orders);
            return;
        }
        for (int i = 0; i < partitionNum; i++) {
            List<Long> indexOrder = new ArrayList<>(temp);
            indexOrder.add((long) (num * (i + 1) / partitionNum));
            getCurveValues(partitionNum, num, dim - 1, indexOrder, result);
        }
    }

    public void build() {
        points = Curve.getPointByCurve(points, "Z", false);
        int bitNum = (int) (Math.log(points.size()) / Math.log(2.0)) + 1;
        List<long[]> result = new ArrayList<>();
        getCurveValues(partitionNum, points.size(), points.get(0).getDim(), new ArrayList<>(), result);
        List<Long> zCurves = new ArrayList<>();
        System.out.println(bitNum);
        result.forEach(longs -> zCurves.add(ZCurve.getZcurve(longs, bitNum)));
        System.out.println(zCurves);
        zCurves.sort(Long::compareTo);
//        ZCurve.getZcurve(indexOrder,bitNum)
//
//
        Partition partition = new Partition(level + 1, partitionNum, threshold);
        children.add(partition);
        int borderIndex = 0;
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            if (zCurves.get(borderIndex) > point.getCurveValue()) {
                partition.getPoints().add(point);
            } else {
                borderIndex++;
                partition = new Partition(level + 1, partitionNum, threshold);
                children.add(partition);
                partition.getPoints().add(point);
            }
        }
        children.forEach(partition1 -> System.out.println(partition1.getPoints().size()));
    }
}
