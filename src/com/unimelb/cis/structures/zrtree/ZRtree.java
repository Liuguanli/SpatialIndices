package com.unimelb.cis.structures.zrtree;

import com.unimelb.cis.CSVFileWriter;
import com.unimelb.cis.ZCurve;
import com.unimelb.cis.node.LeafNode;
import com.unimelb.cis.node.Node;
import com.unimelb.cis.node.NonLeafNode;
import com.unimelb.cis.node.Point;
import com.unimelb.cis.structures.IRtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.unimelb.cis.CSVFileReader.read;
import static com.unimelb.cis.Constant.PAGE_SIZE;

public class ZRtree extends IRtree {

    private NonLeafNode root;

    private List<Node> leafNodes;

    int totalLevel = 0;

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
    public boolean buildRtree(String path) {
        List<String> lines = read(path);


        List<Point> points = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Point point = new Point(line);
            points.add(point);
        }

        int dimension = points.get(0).getDimension();

        for (int i = 0; i < dimension; i++) {
            sortDimensiont(points, i);
        }

        points = ZCurve.zCurve(points);

        System.out.println(points.get(points.size() - 1));

        LeafNode leafNode = null;
        List<Node> childrenNodes = new ArrayList<>();
        int currentLevel = 1;
        for (int i = 0; i < points.size(); i++) {
            if (i % PAGE_SIZE == 0) {
                leafNode = new LeafNode(PAGE_SIZE);
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
                if (i % PAGE_SIZE == 0) {
                    nonLeafNode = new NonLeafNode(PAGE_SIZE);
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

        return true;
    }

    public void output() {
        CSVFileWriter.write(this, "datasets/Z_normal_2000000_.csv");
    }

    public List<String> getOutput() {
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
        return lines;
    }

    public static void main(String[] args) {
        ZRtree zRtree = new ZRtree();

        zRtree.buildRtree("datasets/normal_2000000_.csv");

        zRtree.output();
    }

}
