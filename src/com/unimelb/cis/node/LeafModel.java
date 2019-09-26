package com.unimelb.cis.node;

import com.unimelb.cis.structures.recursivemodel.RecursiveModelRtree;
import weka.classifiers.Classifier;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeafModel extends Model {

    Map<Integer, LeafNode> leafNodes;

    public Map<Integer, LeafNode> getLeafNodes() {
        return leafNodes;
    }

    public void setLeafNodes(Map<Integer, LeafNode> leafNodes) {
        this.leafNodes = leafNodes;
    }

    public LeafModel(int index, int pageSize, String name) {
        super(index, pageSize, name);
        leafNodes = new HashMap<>();
    }

    public LeafModel(int index, String name) {
        super(index, name);
        leafNodes = new HashMap<>();
    }

    /**
     * TODO if there are more than 200 leafnodes, I may try to change the LeafModel to a NonLeafNode
     *
     * @param predictedVal
     * @param point
     */
    public void add(int predictedVal, Point point) {
        if (leafNodes.containsKey(predictedVal)) {
            leafNodes.get(predictedVal).add(point);
        } else {
            int dim = point.getDim();
            LeafNode model = new LeafNode(pageSize, dim);
            model.add(point);
            leafNodes.put(predictedVal, model);
        }
    }

    @Override
    public void build() {
//        System.out.println("LeafNode is Building");
        List<Point> points = getChildren();
        for (int i = 0; i < points.size(); i++) {
            points.get(i).setIndex(i / pageSize);
            add(i / pageSize, points.get(i));
        }

        classNum = points.size() / pageSize + (points.size() % pageSize == 0 ? 0 : 1);
//        System.out.println("classNum:" +classNum +" points.size()" + points.size());
        Instances instances = getInstances(name, points);
        classifier = getModels(name);
        train(classifier, instances);
//        List<Double> results = getPredRes(classifier, instances);
//        for (int i = 0; i < results.size(); i++) {
//            add(results.get(i).intValue(), getChildren().get(i));
//        }
        Map<Integer, LeafNode> leafNodes = getLeafNodes();
//        leafNodes.forEach((integer, leafNode) -> System.out.println("LeafNode:" + integer + " " + leafNode.getMbr()));
    }

    @Override
    public void pointQuery(Point point) {
//        System.out.println("LeafModel pointQuery->" + point);
        List<Point> points = new ArrayList<>();
        points.add(point);
        pointQuery(points);
    }

    @Override
    public void pointQuery(List<Point> points) {
//        System.out.println("LeafNode pointQuery(List<Point> points)");
        Instances instances = getInstances(name, points);
        List<Double> results = getPredVals(classifier, instances);
        int max = leafNodes.size();
        int min = 0;
        for (int i = 0; i < results.size(); i++) {
            int index = results.get(i).intValue();
            int gap = 1;
            int pageAccess = 1;
            if (index < 0) {
                index = 0;
            } else if (index >= leafNodes.size()) {
                index = leafNodes.size() - 1;
            }
            if (leafNodes.get(index).getChildren().contains(points.get(i))) {
                break;
            } else {
                while (true) {
                    pageAccess++;
                    int real = index - gap;
                    if (real >= min && leafNodes.get(real).getChildren().contains(points.get(i))) {
//                        System.out.println("find it: real" + real +" index" + index);
                        break;
                    }
                    pageAccess++;
                    real = index + gap;
                    if (real < max && leafNodes.get(real).getChildren().contains(points.get(i))) {
//                        System.out.println("find it: real" + real +" index" + index);
                        break;
                    }
                    gap++;
                }
            }
            RecursiveModelRtree.pageAccess += pageAccess;
        }
    }
}
