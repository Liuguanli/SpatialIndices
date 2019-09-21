package com.unimelb.cis.node;

import com.unimelb.cis.geometry.Mbr;

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
    public LeafNode splitRStar(int m, Point insertedPoint) {
        int minAxis = Integer.MAX_VALUE;
        float minPerim = Float.MAX_VALUE;
        float minOverlap = Float.MAX_VALUE;
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
        int minOvlpI = -1;
        int minPerimI = -1;
        points.sort(getComparator(minAxis));
        int minWI = 0;
        int minI = 0;
        double minW = Double.MAX_VALUE;
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

            float tempOverlap = leafNode.getMbr().getOverlapVol(rightNode.getMbr());
            float tempPerim = leafNode.getMbr().perimeter() + rightNode.getMbr().perimeter();
            if (tempOverlap < minOverlap || tempOverlap == 0) {
                minOverlap = tempOverlap;
                minOvlpI = i;
                if (minOverlap == 0) {
                    if (tempPerim < minPerim) {
                        minPerim = tempPerim;
                        minPerimI = i;
                    }
                }
            }
            // if only use the original R*tree, only the following line is enough
            minI = minOverlap == 0 ? minPerimI : minOvlpI;
            //  For revisited R*tree minI is not the final result. w = wg * wf;  minI is the wg
            double wf = weightFunction(m, i, 0.5, minAxis);
            if (wf < 0) {
                // use the original R*tree method.
                minWI = minI;
            } else {
                double wg = minI;
                double w = minOverlap == 0 ? wf * wg : wg / wf;
                if (w < minW) {
                    minW = w;
                    minWI = i;
                }
            }
        }
        minI = minWI;
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
