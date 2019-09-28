package test.com.unimelb.cis.structures.rstar; 

import org.junit.Test; 
import org.junit.Before; 
import org.junit.After; 

/** 
* RstarTree Tester. 
* 
* @author <Authors name> 
* @since <pre>Sep 28, 2019</pre> 
* @version 1.0 
*/ 
public class RstarTreeTest { 

@Before
public void before() throws Exception { 
} 

@After
public void after() throws Exception { 
} 

/** 
* 
* Method: buildRtree(String path) 
* 
*/ 
@Test
public void testBuildRtreePath() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: buildRtree(List<Point> points) 
* 
*/ 
@Test
public void testBuildRtreePoints() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: windowQuery(Mbr window) 
* 
*/ 
@Test
public void testWindowQuery() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: output(String file) 
* 
*/ 
@Test
public void testOutput() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: buildRtreeAfterTuning(String path, int dim, int level) 
* 
*/ 
@Test
public void testBuildRtreeAfterTuning() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: insert(Point point) 
* 
*/ 
@Test
public void testInsert() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: main(String[] args) 
* 
*/ 
@Test
public void testMain() throws Exception { 
//TODO: Test goes here... 
} 


/** 
* 
* Method: splitAndInsert(LeafNode insertTarget, Point point) 
* 
*/ 
@Test
public void testSplitAndInsert() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = RstarTree.getClass().getMethod("splitAndInsert", LeafNode.class, Point.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: overflowtreatment(LeafNode insertTarget, Point point) 
* 
*/ 
@Test
public void testOverflowtreatment() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = RstarTree.getClass().getMethod("overflowtreatment", LeafNode.class, Point.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: ChooseSubtreeOriginal(Node tempRoot, Point point) 
* 
*/ 
@Test
public void testChooseSubtreeOriginal() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = RstarTree.getClass().getMethod("ChooseSubtreeOriginal", Node.class, Point.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: chooseSubTree(Node tempRoot, Point point) 
* 
*/ 
@Test
public void testChooseSubTree() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = RstarTree.getClass().getMethod("chooseSubTree", Node.class, Point.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: chooseSubTreeRevisited(Node tempRoot, Point point) 
* 
*/ 
@Test
public void testChooseSubTreeRevisited() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = RstarTree.getClass().getMethod("chooseSubTreeRevisited", Node.class, Point.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: checkComp(int t, List<LeafNode> entries, List<LeafNode> CAND, String func, float[] deltaOvlp) 
* 
*/ 
@Test
public void testCheckComp() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = RstarTree.getClass().getMethod("checkComp", int.class, List<LeafNode>.class, List<LeafNode>.class, String.class, float[].class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

} 
