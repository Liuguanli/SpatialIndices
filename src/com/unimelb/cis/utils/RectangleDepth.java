package com.unimelb.cis.utils;

import com.unimelb.cis.geometry.Mbr;

 class RectangleDepth {
    private final Mbr rectangle;
    private final int depth;

    RectangleDepth(Mbr rectangle, int depth) {
        super();
        this.rectangle = rectangle;
        this.depth = depth;
    }

    Mbr getRectangle() {
        return rectangle;
    }

    int getDepth() {
        return depth;
    }

}