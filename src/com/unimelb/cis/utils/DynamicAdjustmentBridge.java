package com.unimelb.cis.utils;

import com.unimelb.cis.curve.HilbertCurve;
import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.LeafNode;
import com.unimelb.cis.node.NonLeafNode;
import com.unimelb.cis.node.Point;
import com.unimelb.cis.structures.hrtree.HRRtree;
import com.unimelb.cis.structures.queryadaptive.Opt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.unimelb.cis.CSVFileReader.readPoints;

public class DynamicAdjustmentBridge {

    public static void main(String[] args) {
        String dataset = "/Users/guanli/Documents/datasets/RLRtree/raw/uniform_10000_1_2_.csv";
        List<Point> points = readPoints(dataset);

        HRRtree hrRtree1 = adjustbyDP(points, 50, 102, 100);
        System.out.println("finish adjustbyDP");
//        adjustbyRL(points, 50, 102, "/Users/guanli/Documents/datasets/RLRtree/trees/build_temp_file.csv", "/Users/guanli/Documents/datasets/RLRtree/trees/build_temp_file_after_tuning.csv");
        HRRtree hrRtree2 = adjustbyRL("/Users/guanli/Dropbox/shared/RLR-trees/codes/python/RLRtree/structure/rtree.py", "DQN", points, 102, 100, "build_temp_file.csv", "build_temp_file_after_tuning.csv");
        System.out.println("finish adjustbyRL");
        System.out.println(hrRtree1.windowQuery(new Mbr(0.2f, 0.2f, 0.5f, 0.5f)));
        System.out.println(hrRtree2.windowQuery(new Mbr(0.2f, 0.2f, 0.5f, 0.5f)));
    }

    public static HRRtree adjustbyDP(List<Point> points, int b, int B, int initialSize) {
        points = HilbertCurve.hilbertCurve(points);
        Opt opt = new Opt();
        return opt.exp(points, b, B, initialSize);
    }

    public static HRRtree adjustbyRL(String pyFile, String method, List<Point> points, int B, int initialSize, String outputFile, String buildFile) {
        HRRtree rtree = new HRRtree(initialSize);
        rtree.buildRtree(points);

        final float[] area = {0, 0};
        ((NonLeafNode) rtree.getRoot()).getChildren().forEach(node -> {
                    area[0] += node.getMbr().volume();
                }
        );
//        System.out.println(area[0]);
        rtree.output(outputFile);
        int dim = 2;
        int level = rtree.getLevel();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("python ").append(pyFile)
                .append(" -l ").append(level)
                .append(" -i ").append(outputFile)
                .append(" -o ").append(buildFile)
                .append(" -d ").append(dim)
                .append(" -p ").append(B)
                .append(" -a ").append(method);
        String command = stringBuilder.toString();
        Process proc;
        try {
            proc = Runtime.getRuntime().exec(command);// 执行py文件
            //用输入输出流来截取结果
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//            String line;
//            while ((line = in.readLine()) != null) {
//                System.err.println("from python:" + line);
//            }
            in.close();
            proc.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        rtree = new HRRtree(B);
        rtree.buildRtreeAfterTuning(buildFile, dim, level);

        AtomicInteger sum = new AtomicInteger();
        NonLeafNode rootNode;
        if (((NonLeafNode) rtree.getRoot()).getChildren().get(0) instanceof NonLeafNode) {
            rootNode = (NonLeafNode) ((NonLeafNode) rtree.getRoot()).getChildren().get(0);
        } else {
            rootNode = (NonLeafNode) rtree.getRoot();
        }
        rootNode.getChildren().forEach(node -> {
            area[1] += node.getMbr().volume();
//            pagesizes.add(((LeafNode) node).getChildren().size());
            sum.addAndGet(((LeafNode) node).getChildren().size());
        });
//        System.out.println(Arrays.toString(area));
//        System.out.println(sum);
        System.out.println("RL opt rate:" + (1 - area[1] / area[0]) * 100 + "%");
        return rtree;
    }

}
