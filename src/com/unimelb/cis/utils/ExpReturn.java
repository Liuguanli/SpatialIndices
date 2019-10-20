package com.unimelb.cis.utils;

import com.unimelb.cis.node.Point;

import java.util.ArrayList;
import java.util.List;

public class ExpReturn {

    public long time;

    public long pageaccess;

    public List<Point> result = new ArrayList<>();

    public double accuracy;

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("time=").append(time).append(System.lineSeparator()).append("pageaccess=")
                .append(pageaccess).append(System.lineSeparator());
//        if (result.size() < 100) {
//            builder.append("result=").append(result);
//        } else {
//            builder.append("result=").append(result.size());
//        }
//        builder.append(System.lineSeparator());

        if (accuracy != 0) {
            builder.append("accuracy=").append(accuracy).append(System.lineSeparator());
        }

        return builder.toString();
    }

    public void plus(ExpReturn temp) {
        this.pageaccess += temp.pageaccess;
        this.time += temp.pageaccess;
        this.result.addAll(temp.result);

    }
}
