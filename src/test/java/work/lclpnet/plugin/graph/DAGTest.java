package work.lclpnet.plugin.graph;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DAGTest {

    private static boolean before(List<String> list, String a, String b) {
        return list.indexOf(a) < list.indexOf(b);
    }

    @Test
    void getTopologicalOrder_simple_valid() {
        var G = new TestGraph();
        var a = G.getOrCreate("a");
        var b = G.getOrCreate("b");

        a.addChild(b);

        var order = G.topologicalOrder();

        assertTrue(before(order, "a", "b"));
    }

    @Test
    void getTopologicalOrder_complex_valid() {
        var G = new TestGraph();
        var a = G.getOrCreate("a");
        var b = G.getOrCreate("b");
        var c = G.getOrCreate("c");
        var d = G.getOrCreate("d");
        var e = G.getOrCreate("e");
        var f = G.getOrCreate("f");

        a.addChild(c);
        b.addChild(d);
        b.addChild(f);
        f.addChild(c);
        d.addChild(e);

        var order = G.topologicalOrder();

        assertTrue(before(order, "a", "c"));
        assertTrue(before(order, "b", "d"));
        assertTrue(before(order, "b", "f"));
        assertTrue(before(order, "d", "e"));
        assertTrue(before(order, "f", "c"));
        assertFalse(before(order, "e", "b"));
        assertFalse(before(order, "c", "b"));
    }

    @Test
    void getTopologicalOrder_complexPartial_valid() {
        var G = new TestGraph();
        var a = G.getOrCreate("a");
        var b = G.getOrCreate("b");
        var c = G.getOrCreate("c");
        var d = G.getOrCreate("d");

        assertTrue(a.addChild(c));
        assertTrue(c.addChild(d));
        assertTrue(b.addChild(d));

        var order = G.topologicalOrder(Set.of(a));
        assertEquals(3, order.size());
        assertTrue(before(order, "a", "c"));
        assertTrue(before(order, "c", "d"));

        order = G.topologicalOrder(Set.of(b));
        assertEquals(2, order.size());
        assertTrue(before(order, "b", "d"));

        order = G.topologicalOrder();
        assertEquals(4, order.size());
        assertTrue(before(order, "a", "c"));
        assertTrue(before(order, "c", "d"));
        assertTrue(before(order, "b", "d"));
    }

    @Test
    void Node$addChild_cycle_false() {
        var a = new TestNode("a");
        var b = new TestNode("b");

        assertTrue(a.addChild(b));
        assertFalse(b.addChild(a));
    }

    @Test
    void Node$addChild_cycleBig_false() {
        var a = new TestNode("a");
        var b = new TestNode("b");
        var c = new TestNode("c");

        assertTrue(a.addChild(b));
        assertTrue(b.addChild(c));
        assertFalse(c.addChild(a));
    }

    private static class TestGraph extends DAG<String> {
        public Node<String> getOrCreate(String s) {
            return getOrCreateNode(s, s);
        }

        private List<String> topologicalOrder() {
            return getTopologicalOrder().stream()
                    .map(DAG.Node::getObj)
                    .toList();
        }

        private List<String> topologicalOrder(Set<Node<String>> rootNodes) {
            return getTopologicalOrder(rootNodes).stream()
                    .map(DAG.Node::getObj)
                    .toList();
        }
    }

    private static class TestNode extends DAG.Node<String> {

        public TestNode(String obj) {
            super(obj, obj);
        }
    }
}