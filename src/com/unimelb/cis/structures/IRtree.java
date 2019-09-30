package com.unimelb.cis.structures;

import com.unimelb.cis.HilbertCurve;
import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.LeafNode;
import com.unimelb.cis.node.Node;
import com.unimelb.cis.node.NonLeafNode;
import com.unimelb.cis.node.Point;
import com.unimelb.cis.utils.ExpReturn;
import com.unimelb.cis.utils.Visualizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class IRtree {

    public IRtree() {
    }

    public IRtree(int pagesize) {
        this.pagesize = pagesize;
    }

    protected Node root;

    protected List<Node> leafNodes;

    protected List<Point> points;

    protected String dataFile;

    protected int pagesize;

    public Node getRoot() {
        return root;
    }

    public List<Node> getLeafNodes() {
        return leafNodes;
    }

    protected int level;

    protected int dim;

    public int getDim() {
        return dim;
    }

    public void setDim(int dim) {
        this.dim = dim;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public abstract boolean buildRtree(String path);

    public abstract boolean buildRtree(List<Point> points);

    public abstract ExpReturn windowQuery(Mbr window);

    public abstract ExpReturn pointQuery(List<Point> points);

    public abstract ExpReturn pointQuery(Point point);

    public abstract ExpReturn insert(List<Point> points);

    public abstract ExpReturn insert(Point point);

    public abstract NonLeafNode buildRtreeAfterTuning(String path, int dim, int level);

    public Visualizer visualize(int width, int height) {
        Mbr view = new Mbr(0, 0, 1, 1);
        return new Visualizer(this, width, height, view);
    }

    public int bitNum;
    public Map<Integer, List<Float>> axisLocations = new HashMap<>();
    public List<Long> curveValues = new ArrayList<>();

    public NonLeafNode findNode(LeafNode leafNode) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(root);
        while (nodes.size() > 0) {
            Node top = nodes.remove(0);
            if (top instanceof NonLeafNode) {
                NonLeafNode nonLeafNode = (NonLeafNode) top;
                if (nonLeafNode.getChildren().contains(leafNode)) {
                    return nonLeafNode;
                } else {
                    nodes.addAll(nonLeafNode.getChildren());
                }
            }
        }
        return null;
    }

    public int binarySearch(List<Long> values, Long targer) {
        int begin = 0;
        int end = values.size() - 1;
        if (targer <= values.get(begin)) {
            return begin;
        }
        if (targer >= values.get(end)) {
            return end;
        }
        int mid = (begin + end) / 2;
        while (values.get(mid) > targer || values.get(mid + 1) < targer) {
            if (values.get(mid) > targer) {
                end = mid;
            } else if (values.get(mid) < targer) {
                begin = mid;
            } else {
                return mid;
            }
            mid = (begin + end) / 2;
        }
        return mid;
    }

    public int binarySearch(List<Float> values, float targer) {
        int begin = 0;
        int end = values.size() - 1;
        if (targer <= values.get(begin)) {
            return begin;
        }
        if (targer >= values.get(end)) {
            return end;
        }
        int mid = (begin + end) / 2;
        while (values.get(mid) > targer || values.get(mid + 1) < targer) {
            if (values.get(mid) > targer) {
                end = mid;
            } else if (values.get(mid) < targer) {
                begin = mid;
            } else {
                return mid;
            }
            mid = (begin + end) / 2;
        }
        return mid;
    }

}
