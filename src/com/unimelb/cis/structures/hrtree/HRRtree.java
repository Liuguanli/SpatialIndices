package com.unimelb.cis.structures.hrtree;

import com.unimelb.cis.CSVFileWriter;
import com.unimelb.cis.HilbertCurve;
import com.unimelb.cis.ZCurve;
import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.LeafNode;
import com.unimelb.cis.node.Node;
import com.unimelb.cis.node.NonLeafNode;
import com.unimelb.cis.node.Point;
import com.unimelb.cis.structures.RLRtree;
import com.unimelb.cis.structures.zrtree.ZRtree;
import com.unimelb.cis.utils.ExpReturn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static com.unimelb.cis.CSVFileReader.read;

public class HRRtree extends RLRtree {

//
//    public HRRtree() {
//    }

    public HRRtree(int pagesize) {
        super(pagesize);
    }

    public Node getRoot() {
        return root;
    }

    public List<Node> getLeafNodes() {
        return leafNodes;
    }

    @Override
    public ExpReturn buildRtree(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        bitNum = (int) (Math.log(points.size()) / Math.log(2.0)) + 1;
        dim = points.get(0).getDim();
        for (int i = 0; i < dim; i++) {
            List<Float> locations = new ArrayList<>();
            int finalI = i;
            points.forEach(point -> locations.add(point.getLocation()[finalI]));
            locations.sort(Float::compareTo);
            axisLocations.put(i, locations);
        }
        points = HilbertCurve.hilbertCurve(points);
        points.forEach(point -> curveValues.add(point.getCurveValue()));
        this.points = points;
        LeafNode leafNode = null;
        List<Node> childrenNodes = new ArrayList<>();
        int currentLevel = 0;
        for (int i = 0; i < points.size(); i++) {
            if (i % pagesize == 0) {
                leafNode = new LeafNode(pagesize, dim);
                leafNode.setLevel(currentLevel);
                childrenNodes.add(leafNode);
            }
            Point point = points.get(i);
            leafNode.add(point);
            point.setParent(leafNode);
            point.setOrderInLevel(i);
        }
        currentLevel++;
        leafNodes = new ArrayList<>(childrenNodes);

        List<Node> nonLeafNodes = new ArrayList<>();
        NonLeafNode nonLeafNode = null;
        while (childrenNodes.size() != 1) {
            for (int i = 0; i < childrenNodes.size(); i++) {
                if (i % pagesize == 0) {
                    nonLeafNode = new NonLeafNode(pagesize, dim);
                    nonLeafNode.setLevel(currentLevel);
                    nonLeafNodes.add(nonLeafNode);
                }
                Node temp = childrenNodes.get(i);
                nonLeafNode.add(temp);
                temp.setOrderInLevel(i);
                temp.setParent(nonLeafNode);
            }
            currentLevel++;
            childrenNodes = new ArrayList<>(nonLeafNodes);
            nonLeafNodes.clear();
        }

        root = childrenNodes.get(0);
        this.setLevel(currentLevel);
        this.setDim(dim);
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn buildRtree(String path) {
        this.dataFile = path;
        List<String> lines = read(path);

        List<Point> points = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Point point = new Point(line);
            points.add(point);
        }

        return buildRtree(points);
    }

    @Override
    public ExpReturn windowQuery(Mbr window) {
        ExpReturn expReturn = new ExpReturn();
        int pageAccessNum = 0;
        long begin = System.nanoTime();
        ArrayList<Node> list = new ArrayList();
        list.add(root);
        while (list.size() > 0) {
            Node top = list.remove(0);
            if (top instanceof NonLeafNode) {
                NonLeafNode nonLeaf = (NonLeafNode) top;
                if (nonLeaf.getMbr().interact(window)) {
                    List<Node> children = nonLeaf.getChildren();
                    list.addAll(list.size(), children);
                    pageAccessNum++;
                }
            } else if (top instanceof LeafNode) {
                LeafNode leaf = (LeafNode) top;
                if (leaf.getMbr().interact(window)) {
                    List<Point> children = leaf.getChildren();
                    for (int i = 0; i < children.size(); i++) {
                        if (window.contains(children.get(i))) {
                            expReturn.result.add(children.get(i));
                        }
                    }
                    pageAccessNum++;
                }
            }
        }
        long end = System.nanoTime();
        expReturn.pageaccess = pageAccessNum;
        expReturn.time = end - begin;
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
    public ExpReturn pointQuery(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        points.forEach(point -> {
            List<Node> nodes = new ArrayList<>();
            nodes.add(root);
            while (nodes.size() > 0) {
                Node top = nodes.remove(0);
                if (top instanceof NonLeafNode) {
                    if (top.getMbr().contains(point)) {
                        expReturn.pageaccess++;
                        nodes.addAll(((NonLeafNode) top).getChildren());
                    }
                } else if (top instanceof LeafNode) {
                    if (top.getMbr().contains(point)) {
                        expReturn.pageaccess++;
                        if (((LeafNode) top).getChildren().contains(point)) {
                            break;
                        }
                    }
                }
            }
        });
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn knnQuery(Point point, int k) {
//        ExpReturn expReturn = new ExpReturn();
//        long begin = System.nanoTime();
//        PriorityQueue<Object> queue = getQueue(point, k);
//        ArrayList<Node> list = new ArrayList();
//        list.add(root);
//        while (list.size() > 0) {
//            Node top = list.remove(0);
//            if (top instanceof NonLeafNode) {
//                NonLeafNode nonLeaf = (NonLeafNode) top;
//                List<Node> children = nonLeaf.getChildren();
//                for (int i = 0; i < children.size(); i++) {
//                    Node former = children.get(i);
//                    boolean isProne = false;
//                    for (int j = 0; j < children.size(); j++) {
//                        if (i == j) {
//                            continue;
//                        }
//                        Node later = children.get(j);
//                        if (former.getMbr().calMINDIST(point) > later.getMbr().calMINMAXDIST(point)) {
//                            isProne = true;
//                            break;
//                        }
//                    }
//                    if (!isProne) {
//                        list.add(former);
//                    }
//                }
//                expReturn.pageaccess++;
//            } else if (top instanceof LeafNode) {
//                LeafNode leaf = (LeafNode) top;
//                List<Point> children = leaf.getChildren();
//                queue.addAll(children);
//                expReturn.pageaccess++;
//            } else {
//                queue.add(top);
//            }
//        }
//        for (int i = 0; i < k; i++) {
//            expReturn.result.add((Point) queue.poll());
//        }
//        long end = System.nanoTime();
//        expReturn.time = end - begin;
//        return expReturn;
        float knnquerySide = (float) Math.sqrt((float) k / points.size());
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        while (true) {
            Mbr window = Mbr.getMbr(point, knnquerySide);
            ExpReturn tempExpReturn = windowQuery(window);
            List<Point> tempResult = tempExpReturn.result;
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
        }
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public NonLeafNode buildRtreeAfterTuning(String path, int dim, int level) {
        level--;
        this.dataFile = path;
        this.setDim(dim);
        this.setLevel(level);
        List<String> lines = read(path);
//        List<Point> points = new ArrayList<>(lines.size());
        int[] levelIndex = new int[level];
        Node[] nodes = new Node[level];
        NonLeafNode root = new NonLeafNode(pagesize, dim);
        nodes[0] = new LeafNode(pagesize, dim);
        for (int i = 1; i < level; i++) {
            nodes[i] = new NonLeafNode(pagesize, dim);
        }

        List<Point> points = new ArrayList<>();
//        root.add(nodes[level - 1]);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] items = line.split(",");

            float[] locations = new float[dim];
            for (int j = 0; j < dim; j++) {
                locations[j] = Float.valueOf(items[j]);
            }
            Point point = new Point(0, locations);
            points.add(point);
            if (i == 0) {
                ((LeafNode) nodes[0]).add(point);
//                for (int j = 1; j < level; j++) {
//                    ((NonLeafNode) nodes[j]).add(nodes[j - 1]);
//                }
//                root.add(nodes[level - 1]);
                continue;
            }

            for (int j = 0; j < level; j++) {
                int index = Integer.valueOf(items[dim + j]);
                if (j == 0) {
                    if (index <= levelIndex[j]) {
                        ((LeafNode) nodes[j]).add(point);
                        break;
                    } else {
                        levelIndex[j] = index;
                        LeafNode leafNode = new LeafNode(pagesize, dim);
                        leafNode.add(point);
                        nodes[j] = leafNode;
                    }
                } else {
                    if (index <= levelIndex[j]) {
                        ((NonLeafNode) nodes[j]).add(nodes[j - 1]);
                        break;
                    } else {
                        if (j == level - 1) {
                            root.add(nodes[j]);
                        }
                        levelIndex[j] = index;
                        NonLeafNode nonLeafNode = new NonLeafNode(pagesize, dim);
                        nodes[j] = nonLeafNode;
                        ((NonLeafNode) nodes[j]).add(nodes[j - 1]);
                    }
                }
            }
//            points.add(point);
        }
        this.points = points;
        for (int i = 0; i < dim; i++) {
            List<Float> locations = new ArrayList<>();
            int finalI = i;
            points.forEach(point -> locations.add(point.getLocation()[finalI]));
            locations.sort(Float::compareTo);
            axisLocations.put(i, locations);
        }
        points = HilbertCurve.hilbertCurve(points);
        points.forEach(point -> curveValues.add(point.getCurveValue()));
        root.add(nodes[level - 1]);
        this.root = root;
        return root;
    }

    public ExpReturn insert(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        points.forEach(point -> {
            long[] indexOrder = new long[dim];
            for (int i = 0; i < dim; i++) {
                indexOrder[i] = binarySearch(axisLocations.get(i), point.getLocation()[i]);
            }
            int pos = binarySearch(curveValues, HilbertCurve.getHilbertValue(bitNum, indexOrder));
            int index = pos / pagesize;
            if (index > 0 && index < leafNodes.size()) {
                LeafNode node = (LeafNode) leafNodes.get(index);
                if (node.isFull()) {
                    NonLeafNode parent = findNode(node);
                    if (parent != null) {
                        int nodeIndex = parent.getChildren().indexOf(node);
                        LeafNode newLeafNode = node.split();
                        expReturn.pageaccess++;
                        NonLeafNode child = new NonLeafNode(pagesize, dim);
                        child.add(node);
                        child.add(newLeafNode);

                        parent.getChildren().set(nodeIndex, child);
                        leafNodes.add(index + 1, newLeafNode);
                    }
                }
            }
        });
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn insert(Point point) {
        return insert(Arrays.asList(point));
    }


    public static void main(String[] args) {
        HRRtree hRRtree = new HRRtree(100);

//        hRRtree.buildRtree("D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_2_.csv");
//        hRRtree.buildRtree("/Users/guanli/Documents/datasets/RLRtree/raw/uniform_10000_1_2_.csv");

        hRRtree.buildRtreeAfterTuning("/Users/guanli/Documents/datasets/RLRtree/newtrees/H_uniform_10000_1_2_DQN.csv", 2, 3);
//        hRRtree.visualize(600, 600).save("DQN_uniform_1000_1_2_.png");

//        zRtree.output("/Users/guanli/Documents/datasets/RLRtree/trees/Z_uniform_10000_1_2_.csv");

//        zRtree.buildRtreeAfterTuning("/Users/guanli/Documents/datasets/RLRtree/trees/Z_uniform_10000_1_3_.csv", zRtree.getDim(), zRtree.getLevel());
//        zRtree.getRoot();

        System.out.println(hRRtree.windowQuery(Mbr.getMbrs(0.01f, 10, 2).get(0)));
//        System.out.println(zRtree.windowQuery(Mbr.getMbrs(0.01f, 9, 3).get(0)));
//        System.out.println(zRtree.windowQuery(Mbr.getMbrs(0.01f, 11, 3).get(0)));
//        System.out.println(hRRtree.pointQuery(hRRtree.getPoints()));
//        hRRtree.insert(new Point(0.5f,0.5f));
        System.out.println("knn query:" + hRRtree.knnQuery(new Point(0.5f, 0.5f), 1));
    }

}
