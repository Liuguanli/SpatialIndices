package com.unimelb.cis.utils;

import com.unimelb.cis.node.Point;

import java.util.ArrayList;
import java.util.List;

public class ExpReturn {

    public long time;

    public int pageaccess;

    public List<Point> result = new ArrayList<>();

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("time=").append(time).append(System.lineSeparator()).append("pageaccess=")
                .append(pageaccess).append(System.lineSeparator());

        return builder.toString();
    }
}
