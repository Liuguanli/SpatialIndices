package com.unimelb.cis.node;

import com.unimelb.cis.geometry.Mbr;

public abstract class Node implements Comparable {

    protected Mbr mbr;

    protected Node parent;

    protected int level;

    protected int orderInLevel;

    protected int dim;

    public int getDim() {
        return dim;
    }

    public void setDim(int dim) {
        this.dim = dim;
    }

    public abstract boolean isFull();

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

    public float getDeltaOvlpPerim(Point point) {
        Mbr mbr = this.getMbr().clone();
        mbr.updateMbr(point, dim);
        return mbr.perimeter() - this.getMbr().perimeter();
    }

    public float getDeltaOvlpVol(Point point) {
        Mbr mbr = this.getMbr().clone();
        mbr.updateMbr(point, dim);
        return mbr.perimeter() - this.getMbr().perimeter();
    }

    public float getDeltaOvlp(Node node) {
        return getDeltaOvlp(node, "vol");
    }

    public float getDeltaOvlp(Node node, String func) {
        if (func.equals("vol")) {
            return this.getMbr().getOverlapVol(node.getMbr());
        } else if (func.equals("perim")){
            return this.getMbr().getOverlapPerim(node.getMbr());
        } else {
            return this.getMbr().getOverlapVol(node.getMbr());
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
