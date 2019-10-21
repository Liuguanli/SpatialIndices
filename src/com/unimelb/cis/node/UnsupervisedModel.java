package com.unimelb.cis.node;

import com.unimelb.cis.Curve;
import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.utils.ExpReturn;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnsupervisedModel extends Model {

    int maxIteration = 10000;

    int threshold;

    LeafModel leafModel;

    Map<Integer, UnsupervisedModel> subModels;

    SimpleKMeans kmeans = null;

    String curve;

    public UnsupervisedModel(int index, int pageSize, String algorithm, String curve, int threshold, int maxIteration) {
        super(index, pageSize, algorithm);
        this.threshold = threshold;
        this.curve = curve;
        this.maxIteration = maxIteration;
    }

    public Map<Integer, UnsupervisedModel> getSubModels() {
        return subModels;
    }

    @Override
    public void build() {
        int K = children.size() / this.threshold;
        K = Math.min(pageSize, K);
        if (K <= 1) {
            leafModel = new LeafModel(-1, pageSize, name);
            children = Curve.getPointByCurve(children, curve, true);
            leafModel.setChildren(children);
            leafModel.build();
//            System.out.println("last level:" + children.size());
        } else {
            subModels = new HashMap<>();
            Instances data = prepareDataSet(children);
            kmeans = new SimpleKMeans();
            try {
                kmeans.setNumClusters(K);
                kmeans.setMaxIterations(maxIteration);
            } catch (Exception e) {
                e.printStackTrace();
            }
            kmeans.setPreserveInstancesOrder(true);
            try {
                kmeans.buildClusterer(data);
            } catch (Exception ex) {
                System.err.println("Unable to buld Clusterer: " + ex.getMessage());
                ex.printStackTrace();
            }

//            Instances centroids = kmeans.getClusterCentroids();
//            for (int i = 0; i < K; i++) {
//                System.out.print("Cluster " + i + " size: " + kmeans.getClusterSizes()[i]);
//                System.out.println(" Centroid: " + centroids.instance(i));
//            }

            children.forEach(point -> {
                try {
                    int index = kmeans.clusterInstance(new Instance(1.0, point.getLocationDouble()));
                    if (!subModels.containsKey(index)) {
                        UnsupervisedModel unsupervisedModel = new UnsupervisedModel(index, pageSize, name, curve, threshold, maxIteration);
                        subModels.put(index, unsupervisedModel);
                    }
                    subModels.get(index).add(point);
//                    System.out.println(index);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            subModels.forEach((integer, unsupervisedModel) -> unsupervisedModel.build());
        }
    }

    @Override
    public ExpReturn pointQuery(Point point) {
        ExpReturn result = null;
        if (leafModel == null) {
            try {
                int index = kmeans.clusterInstance(new Instance(1.0, point.getLocationDouble()));
                result = subModels.get(index).pointQuery(point);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            result = leafModel.pointQuery(point);
//            System.out.println(result);
        }
        return result;
    }

    @Override
    public ExpReturn pointQuery(List<Point> points) {
        ExpReturn result = new ExpReturn();
        points.forEach(point -> {
            ExpReturn temp = pointQuery(point);
            result.plus(temp);
        });
        return result;
    }

    @Override
    public ExpReturn windowQuery(Mbr window) {
        ExpReturn expReturn = new ExpReturn();
        if (leafModel == null) {
            subModels.forEach((integer, unsupervisedModel) -> {
                if (unsupervisedModel.getMbr().interact(window)) {
                    ExpReturn temp = unsupervisedModel.windowQuery(window);
                    expReturn.plus(temp);
//                    System.out.println("expReturn.pageaccess" + expReturn.pageaccess);
                }
            });
        } else {
            return leafModel.windowQuery(window);
        }
        return expReturn;
    }

    @Override
    public ExpReturn windowQueryByScanAll(Mbr window) {
        return null;
    }

    @Override
    public ExpReturn insert(Point point) {
        ExpReturn result;
        if (leafModel == null) {
            int index = 0;
            try {
                index = kmeans.clusterInstance(new Instance(1.0, point.getLocationDouble()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            result = subModels.get(index).insert(point);
        } else {
            result = leafModel.insert(point);
        }
        return result;
    }

    @Override
    public ExpReturn insert(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        points.forEach(point -> expReturn.plus(insert(point)));
        return expReturn;
    }
}
