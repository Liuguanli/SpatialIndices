package com.unimelb.cis.structures.rstar;

import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.LeafNode;
import com.unimelb.cis.node.Node;
import com.unimelb.cis.node.NonLeafNode;
import com.unimelb.cis.node.Point;
import com.unimelb.cis.structures.RLRtree;
import com.unimelb.cis.utils.ExpReturn;

import java.util.*;

import static com.unimelb.cis.CSVFileReader.read;

public class RstarTree extends RLRtree {

    private int m;
    private int p;

    private boolean isRevisited;

    public RstarTree(int pagesize) {
        super(pagesize);
        // TODO from paper m=40%M  from paper p=30%M
        m = (int) (pagesize * 0.4);
        p = (int) (pagesize * 0.3);
        this.isRevisited = false;
    }

    public RstarTree(int pagesize, boolean isRevisited) {
        super(pagesize);
        // TODO from paper m=40%M  from paper p=30%M
        m = (int) (pagesize * 0.4);
        p = (int) (pagesize * 0.3);
        this.isRevisited = isRevisited;
    }

    @Override
    public ExpReturn buildRtree(String path) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        this.dataFile = path;
        List<String> lines = read(path);

        List<Point> points = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Point point = new Point(line);
            point.setCurveValue(i);
            points.add(point);
        }
        buildRtree(points);
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn buildRtree(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        int dimension = points.get(0).getDim();
        this.setDim(dimension);
        this.points = points;
        for (int i = 0; i < points.size(); i++) {
            rstarInsert(points.get(i));
        }
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn windowQuery(Mbr window) {
        ExpReturn expReturn = new ExpReturn();
        int pageAccessNum = 0;
        long begin = System.nanoTime();
        ArrayList<Node> list = new ArrayList();
        list.add(root);
        while (list.size() > 0) {
            Node top = list.remove(0);
            if (top instanceof NonLeafNode) {
                NonLeafNode nonLeaf = (NonLeafNode) top;
                if (nonLeaf.getMbr().interact(window)) {
                    List<Node> children = nonLeaf.getChildren();
                    list.addAll(list.size(), children);
                    pageAccessNum++;
                }
            } else if (top instanceof LeafNode) {
                LeafNode leaf = (LeafNode) top;
                if (leaf.getMbr().interact(window)) {
                    List<Point> children = leaf.getChildren();
                    for (int i = 0; i < children.size(); i++) {
                        if (window.contains(children.get(i))) {
                            expReturn.result.add(children.get(i));
                        }
                    }
                    pageAccessNum++;
                }
            }
        }
        long end = System.nanoTime();
        expReturn.pageaccess = pageAccessNum;
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn pointQuery(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        points.forEach(point -> {
            List<Node> nodes = new ArrayList<>();
            nodes.add(root);
            while (nodes.size() > 0) {
                Node top = nodes.remove(0);
                if (top instanceof NonLeafNode) {
                    if (top.getMbr().contains(point)) {
                        expReturn.pageaccess++;
                        nodes.addAll(((NonLeafNode) top).getChildren());
                    }
                } else if (top instanceof LeafNode) {
                    if (top.getMbr().contains(point)) {
                        expReturn.pageaccess++;
                        break;
                    }
                }

            }
        });
        long end = System.nanoTime();
        expReturn.time = end - begin;
        expReturn.time /= points.size();
        expReturn.pageaccess = expReturn.pageaccess / points.size();
        return expReturn;
    }

    @Override
    public ExpReturn windowQueryByScanAll(List<Mbr> windows) {
        return null;
    }

    @Override
    public ExpReturn pointQuery(Point point) {
        return pointQuery(Arrays.asList(point));
    }

    @Override
    public void output(String file) {

    }

    @Override
    public NonLeafNode buildRtreeAfterTuning(String path, int dim, int level) {
        return null;
    }

    /**
     * insertTargetSplit and insertTarget at the same level.
     *
     * @param insertTarget
     * @param point
     * @return
     */
    private boolean splitAndInsert(LeafNode insertTarget, Point point) {
        // TODO check whether two nodes are right
//        System.out.println(insertTarget.getMbr());
        Node insertTargetSplit = insertTarget.splitRStar(m, point, isRevisited);
//        System.out.println(insertTarget.getMbr() + " " + insertTargetSplit.getMbr());
        // no matter what, add the point first!!
        NonLeafNode parent = (NonLeafNode) insertTarget.getParent();
        if (parent == null) {
            // insertTarget is the root.
            NonLeafNode newRoot = new NonLeafNode(pagesize, root.getDim());
            newRoot.setLevel(insertTarget.getLevel() + 1);
            newRoot.add(insertTarget);
            newRoot.add(insertTargetSplit);
            root = newRoot;
        } else {
            while (parent.isFull()) {
                if (parent == root) {
                    NonLeafNode newRoot = new NonLeafNode(pagesize, root.getDim());
                    newRoot.setLevel(parent.getLevel() + 1);
                    newRoot.add(parent);
                    root = newRoot;
                    level++;
                }
                NonLeafNode parentSplit = parent.splitRStar(m, insertTargetSplit, isRevisited);
                parent = (NonLeafNode) parent.getParent();
                insertTargetSplit = parentSplit;
            }
            parent.add(insertTargetSplit);
        }
        return true;
    }

    public boolean rstarInsert(Point point) {
        LeafNode insertTarget = chooseSubTree(root, point);
        if (insertTarget.add(point)) {
            point.adjust();
            return true;
        } else {
            return overflowtreatment(insertTarget, point);
        }
    }

    @Override
    public ExpReturn insert(List<Point> points) {
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        points.forEach(point -> rstarInsert(point));
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    @Override
    public ExpReturn insert(Point point) {
        return insert(Arrays.asList(point));
    }

    private Set<Integer> overflowtreatmentSet = new HashSet<>();

    private boolean overflowtreatment(LeafNode insertTarget, Point point) {
        // level I use level, because when level changed, overflowtreatmentSet does not contain level.
        // I do not have to adjust the value of level.
        if (overflowtreatmentSet.contains(level)) {
            return splitAndInsert(insertTarget, point);
        } else {
            overflowtreatmentSet.add(level);
            point.adjust();
            List<Point> reInsertPoints = new ArrayList<>(insertTarget.reInsert(p, point));
            for (int i = 0; i < reInsertPoints.size(); i++) {
                rstarInsert(reInsertPoints.get(i));
            }
            return true;
        }
    }

    private LeafNode ChooseSubtreeOriginal(Node tempRoot, Point point) {
        if (tempRoot == null) {
            root = new LeafNode(pagesize, point.getDim());
            root.setOMbr(point);
            root.setParent(null);
            tempRoot = root;
        }
        if (tempRoot instanceof LeafNode) {
            return (LeafNode) tempRoot;
        } else if (tempRoot instanceof NonLeafNode) {
            List<Node> children = ((NonLeafNode) tempRoot).getChildren();
            if (children.get(0) instanceof LeafNode) {
                if (children.size() == 1) {
                    return (LeafNode) children.get(0);
                }
                List<LeafNode> COV = new ArrayList<>();
                for (int i = 0; i < children.size(); i++) {
                    LeafNode child = (LeafNode) children.get(i);
                    if (child.getMbr().contains(point)) {
                        COV.add(child);
                    }
                }
                if (COV.size() > 0) {
                    COV.sort((o1, o2) -> {
                        if (o1.getMbr().volume() > o2.getMbr().volume()) {
                            return 1;
                        } else if (o1.getMbr().volume() < o2.getMbr().volume()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    });
                    return COV.get(0);
                }
                children.sort((o1, o2) -> {
                    if (o1.getDeltaOvlp(point) > o2.getDeltaOvlp(point)) {
                        return 1;
                    } else if (o1.getDeltaOvlp(point) < o2.getDeltaOvlp(point)) {
                        return -1;
                    } else {
                        if (o1.getDeltaVol(point) > o2.getDeltaVol(point)) {
                            return 1;
                        } else if (o1.getDeltaVol(point) < o2.getDeltaVol(point)) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });
                return (LeafNode) children.get(0);
//                float minDeltaOvlp = Float.MAX_VALUE;
//                int minDeltaOvlpIndex = 0;
//                for (int i = 0; i < children.size(); i++) {
//                    float temp = 0;
//                    for (int j = 0; j < children.size(); j++) {
//                        if (i == j)
//                            continue;
//                        temp += children.get(i).getDeltaOvlp(children.get(j));
//                    }
//                    if (temp == 0) {
//                        return (LeafNode) children.get(i);
//                    }
//                    if (temp < minDeltaOvlp) {
//                        minDeltaOvlp = temp;
//                        minDeltaOvlpIndex = i;
//                    }
//                }
//                return (LeafNode) children.get(minDeltaOvlpIndex);
            } else {
                // the entries of N do not refer to leaves
                children.sort((o1, o2) -> {
                    if (o1.getDeltaVol(point) > o2.getDeltaVol(point)) {
                        return 1;
                    } else if (o1.getDeltaVol(point) < o2.getDeltaVol(point)) {
                        return -1;
                    } else {
                        return 0;
                    }
                });
                return ChooseSubtreeOriginal(children.get(0), point);
            }
        }
        return null;
    }

    private LeafNode chooseSubTree(Node tempRoot, Point point) {
        if (isRevisited) {
            return chooseSubTreeRevisited(tempRoot, point);
        } else {
            return ChooseSubtreeOriginal(tempRoot, point);
        }
    }

    private LeafNode chooseSubTreeRevisited(Node tempRoot, Point point) {
        if (tempRoot == null) {
            root = new LeafNode(pagesize, point.getDim());
            root.setOMbr(point);
            root.setParent(null);
            tempRoot = root;
        }
        if (tempRoot instanceof LeafNode) {
            return (LeafNode) tempRoot;
        } else if (tempRoot instanceof NonLeafNode) {
            List<Node> children = ((NonLeafNode) tempRoot).getChildren();
            if (children.get(0) instanceof LeafNode) {
                if (children.size() == 1) {
                    return (LeafNode) children.get(0);
                }
                List<LeafNode> COV = new ArrayList<>();
                for (int i = 0; i < children.size(); i++) {
                    LeafNode child = (LeafNode) children.get(i);
                    if (child.getMbr().contains(point)) {
                        COV.add(child);
                    }
                }
                if (COV.size() > 0) {
                    Iterator<LeafNode> iterator = COV.stream().filter(o -> o.getMbr().volume() == 0).sorted((o1, o2) -> {
                        if (o1.getMbr().perimeter() > o2.getMbr().perimeter()) {
                            return 1;
                        } else if (o1.getMbr().perimeter() < o2.getMbr().perimeter()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }).iterator();
                    if (iterator.hasNext()) {
                        return iterator.next();
                    } else {
                        COV.sort((o1, o2) -> {
                            if (o1.getMbr().volume() > o2.getMbr().volume()) {
                                return 1;
                            } else if (o1.getMbr().volume() < o2.getMbr().volume()) {
                                return -1;
                            } else {
                                return 0;
                            }
                        });
                        return COV.get(0);
                    }
                } else {
                    // sort entries in ascending order of their deltaPerim.
                    // TODO entries are inserted！！！
                    List<LeafNode> entries = new ArrayList(children);
                    entries.sort((o1, o2) -> {
                        float delta1 = o1.getDeltaPerim(point);
                        float delta2 = o2.getDeltaPerim(point);
                        if (delta1 > delta2) {
                            return 1;
                        } else if (delta1 < delta2) {
                            return -1;
                        } else {
                            return 0;
                        }
                    });
                    LeafNode first = entries.get(0);
                    if (first.getDeltaPerim(point, entries) == 0) {
                        return first;
                    }
                    List<LeafNode> CAND = new ArrayList<>();
                    CAND.add(first);
                    int p = 0;
                    for (int i = 1; i < entries.size(); i++) {
                        float deltaOvlp = first.getDeltaOvlp(entries.get(i));
                        if (deltaOvlp != 0) {
                            CAND.clear();
                            p = i;
                        }
                    }
                    if (CAND.size() == 1) {
                        return first;
                    } else {
                        // consider only the first p entries in the remaining steps
                        entries = entries.subList(0, p);
                        Iterator<LeafNode> iterator = entries.stream().filter(o -> o.getVolume(point) == 0).iterator();
                        float[] deltaOvlp = new float[p];
                        LeafNode result;
                        if (iterator.hasNext()) {
                            result = checkComp(0, entries, CAND, "perim", deltaOvlp);
                        } else {
                            result = checkComp(0, entries, CAND, "vol", deltaOvlp);
                        }
                        if (result != null) {
                            return result;
                        } else {
                            // return armin{Delta ovlp[i]i belongs to CAND}
                            float minDeltaOvlp = Float.MAX_VALUE;
                            int minDeltaOvlpI = 0;
                            for (int i = 0; i < deltaOvlp.length; i++) {
                                if (CAND.contains(entries.get(i))) {
                                    if (deltaOvlp[i] < minDeltaOvlp) {
                                        minDeltaOvlp = deltaOvlp[i];
                                        minDeltaOvlpI = i;
                                    }
                                }
                            }

//                            CAND.sort((o1, o2) -> {
//                                if (o1.getDeltaVol(point) > o2.getDeltaVol(point)) {
//                                    return 1;
//                                } else if (o1.getDeltaVol(point) < o2.getDeltaVol(point)) {
//                                    return -1;
//                                } else {
//                                    return 0;
//                                }
//                            });
                            return entries.get(minDeltaOvlpI);
                        }
                    }

                }

            } else if (children.get(0) instanceof NonLeafNode) {
                children.sort((o1, o2) -> {
                    if (o1.getDeltaVol(point) > o2.getDeltaVol(point)) {
                        return 1;
                    } else if (o1.getDeltaVol(point) < o2.getDeltaVol(point)) {
                        return -1;
                    } else {
                        // Resolve ties by choosing the entry with the rectangle
                        if (o1.getMbr().volume() > o2.getMbr().volume()) {
                            return 1;
                        } else if (o1.getMbr().volume() < o2.getMbr().volume()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });
                return chooseSubTree(children.get(0), point);
            }

        }
        return null;
    }

    private LeafNode checkComp(int t, List<LeafNode> entries, List<LeafNode> CAND, String func, float[] deltaOvlp) {
        CAND.add(entries.get(t));
        deltaOvlp[t] = 0;
        for (int j = 0; j < entries.size(); j++) {
            if (j == t) {
                continue;
            }
            float deltaOvlpJ = entries.get(t).getDeltaOvlp(entries.get(j), func);
            deltaOvlp[t] += deltaOvlpJ;
            if (deltaOvlpJ != 0 && !CAND.contains(entries.get(j))) {
                return checkComp(j, entries, CAND, func, deltaOvlp);
            }
        }
        if (deltaOvlp[t] == 0) {
            return entries.get(t);
        } else {
            return null;
        }
    }

    @Override
    public ExpReturn windowQuery(List<Mbr> windows) {
        ExpReturn expReturn = new ExpReturn();
        windows.forEach(mbr -> {
            ExpReturn temp = windowQuery(mbr);
            expReturn.time += temp.time;
            expReturn.pageaccess += temp.pageaccess;
        });
        expReturn.time /= windows.size();
        expReturn.pageaccess /= windows.size();
        return expReturn;
    }

    @Override
    public ExpReturn knnQuery(List<Point> points, int k) {
        ExpReturn expReturn = new ExpReturn();
        points.forEach(point -> {
            ExpReturn temp = knnQuery(point, k);
            expReturn.time += temp.time;
            expReturn.pageaccess += temp.pageaccess;
        });
        expReturn.time /= points.size();
        expReturn.pageaccess /= points.size();
        return expReturn;
    }

    @Override
    public ExpReturn knnQuery(Point point, int k) {
//        ExpReturn expReturn = new ExpReturn();
//        long begin = System.nanoTime();
//        PriorityQueue<Object> queue = getQueue(point, k);
//        ArrayList<Node> list = new ArrayList();
//        list.add(root);
//        while (list.size() > 0) {
//            Node top = list.remove(0);
//            if (top instanceof NonLeafNode) {
//                NonLeafNode nonLeaf = (NonLeafNode) top;
//                List<Node> children = nonLeaf.getChildren();
//                for (int i = 0; i < children.size(); i++) {
//                    Node former = children.get(i);
//                    boolean isProne = false;
//                    for (int j = 0; j < children.size(); j++) {
//                        if (i == j) {
//                            continue;
//                        }
//                        Node later = children.get(j);
//                        if (former.getMbr().calMINMAXDIST(point) > later.getMbr().calMINMAXDIST(point)) {
//                            isProne = true;
//                            break;
//                        }
//                    }
//                    if (!isProne) {
//                        list.add(former);
//                    }
//                }
//                expReturn.pageaccess++;
//            } else if (top instanceof LeafNode) {
//                LeafNode leaf = (LeafNode) top;
//                List<Point> children = leaf.getChildren();
//                queue.addAll(children);
//                expReturn.pageaccess++;
//            } else if (top instanceof Point){
//                expReturn.result.add((Point) top);
//                if (expReturn.result.size() == k) {
//                    break;
//                }
//            }
//        }
//        long end = System.nanoTime();
//        expReturn.time = end - begin;
//        return expReturn;
        float knnquerySide = (float) Math.sqrt((float) k / points.size());
        ExpReturn expReturn = new ExpReturn();
        long begin = System.nanoTime();
        while (true) {
            Mbr window = Mbr.getMbr(point, knnquerySide);
            ExpReturn tempExpReturn = windowQuery(window);
            List<Point> tempResult = tempExpReturn.result;
            if (tempResult.size() >= k) {
                tempResult.sort((o1, o2) -> {
                    double d1 = point.getDist(o1);
                    double d2 = point.getDist(o2);
                    if (d1 > d2) {
                        return 1;
                    } else if (d1 < d2) {
                        return -1;
                    } else {
                        return 0;
                    }
                });
                if (tempResult.get(k - 1).getDist(point) <= knnquerySide) {
                    expReturn.result = tempResult.subList(0, k);
                    expReturn.pageaccess += tempExpReturn.pageaccess;
                    break;
                }
            }
            knnquerySide = knnquerySide * 2;
        }
        long end = System.nanoTime();
        expReturn.time = end - begin;
        return expReturn;
    }

    public static void main(String[] args) {
        RstarTree rstarTree = new RstarTree(100, true);

        rstarTree.buildRtree("/Users/guanli/Documents/datasets/RLRtree/raw/uniform_10000_1_2_.csv");

        System.out.println("knn query:" + rstarTree.knnQuery(new Point(0.5f, 0.5f), 1));

//        System.out.println(rstarTree.root);
        rstarTree.insert(new Point(0.5f, 0.5f));
//        rstarTree.visualize(600, 600).save("rstar_revisit.png");
//        rstarTree.output("/Users/guanli/Documents/datasets/RLRtree/trees/Z_uniform_10000_1_3_.csv");
//
//        rstarTree.buildRtreeAfterTuning("/Users/guanli/Documents/datasets/RLRtree/trees/Z_uniform_10000_1_3_.csv", rstarTree.getDim(), rstarTree.getLevel());
//
//        System.out.println(rstarTree.windowQuery(Mbr.getMbrs(0.01f, 10, 3).get(0)));
//        System.out.println(rstarTree.windowQuery(Mbr.getMbrs(0.01f, 9, 3).get(0)));
//        System.out.println(rstarTree.windowQuery(Mbr.getMbrs(0.01f, 11, 3).get(0)));
    }

}
