package com.unimelb.cis.structures;

import com.unimelb.cis.node.LeafNode;
import com.unimelb.cis.node.Node;
import com.unimelb.cis.node.NonLeafNode;
import com.unimelb.cis.node.Point;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class RLRtree extends IRtree {

    public RLRtree(int pagesize) {
        super(pagesize);
    }

    public void output(String file) {

        File csv = new File(file);  // CSV文件路径
        FileWriter fw = null;
        try {
            fw = new FileWriter(csv);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        List<String> lines = new ArrayList<>();
        List<Node> nodes = new ArrayList<>();
        nodes.add(root);
        int lineNum = 1;
        while (nodes.size() > 0) {
            Node top = nodes.remove(0);
            if (top instanceof NonLeafNode) {
                nodes.addAll(((NonLeafNode) top).getChildren());
            } else if (top instanceof LeafNode) {
                nodes.addAll(((LeafNode) top).getChildren());
            } else {
//                lines.add(((Point) top).getOutPutString(root));

                lineNum++;
                String line = ((Point) top).getOutPutString(root);
                try {
                    fw.write(line + "\r\n");
                    if (lineNum % 1000000 == 0) {
                        fw.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        try {
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
