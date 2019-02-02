package p_heu.entity;

import org.junit.Test;

import static org.junit.Assert.*;

public class PatternTypeTest {
    @Test
    public void test() throws Exception {
        PatternType patternType = new PatternType("W1(x), R2(x), W1(x)");
        for (Node node : patternType.getNodes()) {
            System.out.println(node);
        }
    }

}