package com.unimelb.cis.node;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

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

    boolean isSubNonLeafModel = true;

    @Override
    public void build() {
        List<Point> points = getChildren();
        int bottomClassNum = points.size() / threshold + (points.size() % threshold == 0 ? 0 : 1);
        level = (int) (Math.log(bottomClassNum) / Math.log(pageSize));
        classNum = bottomClassNum / (int) Math.pow(pageSize, level);
        if (classNum <= 2) {
            isSubNonLeafModel = false;
        }
        for (int i = 0; i < points.size(); i++) {
            points.get(i).setIndex(i / (int) (threshold * Math.pow(pageSize, level)));
        }
        Instances instances = getInstances(name, getChildren());
        Classifier classifier = getModels(name);
        List<Double> results = getPredRes(classifier, instances);
        for (int i = 0; i < results.size(); i++) {
            add(results.get(i).intValue(), getChildren().get(i));
        }
        Map<Integer, Model> subModels = getSubModels();
        System.out.println("NonLeafModel:" + index + " levle:" + level);
        subModels.forEach(new BiConsumer<Integer, Model>() {
            @Override
            public void accept(Integer integer, Model model) {
//                System.out.println(integer + " " + model.getChildren().size());
                model.getChildren().sort((o1, o2) -> o1.getzCurveValue() > o2.getzCurveValue() ? 1 : -1);

                model.build();
            }
        });
    }
}
