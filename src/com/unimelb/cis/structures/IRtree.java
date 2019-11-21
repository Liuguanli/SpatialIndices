package com.unimelb.cis.structures;

import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.LeafNode;
import com.unimelb.cis.node.Node;
import com.unimelb.cis.node.NonLeafNode;
import com.unimelb.cis.node.Point;
import com.unimelb.cis.utils.ExpReturn;
import com.unimelb.cis.utils.Visualizer;

import java.util.*;

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

    public List<Point> getQueryPoints(double percentage) {
        Collections.shuffle(points, new Random(0));
        return points.subList(0, (int) (points.size() * percentage));
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public abstract ExpReturn buildRtree(String path);

    public abstract ExpReturn buildRtree(List<Point> points);

    public abstract ExpReturn windowQuery(Mbr window);

    public abstract ExpReturn windowQuery(List<Mbr> windows);

    public abstract ExpReturn pointQuery(List<Point> points);

    public abstract ExpReturn windowQueryByScanAll(List<Mbr> windows);

    public abstract ExpReturn knnQuery(Point point, int k);

    public abstract ExpReturn knnQuery(List<Point> points, int k);

    public abstract ExpReturn pointQuery(Point point);

    public abstract ExpReturn insert(List<Point> points);

    public abstract ExpReturn insert(Point point);

    public abstract Node buildRtreeAfterTuning(String path, int dim, int level);

    public double claAcc(List<Point> result, List<Point> accurateResult) {
        final int[] num = {0};
        result.forEach(point -> {
            if (accurateResult.contains(point))
                num[0]++;
        });
        return ((double) num[0]) / result.size();
    }

    public Visualizer visualize(int width, int height) {
        Mbr view = new Mbr(0, 0, 1, 1);
        return new Visualizer(this, width, height, view);
    }

    public Visualizer visualize(int width, int height, List<Mbr> mbrs) {
        Mbr view = new Mbr(0, 0, 1, 1);
        return new Visualizer(mbrs, width, height, view);
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

    public PriorityQueue<Object> getQueue(Point point, int k) {
        PriorityQueue<Object> queue = new PriorityQueue(k, (o1, o2) -> {
            double dist1;
            double dist2;
            if (o1 instanceof NonLeafNode) {
                dist1 = ((NonLeafNode) o1).getMbr().claDist(point);
            } else if (o1 instanceof LeafNode) {
                dist1 = ((LeafNode) o1).getMbr().claDist(point);
            } else {
                dist1 = ((Point) o1).calDist(point);
            }
            if (o2 instanceof NonLeafNode) {
                dist2 = ((NonLeafNode) o2).getMbr().claDist(point);
            } else if (o2 instanceof LeafNode) {
                dist2 = ((LeafNode) o2).getMbr().claDist(point);
            } else {
                dist2 = ((Point) o2).calDist(point);
            }
            if (dist1 > dist2) {
                return 1;
            } else if (dist1 < dist2) {
                return -1;
            } else {
                return 0;
            }
        });
        return queue;
    }

//    public double calMINDIST(Point point, Geometry geometry) {
//        geometry.mbr().x1();
//        geometry.mbr().y1();
//        geometry.mbr().x2();
//        geometry.mbr().y2();
//        double dist = 0;
//        if (geometry.intersects(point)) {
//            return 0;
//        } else {
//            if (point.x() < geometry.mbr().x1()) {
//                if (point.y() < geometry.mbr().y1()) {
//                    Point temp = Geometries.point(geometry.mbr().x1(),
//                            geometry.mbr().y1());
//                    dist = point.geometry().distance(temp);
//                } else if (point.y() > geometry.mbr().y2()) {
//                    Point temp = Geometries.point(geometry.mbr().x1(),
//                            geometry.mbr().y2());
//                    dist = point.geometry().distance(temp);
//                } else {
//                    dist = geometry.mbr().x1() - point.x();
//                }
//            } else if (point.x() > geometry.mbr().x2()) {
//                if (point.y() < geometry.mbr().y1()) {
//                    Point temp = Geometries.point(geometry.mbr().x2(),
//                            geometry.mbr().y1());
//                    dist = point.geometry().distance(temp);
//                } else if (point.y() > geometry.mbr().y2()) {
//                    Point temp = Geometries.point(geometry.mbr().x2(),
//                            geometry.mbr().y2());
//                    dist = point.geometry().distance(temp);
//                } else {
//                    dist = point.x() - geometry.mbr().x2();
//                }
//            } else {
//                if (point.y() < geometry.mbr().y1()) {
//                    dist = geometry.mbr().y1() - point.y();
//                } else {
//                    dist = point.y() - geometry.mbr().y2();
//                }
//            }
//        }
//        return dist;
//    }
//    public double calMINMAXDIST(Point point, Geometry geometry) {
//        double distX = 0;
//        double distY = 0;
//        float midX = (geometry.mbr().x1() + geometry.mbr().x2()) / 2;
//        float midY = (geometry.mbr().y1() + geometry.mbr().y2()) / 2;
//        if (point.x() < midX) {
//            if (point.y() < midY) {
//                Point temp = Geometries.point(geometry.mbr().x1(),
//                        geometry.mbr().y2());
//                distX = point.geometry().distance(temp);
//            } else {
//                Point temp = Geometries.point(geometry.mbr().x1(),
//                        geometry.mbr().y1());
//                distX = point.geometry().distance(temp);
//            }
//        } else {
//            if (point.y() < midY) {
//                Point temp = Geometries.point(geometry.mbr().x2(),
//                        geometry.mbr().y2());
//                distX = point.geometry().distance(temp);
//            } else {
//                Point temp = Geometries.point(geometry.mbr().x2(),
//                        geometry.mbr().y1());
//                distX = point.geometry().distance(temp);
//            }
//        }
//        if (point.y() < midY) {
//            if (point.x() < midX) {
//                Point temp = Geometries.point(geometry.mbr().x2(),
//                        geometry.mbr().y1());
//                distY = point.geometry().distance(temp);
//            } else {
//                Point temp = Geometries.point(geometry.mbr().x1(),
//                        geometry.mbr().y1());
//                distY = point.geometry().distance(temp);
//            }
//        } else {
//            if (point.x() > midX) {
//                Point temp = Geometries.point(geometry.mbr().x1(),
//                        geometry.mbr().y2());
//                distY = point.geometry().distance(temp);
//            } else {
//                Point temp = Geometries.point(geometry.mbr().x2(),
//                        geometry.mbr().y2());
//                distY = point.geometry().distance(temp);
//            }
//        }
//        return distX < distY ? distX : distY;
//    }

}
