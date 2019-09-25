package com.unimelb.cis.node;

import weka.classifiers.Classifier;
import weka.core.Instances;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

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
        List<Point> points = getChildren();
        for (int i = 0; i < points.size(); i++) {
            points.get(i).setIndex(i / pageSize);
        }
        classNum = points.size() / pageSize + (points.size() % pageSize == 0 ? 0 : 1);
        Instances instances = getInstances(name, getChildren());
        Classifier classifier = getModels(name);
        List<Double> results = getPredRes(classifier, instances);
        for (int i = 0; i < results.size(); i++) {
            add(results.get(i).intValue(), getChildren().get(i));
        }
        Map<Integer, LeafNode> leafNodes = getLeafNodes();
        leafNodes.forEach((integer, leafNode) -> System.out.println("LeafNode:" + integer + " " + leafNode.getChildren().size()));
    }
}
