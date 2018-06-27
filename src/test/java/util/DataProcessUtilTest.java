package util;

import classification.Classification;
import org.junit.Assert;
import org.junit.Test;
import resample.OverSubsample;
import weka.core.AttributeStats;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;

import java.io.*;
import java.util.*;

public class DataProcessUtilTest {

    public void covertAllDetailFileToSK_ESDFile() throws Exception {
        DataProcessUtil.covertAllDetailFileToSK_ESDFile("testCovert", "SK_ESD",
                Classification.DETAIL_NUM, PropertyUtil.METHOD_NAMES, Classification.EVALUATION_NAMES);
    }

    public void covertDetailFileToSK_ESDFile() throws Exception {
        DataProcessUtil.covertDetailFileToSK_ESDFile("OriginDetailFiles/j48_MyAnt_DETAIL", "J_SK_ESD",
                Classification.DETAIL_NUM, PropertyUtil.METHOD_NAMES, Classification.EVALUATION_NAMES);
    }

    @Test
    public void testDoubleArrayAdd() {
        double[] array = new double[2];
        array[0] = 0.3;
        array[1] = 1.3;
        Assert.assertEquals(++array[0], 1.3, 0.001);
        Assert.assertEquals(array[0], 1.3, 0.001);
    }

    @Test
    public void testResample() throws Exception {
        BufferedReader bReader = new BufferedReader(new FileReader(new File("TestFolder/MyVoldemort.arff")));
        Instances data = new Instances(bReader);
        bReader.close();
        data.setClassIndex(data.numAttributes() - 1);
        System.out.println("Total number of instances in Arff file : " + data.numInstances());
        AttributeStats as = data.attributeStats(data.numAttributes() - 1);
        int count[] = as.nominalCounts;
        int bugNum = count[1];
        int cleanNum = count[0];
        System.out.println("Number of buggy instances: " + count[1]);

        System.out.println("Test OverSubsample");
        OverSubsample oversample = new OverSubsample();
        oversample.setInputFormat(data);
        oversample.setDistributionSpread(1);
        Instances tmpData = Filter.useFilter(data, oversample);
        AttributeStats asTmp = tmpData.attributeStats(data.numAttributes() - 1);
        Assert.assertEquals(asTmp.nominalCounts[1], cleanNum);
        Assert.assertEquals(asTmp.nominalCounts[0], cleanNum);

        System.out.println("Test SMOTE");
        SMOTE smotesample = new SMOTE();
        smotesample.setInputFormat(data);
        double percent = ((double) 1 / 2 * count[0] - count[1]) / count[1] * 100;
        smotesample.setPercentage(percent);
        tmpData = Filter.useFilter(data, smotesample);
        asTmp = tmpData.attributeStats(data.numAttributes() - 1);
        Assert.assertEquals(asTmp.nominalCounts[1], cleanNum / 2);
        Assert.assertEquals(asTmp.nominalCounts[0], cleanNum);

        SpreadSubsample undersample = new SpreadSubsample();
        undersample.setInputFormat(data);
        undersample.setDistributionSpread(2);
        tmpData = Filter.useFilter(data, undersample);
        asTmp = tmpData.attributeStats(data.numAttributes() - 1);
        System.out.println(asTmp.nominalCounts[0] + "," + asTmp.nominalCounts[1]);
        Assert.assertEquals(asTmp.nominalCounts[1], bugNum);
        Assert.assertEquals(asTmp.nominalCounts[0], bugNum * 2);
    }

    @Test
    public void testChangedLineBetweenFileAndHunk() throws IOException {
        File hunkFile = new File("TestFolder/MyItextpdfHunkLOC");
        File changeFile = new File("TestFolder/MyItextpdfLOC");
        Map<List<Integer>, Integer> commitId_FileId_changedLine = new LinkedHashMap<>();
        BufferedReader bReader = new BufferedReader(new FileReader(hunkFile));
        String line;
        while ((line = bReader.readLine()) != null) {
            if (line.startsWith("commit")) {
                continue;
            }
            String[] content = line.trim().split(",");
            List<Integer> key = Arrays.asList(Integer.parseInt(content[0]), Integer.parseInt(content[1]));
            if (commitId_FileId_changedLine.containsKey(key)) {
                commitId_FileId_changedLine.put(key, commitId_FileId_changedLine.get(key) + Integer.parseInt(content[4]));
            } else {
                commitId_FileId_changedLine.put(key, Integer.parseInt(content[4]));
            }
        }
        System.out.println(commitId_FileId_changedLine.size());
        bReader.close();
        Set<List<Integer>> change_commitId_FileId_key = new HashSet<>();
        bReader = new BufferedReader(new FileReader(changeFile));
        while ((line = bReader.readLine()) != null) {
            if (line.startsWith("commit")) {
                continue;
            }
            String[] content = line.trim().split(",");
            List<Integer> key = Arrays.asList(Integer.parseInt(content[0]), Integer.parseInt(content[1]));
            change_commitId_FileId_key.add(key);
            if (commitId_FileId_changedLine.get(key)==null){
                System.out.println(key+" null");
                continue;
            }
            if (commitId_FileId_changedLine.get(key).intValue()!=Integer.parseInt(content[4])){
                System.out.println(key);
            }
            double hunk_changed_line = 0;
            for (List<Integer> commit_file : commitId_FileId_changedLine.keySet()) {
                hunk_changed_line+=commitId_FileId_changedLine.get(commit_file);
            }
        }
        for (List<Integer> key : commitId_FileId_changedLine.keySet()) {
            if (!change_commitId_FileId_key.contains(key)){
                System.out.println("Hunk contains key "+key+" but file level  don't contains.");
            }
        }
    }
}