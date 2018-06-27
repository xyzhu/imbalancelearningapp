package util;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class PrintUtilTest {
    @Test
    public void formatDouble() throws Exception {
        double format = PrintUtil.formatDouble(4, 0.0);
        System.out.println(format);
    }

    @Test
    public void arrayStringFormat() throws Exception {
        double[] test = {1.3, 2.345, 3.456, 4.3233, 7, 10.22345};
        Assert.assertTrue(PrintUtil.arrayStringFormat(test, 2).equals("1.30,2.35,3.46,4.32,7.00,10.22"));
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Test
    public void testFormatDouble() {
        double output = PrintUtil.formatDouble(3, 5.467231);
        Assert.assertTrue(output == 5.467);
        output = PrintUtil.formatDouble(2, 5.467);
        Assert.assertTrue(output == 5.47);
    }

    @Test
    public void formatDoubleArray() throws Exception {
        double[] test = {1.3, 2.345, 3.456, 4.3233, 7, 10.22345};
        double[] formatArray = PrintUtil.formatDoubleArray(test, 2);
        String s = "";
        for (int i = 0; i < formatArray.length; i++) {
            s += formatArray[i] + ",";
        }
        Assert.assertTrue(s.equals("1.3,2.35,3.46,4.32,7.0,10.22,"));
    }

    @Test
    public void testForOrder() {
        String[] test = PropertyUtil.PROJECTS;
        for (int i = 0; i < 100; i++) {
            StringBuffer sBuffer = new StringBuffer();
            StringBuffer cBuffer = new StringBuffer();
            for (String s : test) {
                sBuffer.append(s);
            }
            for (String s : test) {
                cBuffer.append(s);
            }
            Assert.assertTrue(sBuffer.toString().equals(cBuffer.toString()));
        }
    }

    @Test
    public void testSystemType(){
        System.out.println(System.getProperty("os.name"));
    }
}