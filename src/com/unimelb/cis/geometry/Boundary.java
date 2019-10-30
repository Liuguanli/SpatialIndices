package com.unimelb.cis.geometry;

import com.unimelb.cis.HilbertCurve;
import com.unimelb.cis.ZCurve;

import java.util.ArrayList;
import java.util.List;

public class Boundary {

    int index = -1;

    long[] loactionIndex;

    public Boundary(int index, int dim) {
        this.index = index;
        this.dim = dim;
        loactionIndex = new long[dim];
    }

    public Boundary(int dim) {
        this.dim = dim;
        loactionIndex = new long[dim];
    }

    public void setDimOrder(int dim, int order) {
        loactionIndex[dim - 1] = order;
    }

    public long getHCurveValue(int bitNum) {
//        return HilbertCurve.getHilbertValue(bitNum, loactionIndex);
        return ZCurve.getZcurve(loactionIndex, bitNum);
    }

    int dim;

    List<Line> boundries = new ArrayList<>();

    List<Boundary> children = new ArrayList<>();

    public void addBoundry(Line line) {
        boundries.add(line);
    }

    public void addChild(Boundary child) {
        children.add(child);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void addBoundary(Boundary another) {
        this.boundries.addAll(another.getBoundries());
        this.children.addAll(another.getChildren());
    }

    public List<Line> getBoundries() {
        return boundries;
    }

    public List<Boundary> getChildren() {
        return children;
    }
}
