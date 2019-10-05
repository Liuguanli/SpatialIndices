package com.unimelb.cis.structures.zrtree;

import com.unimelb.cis.CSVFileWriter;
import com.unimelb.cis.HilbertCurve;
import com.unimelb.cis.ZCurve;
import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.*;
import com.unimelb.cis.structures.IRtree;
import com.unimelb.cis.structures.RLRtree;
import com.unimelb.cis.utils.ExpReturn;

import java.util.*;
import java.util.function.Consumer;

import static com.unimelb.cis.CSVFileReader.read;

public class ZRtree extends RLRtree {

//    public ZRtree() {
//    }

    public ZRtree(int pagesize) {
        super(pagesize);
    }


    @Override
    public ExpReturn buildRtree(String path) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        this.dataFile = path;
        List<String> lines = read(path);

        List<Point> points = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Point point = new Point(line);
            points.add(point);
        }

        buildRtree(points);

        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
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
        points = ZCurve.zCurve(points);
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
        root.setLevel(currentLevel);
        this.setLevel(currentLevel);
        this.setDim(dim);
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
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
    public ExpReturn pointQuery(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        points.forEach(point -> {
            expReturn.pageaccess += pointQuery(point).pageaccess;
        });
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn knnQuery(Point point, int k) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        PriorityQueue<Object> queue = getQueue(point, k);
        ArrayList<Node> list = new ArrayList();
        list.add(root);
        while (list.size() > 0) {
            Node top = list.remove(0);
            if (top instanceof NonLeafNode) {
                NonLeafNode nonLeaf = (NonLeafNode) top;
                List<Node> children = nonLeaf.getChildren();
                for (int i = 0; i < children.size(); i++) {
                    Node former = children.get(i);
                    boolean isProne = false;
                    for (int j = 0; j < children.size(); j++) {
                        if (i == j) {
                            continue;
                        }
                        Node later = children.get(j);
                        if (former.getMbr().calMINDIST(point) > later.getMbr().calMINMAXDIST(point)) {
                            isProne = true;
                            break;
                        }
                    }
                    if (!isProne) {
                        list.add(former);
                    }
                }
                expReturn.pageaccess++;
            } else if (top instanceof LeafNode) {
                LeafNode leaf = (LeafNode) top;
                List<Point> children = leaf.getChildren();
                queue.addAll(children);
                expReturn.pageaccess++;
            } else {
                queue.add(top);
            }
        }
        for (int i = 0; i < k; i++) {
            expReturn.result.add((Point) queue.poll());
        }
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }


    @Override
    public ExpReturn pointQuery(Point point) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
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
                    break;
                }
            }

        }
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    public ExpReturn insert(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        points.forEach(point -> {
            long[] indexOrder = new long[dim];
            for (int i = 0; i < dim; i++) {
                indexOrder[i] = binarySearch(axisLocations.get(i), point.getLocation()[i]);
            }
            int pos = binarySearch(curveValues, ZCurve.getZcurve(indexOrder, bitNum));
            int index = pos / pagesize;
            if (index > 0 && index < leafNodes.size()) {
                LeafNode node = (LeafNode) leafNodes.get(index);
                if (node.isFull()) {
                    NonLeafNode parent = findNode(node);
                    if (parent != null) {
                        int nodeIndex = parent.getChildren().indexOf(node);
                        LeafNode newLeafNode = node.split();
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

    @Override
    public NonLeafNode buildRtreeAfterTuning(String path, int dim, int level) {
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

//        for (int i = 0; i < level - 1; i++) {
//            ((NonLeafNode) nodes[i + 1]).add(nodes[i]);
//        }
        List<Point> points = new ArrayList<>();
        root.add(nodes[level - 1]);
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
                for (int j = 1; j < level; j++) {
                    ((NonLeafNode) nodes[j]).add(nodes[j - 1]);
                }
                continue;
            }

            for (int j = 0; j < level; j++) {
                int index = Integer.valueOf(items[dim + j]);
                if (j == 0) {
                    if (index == levelIndex[j]) {
                        ((LeafNode) nodes[j]).add(point);
                        break;
                    } else {
                        levelIndex[j] = index;
                        LeafNode leafNode = new LeafNode(pagesize, dim);
                        leafNode.add(point);
                        nodes[j] = leafNode;
                    }
                } else {
                    if (index == levelIndex[j]) {
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
        points = ZCurve.zCurve(points);
        points.forEach(point -> curveValues.add(point.getCurveValue()));
        root.add(nodes[level - 1]);
        this.root = root;
        return root;
    }

    @Override
    public void output(String file) {
        List<String> lines = new ArrayList<>();
        List<Node> nodes = new ArrayList<>();
        nodes.add(root);
        while (nodes.size() > 0) {
            Node top = nodes.remove(0);
            if (top instanceof NonLeafNode) {
                nodes.addAll(((NonLeafNode) top).getChildren());
            } else if (top instanceof LeafNode) {
                nodes.addAll(((LeafNode) top).getChildren());
            } else {
                lines.add(((Point) top).getOutPutString(root));
            }
        }
        CSVFileWriter.write(lines, file);
    }

    public static void main(String[] args) {
        ZRtree zRtree = new ZRtree(100);

        zRtree.buildRtree("/Users/guanli/Documents/datasets/RLRtree/raw/uniform_10000_1_2_.csv");

//        zRtree.output("/Users/guanli/Documents/datasets/RLRtree/trees/Z_uniform_10000_1_2_.csv");

//        zRtree.buildRtreeAfterTuning("/Users/guanli/Documents/datasets/RLRtree/trees/Z_uniform_160000_1_2_.csv", 2, 2);
//        zRtree.buildRtree("D:\\datasets\\RLRtree\\raw\\uniform_1000000_1_2_.csv");
//        zRtree.getRoot();

//        System.out.println(zRtree.windowQuery(Mbr.getMbrs(0.01f, 10, 3).get(0)));
//        System.out.println(zRtree.windowQuery(Mbr.getMbrs(0.01f, 9, 3).get(0)));
//        System.out.println(zRtree.windowQuery(Mbr.getMbrs(0.01f, 11, 3).get(0)));

//        zRtree.visualize(600, 600).save("ztree_skewed.png");

//        zRtree.pointQuery(zRtree.getPoints());


//        System.out.println("point query:" + zRtree.pointQuery(zRtree.points));
//        zRtree.insert(new Point(0.5f, 0.5f));
        System.out.println("knn query:" + zRtree.knnQuery(new Point(0.5f, 0.5f), 1));


//        Mbr mbr = new Mbr(1,2,3,4);


//        List<Point> points = new ArrayList<>();
//        mbr.getAllVertexs(mbr, 0, 2, new float[2], points);

//        System.out.println(points);


    }

}