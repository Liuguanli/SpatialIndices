package com.unimelb.cis.node;

import com.unimelb.cis.geometry.Mbr;

public abstract class Node implements Comparable {

    protected Mbr mbr;

    protected Node parent;

    protected int level;

    protected int orderInLevel;

    protected int dim;

    public int getOrderInLevel() {
        return orderInLevel;
    }

    public void setOrderInLevel(int orderInLevel) {
        this.orderInLevel = orderInLevel;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Mbr getMbr() {
        return mbr;
    }

    protected int pageSize;

    public Node() {
        mbr = new Mbr();
    }

    public Node(int pageSize) {
        mbr = new Mbr();
        this.pageSize = pageSize;
    }

    public Node(int pageSize, int dim) {
        mbr = new Mbr(dim);
        this.pageSize = pageSize;
        this.dim = dim;
    }

    protected double dist;

    public double calDist(Point point) {
        if (this instanceof Point) {
            return ((Point) this).calDist(point);
        } else {
            return mbr.calMINDIST(point);
        }
    }

    @Override
    public int compareTo(Object o) {
        Node node = (Node) o;
        if (this.dist == node.dist) {
            return 0;
        }
        if (this.dist > node.dist) {
            return 1;
        } else if (this.dist < node.dist) {
            return -1;
        }
        return 0;
    }
}
