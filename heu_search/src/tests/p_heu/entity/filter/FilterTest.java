package p_heu.entity.filter;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import org.junit.Before;
import org.junit.Test;
import p_heu.entity.Node;
import p_heu.entity.ReadWriteNode;
import p_heu.entity.sequence.Sequence;
import p_heu.listener.SequenceProduceListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class FilterTest {
    private Sequence sequence;
    @Before
    public void init() throws Exception {
        String[] str = new String[]{
                "+classpath=../../out/production/heu_search",
                "+search.class=p_heu.search.SingleExecutionSearch",
                "CheckField"};
        Config config = new Config(str);
        JPF jpf = new JPF(config);
        SequenceProduceListener listener = new SequenceProduceListener();
        jpf.addListener(listener);
        jpf.run();
        sequence = listener.getSequence();
    }

    @Test
    public void filter() throws Exception {
        Filter filter = Filter.createFilePathFilter();
        System.out.println(filter.getRegex());
        for (Node node : sequence.getNodes()) {
            if (node instanceof ReadWriteNode) {
                ReadWriteNode rwNode = (ReadWriteNode)node;
                System.out.println(rwNode.getPosition());
                System.out.println(filter.filter(rwNode.getPosition()));
            }
        }
    }

}