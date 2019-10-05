package test.com.unimelb.cis.geometry;

import com.unimelb.cis.geometry.Mbr;
import com.unimelb.cis.node.Point;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * Mbr Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Sep 28, 2019</pre>
 */
public class MbrTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: getMbrFromPoint(Point point)
     */
    @Test
    public void testGetMbrFromPoint() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getLocation()
     */
    @Test
    public void testGetLocation() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: setLocation(float[] location)
     */
    @Test
    public void testSetLocation() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getX1()
     */
    @Test
    public void testGetX1() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getX2()
     */
    @Test
    public void testGetX2() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getY1()
     */
    @Test
    public void testGetY1() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getY2()
     */
    @Test
    public void testGetY2() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: hashCode()
     */
    @Test
    public void testHashCode() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: volume()
     */
    @Test
    public void testVolume() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: perimeter()
     */
    @Test
    public void testPerimeter() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getAllVertexs(Mbr mbr, int index, int dim, float[] locations, List<Point> points)
     */
    @Test
    public void testGetAllVertexs() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: interact(Mbr mbr)
     */
    @Test
    public void testInteractMbr() throws Exception {
        Mbr mbr1 = new Mbr(0.1F, 0.1F, 0.3F, 0.3F);
        Mbr mbr2 = new Mbr(0.2F, 0.2F, 0.4F, 0.4F);
        Mbr mbr3 = new Mbr(0.0F, 0.0F, 0.1F, 0.1F);
        Assert.assertEquals(mbr1.interact(mbr2), true);
        Assert.assertEquals(mbr1.interact(mbr3), true);
    }

    /**
     * Method: interact(Point point)
     */
    @Test
    public void testInteractPoint() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: contains(Point point)
     */
    @Test
    public void testContains() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: equals(Object o)
     */
    @Test
    public void testEquals() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: calMINMAXDIST(Point point)
     */
    @Test
    public void testCalMINMAXDIST() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: calMINDIST(Point point)
     */
    @Test
    public void testCalMINDIST() throws Exception {
//TODO: Test goes here...

        Point point1 = new Point(0.4f, 0.5f);
        Mbr mbr = new Mbr(0.3f, 0.3f, 0.4f, 0.4f);
//        System.out.println(mbr.calMINMAXDIST(point1));
        Assert.assertEquals(0.1f, mbr.calMINMAXDIST(point1), 0.00001f);

        Point point2 = new Point(0.2f, 0.3f);
//        System.out.println(mbr.calMINMAXDIST(point1));
        Assert.assertEquals(0.1f, mbr.calMINMAXDIST(point2), 0.00001f);

        Point point3 = new Point(0.35f, 0.35f);
//        System.out.println(mbr.calMINMAXDIST(point1));
        Assert.assertEquals(0.0f, mbr.calMINMAXDIST(point3), 0.00001f);

    }

    /**
     * Method: getOverlapVol(Mbr mbr)
     */
    @Test
    public void testGetOverlapVol() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getOverlapPerim(Mbr mbr)
     */
    @Test
    public void testGetOverlapPerim() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: calInteract(Mbr mbr)
     */
    @Test
    public void testCalInteract() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: genMbr(String s, String separator)
     */
    @Test
    public void testGenMbr() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: toString()
     */
    @Test
    public void testToString() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getMbr(float side, int dim)
     */
    @Test
    public void testGetMbr() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getMbr2D(float side)
     */
    @Test
    public void testGetMbr2D() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getMbr3D(float side)
     */
    @Test
    public void testGetMbr3D() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getMbrs(float side, int num, int dim)
     */
    @Test
    public void testGetMbrs() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: updateMbr(Point point, int dim)
     */
    @Test
    public void testUpdateMbr() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: clone()
     */
    @Test
    public void testClone() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getLambdaByAxis(int axis)
     */
    @Test
    public void testGetLambdaByAxis() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getPerimMax()
     */
    @Test
    public void testGetPerimMax() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getCenterByAxis(int axis)
     */
    @Test
    public void testGetCenterByAxis() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getProjectionByAxis(int axis)
     */
    @Test
    public void testGetProjectionByAxis() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getDistToCenter(Point point)
     */
    @Test
    public void testGetDistToCenter() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getPointLow()
     */
    @Test
    public void testGetPointLow() throws Exception {
//TODO: Test goes here...
    }

    /**
     * Method: getPointHigh()
     */
    @Test
    public void testGetPointHigh() throws Exception {
//TODO: Test goes here...
    }

    /**
     * Method: getAllPoints()
     */
    @Test
    public void testGetAllPoints() throws Exception {
//TODO: Test goes here...
    }

    /**
     * Method: getMbr(Point point, float side)
     */
    @Test
    public void testGetMbrForPointSide() throws Exception {
//TODO: Test goes here...
    }


} 
