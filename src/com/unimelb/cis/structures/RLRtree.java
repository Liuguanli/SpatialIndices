package com.unimelb.cis.structures;

import com.unimelb.cis.node.NonLeafNode;

public abstract class RLRtree extends IRtree {

    public RLRtree(int pagesize) {
        super(pagesize);
    }

    public abstract void output(String file);

    public abstract NonLeafNode buildRtreeAfterTuning(String path, int dim, int level);

}
