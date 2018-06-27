package main;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

import org.apache.log4j.Logger;
import util.*;
import weka.core.AttributeStats;
import weka.core.Instance;
import weka.core.Instances;
import classification.Classification;

public class Start {
    private static Logger logger = Logger.getLogger(Start.class);

    public static void main(String argv[]) throws Exception {
        getClassificationResult(PropertyUtil.LOC_FILE_PATH, PropertyUtil.ARFF_PATH, PropertyUtil.PROJECTS, PropertyUtil
                .BASE_LEARNERS, 100, true);
    }

    private static void getClassificationResult(String locFilePath, String arffPath, String[] projects,
                                                String[] baseLearners, int times, boolean calcuteCost) throws Exception {
        String predict_result = "";
        PropertyUtil.CALCULATION_COST = calcuteCost;
        PropertyUtil.CALCULATION_FILE_TO_HUNK_COST = false;
        logger.info("Arff Fold is :" + arffPath);
        logger.info("LOC Fold is :" + locFilePath);
        logger.info("Calculate cost = " + calcuteCost);
        logger.info("Resample ratio = " + PropertyUtil.SAMPLE_RATIO);
        logger.info("Calculate cost from file to hunk is :" + PropertyUtil.CALCULATION_FILE_TO_HUNK_COST);
        FileUtil.checkFolder();
        for (String base : baseLearners) {
            String output_file_name = PropertyUtil.RESULT_FOLDER_PATH + PropertyUtil.FILE_PATH_DELIMITER + base + "Result.csv";
            File outFile = new File(output_file_name);
            if (outFile.exists()) {
                outFile.delete();
            }
            outFile.createNewFile();
            String measure_name = "project, method, recall-1, precision-1, fMeasure-1, auc";
            PrintUtil.saveResult(measure_name, output_file_name);
            logger.info(base + " for detail");
            for (int i = 0; i < projects.length; i++) {
                String project = projects[i];
                PropertyUtil.CUR_DETAIL_FILENAME = PropertyUtil.DETAIL_FOLDER_PATH + PropertyUtil
                        .FILE_PATH_DELIMITER + PropertyUtil.FILE_PATH_DELIMITER + base + "_" + project + "_" + "DETAIL";
                PropertyUtil.CUR_COST_EFFECTIVE_RECORD = PropertyUtil.COST_FOLDER_PATH + PropertyUtil
                        .FILE_PATH_DELIMITER + base + "_" + project + "_" + "COST";
                PropertyUtil.CUR_COST_20PB_SK_ONE = PropertyUtil.COST_FOLDER_PATH + PropertyUtil
                        .FILE_PATH_DELIMITER + "COST20Pb_" + base + "_" + project + "_DETAIL.csv";
                PropertyUtil.CUR_DATABASE = projects[i];
                File cur_detail_file = new File(PropertyUtil.CUR_DETAIL_FILENAME);
                cur_detail_file.delete();
                cur_detail_file.createNewFile();
                File cur_cost_file = new File(PropertyUtil.CUR_COST_EFFECTIVE_RECORD);
                cur_cost_file.delete();
                cur_detail_file.createNewFile();
                File cur_cost20pb_file = new File(PropertyUtil.CUR_COST_20PB_SK_ONE);
                cur_cost20pb_file.delete();
                cur_cost20pb_file.createNewFile();
                logger.info(project);

                String inputfile = arffPath + "/" + project + ".arff";
                BufferedReader br = new BufferedReader(new FileReader(inputfile));
                Instances data = new Instances(br);
                br.close();
                data.setClassIndex(data.numAttributes() - 1);
                logger.info("Total number of instances in Arff file : " + data.numInstances());
                AttributeStats as = data.attributeStats(data.numAttributes() - 1);
                int count[] = as.nominalCounts;
                logger.info("Number of buggy instances: " + count[1]);
                Map<Instance, List<Integer>> ins_Loc = new LinkedHashMap<>();
                if (PropertyUtil.CALCULATION_COST) {
                    if (PropertyUtil.CALCULATION_FILE_TO_HUNK_COST) {
                        PropertyUtil.sqlL = new SQLConnection(PropertyUtil.CUR_DATABASE);
                        PropertyUtil.stmt = PropertyUtil.sqlL.getStmt();
                        PropertyUtil.COMMITID_FILEID_CHANGEDLINE_ISBUGS = new LinkedHashMap<>();
                        PropertyUtil.TOTAL_ACTUAL_HUNK_BUG_NUM = 0;
                        PropertyUtil.TOTAL_CHANGED_HUNK_LINE_NUM = 0;
                    }
                    if (!initialInsLoc(ins_Loc, data, locFilePath, project)) {
                        return;
                    }
                    DataStorageUtil.method_cost20pbs_skOne_basedOnProject = new LinkedHashMap<>();
                    for (int j = 0; j < PropertyUtil.METHOD_USE_MAP.length; j++) {
                        for (int k = 0; k < PropertyUtil.METHOD_USE_MAP[0].length; k++) {
                            if (PropertyUtil.METHOD_USE_MAP[j][k]) {
                                DataStorageUtil.method_cost20pbs_skOne_basedOnProject.put(PropertyUtil.METHOD_NAMES[j][k],
                                        new ArrayList<>());
                            }
                        }
                    }
                }

                Classification classification = new Classification(data);
                predict_result = classification.predict(base, project, times, ins_Loc);
                PrintUtil.appendResult(predict_result, output_file_name);
            }
        }
    }

    private static boolean initialInsLoc(Map<Instance, List<Integer>> ins_loc, Instances data, String locFilePath,
                                         String project) throws IOException, SQLException {
        List<List<Integer>> changedLineList = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(new File(locFilePath
                + "/" + project + "LOC")));
        String line;
        while ((line = br.readLine()) != null && (!line.equals(""))) {
            if (line.startsWith("commit_id")) {
                continue;
            }
            String[] array = line.split(",");
            List<Integer> tmp = new ArrayList<>();
            for (int j = 0; j < array.length; j++) {
                tmp.add(Integer.parseInt(array[j]));
            }
            if (PropertyUtil.CALCULATION_FILE_TO_HUNK_COST) {
                PropertyUtil.resultSet = PropertyUtil.stmt.executeQuery("select count(*) from " + PropertyUtil.HUNK_TABLE_NAME
                        + " where commit_id=" + tmp.get(0) + " and file_id=" + tmp.get(1) + " and bug_introducing = 1");
                while (PropertyUtil.resultSet.next()) {
                    PropertyUtil.TOTAL_ACTUAL_HUNK_BUG_NUM += PropertyUtil.resultSet.getInt(1);
                }
                PropertyUtil.resultSet = PropertyUtil.stmt.executeQuery("select la+ld,bug_introducing from " + PropertyUtil.HUNK_TABLE_NAME
                        + " where commit_id=" + tmp.get(0) + " and file_id=" + tmp.get(1) + " order by la+ld asc");
                List<Integer> commitId_fileId = new ArrayList<>();
                commitId_fileId.add(tmp.get(0));
                commitId_fileId.add(tmp.get(1));
                List<List<Integer>> changedLine_isBugs = new ArrayList<>();
                while (PropertyUtil.resultSet.next()) {
                    List<Integer> changedLine_isBug = new ArrayList<>();
                    changedLine_isBug.add(PropertyUtil.resultSet.getInt(1));
                    changedLine_isBug.add(PropertyUtil.resultSet.getInt(2));
                    changedLine_isBugs.add(changedLine_isBug);
                    PropertyUtil.TOTAL_CHANGED_HUNK_LINE_NUM += PropertyUtil.resultSet.getInt(1);
                }
                PropertyUtil.COMMITID_FILEID_CHANGEDLINE_ISBUGS.put(commitId_fileId, changedLine_isBugs);
            }
            changedLineList.add(tmp);
        }
        br.close();
        if (PropertyUtil.CALCULATION_FILE_TO_HUNK_COST) {
            logger.info("total_actual_bug_num =" + PropertyUtil.TOTAL_ACTUAL_HUNK_BUG_NUM);
            logger.info("total_changedLine_num =" + PropertyUtil.TOTAL_CHANGED_HUNK_LINE_NUM);
        }
        if (changedLineList.size() != data.numInstances()) {
            logger.error("Error! The number in LOC File is different with the number in Arff File!");
            return false;
        }
        for (int j = 0; j < data.numInstances(); j++) {
            ins_loc.put(data.instance(j), changedLineList.get(j));
        }
        return true;
    }
}
