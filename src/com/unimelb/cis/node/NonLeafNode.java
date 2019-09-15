package com.unimelb.cis.node;

import com.unimelb.cis.geometry.Mbr;

import java.util.ArrayList;
import java.util.List;

public class NonLeafNode extends Node {

    private List<Node> children;

    @Override
    public boolean isFull() {
        return children.size() == pageSize;
    }

    public NonLeafNode() {
        super();
        children = new ArrayList<>();
    }

    public NonLeafNode(int pageSize, int dim) {
        super(pageSize, dim);
        children = new ArrayList<>();
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }


    public void addAll(List<Node> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            add(nodes.get(i));
        }
    }

    public boolean contains(Node child) {
        return this.children.contains(child);
    }

    public NonLeafNode splitBybisection() {

        // right part
        NonLeafNode nonLeafNode = new NonLeafNode(pageSize, dim);
        nonLeafNode.addAll(new ArrayList(children.subList(pageSize / 2, pageSize)));
        nonLeafNode.setParent(this.parent);
        nonLeafNode.setLevel(this.getLevel());

        // left part
        List<Node> temp = new ArrayList<>(children.subList(0, pageSize / 2));
        children.clear();
        this.addAll(temp);
        return nonLeafNode;
    }

    public void add(int index, Node node) {
        node.parent = this;
        children.add(index, node);
        updateMbr(node, dim);
    }

    public void add(Node node) {
        node.parent = this;
        children.add(node);
        updateMbr(node, dim);
//        updateMbr(node);
    }

    public void addAfterSplit(Node originalNode, Node newNode) {
        int index = children.indexOf(originalNode);
        index++;
        add(index, newNode);
    }

    private void updateMbr(Node node, int dim) {
        for (int i = 0; i < dim; i++) {
            float val = node.mbr.getLocation()[i];
            if (mbr.getLocation()[i] > val) {
                mbr.getLocation()[i] = val;
            }
            val = node.mbr.getLocation()[i + dim];
            if (mbr.getLocation()[i + dim] < val) {
                mbr.getLocation()[i + dim] = val;
            }
        }
    }

    private void updateMbr(Node node) {
        Mbr mbr = node.mbr;
        float x1 = mbr.getX1();
        float x2 = mbr.getX2();
        float y1 = mbr.getY1();
        float y2 = mbr.getY2();
        if (this.mbr.getX1() > x1) {
            this.mbr.setX1(x1);
        }
        if (this.mbr.getX2() < x2) {
            this.mbr.setX2(x2);
        }
        if (this.mbr.getY1() > y1) {
            this.mbr.setY1(y1);
        }
        if (this.mbr.getY2() < y2) {
            this.mbr.setY2(y2);
        }
    }

}
