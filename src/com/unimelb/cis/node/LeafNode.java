package com.unimelb.cis.node;

import com.unimelb.cis.geometry.Mbr;

import java.util.ArrayList;
import java.util.List;

public class LeafNode extends Node {

    private List<Point> children;

    public LeafNode() {
        children = new ArrayList<>();
    }

    public LeafNode(int pageSize, int dim) {
        super(pageSize, dim);
        children = new ArrayList<>();
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
        return true;
    }

    public boolean add(List<Point> points) {
        if (children.size() + points.size() > pageSize) {
            return false;
        }
        children.addAll(points);
        for (Point point : points) {
            updateMbr(point, dim);
//            updateMbr(point);
        }
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
//
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
    public LeafNode splitBybisection() {
        // right part
        LeafNode leafNode = new LeafNode(pageSize, dim);
        leafNode.add(new ArrayList<>(children.subList(pageSize / 2, pageSize)));
        leafNode.setParent(this.parent);
        leafNode.setLevel(this.getLevel());

        // left part
        List<Point> temp = new ArrayList<>(children.subList(0, pageSize / 2));
        children.clear();
        this.add(temp);
        // TODO here is a bug!!!
        this.updateMbr();
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
