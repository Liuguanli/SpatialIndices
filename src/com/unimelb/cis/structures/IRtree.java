package com.unimelb.cis.structures;

import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.NonLeafNode;
import com.unimelb.cis.utils.ExpReturn;

public abstract class IRtree {

    private int level;

    private int dim;

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

    public abstract String getDataFile();

    public abstract boolean buildRtree(String path);

    public abstract ExpReturn windowQuery(Mbr window);

    public abstract void output(String file);

    public abstract NonLeafNode buildRtreeAfterTuning(String path, int dim, int level);

}
