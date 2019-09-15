package com.unimelb.cis.structures.rstar;

import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.LeafNode;
import com.unimelb.cis.node.Node;
import com.unimelb.cis.node.NonLeafNode;
import com.unimelb.cis.node.Point;
import com.unimelb.cis.structures.IRtree;
import com.unimelb.cis.utils.ExpReturn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.unimelb.cis.CSVFileReader.read;

public class RstarTree extends IRtree {


    public RstarTree() {
    }

    public RstarTree(int pagesize) {
        super(pagesize);
    }

    @Override
    public String getDataFile() {
        return null;
    }

    @Override
    public boolean buildRtree(String path) {
        this.dataFile = path;
        List<String> lines = read(path);

        List<Point> points = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Point point = new Point(line);
            points.add(point);
        }
        int dimension = points.get(0).getDimension();
        this.setDim(dimension);
        for (int i = 0; i < points.size(); i++) {
            System.out.println("insert index: " + i);
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


    private boolean splitAndInsert(LeafNode insertTarget, Point point) {
        // TODO check whether two nodes are right
        Node insertTargetSplit = insertTarget.splitBybisection();
        // TODO pick one leaf node to add.  I just put it into the target
        // no matter what, add the point first!!
        insertTarget.add(point);
        NonLeafNode parent = (NonLeafNode) insertTarget.getParent();

        while (parent.isFull()) {
            if (parent == root) {
                NonLeafNode newRoot = new NonLeafNode(pagesize, root.getDim());
                newRoot.add(parent);
                root = newRoot;
            }
            NonLeafNode parentSplit = parent.splitBybisection();
            if (parent.contains(insertTarget)) {
                parent.addAfterSplit(insertTarget, insertTargetSplit);
            } else {
                parentSplit.addAfterSplit(insertTarget, insertTargetSplit);
            }
            parent = (NonLeafNode) parent.getParent();
            insertTargetSplit = parentSplit;
        }
        parent.addAfterSplit(insertTarget, insertTargetSplit);
        // TODO Updata MBR

        return false;
    }

    public boolean insert(Point point) {
        LeafNode insertTarget = chooseSubTree(root, point);
        if (insertTarget.add(point)) {
            return true;
        } else {
            return splitAndInsert(insertTarget, point);
        }
    }

    private LeafNode chooseSubTree(Node tempRoot, Point point) {
        if (tempRoot == null) {
            root = new NonLeafNode(pagesize, point.getDimension());
            LeafNode temp = new LeafNode(pagesize, point.getDimension());
            temp.setParent(root);
            root.add(temp);
            tempRoot = temp;
        }
        if (tempRoot instanceof LeafNode) {
            return (LeafNode) tempRoot;
        } else {
            //
            List<Node> children = ((NonLeafNode) tempRoot).getChildren();

            if (children.get(0) instanceof LeafNode) {
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
                            if (o1.getMbr().perimeter() > o2.getMbr().perimeter()) {
                                return 1;
                            } else if (o1.getMbr().perimeter() < o2.getMbr().perimeter()) {
                                return -1;
                            } else {
                                return 0;
                            }
                        }
                    });
                    return COV.get(0);
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
                    List<LeafNode> CAND = new ArrayList<>();
                    CAND.add(first);
                    float maxDeltaOvlp = Float.MIN_VALUE;
                    int p = 0;
                    for (int i = 1; i < entries.size(); i++) {
                        float deltaOvlp = first.getDeltaOvlp(entries.get(i));
                        if (deltaOvlp != 0) {
                            CAND.clear();
                        }
                        if (maxDeltaOvlp < deltaOvlp) {
                            maxDeltaOvlp = deltaOvlp;
                            p = i;
                        }
                    }
                    if (CAND.size() == 1) {
                        return first;
                    } else {
                        // consider only the first p entries in the remaining steps
                        entries = entries.subList(0, p);
                        LeafNode result = checkComp(0, entries, CAND, "vol");
                        if (result != null) {
                            return result;
                        } else {
                            // return armin{Delta ovlp[i]i belongs to CAND}
                            CAND.sort((o1, o2) -> {
                                if (o1.getDeltaOvlpVol(point) > o2.getDeltaOvlpVol(point)) {
                                    return 1;
                                } else if(o1.getDeltaOvlpVol(point) < o2.getDeltaOvlpVol(point)) {
                                    return -1;
                                } else {
                                    return 0;
                                }
                            });
                            return CAND.get(0);
                        }

                    }

                }

            } else {
                children.sort((o1, o2) -> {
                    if (o1.getDeltaOvlpVol(point) > o2.getDeltaOvlpVol(point)) {
                        return 1;
                    } else if(o1.getDeltaOvlpVol(point) < o2.getDeltaOvlpVol(point)) {
                        return -1;
                    } else {
                        return 0;
                    }
                });
                return chooseSubTree(children.get(0), point);
            }

        }
    }

    private LeafNode checkComp(int t, List<LeafNode> entries, List<LeafNode> CAND, String func) {
        LeafNode temp = entries.get(t);
        CAND.add(temp);

        float deltaOvlp = 0;
        for (int j = 0; j < entries.size(); j++) {
            if (j == t) {
                continue;
            }
            deltaOvlp += temp.getDeltaOvlp(entries.get(j), func);
            if (deltaOvlp != 0 && !CAND.contains(entries.get(j))) {
                return checkComp(j, entries, CAND, func);
            }
        }
        if (deltaOvlp == 0) {
            return temp;
        } else {
            LeafNode minOvlp = CAND.get(0);
            for (int i = 1; i < CAND.size(); i++) {
                if (minOvlp.getDeltaOvlp(temp, func) > CAND.get(i).getDeltaOvlp(temp, func)) {
                    minOvlp = CAND.get(i);
                }
            }
            return minOvlp;
        }

    }


    public static void main(String[] args) {
        RstarTree rstarTree = new RstarTree(100);

        rstarTree.buildRtree("/Users/guanli/Documents/datasets/RLRtree/raw/uniform_10000_1_3_.csv");

        System.out.println(rstarTree.root);

//        rstarTree.output("/Users/guanli/Documents/datasets/RLRtree/trees/Z_uniform_10000_1_3_.csv");
//
//        rstarTree.buildRtreeAfterTuning("/Users/guanli/Documents/datasets/RLRtree/trees/Z_uniform_10000_1_3_.csv", rstarTree.getDim(), rstarTree.getLevel());
//
//        System.out.println(rstarTree.windowQuery(Mbr.getMbrs(0.01f, 10, 3).get(0)));
//        System.out.println(rstarTree.windowQuery(Mbr.getMbrs(0.01f, 9, 3).get(0)));
//        System.out.println(rstarTree.windowQuery(Mbr.getMbrs(0.01f, 11, 3).get(0)));

    }

}
