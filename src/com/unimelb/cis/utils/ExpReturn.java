package com.unimelb.cis.utils;

public class ExpReturn {

    public long time;

    public int pageaccess;

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("time=").append(time).append(System.lineSeparator()).append("pageaccess=")
                .append(pageaccess).append(System.lineSeparator());

        return builder.toString();
    }
}
