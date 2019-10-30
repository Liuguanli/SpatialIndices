package com.unimelb.cis.utils;

import com.unimelb.cis.node.Point;

import java.util.ArrayList;
import java.util.List;

public class ExpReturn {

    public long time;

    public double pageaccess;

    public List<Point> result = new ArrayList<>();

    public double accuracy;

    public int index;

    public int maxErr;

    public int minErr;

    public List<Double> predictResults = new ArrayList<>();

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("time=").append(time).append(System.lineSeparator()).append("pageaccess=")
                .append(pageaccess).append(System.lineSeparator()).append("result=").append(result.size())
                .append(System.lineSeparator());
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
        pageaccess += temp.pageaccess;
        time += temp.time;
        result.addAll(temp.result);

    }

//    @Override
//    public String toString() {
//        return "ExpReturn{" +
//                "time=" + time +
//                ", pageaccess=" + pageaccess +
//                ", result=" + result +
//                ", accuracy=" + accuracy +
//                ", index=" + index +
//                ", maxErr=" + maxErr +
//                ", minErr=" + minErr +
//                '}';
//    }
}
