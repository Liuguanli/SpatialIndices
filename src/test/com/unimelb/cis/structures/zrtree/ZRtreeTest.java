package test.com.unimelb.cis.structures.zrtree;

import com.unimelb.cis.structures.zrtree.ZRtree;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * ZRtree Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Jul 29, 2019</pre>
 */
public class ZRtreeTest {

    int PAGE_SIZE = 100;

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: buildRtree()
     */
    @Test
    public void testBuildRtree() throws Exception {
        ZRtree zRtree = new ZRtree();
        zRtree.buildRtree("datasets/normal_10000_.csv");
        Assert.assertNotNull(zRtree.getRoot());
        Assert.assertNotNull(zRtree.getLeafNodes());
        Assert.assertEquals(zRtree.getLeafNodes().size(), 10000 % PAGE_SIZE == 0 ? 10000 / PAGE_SIZE : 10000 / PAGE_SIZE + 1);

    }

    /**
     * Method: setDataSet(String path)
     */
    @Test
    public void testSetDataSet() throws Exception {
//TODO: Test goes here... 
    }


} 
