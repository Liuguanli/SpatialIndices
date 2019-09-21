package com.unimelb.cis.structures;

import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.Node;
import com.unimelb.cis.node.NonLeafNode;
import com.unimelb.cis.utils.ExpReturn;
import com.unimelb.cis.utils.Visualizer;

import java.util.List;

public abstract class IRtree {

    public IRtree() {
    }

    public IRtree(int pagesize) {
        this.pagesize = pagesize;
    }

    protected Node root;

    protected List<Node> leafNodes;

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

    public abstract boolean buildRtree(String path);

    public abstract ExpReturn windowQuery(Mbr window);

    public abstract void output(String file);

    public abstract NonLeafNode buildRtreeAfterTuning(String path, int dim, int level);

    public Visualizer visualize(int width, int height) {
        Mbr view = new Mbr(0, 0, 1, 1);
        return new Visualizer(this, width, height, view);
    }

}
