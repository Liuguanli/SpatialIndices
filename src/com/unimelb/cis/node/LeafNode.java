package com.unimelb.cis.node;

import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.utils.ExpReturn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.E;

public class LeafNode extends Node {

    private List<Point> children;

    public LeafNode() {
        children = new ArrayList<>();
        this.level = 1;
    }

    public LeafNode(int pageSize, int dim) {
        super(pageSize, dim);
        children = new ArrayList<>();
        this.level = 1;
    }

    @Override
    public void adjust() {
        updateMbr();
    }

    public boolean addForChooseSub(Point point) {
        children.add(point);
//        updateMbr(point);
        updateMbr(point, dim);
        return true;
    }

    public boolean add(Point point) {
        if (isFull()) {
            return false;
        }
        children.add(point);
//        updateMbr(point);
        updateMbr(point, dim);
        if (oMbr == null) {
            oMbr = mbr.clone();
        }
        point.setParent(this);
        return true;
    }

    public boolean addAll(List<Point> points) {
        if (children.size() + points.size() > pageSize) {
            return false;
        }
        children.addAll(points);
        for (Point point : points) {
            updateMbr(point, dim);
        }
        setOMbr(mbr.clone(), true);
        return true;
    }

    public LeafNode copy() {
        LeafNode result = new LeafNode();
        result.pageSize = this.pageSize;
        result.parent = this.parent;
        for (int i = 0; i < children.size(); i++) {
            result.add(children.get(i));
        }
        return result;
    }

    public List<Point> getChildren() {
        return children;
    }

    private void updateMbr() {
        mbr = new Mbr(dim);
        for (int i = 0; i < children.size(); i++) {
            updateMbr(children.get(i), dim);
        }
    }

//    private void updateMbr(Point point) {
//        float x = point.getX();
//        float y = point.getY();
//        if (mbr.getX1() > x) {
//            mbr.setX1(x);
//            mbr.getLocation()[0] = x;
//        }
//        if (mbr.getX2() < x) {
//            mbr.setX2(x);
//            mbr.getLocation()[2] = x;
//        }
//        if (mbr.getY1() > y) {
//            mbr.setY1(y);
//            mbr.getLocation()[1] = y;
//        }
//        if (mbr.getY2() < y) {
//            mbr.setY2(y);
//            mbr.getLocation()[3] = y;
//        }
//    }

    private void updateMbr(Point point, int dim) {
//        for (int i = 0; i < dim; i++) {
//            float val = point.getLocation()[i];
//            if (mbr.getLocation()[i] > val) {
//                mbr.getLocation()[i] = val;
//            }
//            if (mbr.getLocation()[i + dim] < val) {
//                mbr.getLocation()[i + dim] = val;
//            }
//        }
        this.getMbr().updateMbr(point, dim);
    }

    public boolean isFull() {
        return children.size() == pageSize;
    }

    /**
     * split the node from middle
     *
     * @return
     */
    public LeafNode splitByPosition(int splitPosition) {
        // right part
        LeafNode leafNode = new LeafNode(pageSize, dim);
        leafNode.addAll(new ArrayList<>(children.subList(splitPosition, pageSize)));
        leafNode.setParent(this.parent);
        leafNode.setLevel(this.getLevel());

        // left part
        List<Point> temp = new ArrayList<>(children.subList(0, splitPosition));
        children.clear();
        this.addAll(temp);
        this.updateMbr();
        leafNode.setLevel(this.getLevel());
        return leafNode;
    }

    public LeafNode splitByBisection() {
        return splitByPosition(pageSize / 2);
    }

    public Comparator<Point> getComparator(final int index) {
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

    /**
     * original R*tree split
     *
     * @param m
     * @param insertedPoint
     * @return
     */
    public LeafNode splitRStar(int m, Point insertedPoint, boolean isRevisited) {
        int minAxis = Integer.MAX_VALUE;
        float minPerim = Float.MAX_VALUE;
        float minOverlapVol = Float.MAX_VALUE;
        float minOverlapPerim = Float.MAX_VALUE;
        LeafNode result;
        List<Point> points = new ArrayList<>(children);
        points.add(insertedPoint);
        for (int j = 0; j < dim; j++) {
            final int index = j;
            points.sort(getComparator(index));

            float tempPerimeter = 0;
            for (int i = m; i < points.size() - m; i++) {
                List<Point> left = points.subList(0, i);
                List<Point> right = points.subList(i, points.size());

                LeafNode leafNode = new LeafNode(pageSize, dim);
                leafNode.addAll(new ArrayList<>(left));
                leafNode.setParent(this.parent);
                leafNode.setLevel(this.getLevel());

                LeafNode rightNode = new LeafNode(pageSize, dim);
                rightNode.addAll(new ArrayList<>(right));
                rightNode.setParent(this.parent);
                rightNode.setLevel(this.getLevel());

                tempPerimeter += leafNode.getMbr().perimeter() + rightNode.getMbr().perimeter();
            }
            if (tempPerimeter < minPerim) {
                minPerim = tempPerimeter;
                minAxis = j;
            }
        }

        minPerim = Float.MAX_VALUE;
        int minOvlpPerimI = -1;
        int minOvlpVolI = -1;
        int minPerimI = -1;
        points.sort(getComparator(minAxis));
        int minI = 0;
        double minW = Double.MAX_VALUE;
        boolean isSwitchToPerim = false;

        List<Point> left = points.subList(0, m);
        List<Point> right = points.subList(m, points.size());
        LeafNode leftNode = new LeafNode(pageSize, dim);
        leftNode.addAll(new ArrayList<>(left));
        LeafNode rightNode = new LeafNode(pageSize, dim);
        rightNode.addAll(new ArrayList<>(right));

        if (isRevisited) {
            if ((leftNode.getMbr().volume() == 0 || rightNode.getMbr().volume() == 0)) {
                isSwitchToPerim = true;
            }
        }

        LeafNode tempForPerimMax = new LeafNode(points.size(), dim);
        tempForPerimMax.addAll(points);
        float maxPerim = tempForPerimMax.getMbr().getPerimMax();

        for (int i = m; i < points.size() - m; i++) {
            left = points.subList(0, i);
            right = points.subList(i, points.size());

            leftNode = new LeafNode(pageSize, dim);
            leftNode.addAll(new ArrayList<>(left));
            leftNode.setParent(this.parent);
            leftNode.setLevel(this.getLevel());

            rightNode = new LeafNode(pageSize, dim);
            rightNode.addAll(new ArrayList<>(right));
            rightNode.setParent(this.parent);
            rightNode.setLevel(this.getLevel());

            // from Revisited. Last line of section 4.1
            float tempOverlapPerim = leftNode.getMbr().getOverlapPerim(rightNode.getMbr());
            float tempOverlapVol = leftNode.getMbr().getOverlapVol(rightNode.getMbr());
            float tempPerim = leftNode.getMbr().perimeter() + rightNode.getMbr().perimeter();

            if (tempOverlapVol == 0) {
                minOverlapVol = 0;
                if (tempPerim < minPerim) {
                    minPerim = tempPerim;
                    minPerimI = i;
                }
            } else {
                if (isSwitchToPerim) {
                    if (tempOverlapPerim < minOverlapPerim) {
                        minOverlapPerim = tempOverlapPerim;
                        minOvlpPerimI = i;
                    }
                } else {
                    if (tempOverlapVol < minOverlapVol) {
                        minOverlapVol = tempOverlapVol;
                        minOvlpVolI = i;
                    }
                }
            }
            if (minOverlapVol == 0) {
                minI = minPerimI;
            } else {
//                if (isSwitchToPerim) {
//                    minI = minOvlpPerimI;
//                } else {
//                    minI = minOvlpVolI;
//                }
                minI = minOvlpVolI;
            }
//            if (isRevisited) {
//                double wf = weightFunction(m, i, 0.5, minAxis);
//                if (wf < 0) {
//                    // use the original R*tree method.
//                } else {
//                    double wg;
//                    double w;
//                    if (minOverlapVol == 0) {
//                        wg = tempPerim - maxPerim;
//                        w = wg * wf;
//                    } else {
//                        wg = minOverlapVol;
//                        w = wg/wf;
//                    }
//                    if (w < minW) {
//                        minI = i;
//                    }
//                }
//            }
        }
//        System.out.println("LeafNode minI:" + minI + " points.size():" + points.size() + " m:" + m);
        // right part
        result = new LeafNode(pageSize, dim);
        result.addAll(new ArrayList<>(points.subList(minI, points.size())));
        result.setParent(this.parent);
        result.setLevel(this.getLevel());

        // left part
        List<Point> temp = new ArrayList<>(points.subList(0, minI));
        children.clear();
        this.addAll(temp);
        this.updateMbr();
        this.updateOMbr();
        result.setLevel(this.getLevel());

        if (result.getChildren().contains(insertedPoint)) {
            insertedPoint.setParent(result);
        } else {
            insertedPoint.setParent(this);
        }

        return result;
    }

    public List<Point> reInsert(int p, Point insertedPoint) {
        List<Point> points = new ArrayList<>(children);
        points.add(insertedPoint);
        // descending order
        points.sort((o1, o2) -> {
            double d1 = getMbr().getDistToCenter(o1);
            double d2 = getMbr().getDistToCenter(o1);
            if (d1 > d2) {
                return -1;
            } else if (d1 < d2) {
                return 1;
            } else {
                return 0;
            }
        });
        List<Point> removed = points.subList(0, p);
        children.clear();
        children = points.subList(p, points.size());
        this.updateMbr();
        this.updateOMbr();

        if (children.contains(insertedPoint)) {
            insertedPoint.setParent(this);
        }
        return removed;
    }

    public Mbr genNewMbr() {
        Mbr temp = new Mbr(2);
        for (int i = 0; i < children.size(); i++) {
            Point point = children.get(i);
            temp.updateMbr(point, dim);
        }
        return temp;
    }

    public LeafNode split() {
        //right part
        LeafNode leafNode = new LeafNode(pageSize, dim);
        leafNode.addAll(children.subList(pageSize / 2, pageSize));
        // left part
        List<Point> temp = new ArrayList<>(children.subList(0, pageSize / 2));
        children.clear();
        this.addAll(temp);
        return leafNode;
    }

    @Override
    public String toString() {
        return "LeafNode{" +
                "children=" + children +
                ", mbr=" + mbr +
                ", parent=" + parent +
                ", level=" + level +
                ", orderInLevel=" + orderInLevel +
                ", pageSize=" + pageSize +
                ", dist=" + dist +
                '}';
    }
}
