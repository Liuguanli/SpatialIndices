package com.unimelb.cis.geometry;

import java.util.ArrayList;
import java.util.List;

public class Boundary {

    int index = -1;

    public Boundary(int index, int dim) {
        this.index = index;
        this.dim = dim;
    }

    public Boundary(int dim) {
        this.dim = dim;
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
