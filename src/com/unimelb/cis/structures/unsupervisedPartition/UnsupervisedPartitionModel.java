package com.unimelb.cis.structures.unsupervisedPartition;

import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.NonLeafNode;
import com.unimelb.cis.node.Point;
import com.unimelb.cis.node.UnsupervisedModel;
import com.unimelb.cis.structures.IRtree;
import com.unimelb.cis.utils.ExpReturn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static com.unimelb.cis.CSVFileReader.read;

public class UnsupervisedPartitionModel extends IRtree {

    private int threshold;

    private String curve;

    private int pageSize;

    private String algorithm;

    private UnsupervisedModel root;

    private int maxIteration;

    public static void main(String[] args) {
        UnsupervisedPartitionModel unsupervisedPartitionModel = new UnsupervisedPartitionModel(4000, "H", 100, "NaiveBayes", 10);
        unsupervisedPartitionModel.buildRtree("/Users/guanli/Documents/datasets/RLRtree/raw/uniform_160000_1_2_.csv");
        ExpReturn expReturn = unsupervisedPartitionModel.pointQuery(unsupervisedPartitionModel.points);
        System.out.println("pointQuery:" + expReturn);
        unsupervisedPartitionModel.visualize(600, 600, unsupervisedPartitionModel.getmbrFigures()).saveMBR("kmeans_uniform_160000.png");
    }


    public UnsupervisedPartitionModel(int threshold, String curve, int pageSize, String algorithm, int maxIteration) {
        this.threshold = threshold;
        this.curve = curve;
        this.pageSize = pageSize;
        this.algorithm = algorithm;
        this.maxIteration = maxIteration;
    }

    public List<Mbr> getmbrFigures() {
        List<Mbr> mbrFigures = new ArrayList<>();
        root.getSubModels().forEach((integer, unsupervisedModel) -> mbrFigures.add(unsupervisedModel.getMbr()));
        return mbrFigures;
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
        root = new UnsupervisedModel(-1, pageSize, algorithm, curve, threshold, maxIteration);
        root.setChildren(points);
        root.build();
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn buildRtree(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        root = new UnsupervisedModel(-1, pageSize, algorithm, curve, threshold, maxIteration);
        root.setChildren(points);
        root.build();
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    public ExpReturn windowQueryByScanAll(List<Mbr> windows) {
        ExpReturn expReturn = new ExpReturn();
        windows.forEach(mbr -> expReturn.plus(root.windowQueryByScanAll(mbr)));
        expReturn.time /= windows.size();
        expReturn.pageaccess /= windows.size();
        return expReturn;
    }

    @Override
    public ExpReturn windowQuery(Mbr window) {
        return root.windowQuery(window);
    }

    @Override
    public ExpReturn windowQuery(List<Mbr> windows) {
        ExpReturn expReturn = new ExpReturn();
        windows.forEach(window -> expReturn.plus(windowQuery(window)));
        expReturn.time /= windows.size();
        expReturn.pageaccess /= windows.size();
        return expReturn;
    }

    @Override
    public ExpReturn pointQuery(List<Point> points) {
        ExpReturn expReturn = root.pointQuery(points);
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
        return pointQuery(Arrays.asList(point));
    }

    @Override
    public ExpReturn insert(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        points.forEach(point -> {
            expReturn.plus(insert(point));
        });
        return expReturn;
    }

    @Override
    public ExpReturn insert(Point point) {
        return root.insert(point);
    }

    @Override
    public ExpReturn insertByLink(List<Point> points) {
        return root.insertByLink(points);
    }

    @Override
    public ExpReturn delete(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        points.forEach(new Consumer<Point>() {
            @Override
            public void accept(Point point) {
                root.delete(point);
            }
        });
        this.getPoints().removeAll(points);
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public NonLeafNode buildRtreeAfterTuning(String path, int dim, int level) {
        return null;
    }
}
