package com.unimelb.cis.structures.partitionmodel;

import com.unimelb.cis.Curve;
import com.unimelb.cis.geometry.Line;
import com.unimelb.cis.node.LeafModel;
import com.unimelb.cis.node.Point;
import com.unimelb.cis.geometry.Boundary;

import java.util.*;
import java.util.function.Consumer;

import static com.unimelb.cis.CSVFileReader.read;

public class PartitionModelRtree {

    private int threshold;

    private String curve;

    private int pageSize;

    private String algorithm;

    private Boundary boundary;


    public PartitionModelRtree(int threshold, String curve, int pageSize, String algorithm) {
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

    Map<Integer, LeafModel> partitionModels = new HashMap<>();


    int modelIndex = 0;

    private LeafModel addPoints(List<Point> points) {
        points = Curve.getPointByCurve(points, curve);
        LeafModel leafModel = new LeafModel(-1, pageSize, algorithm);
        leafModel.setChildren(points);
        leafModel.build();
        return leafModel;
    }


    public Boundary getPartition(List<Point> points, int dim, int length) {
        if (dim == 0) {
            Boundary boundary = new Boundary(modelIndex, dim - 1);
//            boundary.addBoundry(new Line(points.get(0).getLocation()[dim - 1], points.get(points.size() - 1).getLocation()[dim - 1]));
            partitionModels.put(modelIndex++, addPoints(points));
            return boundary;
        }
        Boundary boundary = new Boundary(dim);
        points.sort(getComparator(dim - 1));
        for (int i = 0; i < length; i++) {
            int begin = i * threshold * (int) Math.pow(length, dim - 1);
            int end = Math.min((i + 1) * threshold * (int) Math.pow(length, dim - 1), points.size());
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

    List<Point> points;

    public void build(String path) {
        List<String> lines = read(path);
        points = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Point point = new Point(line);
            points.add(point);
        }
        points.sort(getComparator(points.get(0).getDim() - 1));
        dataPartition(points, points.get(0).getDim());
    }

    public int getModelIndex(Boundary boundary, Point point, int dim) {
        // first get LeafModel
        if (dim == 0) {
            return boundary.getIndex();
        } else {
            int result = 0;
            int begin = 0;
            int end = boundary.getBoundries().size();
            while (begin <= end) {
                int mid = (begin + end) / 2;
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

    public void pointQuery(List<Point> points) {
        System.out.println("pointQuery");
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            int modelIndex = getModelIndex(boundary, point, point.getDim());
            LeafModel model = partitionModels.get(modelIndex);
            if (model.getChildren().contains(point)) {
//                System.out.println("right");
//                System.out.println(i + "   " + modelIndex);
            } else {
                System.out.println("wrong");
                System.out.println(i + "   " + modelIndex);
            }
        }
//        points.forEach(point -> {
//            int modelIndex = getModelIndex(boundary, point, point.getDim());
//            LeafModel model = partitionModels.get(modelIndex);
//            if (model.getChildren().contains(point)) {
//                System.out.println("right");
//            } else {
//                System.out.println("wrong");
//
//            }
//        });
    }


    public static void main(String[] args) {
        PartitionModelRtree partitionModelRtree = new PartitionModelRtree(10000, "H", 100, "LinearRegression");
        partitionModelRtree.build("/Users/guanli/Documents/datasets/RLRtree/raw/uniform_160000_1_2_.csv");
//        partitionModelRtree.build("D:\\datasets\\RLRtree\\raw\\normal_160000_1_2_.csv", all.get(i));
        partitionModelRtree.pointQuery(partitionModelRtree.points);
    }

}