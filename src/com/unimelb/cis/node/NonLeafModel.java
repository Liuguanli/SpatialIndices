package com.unimelb.cis.node;

import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.utils.ExpReturn;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class NonLeafModel extends Model {

    Map<Integer, Model> subModels;

    int level;

    int threshold;

    public int getLevel() {
        return level;
    }

    public NonLeafModel(int index, String name, int level, int threshold, int pageSize) {
        super(index, pageSize, name);
        this.subModels = new HashMap();
        this.level = level;
        this.threshold = threshold;
    }

    public NonLeafModel(int index, int pageSize, String name, int threshold) {
        super(index, pageSize, name);
        this.subModels = new HashMap();
        this.threshold = threshold;
    }

    public Map<Integer, Model> getSubModels() {
        return subModels;
    }

    public void setSubModels(Map<Integer, Model> subModels) {
        this.subModels = subModels;
    }

    public void add(int predictedVal, Point point) {
        if (subModels.containsKey(predictedVal)) {
            subModels.get(predictedVal).add(point);
        } else {
            Model model;
            if (isSubNonLeafModel) {
                model = new NonLeafModel(predictedVal, name, level - 1, threshold, pageSize);
            } else {
                model = new LeafModel(predictedVal, pageSize, name);
            }
            model.add(point);
            subModels.put(predictedVal, model);
        }
    }

    boolean isSubNonLeafModel = false;

    @Override
    public void build() {
        List<Point> points = getChildren();
        int bottomClassNum = points.size() / threshold + (points.size() % threshold == 0 ? 0 : 1);
        level = (int) (Math.log(bottomClassNum) / Math.log(pageSize));
        classNum = bottomClassNum / (int) Math.pow(pageSize, level);
        if (classNum > pageSize) {
            isSubNonLeafModel = true;
        }
        int denominator = (int) (threshold * Math.pow(pageSize, level));
        for (int i = 0; i < points.size(); i++) {
            points.get(i).setIndex(i / denominator);
        }
        Instances instances = getInstances(name, points);
        classifier = getModels(name);
        train(classifier, instances);
        List<Double> results = getPredVals(classifier, instances);
        for (int i = 0; i < results.size(); i++) {
            add(results.get(i).intValue(), getChildren().get(i));
        }
        Map<Integer, Model> subModels = getSubModels();
//        System.out.println("NonLeafModel:" + index + " levle:" + level);
        subModels.forEach(new BiConsumer<Integer, Model>() {
            @Override
            public void accept(Integer integer, Model model) {
//                System.out.println("LeafModel:" + integer + " " + model.getMbr());
                model.getChildren().sort((o1, o2) -> o1.getzCurveValue() > o2.getzCurveValue() ? 1 : -1);
                model.build();
            }
        });
    }

    @Override
    public ExpReturn pointQuery(Point point) {
        List<Point> points = new ArrayList<>();
        points.add(point);
        Instances instances = getInstances(name, points);
        List<Double> results = getPredVals(classifier, instances);
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        for (int i = 0; i < results.size(); i++) {
            ExpReturn eachExpReturn = subModels.get(results.get(i).intValue()).pointQuery(point);
            expReturn.pageaccess += eachExpReturn.pageaccess;
        }
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn pointQuery(List<Point> points) {
//        System.out.println("NonLeafNode pointQuery(List<Point> points)");
        Instances instances = getInstances(name, points);
        List<Double> results = getPredVals(classifier, instances);
        Double index = results.get(0);
        List<Point> sameIndexPoints = new ArrayList<>();
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        for (int i = 0; i < results.size(); i++) {
//            subModels.get(results.get(i).intValue()).pointQuery(points.get(i));
            if (results.get(i).intValue() == index.intValue()) {
//                sameIndexPoints.add(points.get(i));
            } else {
                ExpReturn eachExpReturn = subModels.get(index.intValue()).pointQuery(sameIndexPoints);
                expReturn.pageaccess += eachExpReturn.pageaccess;
                index = results.get(i);
                sameIndexPoints = new ArrayList<>();
//                sameIndexPoints.add(points.get(i));
            }
            sameIndexPoints.add(points.get(i));
            if (i == results.size() - 1) {
                ExpReturn eachExpReturn = subModels.get(index.intValue()).pointQuery(sameIndexPoints);
                expReturn.pageaccess += eachExpReturn.pageaccess;
            }
        }
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn windowQuery(Mbr window) {
        ExpReturn expReturn = new ExpReturn();
        final int[] pageAccessArray = {0};
        long begin = System.nanoTime();
        subModels.forEach((integer, leafModel) -> {
            if (leafModel.getMbr().interact(window)) {
                pageAccessArray[0]++;
                ExpReturn eachExpReturn = leafModel.windowQuery(mbr);
                expReturn.pageaccess += eachExpReturn.pageaccess;
                expReturn.result.addAll(eachExpReturn.result);
            }
        });
        long end = System.nanoTime();
        expReturn.pageaccess = pageAccessArray[0];
        expReturn.time = end - begin;
        return expReturn;
    }

}
