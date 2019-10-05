package com.unimelb.cis.utils;

import com.unimelb.cis.node.Point;

import java.util.ArrayList;
import java.util.List;

public class ExpReturn {

    public long time;

    public long pageaccess;

    public List<Point> result = new ArrayList<>();

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("time=").append(time).append(System.lineSeparator()).append("pageaccess=")
                .append(pageaccess).append(System.lineSeparator());
        if (result.size() < 100) {
            builder.append("result=").append(result);
        } else {
            builder.append("result=").append(result.size());
        }
        return builder.toString();
    }
}
