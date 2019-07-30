package com.unimelb.cis.node;

import java.util.ArrayList;
import java.util.List;

public class LeafNode extends Node{

    private List<Point> children;

    public LeafNode() {
        children = new ArrayList<>();
    }

    public LeafNode(int pageSize) {
        super(pageSize);
        children = new ArrayList<>();
    }

    public boolean add(Point point) {
        if (isFull()) {
            return false;
        }
        children.add(point);
        updateMbr(point);
        return true;
    }

    public boolean add(List<Point> points) {
        if (children.size() + points.size() > pageSize) {
            return false;
        }
        children.addAll(points);
        for (Point point: points) {
            updateMbr(point);
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

    private void updateMbr(Point point) {
        float x = point.getX();
        float y = point.getY();
        if (mbr.getX1() > x) {
            mbr.setX1(x);
        }
        if (mbr.getX2() < x) {
            mbr.setX2(x);
        }
        if (mbr.getY1() > y) {
            mbr.setY1(y);
        }
        if (mbr.getY2() < y) {
            mbr.setY2(y);
        }
    }

    public boolean isFull() {
        return children.size() == pageSize;
    }

    public LeafNode split() {
        // right part
        LeafNode leafNode = new LeafNode();
        leafNode.add(new ArrayList<Point>(children.subList(pageSize/2, pageSize)));
        // left part
        List<Point> temp = new ArrayList<>(children.subList(0, pageSize/2));
        children.clear();
        this.add(temp);
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
