package com.unimelb.cis.node;

import com.unimelb.cis.geometry.Mbr;

import java.util.ArrayList;
import java.util.Comparator;
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

    @Override
    public void adjust() {
        updateMbr();
    }

    private void updateMbr() {
        mbr = new Mbr(dim);
        for (int i = 0; i < children.size(); i++) {
            updateMbr(children.get(i), dim);
        }
        setOMbr(mbr.clone());
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
        setOMbr(mbr.clone(), true);
    }

    public boolean contains(Node child) {
        return this.children.contains(child);
    }


    /**
     * split the node from middle
     *
     * @return
     */
    public NonLeafNode splitByBisection(int splitPosition) {

        // right part
        NonLeafNode nonLeafNode = new NonLeafNode(pageSize, dim);
        nonLeafNode.addAll(new ArrayList(children.subList(splitPosition, pageSize)));
        nonLeafNode.setParent(this.parent);
        nonLeafNode.setLevel(this.getLevel());

        // left part
        List<Node> temp = new ArrayList<>(children.subList(0, splitPosition));
        children.clear();
        this.addAll(temp);
        nonLeafNode.setLevel(this.getLevel());
        return nonLeafNode;
    }

    public NonLeafNode splitByBisection() {
        return splitByBisection(pageSize / 2);
    }


    public Comparator<Node> getComparator(final int index) {
        return (o1, o2) -> {
            if (o1.getMbr().getProjectionByAxis(index) > o2.getMbr().getProjectionByAxis(index)) {
                return 1;
            } else if (o1.getMbr().getProjectionByAxis(index) < o2.getMbr().getProjectionByAxis(index)) {
                return -1;
            } else {
                return 0;
            }
        };
    }

    /**
     * original R*tree split
     *
     * @param m
     * @param insertedNode
     * @return
     */
    public NonLeafNode splitRStar(int m, Node insertedNode) {
        int minAxis = m;
        float minPerim = Float.MAX_VALUE;
        float minOverlap = Float.MAX_VALUE;
        NonLeafNode result;
        List<Node> nodes = new ArrayList<>(children);
        nodes.add(insertedNode);
        for (int j = 0; j < dim; j++) {
            final int index = j;
            nodes.sort(getComparator(index));

            float tempPerimeter = 0;
            for (int i = m; i < nodes.size() - m; i++) {
                List<Node> left = nodes.subList(0, i);
                List<Node> right = nodes.subList(i, nodes.size());

                NonLeafNode leafNode = new NonLeafNode(pageSize, dim);
                leafNode.addAll(new ArrayList<>(left));
                leafNode.setParent(this.parent);
                leafNode.setLevel(this.getLevel());

                NonLeafNode rightNode = new NonLeafNode(pageSize, dim);
                rightNode.addAll(new ArrayList<>(right));
                rightNode.setParent(this.parent);
                rightNode.setLevel(this.getLevel());

                tempPerimeter += leafNode.getMbr().perimeter() + rightNode.getMbr().perimeter();
            }
            if (tempPerimeter < minPerim) {
                minPerim = tempPerimeter;
                minAxis = j;
            }
        }

        minPerim = Float.MAX_VALUE;
        int minOvlpI = -1;
        int minPerimI = -1;
        nodes.sort(getComparator(minAxis));
        int minWI = 0;
        int minI = 0;
        double minW = Double.MAX_VALUE;
        for (int i = m; i < nodes.size() - m; i++) {
            List<Node> left = nodes.subList(0, i);
            List<Node> right = nodes.subList(i, nodes.size());

            NonLeafNode leafNode = new NonLeafNode(pageSize, dim);
            leafNode.addAll(new ArrayList<>(left));
            leafNode.setParent(this.parent);
            leafNode.setLevel(this.getLevel());

            NonLeafNode rightNode = new NonLeafNode(pageSize, dim);
            rightNode.addAll(new ArrayList<>(right));
            rightNode.setParent(this.parent);
            rightNode.setLevel(this.getLevel());

            float tempOverlap = leafNode.getMbr().getOverlapVol(rightNode.getMbr());
            float tempPerim = leafNode.getMbr().perimeter() + rightNode.getMbr().perimeter();
            if (tempOverlap < minOverlap || tempOverlap == 0) {
                minOverlap = tempOverlap;
                minOvlpI = i;
                if (minOverlap == 0) {
                    if (tempPerim < minPerim) {
                        minPerim = tempPerim;
                        minPerimI = i;
                    }
                }
            }
            // if only use the original R*tree, only the following line is enough
            minI = minOverlap == 0 ? minPerimI : minOvlpI;
            //  For revisited R*tree minI is not the final result. w = wg * wf;  minI is the wg
            double wf = weightFunction(m, i, 0.5, minAxis);
            if (wf < 0) {
                // use the original R*tree method.
                minWI = minI;
            } else {
                double wg = minI;
                double w = minOverlap == 0 ? wf * wg : wg / wf;
                if (w < minW) {
                    minWI = i;
                }
            }
        }
        minI = minWI;
        // right part
        result = new NonLeafNode(pageSize, dim);
        result.addAll(new ArrayList(nodes.subList(minI, nodes.size())));
        result.setParent(this.parent);
        result.setLevel(result.getChildren().get(0).getLevel());

        // left part
        List<Node> temp = new ArrayList<>(nodes.subList(0, minI));
        children.clear();
        this.addAll(temp);
        this.adjust();
        return result;
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

}
