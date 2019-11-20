package com.unimelb.cis.utils;

import java.util.List;

public class Search {

    public static int binarySearch(List<Float> values, float targer) {
        int begin = 0;
        int end = values.size() - 1;
        if (targer <= values.get(begin)) {
            return begin;
        }
        if (targer >= values.get(end)) {
            return end;
        }
        int mid = (begin + end) / 2;
        while (values.get(mid) > targer || values.get(mid + 1) < targer) {
            if (values.get(mid) > targer) {
                end = mid;
            } else if (values.get(mid) < targer) {
                begin = mid;
            } else {
                return mid;
            }
            mid = (begin + end) / 2;
        }
        return mid;
    }

}
