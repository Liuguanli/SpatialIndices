package com.unimelb.cis.structures.queryadaptive;


import com.unimelb.cis.CSVFileReader;
import com.unimelb.cis.curve.HilbertCurve;
import com.unimelb.cis.curve.ZCurve;
import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.LeafNode;
import com.unimelb.cis.node.Node;
import com.unimelb.cis.node.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class Opt {

    public Opt() {
    }

    public Opt(String profileFileTag) {
        String profileFile = String.format("D:\\\\UniMelbourne\\\\DL_index4R_tree\\\\dataset\\\\QueryProfiles_%s_.csv", profileFileTag);
        queryProfiles = QueryProfile.getQueryProfile(profileFile);
    }

    private List<Mbr> queryProfiles;

    public static void main(String args[]) {
        Opt opt = new Opt("0.01%");
        opt.exp("Z_uniform_1000000", 96, 102, "0.01%");
//        opt.exp("firstPage", 96, 102, "0.01%");
//        opt.compare();
    }

    public float costVal = 0;

    public void compare() {
        List<Node> nodes = getNodes("D:\\\\UniMelbourne\\\\DL_index4R_tree\\\\dataset\\\\_Z_uniform_1000000.csv");
        System.out.println(claCost(nodes));
        nodes = getNodes("D:\\\\UniMelbourne\\\\RL_Rtree\\\\python\\\\experiment\\\\RL_Z_uniform_1000000_m_1_l_0.01_ms_500_g_0.2.csv");
        System.out.println(claCost(nodes));
    }

    public float claCost(List<Node> nodes) {
        float cost = 0;
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            cost += weightFunc(node.getMbr());
        }
        return cost;
    }

    public void calOriginCost(List<List<Point>> pointGroups) {
        for (int i = 0; i < pointGroups.size(); i++) {
            List<Point> temp = pointGroups.get(i);
            float cost = 0;
            int num = temp.size() / 96;
            int pointNum = 0;
            for (int j = 0; j < num; j++) {
                List<Point> points = temp.subList(j * 96, (j + 1) * 96);
                Mbr rectangle = new Mbr();
                for (int k = 0; k < points.size(); k++) {
                    Point point = points.get(k);
                    rectangle.updateMbr(point, point.getDim());
                    pointNum += 1;
                }
                cost += weightFunc(rectangle);
            }
            System.out.println("calOriginCost:" + cost);
            System.out.println("pointNum:" + pointNum);
        }
//        System.out.println("calOriginCost:" + cost);
    }

    public void exp(String tag, int b, int B, String profileFileTag) {
//        opt.gopt(opt.getPoints(), 80, 110);
        costVal = 0;
        List<Point> points = getPoints(tag);
        List<List<Point>> pointGroups = new ArrayList<>();
        List<Integer> pageSizes = new ArrayList<>();
        int bucketSize = b * B;
        boolean isExactDivision = true;
        int length = points.size() / bucketSize;
        if (length * bucketSize != points.size()) {
            isExactDivision = false;
        }

        for (int i = 0; i < length; i++) {
            pointGroups.add(points.subList(i * bucketSize, (i + 1) * bucketSize));
        }

        if (!isExactDivision) {
            pointGroups.add(points.subList(length * bucketSize, points.size()));
        }

        calOriginCost(pointGroups);

        for (int i = 0; i < pointGroups.size(); i++) {
            pageSizes.addAll(opt(pointGroups.get(i), B / 2, B, B, bucketSize));
        }
        System.out.println(pageSizes.size());
        System.out.println("costVal:" + costVal);

//        String fileName = new FileNameBuilder().buildType(FileNameBuilder.TYPE_OPT).buildDataset(tag).buildWindowSize(Float.valueOf(profileFileTag.split("%")[0])).buildTime().build();
//        FileRecoder.write(fileName, "costVal:" + costVal);

        int index = 0;
        int pointsNum = pageSizes.get(index);
        for (int i = 0; i < points.size(); i++) {
            if (i >= pointsNum) {
                index++;
                pointsNum += pageSizes.get(index);
//                System.out.println(pageSizes.get(index));
            }
            points.get(i).setIndex(index);
        }
        System.out.println(points.get(0));
        System.out.println(points.get(points.size() - 1));
        System.out.println("pageSizes:");
        System.out.println(pageSizes);

//        CSVFileWriter writer = new CSVFileWriter();
//        writer.write(points, "..\\dataset\\_" + tag + ".csv");

//        CSVFileWriter writer = new CSVFileWriter();
//        writer.writePartition(pageSizes, "..\\dataset\\" + tag + ".csv");
    }

    public List<Node> getNodes(String dataset) {
//        String dataset = String.format("D:\\\\UniMelbourne\\\\DL_index4R_tree\\\\RL_gen_dataset\\\\%s_.csv", tag);
        CSVFileReader reader = new CSVFileReader();
        List<String> lines = reader.read(dataset);
        List<Point> points = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] items = line.split(",");
            points.add(new Point(Float.valueOf(items[0]), Float.valueOf(items[1]), Integer.valueOf(items[2])));
        }
        List<Node> childrenNodes = new ArrayList<>();
        int tempPageSize = points.get(0).getIndex();
        int dim = points.get(0).getDim();
        LeafNode leafNode = new LeafNode(tempPageSize, dim);
        childrenNodes.add(leafNode);

        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).getIndex() != tempPageSize) {
                tempPageSize = points.get(i).getIndex();
                leafNode = new LeafNode(tempPageSize, dim);
                childrenNodes.add(leafNode);
            }
            Point point = points.get(i);
            leafNode.add(new Point(point.getLocation()));
        }
        return childrenNodes;
    }

    public List<Point> getPoints(String tag) {
        String dataset = String.format("D:\\\\UniMelbourne\\\\DL_index4R_tree\\\\RL_gen_dataset\\\\%s_.csv", tag);
        CSVFileReader reader = new CSVFileReader();
        List<String> lines = reader.read(dataset);
        List<Point> points = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] items = line.split(",");
            points.add(new Point(Float.valueOf(items[0]), Float.valueOf(items[1])));
        }
        if (tag.startsWith("H")) {
            points = HilbertCurve.hilbertCurve(points);
        } else {
            points = ZCurve.zCurve(points);
        }
        return points;
    }

    public float weightFunc(Mbr mbr) {
        float sum = 0;
        for (int i = 0; i < queryProfiles.size(); i++) {
            sum += queryProfiles.get(i).calInteract(mbr);
        }
        return sum;
    }

    public Mbr getMbr(List<Point> points, int from, int to) {
        float x1 = Integer.MAX_VALUE;
        float x2 = Integer.MIN_VALUE;
        float y1 = Integer.MAX_VALUE;
        float y2 = Integer.MIN_VALUE;
        for (int i = from; i < to; i++) {
            Point point = points.get(i);
            if (x1 > point.getLongitude()) {
                x1 = point.getLongitude();
            }
            if (x2 < point.getLongitude()) {
                x2 = point.getLongitude();
            }
            if (y1 > point.getLatitude()) {
                y1 = point.getLatitude();
            }
            if (y2 < point.getLatitude()) {
                y2 = point.getLatitude();
            }
        }
        return new Mbr(x1, x2, y1, y2);
    }

    public List<Integer> opt(List<Point> points, int b, int B, int m, int bucketSize) {
        int N = points.size();
        float[][] cost = new float[N + 1][m + 1];
        int[][] indices = new int[N + 1][m + 1];
        for (int i = 1; i <= B; i++) {
            float temp = weightFunc(getMbr(points, 0, i));
            cost[i][1] = temp;
            indices[i][1] = i;
        }
        for (int i = 2; i <= m; i++) {
            int length = i * B > N ? N : i * B;
            for (int j = i * b; j <= length; j++) {
                int maxB = (j - B) > 0 ? B : j - B + 1;
                float Rp[] = new float[maxB + 1];
                Mbr rectangle = new Mbr();
                for (int k = 1; k <= maxB; k++) {
                    Point point = points.get(j - k);
                    rectangle.merge(point.getLongitude(), point.getLatitude());
                    Rp[k] = weightFunc(rectangle);
                }
                int minIndex = 0;
                float minValue = Float.MAX_VALUE;
                for (int k = b; k <= maxB; k++) {
                    if (cost[j - k][i - 1] == 0)
                        continue;
                    float temp = cost[j - k][i - 1] + Rp[k];
                    if (temp < minValue) {
                        minIndex = k;
                        minValue = temp;
                    }
                }
                cost[j][i] = minValue;
                indices[j][i] = minIndex;
            }
        }
        int start = N;
        List<Integer> result = new ArrayList<>();
        boolean isGetCost = false;
        for (int i = m; i > 0; i--) {
            int index = indices[start][i];
//            System.out.println(index + " (" + start + "," + i + ")");
            if (index != 0) {
                if (!isGetCost) {
                    float temp = cost[start][i];
                    costVal += temp;
                    System.out.println("cost:" + temp);
                    isGetCost = true;
                }
                result.add(index);
            }
            start -= index;
        }
        Collections.reverse(result);
        return result;
    }

    public void gopt(List<Point> points, int b, int B) {
        int N = points.size();
        float[] cost = new float[N + 1];
        int[] indices = new int[N + 1];
        for (int i = 1; i <= B; i++) {
            float temp = weightFunc(getMbr(points, 0, i));
            cost[i] = temp;
        }
        for (int i = 2 * b; i <= N; i++) {
            float Rp[] = new float[B + 1];
            Mbr rectangle = new Mbr();
            for (int k = 1; k <= B; k++) {
                Point point = points.get(i - k);
                rectangle.merge(point.getLongitude(), point.getLatitude());
                Rp[k] = weightFunc(rectangle);
            }
            float minValue = Float.MAX_VALUE;
            int minIndex = 0;
            for (int k = b; k <= B; k++) {
                if (cost[i - k] == 0)
                    continue;
                float temp = cost[i - k] + Rp[k];
                if (temp < minValue) {
                    minIndex = k;
                    minValue = temp;
                }
            }
            cost[i] = minValue;
            indices[i] = minIndex;
        }

        int start = N;
        List<Integer> result = new ArrayList<>();
        int index = indices[start];
        while (index > 0) {
            result.add(index);
            start -= index;
            System.out.println(index);
            index = indices[start];
        }
        Collections.reverse(result);
        System.out.println(result);
        System.out.println(result.size());
    }

}
