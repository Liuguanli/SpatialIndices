package com.unimelb.cis.node;

import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.utils.ExpReturn;
import weka.core.Instances;

import java.util.*;
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

    public void add(int predictedVal, List<Point> points) {
        points.forEach(point -> {
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
        });
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
            model.setType(type);
            subModels.put(predictedVal, model);
        }
    }

    boolean isSubNonLeafModel = false;

    @Override
    public void build() {
        List<Point> points = getChildren();
        int bottomClassNum = points.size() / threshold + (points.size() % threshold == 0 ? 0 : 1);
        level = (int) (Math.log(bottomClassNum) / Math.log(pageSize));
        classNum = bottomClassNum / (int) Math.pow(pageSize, level) + (bottomClassNum % (int) Math.pow(pageSize, level) == 0 ? 0 : 1);
//        System.out.println("bottomClassNum:" + bottomClassNum);
        if (level >= 1) {
            if (classNum > 2) {
                isSubNonLeafModel = true;
            } else {
                classNum = bottomClassNum;
                level--;
            }
        }
//        System.out.println("classNum:" + classNum);
        int denominator = (int) (threshold * Math.pow(pageSize, level));
        for (int i = 0; i < points.size(); i++) {
            points.get(i).setIndex(i / denominator);
        }
        Instances instances = getInstances(name, points, type);
        classifier = getModels(name);
        train(classifier, instances);
        List<Double> results = getPredVals0(classifier, instances);
        for (int i = 0; i < results.size(); i++) {
            add(results.get(i).intValue(), getChildren().get(i));
        }
        Map<Integer, Model> subModels = getSubModels();
//        System.out.println("NonLeafModel:" + index + " levle:" + level);
        subModels.forEach((integer, model) -> {
//                System.out.println("LeafModel:" + integer + " " + model.getMbr());
            model.getChildren().sort((o1, o2) -> o1.getCurveValue() > o2.getCurveValue() ? 1 : -1);
            model.build();
        });
    }

    @Override
    public ExpReturn pointQuery(Point point) {
        List<Point> points = new ArrayList<>();
        points.add(point);
        Instances instances = getInstances(name, points, type);
        List<Double> results = getPredVals0(classifier, instances);
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        for (int i = 0; i < results.size(); i++) {
            ExpReturn eachExpReturn = subModels.get(results.get(i).intValue()).pointQuery(points.get(i));
            expReturn.pageaccess += eachExpReturn.pageaccess;
        }
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn pointQuery(List<Point> points) {
//        System.out.println("NonLeafNode pointQuery(List<Point> points)");
        Instances instances = getInstances(name, points, type);
        List<Double> results = getPredVals0(classifier, instances);
        Double index = results.get(0);
        List<Point> sameIndexPoints = new ArrayList<>();
        ExpReturn expReturn = new ExpReturn();
        results.sort((Double::compareTo));
        for (int i = 0; i < results.size(); i++) {
//            subModels.get(results.get(i).intValue()).pointQuery(points.get(i));
            if (results.get(i).intValue() == index.intValue()) {
//                sameIndexPoints.add(points.get(i));
            } else {
//                System.out.println(index.intValue());
                // TODO the code should be optimized
                if (subModels.containsKey(index.intValue()) && sameIndexPoints.size() > 0) {
                    ExpReturn eachExpReturn = subModels.get(index.intValue()).pointQuery(sameIndexPoints);
                    expReturn.pageaccess += eachExpReturn.pageaccess;
                    expReturn.time += eachExpReturn.time;
                    index = results.get(i);
                    sameIndexPoints = new ArrayList<>();
                }
            }
            sameIndexPoints.add(points.get(i));
            if (i == results.size() - 1 && subModels.containsKey(index.intValue()) && sameIndexPoints.size() > 0) {
                ExpReturn eachExpReturn = subModels.get(index.intValue()).pointQuery(sameIndexPoints);
                expReturn.pageaccess += eachExpReturn.pageaccess;
            }
        }
        return expReturn;
    }

    public ExpReturn insert(List<Point> points) {
        List<Point> sameIndexPoints = new ArrayList<>();
        ExpReturn expReturn = new ExpReturn();
        Instances instances = getInstances(name, points, type);
        List<Double> results = getPredVals0(classifier, instances);
        Double index = results.get(0);

        long begin = System.nanoTime();
        results.sort((Double::compareTo));
        for (int i = 0; i < results.size(); i++) {
//            subModels.get(results.get(i).intValue()).pointQuery(points.get(i));
            if (results.get(i).intValue() == index.intValue()) {
//                sameIndexPoints.add(points.get(i));
            } else {
                if (subModels.containsKey(index.intValue())) {
                    ExpReturn eachExpReturn = subModels.get(index.intValue()).insert(sameIndexPoints);
                    expReturn.pageaccess += eachExpReturn.pageaccess;
                } else {
                    add(results.get(i).intValue(), sameIndexPoints);
                }
                index = results.get(i);
                sameIndexPoints = new ArrayList<>();
//                sameIndexPoints.add(points.get(i));
            }
            sameIndexPoints.add(points.get(i));
            if (i == results.size() - 1) {
                if (subModels.containsKey(index.intValue())) {
                    ExpReturn eachExpReturn = subModels.get(index.intValue()).insert(sameIndexPoints);
                    expReturn.pageaccess += eachExpReturn.pageaccess;
                } else {
                    add(results.get(i).intValue(), sameIndexPoints);
                }
            }
        }
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    public ExpReturn insert(Point point) {
        List<Point> points = Arrays.asList(point);
        return insert(points);
    }


    @Override
    public ExpReturn windowQuery(Mbr window) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        subModels.forEach((integer, leafModel) -> {
            if (leafModel.getMbr().interact(window)) {
                ExpReturn eachExpReturn = leafModel.windowQuery(mbr);
                expReturn.pageaccess += eachExpReturn.pageaccess;
                expReturn.pageaccess++;
                expReturn.result.addAll(eachExpReturn.result);
            }
        });
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn windowQueryByScanAll(Mbr window) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        subModels.forEach((integer, leafModel) -> {
            if (leafModel.getMbr().interact(window)) {
                ExpReturn eachExpReturn = leafModel.windowQueryByScanAll(mbr);
                expReturn.plus(eachExpReturn);
            }
        });
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

}
