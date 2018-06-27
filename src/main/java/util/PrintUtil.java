package util;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrintUtil {
    private static Logger logger = Logger.getLogger(PrintUtil.class);
    public static int CROSSVAILD_OUTPUT_DECIMAL = 4;

    public static void printListList(List<List<Double>> rankTable) {
        for (int i = 0; i < rankTable.size(); i++) {
            List<Double> list = rankTable.get(i);
            StringBuffer sBuffer = new StringBuffer();
            for (int j = 0; j < list.size() - 1; j++) {
                sBuffer.append(list.get(j) + " ");
            }
            sBuffer.append(list.get(list.size() - 1));
            System.out.println(sBuffer.toString());
        }
    }

    public static void printArray(double[] ratioes) {
        if (ratioes == null || ratioes.length == 0) {
            return;
        }
        for (int i = 0; i < ratioes.length - 1; i++) {
            System.out.print(ratioes[i] + " ");
        }
        System.out.print(ratioes[ratioes.length - 1]);
        System.out.println();
    }

    public static String arrayStringFormat(double[] ratioes, int dicimal) {
        if (ratioes == null || ratioes.length == 0) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        String bitNum = "%." + dicimal + "f";
        stringBuffer.append(String.format(bitNum, ratioes[0]));
        for (int i = 1; i < ratioes.length; i++) {
            stringBuffer.append(",").append(String.format(bitNum, ratioes[i]));
        }
        return stringBuffer.toString();
    }

    //no change on origin array content.
    public static double[] formatDoubleArray(double[] doubleArray, int dicimal) {
        if (doubleArray == null || doubleArray.length == 0) {
            return doubleArray;
        }
        double[] res = new double[doubleArray.length];
        for (int i = 0; i < doubleArray.length; i++) {
            res[i] = formatDouble(dicimal, doubleArray[i]);
        }
        return res;
    }

    /**
     * Keep a few decimal places
     *
     * @param decimal The number of digits behind the decimal point.
     * @param d       The number which need formatted.
     * @return The number after formatting.
     */
    public static double formatDouble(int decimal, double d) {
        BigDecimal bigDecimal = new BigDecimal(d);
        return bigDecimal.setScale(decimal, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static void saveResult(String result, String file) throws IOException {
        FileWriter fw = new FileWriter(file, false);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(result + "\n");
        bw.flush();
        bw.close();
    }

    public static void appendResult(String result, String file) throws IOException {
        FileWriter fa = new FileWriter(file, true);
        BufferedWriter ba = new BufferedWriter(fa);
        ba.write(result + "\n");
        ba.flush();
        ba.close();
    }

    /**
     * Print a two-dimensional table to a file.
     *
     * @param project_method_value Map which store the data.
     * @param baseLearn            The name of base learner.
     * @param saveFolderPath       Folder which the result file will store.
     * @param typeString           Suffix information added in the file name.
     * @throws IOException
     */
    public static void printIntegerTable(Map<String, Map<String, Integer>> project_method_value, String baseLearn,
                                         String saveFolderPath, String typeString) throws IOException {
        StringBuffer write = new StringBuffer();
        for (String project : PropertyUtil.PROJECTS) {
            write.append("," + project);
        }
        write.append("," + PropertyUtil.AVG_NAME);
        write.append("\n");
        for (String[] methodNames : PropertyUtil.METHOD_NAMES) {
            for (String methodName : methodNames) {
                write.append(methodName + ",");
                for (String project : PropertyUtil.PROJECTS) {
                    write.append(project_method_value.get(project).get(methodName) + ",");
                }
                write.append(project_method_value.get(PropertyUtil.AVG_NAME).get(methodName));
                write.append("\n");
            }
        }
        saveResult(write.toString(), saveFolderPath + "/" + Character.toUpperCase(baseLearn.charAt(0)) + typeString);
    }

    public static void printDoubleTable(Map<String, Map<String, Double>> project_method_value, String baseLearn,
                                        String saveFolderPath, String typeString) throws IOException {
        StringBuffer write = new StringBuffer();
        List<String> colNames = new ArrayList<>();
        colNames.addAll(project_method_value.keySet());
        for (String colName : colNames) {
            write.append("," + colName);
        }
        write.append("\n");
        List<String> rowNames = new ArrayList<>();
        rowNames.addAll(project_method_value.get(colNames.get(0)).keySet());
        for (String rowName : rowNames) {
            write.append(rowName + ",");
            for (String colName : colNames) {
                write.append(project_method_value.get(colName).get(rowName) + ",");
            }
            write.append("\n");
        }
        saveResult(write.toString(), saveFolderPath + "/" + Character.toUpperCase(baseLearn.charAt(0)) + typeString);
    }

    public static void printSKOneMap(Map<String, List<Double>> map, String savePath, int decimal) throws IOException {
        List<Map.Entry<String, List<Double>>> list = new ArrayList<>();
        list.addAll(map.entrySet());
        StringBuffer sBuffer = new StringBuffer();
        boolean vaild = checkLengthEqual(list);
        if (!vaild) {
            logger.error("Error Length For Print SK_ONE!");
            return;
        }
        for (int i = 0; i < list.size() - 1; i++) {
            sBuffer.append(list.get(i).getKey() + ",");
        }
        sBuffer.append(list.get(list.size() - 1).getKey() + "\n");
        int len = list.get(0).getValue().size();
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < list.size() - 1; j++) {
                sBuffer.append(PrintUtil.formatDouble(decimal, list.get(j).getValue().get(i)) + ",");
            }
            sBuffer.append(PrintUtil.formatDouble(decimal, list.get(list.size() - 1).getValue().get(i)) + "\n");
        }
        saveResult(sBuffer.toString(), savePath);
    }

    private static boolean checkLengthEqual(List<Map.Entry<String, List<Double>>> list) {
        if (list == null || list.size() == 0) {
            logger.error("Empty Map For print SK_ONE!");
            return false;
        }
        if (list.size() == 1) {
            return true;
        }
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).getValue().size() != list.get(0).getValue().size()) {
                return false;
            }
        }
        return true;
    }
}
