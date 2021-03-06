package com.unimelb.cis.node;

import com.unimelb.cis.geometry.Mbr;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.Math.E;

public abstract class Node implements Comparable {

    protected Mbr mbr;

    // original Box->OBox , for the calculation of wf
    protected Mbr oMbr;

    protected Node parent;

    protected int level;

    protected int orderInLevel;

    protected int dim;

    protected float[] location;

    public Mbr getoMbr() {
        return oMbr;
    }

    public float[] getLocation() {
        return location;
    }

    public double[] getLocationDouble() {
        double[] result = new double[location.length];
        for (int i = 0; i < location.length; i++) {
            result[i] = location[i];
        }
        return result;
    }


    public void setLocation(float[] location) {
        this.location = location;
    }

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
    }

    public Node(int pageSize) {
        this.pageSize = pageSize;
    }

    public Node(int pageSize, int dim) {
        mbr = new Mbr(dim);
        this.pageSize = pageSize;
        this.dim = dim;
    }

    public void setOMbr(Mbr mbr, boolean force) {
        if (force) {
            oMbr = mbr;
        } else {
            if (oMbr == null) {
                oMbr = mbr;
            }
        }
    }

    public void setOMbr(Mbr mbr) {
        setOMbr(mbr, false);
    }

    public void setOMbr(Point point) {
        if (oMbr == null) {
            oMbr = new Mbr(point);
        }
    }

    public void updateOMbr() {
        oMbr = mbr.clone();
    }

    protected double dist;

    public float getVolume(Point point) {
        Mbr mbr = this.getMbr().clone();
        mbr.updateMbr(point, dim);
        return mbr.volume();
    }

    public double calDist(Point point) {
        if (this instanceof Point) {
            return this.calDist(point);
        } else {
            return mbr.calMINDIST(point);
        }
    }

    public float getDeltaPerim(Point point, List<LeafNode> leafNodes) {
        Mbr mbr = this.getMbr().clone();
        mbr.updateMbr(point, dim);
        float result = 0;
        for (int i = 0; i < leafNodes.size(); i++) {
            LeafNode leafNode = leafNodes.get(i);
            if (this == leafNode) {
                continue;
            }
            result += mbr.getOverlapPerim(leafNode.getMbr());
        }
        return result;
    }

    public float getDeltaPerim(Point point) {
        if (getMbr().contains(point)) {
            return 0;
        }
        Mbr mbr = this.getMbr().clone();
        mbr.updateMbr(point, dim);
        return mbr.perimeter() - this.getMbr().perimeter();
    }

    public float getDeltaVol(Point point) {
        if (getMbr().contains(point)) {
            return 0;
        }
        Mbr mbr = this.getMbr().clone();
        mbr.updateMbr(point, dim);
        return mbr.volume() - this.getMbr().volume();
    }

    public double getDist(Point point) {
        double result = 0;
        for (int i = 0; i < dim; i++) {
            result += (this.getLocation()[i] - point.getLocation()[i]) * (this.getLocation()[i] - point.getLocation()[i]);
        }
        return Math.sqrt(result);
    }

    public float getDeltaOvlp(Node node) {
        return getDeltaOvlp(node, "vol");
    }

    public float getDeltaOvlp(Node node, String func) {
        if (func.equals("vol")) {
            return this.getMbr().getOverlapVol(node.getMbr());
        } else if (func.equals("perim")) {
            return this.getMbr().getOverlapPerim(node.getMbr());
        } else {
            return this.getMbr().getOverlapVol(node.getMbr());
        }
    }

    // wf   leafNode is this.
    protected double weightFunction(int m, int i, double s, int axis) {
        double y1 = Math.pow(E, -1 / (Math.pow(s, 2)));
        double ys = 1 / (1 - y1);
        double asym = 2 * (this.getMbr().getCenterByAxis(axis) - this.getoMbr().getCenterByAxis(axis) / this.getMbr().getLambdaByAxis(axis));
        asym = asym > 1 ? 1 : asym;
        asym = asym < -1 ? -1 : asym;
        double miu = (1 - 2 * m / (pageSize + 1)) * asym;
        double theta = s * (1 - Math.abs(miu));
        double xi = 2 * i / (pageSize + 1) - 1;
        double result = ys * (Math.pow(E, (-Math.pow((xi - miu) / theta, 2))) - y1);
        return result;
    }

    public abstract void adjust();

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

    @Override
    public String toString() {
        return "Node{" +
                "location=" + Arrays.toString(location) +
                '}';
    }
}
