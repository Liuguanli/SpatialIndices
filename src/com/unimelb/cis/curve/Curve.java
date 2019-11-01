package com.unimelb.cis.curve;

import com.unimelb.cis.node.Point;

import java.util.Collections;
import java.util.List;

public abstract class Curve {

    public static void sortDimensiont(List<Point> points, int dimension) {
        Collections.sort(points, (p1, p2) -> {
            if (p1.getLocation()[dimension] > p2.getLocation()[dimension]) {
                return 1;
            } else if (p1.getLocation()[dimension] < p2.getLocation()[dimension]) {
                return -1;
            } else {
                return 0;
            }
        });
        for (int i = 0; i < points.size(); i++) {
            points.get(i).getLocationOrder()[dimension] = (i + 1);
        }
    }

    public static List<Point> getPointByCurve(List<Point> points, String curve, boolean rankspace) {
        switch (curve) {
            case "Z":
                points = ZCurve.zCurve(points, rankspace);
                break;
            case "H":
                points = HilbertCurve.hilbertCurve(points, rankspace);
                break;
            case "P":
                points = PeanoCurve.peanoCurve(points, rankspace);
                break;
        }
        return points;
    }

}
