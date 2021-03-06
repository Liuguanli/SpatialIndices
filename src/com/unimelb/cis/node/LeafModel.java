package com.unimelb.cis.node;

import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.utils.ExpReturn;
import weka.core.Instances;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
        if (points.size() < pageSize) {
            points.forEach(new Consumer<Point>() {
                @Override
                public void accept(Point point) {
                    point.setIndex(0);
                    add(0, point);
                }
            });
            return;
        }
        for (int i = 0; i < points.size(); i++) {
            points.get(i).setIndex(i / pageSize);
            add(i / pageSize, points.get(i));
        }

        classNum = points.size() / pageSize + (points.size() % pageSize == 0 ? 0 : 1);
//        System.out.println("classNum:" +classNum +" points.size()" + points.size());
        Instances instances = getInstances(name, points, type);
//        Instances instances = prepareDataSetCla(points, classNum);
        classifier = getModels(name);
        train(classifier, instances);
        evaluate();
//        System.out.println("maxError:" + maxError + " minError:" + minError);
    }

    //    level:0
//    getModelIndex time:78597
//    LeafModel time:3516
//    Total search time:364208
//    point query:time=442805
    @Override
    public ExpReturn pointQuery(Point point) {
        Instances instances = getInstances(name, Arrays.asList(point), type);
        ExpReturn expReturn = new ExpReturn();

        ExpReturn predictExpReturn = getPredVals(classifier, instances);

//        System.out.println("predictExpReturn.time:" + predictExpReturn.time);
//        System.out.println("getPredVals time:" + (e1 - b1));
        List<Double> results = predictExpReturn.predictResults;
        long begin = System.nanoTime();
        int max = leafNodes.size();
        int min = 0;
        int result = results.get(0).intValue();
        int index = Math.min(Math.max(result, 0), leafNodes.size() - 1);
        int gap = 1;
        if (index < 0) {
            index = 0;
        } else if (index >= leafNodes.size()) {
            index = leafNodes.size() - 1;
        }
        expReturn.pageaccess++;
        if (!leafNodes.get(index).getChildren().contains(point)) {
            while (true) {
                int real = index - gap;
                LeafNode leafNode = leafNodes.get(real);
                if (real >= min && leafNode.getMbr().contains(point)) {
                    expReturn.pageaccess++;
                    if (leafNode.getChildren().contains(point)) {
                        break;
                    }
                }
                real = index + gap;
                leafNode = leafNodes.get(real);
                if (real < max && leafNode.getMbr().contains(point)) {
                    expReturn.pageaccess++;
                    if (leafNode.getChildren().contains(point)) {
                        break;
                    }
                }
                gap++;
                if (index - gap < min && index + gap > max) {
                    break;
                }
            }
        }
        long end = System.nanoTime();
        expReturn.time = end - begin + predictExpReturn.time;
//        System.out.println("Total search time:" + (end - begin));
        return expReturn;
    }

    public ExpReturn insert(Point point) {
        return insert(Arrays.asList(point));
    }

    public ExpReturn insert(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        Instances instances = getInstances(name, points, type);
        ExpReturn predictExpReturn = getPredVals(classifier, instances);
        List<Double> results = predictExpReturn.predictResults;
        long begin = System.nanoTime();
        for (int i = 0; i < results.size(); i++) {
            int index = results.get(i).intValue();
            Point point = points.get(i);
            LeafNode target = leafNodes.get(index);
            if (target == null) {
                LeafNode newLeafNode = new LeafNode(pageSize, point.getDim());
                newLeafNode.add(point);
                leafNodes.put(index, newLeafNode);
            } else {
                if (target.isFull()) {
                    LeafNode newLeafNode = target.split();
                    expReturn.pageaccess++;
                    int last = leafNodes.size();
                    for (int j = last; j > index + 1; j--) {
                        leafNodes.put(j, leafNodes.get(j - 1));
                    }
                    leafNodes.put(index + 1, newLeafNode);
                } else {
                    target.add(point);
                }
            }
        }

        long end = System.nanoTime();
        expReturn.time = end - begin + predictExpReturn.time;
        return expReturn;
    }

    @Override
    public ExpReturn pointQuery(List<Point> points) {
//        System.out.println("LeafNode pointQuery(List<Point> points)");
        Instances instances = getInstances(name, points, type);
        ExpReturn expReturn = new ExpReturn();
        ExpReturn predictExpReturn = getPredVals(classifier, instances);
        List<Double> results = predictExpReturn.predictResults;
        long begin = System.nanoTime();
        int max = leafNodes.size();
        int min = 0;
        for (int i = 0; i < results.size(); i++) {
            int index = Math.min(Math.max(results.get(i).intValue(), 0), leafNodes.size() - 1);
            int gap = 1;
            int pageAccess = 1;
            if (index < 0) {
                index = 0;
            } else if (index >= leafNodes.size()) {
                index = leafNodes.size() - 1;
            }
            Point queryPoint = points.get(i);
            if (!leafNodes.get(index).getChildren().contains(queryPoint)) {
                while (true) {
                    int real = index - gap;
                    LeafNode leafNode = leafNodes.get(real);
                    if (real >= min && leafNode.getMbr().contains(queryPoint)) {
                        if (leafNode.getChildren().contains(queryPoint)) {
                            pageAccess++;
                            break;
                        }
                    }
                    real = index + gap;
                    leafNode = leafNodes.get(real);
                    if (real < max && leafNode.getMbr().contains(queryPoint)) {
                        if (leafNode.getChildren().contains(queryPoint)) {
                            pageAccess++;
                            break;
                        }
                    }
                    gap++;
                    if (index - gap < min && index + gap > max) {
                        break;
                    }
                }
            }
            expReturn.pageaccess += pageAccess;
        }
        long end = System.nanoTime();
        expReturn.time = end - begin + predictExpReturn.time;
        System.out.println("Total search time:" + (end - begin));
        return expReturn;
    }

    @Override
    public ExpReturn windowQuery(Mbr window) {

//        ExpReturn old = windowQueryByScanAll(window);
//        System.out.println("windowQueryByScanAll:" + old.result.size());
        ExpReturn expReturn = new ExpReturn();
        final int[] pageAccessArray = {0};
        List<Point> vertexes = window.getAllPoints();
        Instances instances = getInstances(name, vertexes, type);
        ExpReturn predictExpReturn = getPredVals(classifier, instances);
        List<Double> results = predictExpReturn.predictResults;
        long begin = System.nanoTime();
        results.sort(Double::compareTo);
        int indexLow = results.get(0).intValue();
        int indexHigh = results.get(results.size() - 1).intValue();
        indexLow = Math.max(0, indexLow - minError);
        indexHigh = Math.min(indexHigh + maxError, leafNodes.size());
//        System.out.println("indexLow:" + indexLow + " indexHigh:" + indexHigh);
        for (int i = indexLow; i < indexHigh; i++) {
            LeafNode leafNode = leafNodes.get(i);
            if (leafNode.getMbr().interact(window)) {
                pageAccessArray[0]++;
                leafNode.getChildren().forEach(point -> {
                    if (window.contains(point)) {
                        expReturn.result.add(point);
                    }
                });
            }
        }
//        leafNodes.forEach((integer, leafNode) -> {
//            if (integer >= Math.max(0, indexLow - minError) && integer < Math.min(indexHigh + maxError, leafNodes.size())) {
//                if (leafNode.getMbr().interact(window)) {
//                    pageAccessArray[0]++;
//                    leafNode.getChildren().forEach(point -> {
//                        if (window.contains(point)) {
//                            expReturn.result.add(point);
//                        }
//                    });
//                }
//            }
//        });
//        System.err.println(results);
        long end = System.nanoTime();
        expReturn.pageaccess = pageAccessArray[0];
        expReturn.time = end - begin + predictExpReturn.time;
//        System.out.println("windowQuery:" + expReturn.result.size());
        return expReturn;
    }

    @Override
    public ExpReturn windowQueryByScanAll(Mbr window) {
        ExpReturn expReturn = new ExpReturn();
        final int[] pageAccessArray = {0};
        long begin = System.nanoTime();
        leafNodes.forEach((integer, leafNode) -> {
            if (leafNode.getMbr().interact(window)) {
                pageAccessArray[0]++;
                leafNode.getChildren().forEach(point -> {
                    if (window.contains(point)) {
                        expReturn.result.add(point);
                    }
                });
            }
        });
        long end = System.nanoTime();
        expReturn.pageaccess = pageAccessArray[0];
        expReturn.time = end - begin;
        return expReturn;
    }
}
