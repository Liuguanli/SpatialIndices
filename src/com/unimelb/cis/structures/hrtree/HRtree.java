package com.unimelb.cis.structures.hrtree;

import com.leo.exp.Experiment;
import com.leo.exp.IRtree;
import com.leo.modelinterface.DataUtils;
import com.leo.r_tree_rxjava.CSVFileReader;
import com.leo.r_tree_rxjava.CSVFileWriter;
import com.leo.r_tree_rxjava.PointForCurve;
import com.leo.r_tree_rxjava.curve.HilbertCurve;
import com.leo.r_tree_rxjava.curve.ZCurve;
import com.leo.recorder.FileNameBuilder;
import com.leo.recorder.FileRecoder;
import com.leo.recorder.annotation.BeforeRecord;
import com.leo.recorder.annotation.Timer;

import java.io.File;
import java.util.*;

public class HCurveRtree extends SFCRtree implements IRtree {

    public int pageSize = 100;

    public Map<Integer, Float> mappingLati = new HashMap();
    public Map<Integer, Float> mappingLongi = new HashMap();
    public List<Float> latis = new ArrayList<>();
    public List<Float> longis = new ArrayList<>();
    public List<Long> hValues = new ArrayList<>();

    public static int bitNum = 0;

    private String name;

    @Override
    public String getName() {
        return name;
    }

    private int datasetSize;

    @Override
    public int getDatasetSize() {
        return datasetSize;
    }

    @Override
    public float pointQuery() {
        if (root == null)
            buildRtree();
        long pageAccessNum = 0;
        CSVFileReader reader = new CSVFileReader();
        List<String> lines = reader.read(dataset);
        List<Node> nodes = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] items = line.split(",");
            Point point = new Point(Float.valueOf(items[0]),
                    Float.valueOf(items[1]));
            nodes.add(root);
            int eachPageAccess = 0;
            while (nodes.size() > 0) {
                Node top = nodes.remove(0);
                if (top instanceof NoneLeafNode) {
                    NoneLeafNode nonLeaf = (NoneLeafNode) top;
                    if (nonLeaf.mbr.contains(point)) {
                        pageAccessNum++;
//                        System.out.println("in the mbr");
                        List<Node> children =
                                ((NoneLeafNode) top).getChildren();
                        nodes.addAll(nodes.size(), children);
                    }
                } else if (top instanceof LeafNode) {
                    LeafNode leaf = (LeafNode) top;
                    if (leaf.mbr.contains(point)) {
                        eachPageAccess++;
                        if (leaf.getChildren().contains(point)) {
                            break;
                        }
                    }
                }
//                eachPageAccess++;
            }
            pageAccessNum += eachPageAccess;
            nodes.clear();
//            if (i % 10000 == 0) {
//                System.out.println("-------" + i);
//                System.out.println("pageAccessNum：" + pageAccessNum);
//            }
        }
        System.out.println("HCurveRtree pageAccessNum：" + pageAccessNum / (float) lines.size());
        return pageAccessNum / (float) lines.size();
    }

    // this is for RL exp
    public int insertPoint(List<Point> points) {
        Set<Integer> visitedPageIndex = new HashSet<>();
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            int xIndex = binarySearch(latis, point.getX());
            int yIndex = binarySearch(longis, point.getY());
            int bitNum = (int) (Math.log(latis.size()) / Math.log(2.0)) + 1;
            long result = HilbertCurve.get2DHilbertCurve(xIndex, yIndex,
                    bitNum);
            visitedPageIndex.add((int) result / pageSize);
        }
        return visitedPageIndex.size() + points.size();
    }

    @Override
    public void batchProcessing(String file) {
        CSVFileReader reader = new CSVFileReader();
        List<String> lines = reader.read(file);
        datasetSize = lines.size();
        List<PointForCurve> points = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] items = line.split(",");
            points.add(new PointForCurve(Float.valueOf(items[0]),
                    Float.valueOf(items[1])));
        }

        bitNum = (int) (Math.log(points.size()) / Math.log(2.0)) + 1;

        points = sortLongitude(points);
        points = sortLatitude(points);
        points = HilbertCurve.hilbertCurve(points);

        String[] names = file.split("\\\\");
        String name = names[names.length - 1].split("\\.")[0];
        String path = file.split(name)[0];
        File child = new File(path);
        File dataset = new File(child.getParent() + "\\dataset\\");
        if (!dataset.exists()) {
            dataset.mkdirs();
        }
        CSVFileWriter writer = new CSVFileWriter();
        writer.write(points, child.getParent() + "\\dataset\\H_" + name +
                ".csv", pageSize);
        File partition = new File(child.getParent() + "\\partition\\");
        if (!partition.exists()) {
            partition.mkdirs();
        }
        File partitionOutline = new File(child.getParent() +
                "\\partition_outline\\");
        if (!partitionOutline.exists()) {
            partitionOutline.mkdirs();
        }
        points = sortLatitude(points);
        List<List<PointForCurve>> partitions = DataUtils.getPartition(points,
                10000);
        for (int i = 0; i < partitions.size(); i++) {
            final List<PointForCurve> temp = partitions.get(i);
            writer.write(temp, partition.getAbsolutePath() + "\\H_" + name +
                    "_" + i + "_.csv", pageSize);
            List<PointForCurve> outline = new ArrayList<>();
            PointForCurve lower = new PointForCurve(0, 0);
            PointForCurve upper = new PointForCurve(0, 0);
            lower.setLongitude(temp.get(0).getLongitude());
            upper.setLongitude(temp.get(temp.size() - 1).getLongitude());
            sortLatitude(temp);
            lower.setLatitude(temp.get(0).getLatitude());
            upper.setLatitude(temp.get(temp.size() - 1).getLatitude());
            outline.add(lower);
            outline.add(upper);
            writer.write(outline, partitionOutline.getAbsolutePath() +
                    "\\outlineH_" + name + "_" + i + "_.txt", pageSize);
//            System.out.println("longi:" + temp.get(0).getLongitude() + " "
//            + temp.get(temp.size() - 1).getLongitude());
//            System.out.println("lati:" + temp.get(0).getLatitude() + " " +
//            temp.get(temp.size() - 1).getLatitude());
            System.out.println();
        }

    }

    @Override
    public float windowQuery(Mbr mbr) {
        System.out.println("-------------------HCurveRtree " +
                "windowQuery-------------------");
        int pageAccessNum = 0;

        List<Point> retults = new ArrayList<>();

        ArrayList<Node> list = new ArrayList();
        list.add(root);
        while (list.size() > 0) {
            Node top = list.remove(0);
            if (top instanceof NoneLeafNode) {
                NoneLeafNode nonLeaf = (NoneLeafNode) top;
                if (nonLeaf.getMbr().interact(mbr)) {
//                    System.out.println("NoneLeafNode interact");
                    List<Node> children = nonLeaf.getChildren();
                    list.addAll(list.size(), children);
                    pageAccessNum++;
                }
            } else if (top instanceof LeafNode) {
                LeafNode leaf = (LeafNode) top;
                if (leaf.getMbr().interact(mbr)) {
//                    System.out.println("leaf interact");
                    List<Point> children = leaf.getChildren();
                    for (int i = 0; i < children.size(); i++) {
                        if (mbr.contains(children.get(i))) {
                            retults.add(children.get(i));
                        }
                    }
                    pageAccessNum++;
                }
            }
//            System.out.println("list : " + list.size() + "  pageAccessNum:"
//            + pageAccessNum + "   retults:" + retults.size());
        }
        System.out.println("-------------------end HCurveRtree " +
                "windowQuery-------------------");
        return pageAccessNum;
    }

    public int kNNQuery1(int k, final Point point) {
        int pageAccessNum = 0;
        PriorityQueue<Object> queue = new PriorityQueue(k, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                double dist1 = 0;
                double dist2 = 0;

                if (o1 instanceof NoneLeafNode) {
                    dist1 = ((NoneLeafNode) o1).getMbr().claDist(point);
                } else if (o1 instanceof LeafNode) {
                    dist1 = ((LeafNode) o1).getMbr().claDist(point);
                } else {
                    dist1 = ((Point) o1).calDist(point);
                }

                if (o2 instanceof NoneLeafNode) {
                    dist2 = ((NoneLeafNode) o2).getMbr().claDist(point);
                } else if (o2 instanceof LeafNode) {
                    dist2 = ((LeafNode) o2).getMbr().claDist(point);
                } else {
                    dist2 = ((Point) o2).calDist(point);
                }

                if (dist1 > dist2) {
                    return 1;
                } else if (dist1 < dist2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        ArrayList<Node> list = new ArrayList();
        list.add(root);
        while (list.size() > 0) {
            Node top = list.remove(0);
            if (top instanceof NoneLeafNode) {
                NoneLeafNode nonLeaf = (NoneLeafNode) top;
                List<Node> children = nonLeaf.getChildren();
                for (int i = 0; i < children.size(); i++) {
                    Node former = children.get(i);
                    boolean isProne = false;
                    for (int j = 0; j < children.size(); j++) {
                        if (i == j) {
                            continue;
                        }
                        Node later = children.get(j);
                        if (former.getMbr().calMINDIST(point) > later.getMbr().calMINMAXDIST(point)) {
                            // 这个点可以被剪掉了
                            isProne = true;
                            break;
                        }
                    }
                    if (!isProne) {
                        list.add(former);
                    }
                }
                pageAccessNum++;
            } else if (top instanceof LeafNode) {
                LeafNode leaf = (LeafNode) top;
                List<Point> children = leaf.getChildren();
                queue.addAll(children);
                pageAccessNum++;
            } else {
                queue.add(top);
            }
        }
        List<Point> result = new ArrayList<>(k);
        for (int i = 0; i < k; i++) {
            result.add((Point) queue.poll());
        }
        return pageAccessNum;
    }

    @Override
    public void kNNQuery(int k, List<Point> queryPoints) {
        System.out.println("-------------------HCurveRtree " +
                "kNNQuery-------------------");
        int pageAccessNum = 0;
        long begin = System.nanoTime();
        for (int i = 0; i < queryPoints.size(); i++) {
            Point query = queryPoints.get(i);
            pageAccessNum += kNNQuery1(k, query);
        }
        long end = System.nanoTime();
        long time = end - begin;
        System.out.println(time / queryPoints.size() + "ns");
        System.out.println("pageAccessNum:" + pageAccessNum / queryPoints.size());
        String[] names = this.dataset.split("\\\\");
        String name = names[names.length - 1].split("\\.")[0];
        String fileName =
                new FileNameBuilder().buildDataset(name).buildStructureName(this.name).buildTime().buildType(FileNameBuilder.TYPE_KNN).buildK(k).build();
        FileRecoder.write(fileName, "queryTime:" + time / queryPoints.size());
        FileRecoder.write(fileName,
                "pageAccess:" + pageAccessNum / queryPoints.size());
        System.out.println("-------------------end HCurveRtree " +
                "kNNQuery-------------------");
    }

    public HCurveRtree(String name) {
        this.name = name;
    }

    public HCurveRtree(String name, int pageSize) {
        this.name = name;
        this.pageSize = pageSize;
    }

    public NoneLeafNode getRoot() {
        return root;
    }


    private String dataset = "D:\\UniMelbourne\\DL_index4R_tree\\dataset" +
            "\\random_uniform_500000_zvalue.csv";

    public static void main(String[] args) {
        HCurveRtree tree = new HCurveRtree();
        tree.setDataset("D:\\\\UniMelbourne\\\\DL_index4R_tree" +
                "\\\\RL_gen_dataset\\\\Z_uniform_1000000_.csv");
        tree.buildRtree();
        tree.calArea();
        tree.calPerimeter();

        List<Mbr> mbrs = Experiment.getMbrs(100, 0.005f);  // 万分之一
        float pageaccess = 0;
        for (int i = 0; i < mbrs.size(); i++) {
            System.out.println(mbrs.get(i));
            Mbr mbr = mbrs.get(i);
            pageaccess += tree.windowQuery(mbr);
        }
        System.out.println("pageaccess:" + pageaccess / mbrs.size());
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public HCurveRtree() {
    }

    public HCurveRtree(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<LeafNode> getLeadNodes(String dataset, int pageSize,
                                       List<Integer> indexes) {
        CSVFileReader reader = new CSVFileReader();
        List<String> lines = reader.read(dataset);
        LeafNode leafNode = null;
        List<LeafNode> childrenNodes = new ArrayList<>();
        int index = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (indexes.get(i) != index) {
                index = indexes.get(i);
                leafNode = new LeafNode(pageSize);
                childrenNodes.add(leafNode);
            }
            String line = lines.get(i);
            String[] items = line.split(",");
            leafNode.add(new Point(Float.valueOf(items[0]),
                    Float.valueOf(items[1])));
        }
//        for (int i = 0; i < lines.size(); i++) {
//            if (i % pageSize == 0) {
//                leafNode = new LeafNode(pageSize);
//                childrenNodes.add(leafNode);
//            }
//            String line = lines.get(i);
//            String[] items = line.split(",");
//            leafNode.add(new Point(Float.valueOf(items[0]), Float.valueOf
//            (items[1])));
//        }
        return childrenNodes;
    }

    public List<LeafNode> getLeadNodes(String dataset, int pageSize) {
        CSVFileReader reader = new CSVFileReader();
        List<String> lines = reader.read(dataset);
        LeafNode leafNode = null;
        List<LeafNode> childrenNodes = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            if (i % pageSize == 0) {
                leafNode = new LeafNode(pageSize);
                childrenNodes.add(leafNode);
            }
            String line = lines.get(i);
            String[] items = line.split(",");
            leafNode.add(new Point(Float.valueOf(items[0]),
                    Float.valueOf(items[1])));
        }
        return childrenNodes;
    }

    public List<PointForCurve> sortLongitude(List<PointForCurve> points) {
        Collections.sort(points, new Comparator<PointForCurve>() {
            public int compare(PointForCurve p1, PointForCurve p2) {
                if (p1.getLongitude() > p2.getLongitude()) {
                    return 1;
                } else if (p1.getLongitude() < p2.getLongitude()) {
                    return -1;
                } else {
                    if (p1.getLatitude() > p2.getLatitude()) {
                        return 1;
                    } else if (p1.getLatitude() < p2.getLatitude()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }
        });
        for (int i = 0; i < points.size(); i++) {
            points.get(i).setxIndex(i + 1);
            mappingLongi.put(i, points.get(i).getLongitude());
            longis.add(points.get(i).getLongitude());
        }
        return points;
    }

    public List<PointForCurve> sortLatitude(List<PointForCurve> points) {
        Collections.sort(points, new Comparator<PointForCurve>() {
            public int compare(PointForCurve p1, PointForCurve p2) {
                if (p1.getLatitude() > p2.getLatitude()) {
                    return 1;
                } else if (p1.getLatitude() < p2.getLatitude()) {
                    return -1;
                } else {
                    if (p1.getLongitude() > p2.getLongitude()) {
                        return 1;
                    } else if (p1.getLongitude() < p2.getLongitude()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }
        });
        for (int i = 0; i < points.size(); i++) {
            points.get(i).setyIndex(i + 1);
            mappingLati.put(i, points.get(i).getLatitude());
            latis.add(points.get(i).getLatitude());
        }
        return points;
    }

    @BeforeRecord
    public void buildRtree() {

        root = null;
        mappingLati.clear();
        mappingLongi.clear();
        latis.clear();
        longis.clear();
        hValues.clear();

        System.out.println("build begin:" + dataset);
        CSVFileReader reader = new CSVFileReader();
        List<String> lines = reader.read(dataset);
        datasetSize = lines.size();
        List<PointForCurve> points = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] items = line.split(",");
            points.add(new PointForCurve(Float.valueOf(items[0]),
                    Float.valueOf(items[1])));
        }

        bitNum = (int) (Math.log(points.size()) / Math.log(2.0)) + 1;

        points = sortLongitude(points);
        points = sortLatitude(points);
        points = HilbertCurve.hilbertCurve(points);

        for (int i = 0; i < points.size(); i++) {
            hValues.add(points.get(i).getzCurveValue());
        }

        LeafNode leafNode = null;
        List<Node> childrenNodes = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            if (i % pageSize == 0) {
                leafNode = new LeafNode(pageSize);
                childrenNodes.add(leafNode);
            }
            PointForCurve pointForCurve = points.get(i);
            leafNode.add(new Point((float) pointForCurve.getLongitude(),
                    (float) pointForCurve.getLatitude()));
        }

        leafnodes = new ArrayList<>(childrenNodes);

        List<Node> noneLeafNodes = new ArrayList<>();
        NoneLeafNode noneLeafNode = null;
        while (childrenNodes.size() != 1) {
            for (int i = 0; i < childrenNodes.size(); i++) {
                if (i % pageSize == 0) {
                    noneLeafNode = new NoneLeafNode(pageSize);
                    noneLeafNodes.add(noneLeafNode);
                }
                Node temp = childrenNodes.get(i);
                noneLeafNode.add(temp);
            }
//            System.out.println("孩子节点的个数:" + noneLeafNodes.size());
            childrenNodes = new ArrayList<>(noneLeafNodes);
            noneLeafNodes.clear();
        }
        root = (NoneLeafNode) childrenNodes.get(0);
    }

    @Timer
    public void query() {

    }

    //    private List<Point> searchKNNNew(int k, final Point query) {
    private List<Point> searchKNNNew(int k) {
        System.out.println("----------------searchKNN new----------------");
//        final Point query = new Point(100.5f, 50.5f);
//        final Point query = new Point(-123.83f, 45.93f);
        final Point query = new Point(0.1f, 0.1f);
        int pageAccessNum = 0;

        long begin = System.nanoTime();
        List<Point> retults = new ArrayList<>();

        PriorityQueue<Node> queue = new PriorityQueue(k, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                double dist1 = 0;
                double dist2 = 0;
                if (o1 instanceof NoneLeafNode || o1 instanceof LeafNode) {
                    dist1 = ((Node) o1).getMbr().calMINDIST(query);
                } else {
                    dist1 = query.calDist((Point) o1);
                }

                if (o2 instanceof NoneLeafNode || o2 instanceof LeafNode) {
                    dist2 = ((Node) o2).getMbr().calMINDIST(query);
                } else {
                    dist2 = query.calDist((Point) o2);
                }
                if (dist1 > dist2) {
                    return 1;
                } else if (dist1 < dist2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        ArrayList<Node> list = new ArrayList();
        list.add(root);
        while (list.size() > 0) {
            Node top = list.remove(0);
            if (top instanceof NoneLeafNode) {
                NoneLeafNode nonLeaf = (NoneLeafNode) top;
                List<Node> children = nonLeaf.getChildren();
                for (int i = 0; i < children.size(); i++) {
                    Node former = children.get(i);
                    boolean isProne = false;
                    for (int j = 0; j < children.size(); j++) {
                        if (i == j) {
                            continue;
                        }
                        Node later = children.get(j);
                        if (former.getMbr().calMINDIST(query) > later.getMbr().calMINMAXDIST(query)) {
                            // 这个点可以被剪掉了
                            isProne = true;
                            break;
                        }
                    }
                    if (!isProne) {
                        list.add(former);
                    }
                }
                pageAccessNum++;
            } else if (top instanceof LeafNode) {
                LeafNode leaf = (LeafNode) top;
                List<Point> children = leaf.getChildren();
                queue.addAll(children);
                pageAccessNum++;
            }
        }
        for (int i = 0; i < k; i++) {
            retults.add((Point) queue.poll());
        }
        long end = System.nanoTime();
        System.out.println(retults);
        System.out.println((end - begin) + "ns");
        System.out.println("pageAccessNum:" + pageAccessNum);
        return retults;
    }

    @Timer
    public void searchKNN() {
//        int k, Point query


    }

    private List<Point> search(int k, int queryXzvalue, int queryYzvalue) {
        List<Point> retults = new ArrayList<>();
        int upperX = queryXzvalue;
        int lowerX = queryXzvalue;

        int upperY = queryYzvalue;
        int lowerY = queryYzvalue;
        while (retults.size() < k) {
            lowerY--;
            for (int i = upperY; i >= lowerY; i--) {
                long result = HilbertCurve.get2DHilbertCurve(upperX, i, bitNum);
                if (hValues.contains(result)) {
                    retults.add(new Point(mappingLati.get(upperX).floatValue(), mappingLongi.get(i).floatValue()));
                }
            }
            lowerX--;
            for (int i = upperX; i >= lowerX; i--) {
                long result = HilbertCurve.get2DHilbertCurve(i, lowerY, bitNum);
                if (hValues.contains(result)) {
                    retults.add(new Point(mappingLati.get(i).floatValue(),
                            mappingLongi.get(lowerY).floatValue()));
                }
            }
            upperY++;
            for (int i = lowerY; i <= upperY; i++) {
                long result = HilbertCurve.get2DHilbertCurve(lowerX, i, bitNum);
                if (hValues.contains(result)) {
                    retults.add(new Point(mappingLati.get(lowerX).floatValue(), mappingLongi.get(i).floatValue()));
                }
            }
            upperX++;
            for (int i = lowerX; i <= upperX; i++) {
                long result = HilbertCurve.get2DHilbertCurve(i, upperY, bitNum);
                if (hValues.contains(result)) {
                    retults.add(new Point(mappingLati.get(i).floatValue(),
                            mappingLongi.get(upperY).floatValue()));
                }
            }
        }
        System.out.println("lowerX:" + lowerX + " lowerY" + lowerY);
        return retults;
    }

    public List<Point> windowQuery() {
        System.out.println("-------------------windowQuery-------------------");
        List<Point> retults = new ArrayList<>();
        Point p1 = new Point(50.5f, 100.5f);
        Point p2 = new Point(50.6f, 100.5f);
        Point p3 = new Point(50.5f, 100.65f);
        Point p4 = new Point(50.6f, 100.6f);


        int lowerX = -1;
        int lowerY = -1;
        int upperX = -1;
        int upperY = -1;
        long begin = System.currentTimeMillis();
        for (int i = 0; i < latis.size(); i++) {
            if (latis.get(i) > p1.getX()) {
                lowerX = i;
                break;
            }
        }

        for (int i = 0; i < longis.size(); i++) {
            if (longis.get(i) > p1.getY()) {
                lowerY = i;
                break;
            }
        }

        for (int i = 0; i < latis.size(); i++) {
            if (latis.get(i) > p4.getX()) {
                upperX = i;
                break;
            }
        }

        for (int i = 0; i < longis.size(); i++) {
            if (longis.get(i) > p4.getY()) {
                upperY = i;
                break;
            }
        }

        for (int i = lowerX; i < upperX; i++) {
            for (int j = lowerY; j < upperY; j++) {
                long result = HilbertCurve.get2DHilbertCurve(i, j, bitNum);
                if (hValues.contains(result)) {
                    retults.add(new Point(mappingLati.get(i).floatValue(),
                            mappingLongi.get(j).floatValue()));
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(retults.size());
//        for (int i = 0; i < retults.size(); i++) {
////            System.out.println(retults.get(i));
////        }
        System.out.println(end - begin);
        return retults;
    }

    public float calArea() {
        ArrayList<Node> list = new ArrayList();
        ArrayList<LeafNode> leaves = new ArrayList();

        list.add(root);
        while (list.size() > 0) {
            Node top = list.remove(0);
            if (top instanceof NoneLeafNode) {
                NoneLeafNode nonLeaf = (NoneLeafNode) top;
                List<Node> children = ((NoneLeafNode) top).getChildren();
                list.addAll(list.size(), children);
            } else {
                LeafNode leaf = (LeafNode) top;
                leaves.add(leaf);
            }
        }
        float area = 0f;
        for (int i = 0; i < leaves.size(); i++) {
            LeafNode leaf = leaves.get(i);
            area += leaf.getMbr().area();
        }
        System.out.println("leaf area:" + area);
        return area;
    }

    public float calPerimeter() {
        ArrayList<Node> list = new ArrayList();
        ArrayList<LeafNode> leaves = new ArrayList();

        list.add(root);
        while (list.size() > 0) {
            Node top = list.remove(0);
            if (top instanceof NoneLeafNode) {
                NoneLeafNode nonLeaf = (NoneLeafNode) top;
                List<Node> children = ((NoneLeafNode) top).getChildren();
                list.addAll(list.size(), children);
            } else {
                LeafNode leaf = (LeafNode) top;
                leaves.add(leaf);
            }
        }
        float peremeter = 0f;
        for (int i = 0; i < leaves.size(); i++) {
            LeafNode leaf = leaves.get(i);
            peremeter += leaf.getMbr().peremeter();
        }
        System.out.println("leaf perimeter:" + peremeter);
        return peremeter;
    }

    @Override
    public void insertPoint4DL(List<Point> points) {
        long begin = System.nanoTime();
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            int xIndex = binarySearch(latis, point.getX());
            int yIndex = binarySearch(longis, point.getY());
            int bitNum = (int) (Math.log(latis.size()) / Math.log(2.0)) + 1;

            // position of index
            int pos = binarySearch(hValues,
                    HilbertCurve.get2DHilbertCurve(xIndex, yIndex, bitNum));
            // inserted position
            int index = pos / pageSize;
            if (index >= 0 && index < leafnodes.size()) {
                LeafNode node = (LeafNode) leafnodes.get(index);
                if (node.isFull()) {

                    NoneLeafNode parent = findNode(node);
                    if (parent == null) {
                        System.out.println("ll");
                        continue;
                    }

                    int nodeIndex = parent.getChildren().indexOf(node);

                    LeafNode newLeafNode = node.split();
                    NoneLeafNode child = new NoneLeafNode(pageSize);
                    child.add(node);
                    child.add(newLeafNode);

                    parent.getChildren().set(nodeIndex, child);

                    leafnodes.add(index + 1, newLeafNode);
                } else {
                    node.add(point);
                }
            }
        }
        long end = System.nanoTime();
        System.out.println("H size:" + points.size() + " insert time:" + ((float) (end - begin)) / 1000000 + "ms");
    }

    @Override
    public String toString() {
        return "ZCurveRtree{" +
                "pageSize=" + pageSize +
                ", root=" + root +
                ", name='" + name + '\'' +
                ", dataset='" + dataset + '\'' +
                '}';
    }
}
