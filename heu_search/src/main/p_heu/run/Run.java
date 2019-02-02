package p_heu.run;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import p_heu.entity.filter.Filter;
import p_heu.entity.sequence.Sequence;
import p_heu.listener.BasicPatternFindingListener;
import p_heu.listener.SequenceProduceListener;

import java.util.HashSet;
import java.util.Set;

public class Run {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("please enter the name of program to be tested");
            return;
        }
        String testFileName = args[0];
        String[] str = new String[]{
                "+classpath=out/production/heu_search",
                "+search.class=p_heu.search.PatternDistanceBasedSearch",
                testFileName};
        Config config = new Config(str);
        Filter filter = Filter.createFilePathFilter();
        int p = 1;

        Set<Sequence> correctSeqs;
         Sequence correctSeq = getCorrectSequence(testFileName);
        if(correctSeq.getResult() == true){

            correctSeqs = new HashSet<>();
            correctSeqs.add(correctSeq);
            BasicPatternFindingListener listener = new BasicPatternFindingListener(correctSeqs);
            listener.setPositionFilter(filter);
            JPF jpf = new JPF(config);
            jpf.addListener(listener);
            jpf.run();
            jpf = null;
            System.gc();
            p = correctSeqs.size() + 1;
        }else{
            System.gc();
        }
        System.out.println("bug found. P-measure = " + p);
    }

    public static Sequence getCorrectSequence(String testFileName){

        String[] str = new String[]{
                "+classpath=out/production/heu_search",
                "+search.class=p_heu.search.SingleExecutionSearch",
                testFileName};
        Config config = new Config(str);
        JPF jpf = new JPF(config);
        SequenceProduceListener listener = new SequenceProduceListener();
        Filter filter = Filter.createFilePathFilter();
        listener.setPositionFilter(filter);

        jpf.addListener(listener);
        jpf.run();
        jpf = null;
        System.gc();
        return listener.getSequence();
    }
}
