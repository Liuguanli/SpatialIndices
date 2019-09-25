package com.unimelb.cis.node;

import com.unimelb.cis.geometry.Mbr;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

public abstract class Model {

    List<String> clas = Arrays.asList("Logistic");
    List<String> regs = Arrays.asList("LinearRegression");

    public Model(int index, int pageSize, String name) {
        this.index = index;
        this.pageSize = pageSize;
        this.name = name;
        this.children = new ArrayList<>();
    }

    public Model(int index, String name) {
        this.index = index;
        this.name = name;
        this.children = new ArrayList<>();
    }

    String name;

    int pageSize;

    Mbr mbr;

    int index;

    List<Point> children;

    int classNum;

    public List<Point> getChildren() {
        return children;
    }

    public void setChildren(List<Point> children) {
        this.children = children;
    }

    public Mbr getMbr() {
        return mbr;
    }

    public void setMbr(Mbr mbr) {
        this.mbr = mbr;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void add(Point point) {
        this.children.add(point);
    }

    public Instances getInstances(String name, List<Point> points) {
        if (clas.contains(name)) {
            return prepareDataSetCla(points, classNum);
        } else {
            return prepareDataSetReg(points, classNum);
        }
    }

    /**
     * step 2
     *
     * @param points
     * @return
     */
    public Instances prepareDataSetCla(List<Point> points, int classNum) {
        int dim = points.get(0).getDim();
        FastVector atts = new FastVector();
        FastVector attVals = new FastVector();
        for (int i = 0; i < dim; i++) {
            atts.addElement(new Attribute("att" + (i + 1)));
        }
        for (int i = 0; i < classNum; i++) {
            attVals.addElement("" + (i));
        }
        atts.addElement(new Attribute("index", attVals));
        Instances dataSet = new Instances("tree", atts, 0);
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            double[] vals = new double[dataSet.numAttributes()];
            for (int j = 0; j < vals.length - 1; j++) {
                vals[j] = point.getLocation()[j];
            }
            vals[vals.length - 1] = attVals.indexOf("" + point.getIndex());
            dataSet.add(new Instance(1.0, vals));
        }
        dataSet.setClassIndex(dataSet.numAttributes() - 1);
        return dataSet;
    }

    public Instances prepareDataSetReg(List<Point> points, int classNum) {
        int dim = points.get(0).getDim();
        FastVector atts = new FastVector();
        for (int i = 0; i < dim; i++) {
            atts.addElement(new Attribute("att" + (i + 1)));
        }
        atts.addElement(new Attribute("index"));
        Instances dataSet = new Instances("tree", atts, 0);
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            double[] vals = new double[dataSet.numAttributes()];
            for (int j = 0; j < vals.length - 1; j++) {
                vals[j] = point.getLocation()[j];
            }
            vals[vals.length - 1] = point.getIndex();
            dataSet.add(new Instance(1.0, vals));
        }
        dataSet.setClassIndex(dataSet.numAttributes() - 1);
        return dataSet;
    }

    public Classifier getModels(String name) {
        Classifier classifier = null;
        switch (name) {
            case "Logistic":
                classifier = new Logistic();
                break;
            case "LinearRegression":
                classifier = new LinearRegression();
                break;
        }
        return classifier;
    }

    public void getStatistics(List<Double> results, Instances instances) {
        int accNum = 0;
        int pageAccess = 0;
        for (int i = 0; i < results.size(); i++) {
            int result = results.get(i).intValue();
            int real = (int) instances.instance(i).classValue();
            if (result == real) {
                accNum++;
                pageAccess++;
            } else {
                pageAccess += Math.abs(result - real) * 2 + 1;
            }
        }
        System.out.println("acc:" + ((double) accNum / results.size()) + " pageAccess:" + ((double) pageAccess / results.size()));
    }

    public List<Double> getPredRes(Classifier classifier, Instances instances) {
        List<Double> results = new ArrayList<>();
        try {
            classifier.buildClassifier(instances);
            for (int i = 0; i < instances.numInstances(); i++) {
                Instance instance = instances.instance(i);
                double value = classifier.classifyInstance(instance);
                results.add(value);
//                System.out.println(instance.classValue() + " " + value);
            }
//            getStatistics(results, instances);
            // put points into different buckets
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    public abstract void build();

}
