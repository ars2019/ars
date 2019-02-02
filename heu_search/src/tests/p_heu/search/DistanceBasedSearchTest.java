package p_heu.search;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.vm.MultiProcessVM;
import gov.nasa.jpf.vm.SingleProcessVM;
import gov.nasa.jpf.vm.VM;
import org.junit.Before;
import org.junit.Test;
import p_heu.entity.Node;
import p_heu.entity.ReadWriteNode;
import p_heu.entity.sequence.Sequence;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class DistanceBasedSearchTest {
    private DistanceBasedSearch search;
    private Sequence seq;

    @Before
    public void init() throws Exception {
        String[] str = new String[]{
                "+classpath=../build/examples",
                "+search.class=p_heu.search.BFSearch",
                "CheckField"};
        Config config = new Config(str);
        JPF jpf = new JPF(config);
        VM vm = new SingleProcessVM(jpf, config);
        search = new PatternDistanceBasedSearch(config, vm);

        seq = new Sequence();
        List<Node> nodes = new ArrayList<>();
        nodes.add(new ReadWriteNode(1, "aa", "xx", "read", "t1", "12"));
        seq = seq.advance(1,null, nodes);
        nodes = new ArrayList<>();
        nodes.add(new ReadWriteNode(2, "aa", "xx", "read", "t2", "13"));
        nodes.add(new ReadWriteNode(3, "aa", "xx", "write", "t2", "13"));
        seq = seq.advance(2,null, nodes);
        search.addQueue(seq);
        seq = new Sequence();
        seq = new Sequence();
        nodes = new ArrayList<>();
        nodes.add(new ReadWriteNode(1, "aa", "xx", "read", "t1", "12"));
        seq = seq.advance(1,null, nodes);
        search.addQueue(seq);
        seq = new Sequence();
        seq = new Sequence();
        nodes = new ArrayList<>();
        nodes.add(new ReadWriteNode(1, "aa", "xx", "read", "t1", "12"));
        seq = seq.advance(1,null, nodes);
        search.addQueue(seq);
    }

    @Test
    public void addCorrectSeq() throws Exception {
    }

    @Test
    public void addQueue() throws Exception {
        Sequence seq = new Sequence();
        search.addQueue(seq);
        System.out.println(search.queue.size());
    }

    @Test
    public void sortQueue() throws Exception {
        for (Sequence seq : search.queue) {
            System.out.println(seq.getDistance());
        }
        search.sortQueue();
        System.out.println("sorted:");
        for (Sequence seq : search.queue) {
            System.out.println(seq.getDistance());
        }
        System.out.println("pick out:");
        Sequence s = search.queue.poll();
        System.out.println(s.getDistance());
        System.out.println("remain:");
        for (Sequence seq : search.queue) {
            System.out.println(seq.getDistance());
        }
    }

    @Test
    public void updateDistanceOfQueue() throws Exception {
    }

    @Test
    public void updateDistance() throws Exception {
        seq = new Sequence();
        System.out.println(seq.getDistance());
    }

}