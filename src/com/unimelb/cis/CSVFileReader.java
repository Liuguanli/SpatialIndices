package com.unimelb.cis;

import com.unimelb.cis.node.Point;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVFileReader {

    public static List<String> read(String fileName) {
        File csv = new File(fileName);  // CSV文件路径
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(csv));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line = "";
        String everyLine = "";
        List<String> allString = null;
        try {
            allString = new ArrayList();
            while ((line = br.readLine()) != null)  //读取到的内容给line变量
            {
                everyLine = line;
//                System.out.println(everyLine);
                allString.add(everyLine);
            }
//            System.out.println("csv表格中所有行数：" + allString.size());
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allString;
    }

    public static List<Point> readPoints(String fileName) {
        List<String> lines = read(fileName);

        List<Point> points = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Point point = new Point(line);
            points.add(point);
        }

        return points;
    }
}
