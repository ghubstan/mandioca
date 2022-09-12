package mandioca.ioc;

import mandioca.ioc.example.MockCache;
import mandioca.ioc.example.MockNode;
import mandioca.ioc.module.AbstractSimpleModule;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

public class DiFrameworkSimpleFieldInjectionTest {

    private final MockCache singletonApplicationCache = new MockCache();


    @Test
    public void testInjectFieldValues() throws Exception {

        // config node a
        DependencyInjectionFramework diFramework = DependencyInjectionConfig.getFramework(new NodeAConfig());
        MockNode nodeA = (MockNode) diFramework.inject(MockNode.class);

        // config node b
        diFramework = DependencyInjectionConfig.getFramework(new NodeBConfig());
        MockNode nodeB = (MockNode) diFramework.inject(MockNode.class);

        // config node c
        diFramework = DependencyInjectionConfig.getFramework(new NodeCConfig());
        MockNode nodeC = (MockNode) diFramework.inject(MockNode.class);
        //
        // each node has it's own di config... unique nodeName, numRequests...
        //
        assertEquals("Node-A", nodeA.getNodeName());
        assertEquals(50_000, nodeA.getPort());
        assertEquals(100, nodeA.getNumRequests());

        assertEquals("Node-B", nodeB.getNodeName());
        assertEquals(50_001, nodeB.getPort());
        assertEquals(10_000, nodeB.getNumRequests());

        assertEquals("Node-C", nodeC.getNodeName());
        assertEquals(50_002, nodeC.getPort());
        assertEquals(1_000_000, nodeC.getNumRequests());

        // add entries to the singleton cache
        nodeA.getSingletonCache().addToCache("A", 1);
        nodeB.getSingletonCache().addToCache("B", 2);
        nodeC.getSingletonCache().addToCache("C", 3);

        assertEquals(3L, (long) nodeA.getSingletonCache().numCacheEntries());
        assertEquals(3L, (long) nodeB.getSingletonCache().numCacheEntries());
        assertEquals(3L, (long) nodeC.getSingletonCache().numCacheEntries());

        assertTrue(nodeA.getSingletonCache().containsKey("A"));
        assertTrue(nodeA.getSingletonCache().containsKey("B"));
        assertTrue(nodeA.getSingletonCache().containsKey("C"));

        /*
        //  see if all the system hash's are the same
        System.out.println("node a cache: " + nodeA.getSingletonCache());
        System.out.println("node b cache: " + nodeB.getSingletonCache());
        System.out.println("node c cache: " + nodeC.getSingletonCache());
         */
    }

    private class NodeAConfig extends AbstractSimpleModule {
        @Override
        public void configure() {
            createMapping("nodeName", "Node-A");
            createMapping("port", 50_000);
            createMapping("numRequests", 100);
            createSingletonMapping(mandioca.ioc.example.MockCache.class, singletonApplicationCache);
        }
    }

    private class NodeBConfig extends AbstractSimpleModule {
        @Override
        public void configure() {
            createMapping("nodeName", "Node-B");
            createMapping("port", 50_001);
            createMapping("numRequests", 10_000);
            createSingletonMapping(mandioca.ioc.example.MockCache.class, singletonApplicationCache);
        }
    }

    private class NodeCConfig extends AbstractSimpleModule {
        @Override
        public void configure() {
            createMapping("nodeName", "Node-C");
            createMapping("port", 50_002);
            createMapping("numRequests", 1_000_000);
            createSingletonMapping(mandioca.ioc.example.MockCache.class, singletonApplicationCache);
        }
    }
}
