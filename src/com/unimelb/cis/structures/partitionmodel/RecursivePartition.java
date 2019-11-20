package com.unimelb.cis.structures.partitionmodel;

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

    Partition root;

    int threshold;

    String algorithm;

    int maxPartitionNumEachDim;

    String curve;

    public RecursivePartition(String curve, int maxPartitionNumEachDim, int threshold, String algorithm) {
        this.curve = curve;
        this.maxPartitionNumEachDim = maxPartitionNumEachDim;
        this.threshold = threshold;
        this.algorithm = algorithm;
    }

    public static void main(String[] args) {
        int maxPartitionNumEachDim = 8;
//        RecursivePartition recursivePartition = new RecursivePartition(maxPartitionNumEachDim, 10000, "SMOreg");
        RecursivePartition recursivePartition = new RecursivePartition("H", maxPartitionNumEachDim, 2000, "NaiveBayes");
//        RecursivePartition recursivePartition = new RecursivePartition(maxPartitionNumEachDim, 000, "LinearRegression"); // time=93329793368 pageaccess=7911393
//        String dataset = "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_160000_1_2_.csv";
//        System.out.println(recursivePartition.buildRtree(dataset));
//        System.out.println("point query:" + recursivePartition.pointQuery(recursivePartition.getQueryPoints(0.01)));
//        System.out.println("window query:" + recursivePartition.windowQuery(Mbr.getMbrs(0.01f, 10, 2).get(0)));
//        System.out.println("knn query:" + recursivePartition.knnQuery(new Point(0.5f, 0.5f), 10));
//        System.out.println("insert :" + recursivePartition.insert(new Point(0.5f, 0.5f)));
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
        root = new Partition(curve, 0, 100, algorithm, maxPartitionNumEachDim, threshold, points);
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
        ExpReturn expReturn = root.windowQuery(window);
        ExpReturn accurate = windowQueryByScanAll(window);
        expReturn.accuracy = (double) expReturn.result.size() / accurate.result.size();
        return expReturn;
    }

    @Override
    public ExpReturn windowQuery(List<Mbr> windows) {
        ExpReturn expReturn = new ExpReturn();
        windows.forEach(mbr -> expReturn.plus(windowQuery(mbr)));
        expReturn.time /= windows.size();
        expReturn.pageaccess /= windows.size();
        expReturn.accuracy /= windows.size();
        return expReturn;
    }

    @Override
    public ExpReturn pointQuery(List<Point> points) {
        ExpReturn expReturn = root.pointQuery(points);
        expReturn.time /= points.size();
        expReturn.pageaccess /= points.size();
        return expReturn;
    }

    public ExpReturn accurateKnnQuery(List<Point> points, int k) {
        ExpReturn expReturn = new ExpReturn();
        points.forEach(point -> {
            ExpReturn temp = accurateKnnQuery(point, k);
            expReturn.plus(temp);
        });
        expReturn.time /= points.size();
        expReturn.pageaccess /= points.size();
        return expReturn;
    }

    public ExpReturn accurateKnnQuery(Point point, int k) {
        float knnquerySide = (float) Math.sqrt((float) k / points.size());
        ExpReturn expReturn = new ExpReturn();
        while (true) {
            Mbr window = Mbr.getMbr(point, knnquerySide);
            ExpReturn tempExpReturn = windowQueryByScanAll(window);
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
    public ExpReturn knnQuery(Point point, int k) {
        float knnquerySide = (float) Math.sqrt((float) k / points.size());
        ExpReturn expReturn = new ExpReturn();
        while (true) {
            Mbr window = Mbr.getMbr(point, knnquerySide);
            ExpReturn tempExpReturn = windowQuery(window);
            List<Point> tempResult = tempExpReturn.result;
            expReturn.time = tempExpReturn.time;
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
                    long end = System.nanoTime();
                    expReturn.result = tempResult.subList(0, k);
                    expReturn.pageaccess += tempExpReturn.pageaccess;
                    expReturn.time = end - begin;
                    break;
                }
            }
            knnquerySide = knnquerySide * 2;
            expReturn.pageaccess = 0;
        }
        ExpReturn accurate = accurateKnnQuery(point, k);
        expReturn.accuracy = claAcc(expReturn.result, accurate.result);
        return expReturn;
    }

    @Override
    public ExpReturn knnQuery(List<Point> points, int k) {
        ExpReturn expReturn = new ExpReturn();
        points.forEach(point -> {
            ExpReturn temp = knnQuery(point, k);
            expReturn.plus(temp);
        });
        expReturn.time /= points.size();
        expReturn.pageaccess /= points.size();
        expReturn.accuracy /= points.size();
        return expReturn;
    }

    @Override
    public ExpReturn pointQuery(Point point) {
        return root.pointQuery(point);
    }

    public ExpReturn windowQueryByScanAll(List<Mbr> windows) {
        ExpReturn expReturn = new ExpReturn();
        windows.forEach(mbr -> expReturn.plus(windowQueryByScanAll(mbr)));
        expReturn.time /= windows.size();
        expReturn.pageaccess /= windows.size();
        return expReturn;
    }

    public ExpReturn windowQueryByScanAll(Mbr window) {
        return root.windowQueryByScanAll(window);
    }

    public ExpReturn windowQueryByOpt(List<Mbr> windows) {
        ExpReturn expReturn = new ExpReturn();
        windows.forEach(mbr -> expReturn.plus(root.windowQueryOpt(mbr)));
        expReturn.time /= windows.size();
        expReturn.pageaccess /= windows.size();
        return expReturn;
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
