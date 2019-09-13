package com.unimelb.cis;

import com.unimelb.cis.structures.zrtree.ZRtree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVFileWriter {

    public static void write(List<String> lines, String fileName) {
        File csv = new File(fileName);  // CSV文件路径
        FileWriter fw = null;
        try {
            fw = new FileWriter(csv);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                fw.write(line + "\r\n");
                fw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    public void writePartition(List<Integer> partitions, String fileName) {
//        File csv = new File(fileName);  // CSV文件路径
//        FileWriter fw = null;
//        try {
//            fw = new FileWriter(csv);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        for (Integer pagesize : partitions) {
//            StringBuilder builder = new StringBuilder();
//            builder.append(pagesize);
//            builder.append("\r\n");
//            try {
//                fw.write(builder.toString());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        try {
//            fw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void writeQueryProfile(List<Mbr> mbrs, String fileName) {
//        File csv = new File(fileName);  // CSV文件路径
//        FileWriter fw = null;
//        try {
//            fw = new FileWriter(csv);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        for (Mbr mbr : mbrs) {
//            StringBuilder builder = new StringBuilder();
//            builder.append(mbr.printFormat());
//            builder.append("\r\n");
//            try {
//                fw.write(builder.toString());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        try {
//            fw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void write(List<PointForCurve> points, String fileName) {
//        File csv = new File(fileName);  // CSV文件路径
//        FileWriter fw = null;
//        try {
//            fw = new FileWriter(csv);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        for (PointForCurve point : points) {
//            StringBuilder builder = new StringBuilder();
//            builder.append(point.getLongitude() + "," + point.getLatitude());
//            builder.append("," + point.getIndex());
//            builder.append("\r\n");
//            try {
//                fw.write(builder.toString());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        try {
//            fw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void write(List<PointForCurve> points, String fileName, int pageSize) {
//        File csv = new File(fileName);  // CSV文件路径
////        if (!csv.exists()) {
////            try {
////                csv.mkdirs();
////            } catch (Exception e) {
////                // TODO: handle exception
////            }
////        }
//        int level = (int) (Math.log(points.size()) / Math.log(pageSize));
//        System.out.println("总数:" + points.size() + " 页数:" + pageSize + " 层数:" + level);
//        FileWriter fw = null;
//        try {
//            fw = new FileWriter(csv);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        int index = 0;
//        for (PointForCurve point : points) {
//            StringBuilder builder = new StringBuilder();
//            builder.append(point.getLongitude() + "," + point.getLatitude());
//            builder.append("," + (index / pageSize));
////            int dividend = pageSize;
////            for (int j = 0; j < level; j++) {
////                builder.append("," + (index / dividend));
////                dividend *= pageSize;
////            }
//            builder.append("\r\n");
//            try {
//                fw.write(builder.toString());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            index++;
//        }
////        for (int i = 0; i < points.size(); i++) {
////            PointForCurve point = points.get(i);
////            StringBuilder builder = new StringBuilder();
////            builder.append(point.getLongitude() + "," + point.getLatitude());
////            int dividend = pageSize;
////            for (int j = 0; j < level; j++) {
////                builder.append("," + (i / dividend));
////                dividend *= pageSize;
////            }
////            builder.append("\r\n");
////            try {
////                fw.write(builder.toString());
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
////        }
//
//        try {
//            fw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void writeEdges(List<Double> latitudeEdges, List<Double> longitudeEdges, String fileName) {
//        File csv = new File(fileName);  // CSV文件路径
//        FileWriter fw = null;
//        try {
//            fw = new FileWriter(csv);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        for (int i = 0; i < latitudeEdges.size(); i++) {
//            try {
//                fw.write(longitudeEdges.get(i) + "," + latitudeEdges.get(i) + "\r\n");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        try {
//            fw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void writeFile(List<Leaf> leaves, String fileName) {
//        File csv = new File(fileName);  // CSV文件路径
//        FileWriter fw = null;
//        try {
//            fw = new FileWriter(csv);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        for (int i = 0; i < leaves.size(); i++) {
//            Leaf leaf = leaves.get(i);
//            List<Point> points = leaf.getLeafNode().getChildren();
//            for (Point point : points) {
//                try {
//                    fw.write(point.getX() + "," + point.getY() + "," + i + "\r\n");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        try {
//            fw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void writeFile1(List<QuadNode> leaves, String fileName) {
//        File csv = new File(fileName);  // CSV文件路径
//        FileWriter fw = null;
//        try {
//            fw = new FileWriter(csv);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        for (int i = 0; i < leaves.size(); i++) {
//            QuadNode leaf = leaves.get(i);
//            List<Point> points = leaf.getChildren();
//            for (Point point : points) {
//                try {
//                    fw.write(point.getX() + "," + point.getY() + "," + i + "\r\n");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        try {
//            fw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public void writeFile2(List<com.leo.r_tree_rxjava.kdb_tree.Node> nodes, String fileName) {
//        File csv = new File(fileName);  // CSV文件路径
//        FileWriter fw = null;
//        try {
//            fw = new FileWriter(csv);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        for (int i = 0; i < nodes.size(); i++) {
//            com.leo.r_tree_rxjava.kdb_tree.Node temp = nodes.get(i);
//            List<com.leo.r_tree_rxjava.kdb_tree.Point> points = temp.getPointList();
//            for (int j = 0; j < points.size(); j++) {
//                com.leo.r_tree_rxjava.kdb_tree.Point point = points.get(j);
//                try {
//                    fw.write(point.getX() + "," + point.getY() + "," + i + "\r\n");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        try {
//            fw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void writeFile3(List<LeafDefault> leaves, String fileName) {
//        File csv = new File(fileName);  // CSV文件路径
//        FileWriter fw = null;
//        try {
//            fw = new FileWriter(csv);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        for (int i = 0; i < leaves.size(); i++) {
//            LeafDefault leaf = leaves.get(i);
//            List<Entry> entries = leaf.entries();
//            for (int j = 0; j < entries.size(); j++) {
//                Entry entry = entries.get(j);
//                try {
//                    fw.write(entry.geometry().mbr().x1() + "," + entry.geometry().mbr().y1() + "," + i + "\r\n");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }
//
//        try {
//            fw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


}
