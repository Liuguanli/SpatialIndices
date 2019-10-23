package com.unimelb.cis.utils;

import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.LeafNode;
import com.unimelb.cis.node.Node;
import com.unimelb.cis.node.NonLeafNode;
import com.unimelb.cis.structures.IRtree;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class Visualizer {

    private IRtree tree;

    private int height;

    private int width;

    private Mbr view;

    private int maxDepth;

    private List<Mbr> mbrs;

    public Visualizer(IRtree tree, int height, int width, Mbr view) {
        this.tree = tree;
        this.height = height;
        this.width = width;
        this.view = view;
        this.maxDepth = tree.getLevel();
    }

    public Visualizer(List<Mbr> mbrs, int height, int width, Mbr view) {
        this.mbrs = mbrs;
        this.height = height;
        this.width = width;
        this.view = view;
    }

    public BufferedImage createImageMBR() {
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = (Graphics2D) image.getGraphics();
        g.setBackground(Color.white);
        g.clearRect(0, 0, width, height);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));

        final List<RectangleDepth> nodeDepths = getNodeDepthsSortedByDepthMBR();
        drawNode(g, nodeDepths);
        return image;
    }

    public BufferedImage createImage() {
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = (Graphics2D) image.getGraphics();
        g.setBackground(Color.white);
        g.clearRect(0, 0, width, height);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));

        final List<RectangleDepth> nodeDepths = getNodeDepthsSortedByDepth(tree.getRoot());
        drawNode(g, nodeDepths);
        return image;
    }

    private List<RectangleDepth> getNodeDepthsSortedByDepthMBR() {
        List<RectangleDepth> list = new ArrayList<>();
        mbrs.forEach(mbr -> list.add(new RectangleDepth(mbr, 1)));
        return list;
    }

    private List<RectangleDepth> getNodeDepthsSortedByDepth(Node root) {
        final List<RectangleDepth> list = getRectangleDepths(root, 0);
        Collections.sort(list, (n1, n2) -> ((Integer) n1.getDepth()).compareTo(n2.getDepth()));
        return list;
    }

    private List<RectangleDepth> getRectangleDepths(Node node,
                                                    int depth) {
        final List<RectangleDepth> list = new ArrayList();
        if (node instanceof LeafNode) {
            list.add(new RectangleDepth(node.getMbr(), depth));
        }
        if (node instanceof LeafNode) {
            final LeafNode leaf = (LeafNode) node;
            for (int i = 0; i < leaf.getChildren().size(); i++) {
                list.add(new RectangleDepth(leaf.getChildren().get(i).getMbr(), depth + 2));
            }
        } else if (node instanceof NonLeafNode){
            final NonLeafNode n = (NonLeafNode) node;
            for (int i = 0; i < n.getChildren().size(); i++) {
                list.addAll(getRectangleDepths(n.getChildren().get(i), depth + 1));
            }
        }
        return list;
    }

    private void drawNode(Graphics2D g, List<RectangleDepth> nodes) {
        for (final RectangleDepth node : nodes) {
            final Color color = Color.getHSBColor(node.getDepth() / (maxDepth + 1f), 1f, 1f);
            g.setStroke(new BasicStroke(Math.max(0.5f, maxDepth - node.getDepth() + 1 - 1)));
            g.setColor(color);
            final Mbr r = node.getRectangle();
            drawRectangle(g, r);
        }
    }

    private void drawRectangle(Graphics2D g, Mbr mbr) {
        final double x1 = (mbr.getX1() - view.getX1()) / (view.getX2() - view.getX1()) * width;
        final double y1 = (mbr.getY1() - view.getY1()) / (view.getY2() - view.getY1()) * height;
        final double x2 = (mbr.getX2() - view.getX1()) / (view.getX2() - view.getX1()) * width;
        final double y2 = (mbr.getY2() - view.getY1()) / (view.getY2() - view.getY1()) * height;
        g.drawRect(rnd(x1), rnd(y1), Math.max(rnd(x2 - x1), 1), Math.max(rnd(y2 - y1), 1));
    }

    private static int rnd(double d) {
        return (int) Math.round(d);
    }

    public void save(File file, String imageFormat) {
        ImageSaver.save(createImage(), file, imageFormat);
    }

    public void saveMBR(String filename) {
        ImageSaver.save(createImageMBR(), new File(filename), "PNG");
    }

    public void save(String filename, String imageFormat) {
        save(new File(filename), imageFormat);
    }

    public void save(String filename) {
        save(new File(filename), "PNG");
    }

}
