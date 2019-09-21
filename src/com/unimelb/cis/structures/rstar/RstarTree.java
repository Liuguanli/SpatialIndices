package com.unimelb.cis.structures.rstar;

import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.LeafNode;
import com.unimelb.cis.node.Node;
import com.unimelb.cis.node.NonLeafNode;
import com.unimelb.cis.node.Point;
import com.unimelb.cis.structures.IRtree;
import com.unimelb.cis.utils.ExpReturn;

import java.util.*;

import static com.unimelb.cis.CSVFileReader.read;

public class RstarTree extends IRtree {

    private int m;
    private int p;

    public RstarTree(int pagesize) {
        super(pagesize);
        // TODO from paper m=40%M  from paper p=30%M
        m = (int) (pagesize * 0.4);
        p = (int) (pagesize * 0.3);
    }

    @Override
    public boolean buildRtree(String path) {
        this.dataFile = path;
        List<String> lines = read(path);

        List<Point> points = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Point point = new Point(line);
            point.setzCurveValue(i);
            points.add(point);
        }
        int dimension = points.get(0).getDim();
        this.setDim(dimension);
        for (int i = 0; i < points.size(); i++) {
            insert(points.get(i));
        }

        return true;
    }

    @Override
    public ExpReturn windowQuery(Mbr window) {
        return null;
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
        System.out.println("splitAndInsert");
        System.out.println(insertTarget.getMbr());
        Node insertTargetSplit = insertTarget.splitRStar(m, point);
        System.out.println(insertTarget.getMbr() + " " + insertTargetSplit.getMbr());
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
                NonLeafNode parentSplit = parent.splitRStar(m, insertTargetSplit);
                parent = (NonLeafNode) parent.getParent();
                insertTargetSplit = parentSplit;
            }
            parent.add(insertTargetSplit);
        }
        return true;
    }

    public boolean insert(Point point) {
        LeafNode insertTarget = chooseSubTree(root, point);
        if (insertTarget.add(point)) {
            point.adjust();
            return true;
        } else {
            return overflowtreatment(insertTarget, point);
        }
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
                insert(reInsertPoints.get(i));
            }
            return true;
        }
    }

    private LeafNode chooseSubTree(Node tempRoot, Point point) {
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
                        float delta1 = o1.getDeltaOvlpPerim(point);
                        float delta2 = o2.getDeltaOvlpPerim(point);
                        if (delta1 > delta2) {
                            return 1;
                        } else if (delta1 < delta2) {
                            return -1;
                        } else {
                            return 0;
                        }
                    });
                    LeafNode first = entries.get(0);
                    if (first.getDeltaOvlpPerim(point, entries) == 0) {
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
//                                if (o1.getDeltaOvlpVol(point) > o2.getDeltaOvlpVol(point)) {
//                                    return 1;
//                                } else if (o1.getDeltaOvlpVol(point) < o2.getDeltaOvlpVol(point)) {
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
                    if (o1.getDeltaOvlpVol(point) > o2.getDeltaOvlpVol(point)) {
                        return 1;
                    } else if (o1.getDeltaOvlpVol(point) < o2.getDeltaOvlpVol(point)) {
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


    public static void main(String[] args) {
        RstarTree rstarTree = new RstarTree(100);

        rstarTree.buildRtree("/Users/guanli/Documents/datasets/RLRtree/raw/uniform_160000_1_2_.csv");

        System.out.println(rstarTree.root);

        rstarTree.visualize(1600, 1600).save("rstar.png");

//        rstarTree.output("/Users/guanli/Documents/datasets/RLRtree/trees/Z_uniform_10000_1_3_.csv");
//
//        rstarTree.buildRtreeAfterTuning("/Users/guanli/Documents/datasets/RLRtree/trees/Z_uniform_10000_1_3_.csv", rstarTree.getDim(), rstarTree.getLevel());
//
//        System.out.println(rstarTree.windowQuery(Mbr.getMbrs(0.01f, 10, 3).get(0)));
//        System.out.println(rstarTree.windowQuery(Mbr.getMbrs(0.01f, 9, 3).get(0)));
//        System.out.println(rstarTree.windowQuery(Mbr.getMbrs(0.01f, 11, 3).get(0)));

    }

}
