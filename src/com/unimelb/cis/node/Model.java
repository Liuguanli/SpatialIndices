package com.unimelb.cis.node;

import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.structures.recursivemodel.RecursiveModelRtree;
import com.unimelb.cis.utils.ExpReturn;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.*;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.*;

import java.util.*;

public abstract class Model {

    int maxError = 0;
    int minError = 0;

    public static List<String> clas = Arrays.asList("NaiveBayes", "Logistic");
    public static List<String> regs = Arrays.asList("LinearRegression");

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

    String type = "LRM";

    public void setType(String type) {
        this.type = type;
    }

    String name;

    int pageSize;

    Mbr mbr;

    int index;

    List<Point> children;

    int classNum;

    Classifier classifier;

    public List<Point> getChildren() {
        return children;
    }

    public void setChildren(List<Point> children) {
        this.children = children;
        if (mbr == null) {
            mbr = new Mbr(children.get(0).getDim());
        }
        children.forEach(point -> updateMbr(point));
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
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(point);
        if (mbr == null) {
            mbr = new Mbr(point);
        } else {
            updateMbr(point);
        }
    }

    public Instances getInstances(String learnerName, List<Point> points, String name) {
        switch (name) {
            case "LRM":
                if (clas.contains(learnerName)) {
                    return prepareDataSetCla(points, classNum);
                } else {
                    return prepareDataSetReg(points);
                }
            case "MDM":
                return prepareDataSetRegMDM(points);
            default:
                return prepareDataSetReg(points);
        }

    }

    public Instances getInstances(String learnerName, List<Point> points) {
        return getInstances(learnerName, points, "LRM");
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
            attVals.addElement("" + i);
        }
        atts.addElement(new Attribute("index", attVals));
        Instances dataSet = new Instances("tree", atts, 0);
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            double[] vals = new double[dataSet.numAttributes()];
            for (int j = 0; j < vals.length - 1; j++) {
                vals[j] = point.getLocation()[j];
            }
            int index = attVals.indexOf("" + point.getIndex());
            vals[vals.length - 1] = index;
            dataSet.add(new Instance(1.0, vals));
        }
        dataSet.setClassIndex(dataSet.numAttributes() - 1);
        return dataSet;
    }

    /**
     * paper from mdm  Learned Index for Spatial Queries
     *
     * @param points
     * @return
     */
    public Instances prepareDataSetRegMDM(List<Point> points) {
        FastVector atts = new FastVector();
        atts.addElement(new Attribute("att" + 1));
        atts.addElement(new Attribute("index"));

        Instances dataSet = new Instances("tree", atts, 0);
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            double[] vals = new double[dataSet.numAttributes()];
            vals[0] = point.getCurveValue();
            vals[1] = point.getIndex();
            dataSet.add(new Instance(1.0, vals));
        }
        dataSet.setClassIndex(dataSet.numAttributes() - 1);
        return dataSet;
    }

    public Instances prepareDataSetReg(List<Point> points) {
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
            case "SMOreg":
                classifier = new SMOreg();
                break;
            case "MultilayerPerceptron":
                classifier = new MultilayerPerceptron();
//                try {
//                    // https://sefiks.com/2017/02/20/building-neural-networks-with-weka/
//                    //https://www.programcreek.com/java-api-examples/?aapi=weka.classifiers.functions.MultilayerPerceptron
//                    //https://blog.csdn.net/qiao1245/article/details/50924242
//                    //setHiddenLayers(“4,5”) 或者 “… -H 4,5”
//                    //代表两个隐含层，第一层4个神经元，第二层5个神经元。
//                    // https://searchcode.com/codesearch/view/21712641/ line 1627 shows the meaning of a
//                    StringBuilder builder = new StringBuilder("-L 0.3 -M 0.2 -N 500 -V 0 -S 0 -E 20 -H ");
//                    builder.append(classNum);
//                    classifier.setOptions(Utils.splitOptions(builder.toString()));
//                    ((MultilayerPerceptron) classifier).setLearningRate(0.01);
//                    ((MultilayerPerceptron) classifier).setNormalizeAttributes(true);
//                    ((MultilayerPerceptron) classifier).setNormalizeNumericClass(true);
//                    ((MultilayerPerceptron) classifier).setSeed(0);
//                    ((MultilayerPerceptron) classifier).setValidationSetSize(20);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                break;
//            case "NeuralNetwork":
//                classifier = new NeuralNetwork();
//                break;
            case "LibSVM":
                LibSVM svm = new LibSVM();
                svm.setKernelType(new SelectedTag(0, LibSVM.TAGS_KERNELTYPE));
                svm.setSVMType(new SelectedTag(0, LibSVM.TAGS_SVMTYPE));
                svm.setProbabilityEstimates(true);
                // svm.buildClassifier(filterdInstances);

                FilteredClassifier filteredClassifier = new FilteredClassifier();
//                filteredClassifier.setFilter(stwvFilter);
                filteredClassifier.setClassifier(svm);
//                filteredClassifier.buildClassifier(this.trainingData);
                classifier = filteredClassifier;
                break;
            case "NaiveBayes":
                classifier = new NaiveBayes();
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

    public void train(Classifier classifier, Instances instances) {
        try {
            classifier.buildClassifier(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void evaluate() {
        Instances instances = getInstances(name, getChildren());
        List<Double> results = getPredVals(classifier, instances).predictResults;
        for (int i = 0; i < results.size(); i++) {
            int result = results.get(i).intValue();
            int real = (int) instances.instance(i).classValue();
            if (result != real) {
                if (real < result) {
                    if (maxError < (result - real)) {
                        maxError = result - real;
                    }
                } else {
                    if (minError < (real - result)) {
                        minError = real - result;
                    }
                }
            }
        }
    }

    public List<Double> getPredVals0(Classifier classifier, Instances instances) {
        List<Double> results = new ArrayList<>();
        try {
            for (int i = 0; i < instances.numInstances(); i++) {
                Instance instance = instances.instance(i);
                if (classifier == null) {
                    results.add(0.0);
                } else {
                    double value = classifier.classifyInstance(instance);
                    results.add(value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    public ExpReturn getPredVals(Classifier classifier, Instances instances) {
        ExpReturn expReturn = new ExpReturn();
        List<Double> results = new ArrayList<>();
        try {
            for (int i = 0; i < instances.numInstances(); i++) {
                Instance instance = instances.instance(i);
                if (classifier == null) {
                    results.add(0.0);
                } else {
                    long begin = System.nanoTime();
                    double value = classifier.classifyInstance(instance);
                    long end = System.nanoTime();
                    expReturn.time += (end - begin);
                    results.add(value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        expReturn.predictResults = results;
        return expReturn;
    }

    public Instances prepareDataSet(List<Point> points) {
        int dim = points.get(0).getDim();
        FastVector atts = new FastVector();
        FastVector attVals = new FastVector();
        for (int i = 0; i < dim; i++) {
            atts.addElement(new Attribute("att" + (i + 1)));
        }
        Instances dataSet = new Instances("tree", atts, 0);
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            double[] vals = new double[dataSet.numAttributes()];
            for (int j = 0; j < vals.length; j++) {
                vals[j] = point.getLocation()[j];
            }
            dataSet.add(new Instance(1.0, vals));
        }
        return dataSet;
    }

    public abstract void build();

    public abstract ExpReturn pointQuery(Point point);

    public abstract ExpReturn pointQuery(List<Point> points);

    public abstract ExpReturn windowQuery(Mbr window);

    public abstract ExpReturn windowQueryByScanAll(Mbr window);

    public abstract ExpReturn insert(Point point);

    public abstract ExpReturn insert(List<Point> points);

    public void updateMbr(Point point) {
        if (mbr == null) {
            mbr = new Mbr(point.getDim());
        }
        mbr.updateMbr(point, point.getDim());
    }

}
