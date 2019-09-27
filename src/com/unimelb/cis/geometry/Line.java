package com.unimelb.cis.geometry;

public class Line {

    float begin;
    float end;

    public Line(float begin, float end) {
        this.begin = begin;
        this.end = end;
    }

    public boolean isContains(float val) {
        return (val >= begin && val <= end);
    }

    public boolean isLeft(float val) {
        if (val < begin) {
            return true;
        } else {
            return false;
        }
    }
}
