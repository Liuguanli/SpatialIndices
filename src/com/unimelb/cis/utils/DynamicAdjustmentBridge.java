package com.unimelb.cis.utils;

import com.unimelb.cis.curve.HilbertCurve;
import com.unimelb.cis.node.NonLeafNode;
import com.unimelb.cis.node.Point;
import com.unimelb.cis.structures.hrtree.HRRtree;
import com.unimelb.cis.structures.queryadaptive.Opt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static com.unimelb.cis.CSVFileReader.readPoints;

public class DynamicAdjustmentBridge {

    public static void main(String[] args) {
        String dataset = "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_10000_1_2_.csv";
        List<Point> points = readPoints(dataset);
//        points = ZCurve.zCurve(points);

        adjustbyDP(points, 50, 102);

//        adjustbyRL(points, 50, 102, "/Users/guanli/Documents/datasets/RLRtree/trees/build_temp_file.csv", "/Users/guanli/Documents/datasets/RLRtree/trees/build_temp_file_after_tuning.csv");
        adjustbyRL(points, 50, 102, "build_temp_file.csv", "build_temp_file_after_tuning.csv");
    }

    public static void adjustbyDP(List<Point> points, int b, int B) {
        points = HilbertCurve.hilbertCurve(points);
        Opt opt = new Opt();
        opt.exp(points, b, B);
    }

    public static void adjustbyRL(List<Point> points, int b, int B, String outputFile, String buildFile) {
        HRRtree rtree = new HRRtree(100);
        rtree.buildRtree(points);

        final float[] area = {0};
        ((NonLeafNode) rtree.getRoot()).getChildren().forEach(node -> {
//                    System.out.println(node.getMbr().volume());
                    area[0] += node.getMbr().volume();
                }
        );
        System.out.println(area[0]);

        rtree.output(outputFile);
        int dim = 2;
        int level = rtree.getLevel();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("python ").append("/Users/guanli/Dropbox/shared/RLR-trees/codes/python/RLRtree/structure/rtree.py")
                .append(" -l ").append(level)
                .append(" -i ").append(outputFile)
                .append(" -o ").append(buildFile)
                .append(" -d ").append(dim)
                .append(" -p ").append(B)
                .append(" -a ").append("DQN");
        String command = stringBuilder.toString();
//        System.out.println(command);
        Process proc;
        try {
            proc = Runtime.getRuntime().exec(command);// 执行py文件
            //用输入输出流来截取结果
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.err.println("from python:" + line);
            }
            in.close();
            proc.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        rtree = new HRRtree(B);
        rtree.buildRtreeAfterTuning(buildFile, dim, level);
        System.out.println(rtree);
        area[0] = 0;
        ((NonLeafNode) rtree.getRoot()).getChildren().forEach(node -> area[0] += node.getMbr().volume());
        System.out.println(area[0]);
    }

}
