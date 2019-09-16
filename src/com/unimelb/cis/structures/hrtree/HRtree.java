package com.unimelb.cis.structures.hrtree;

import com.unimelb.cis.CSVFileWriter;
import com.unimelb.cis.HilbertCurve;
import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.LeafNode;
import com.unimelb.cis.node.Node;
import com.unimelb.cis.node.NonLeafNode;
import com.unimelb.cis.node.Point;
import com.unimelb.cis.structures.IRtree;
import com.unimelb.cis.utils.ExpReturn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.unimelb.cis.CSVFileReader.read;

public class HRtree extends IRtree {


    public HRtree() {
    }

    public HRtree(int pagesize) {
        super(pagesize);
    }

    public NonLeafNode getRoot() {
        return root;
    }

    public List<Node> getLeafNodes() {
        return leafNodes;
    }

    public void sortDimensiont(List<Point> points, int dimension) {
        Collections.sort(points, new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                if (p1.getLocation()[dimension] > p2.getLocation()[dimension]) {
                    return 1;
                } else if (p1.getLocation()[dimension] < p2.getLocation()[dimension]) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        for (int i = 0; i < points.size(); i++) {
            points.get(i).getLocationOrder()[dimension] = (i + 1);
        }
    }

    @Override
    public String getDataFile() {
        return dataFile;
    }

    @Override
    public boolean buildRtree(String path) {
        this.dataFile = path;
        List<String> lines = read(path);

        List<Point> points = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Point point = new Point(line);
            points.add(point);
        }

        int dimension = points.get(0).getDim();
        for (int i = 0; i < dimension; i++) {
            sortDimensiont(points, i);
        }

        points = HilbertCurve.hilbertCurve(points);

        LeafNode leafNode = null;
        List<Node> childrenNodes = new ArrayList<>();
        int currentLevel = 0;
        for (int i = 0; i < points.size(); i++) {
            if (i % pagesize == 0) {
                leafNode = new LeafNode(pagesize, dimension);
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
                    nonLeafNode = new NonLeafNode(pagesize, dimension);
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

        root = (NonLeafNode) childrenNodes.get(0);
        this.setLevel(currentLevel);
        this.setDim(dimension);
        return true;
    }

    @Override
    public ExpReturn windowQuery(Mbr window) {
        int pageAccessNum = 0;
        long begin = System.nanoTime();
        List<Point> retults = new ArrayList<>();
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
                            retults.add(children.get(i));
                        }
                    }
                    pageAccessNum++;
                }
            }
        }
        long end = System.nanoTime();
        ExpReturn expReturn = new ExpReturn();
        expReturn.pageaccess = pageAccessNum;
        expReturn.time = end - begin;
        return expReturn;
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
        root.add(nodes[level - 1]);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] items = line.split(",");

            float[] locations = new float[dim];
            for (int j = 0; j < dim; j++) {
                locations[j] = Float.valueOf(items[j]);
            }

            Point point = new Point(0, locations);

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
}
