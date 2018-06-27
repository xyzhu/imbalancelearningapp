package util;

import classification.Classification;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class DataProcessUtil {
    private static Logger logger = Logger.getLogger(DataProcessUtil.class);
    public static String SK_RESULT_FOLDER = "SK_RESULT";
    public static String FILENAME_DELIMITER = "_";
    public static String PAPER_TABLE_SAVE_PATH = "PaperTables";
    public static String AVERAGE_NAME = "Avg";

    public static void getProjectRankOfMethod(String resultFolder) throws IOException {
        for (String baseLearner : PropertyUtil.BASE_LEARNERS) {
            Map<String, Map<String, Map<String, Double>>> method_evaluation_project_rank = getPaperTable(baseLearner);
            writePaperTableAccordBase(method_evaluation_project_rank, baseLearner);
        }
    }

    public static void getForTwoRankCsv(String resultFolder) throws IOException {
        for (String baseLearner : PropertyUtil.BASE_LEARNERS) {
            Map<String, Map<String, Map<String, Double>>> method_evaluation_project_rank = getPaperTable(baseLearner);
            writeTwoRankAccordEvaluation(method_evaluation_project_rank, baseLearner);
        }
    }

    private static void writeTwoRankAccordEvaluation(Map<String, Map<String, Map<String, Double>>>
                                                             method_evaluation_project_rank, String base) throws IOException {
        for (String evaluationName : Classification.EVALUATION_NAMES) {
            String saveFileName = PropertyUtil.FOR_TWO_RANK + PropertyUtil.FILE_PATH_DELIMITER + base + "_" +
                    evaluationName + "_FirstRank.csv";
            File saveFile = new File(saveFileName);
            if (saveFile.exists()) {
                saveFile.delete();
            }
            StringBuffer line = new StringBuffer();
            for (String[] methodNames : PropertyUtil.METHOD_NAMES) {
                for (String methodName : methodNames) {
                    line.append(methodName + ",");
                }
            }
            PrintUtil.appendResult(line.toString(), saveFileName);
            for (String project : PropertyUtil.PROJECTS) {
                line = new StringBuffer();
                for (String[] methodNames : PropertyUtil.METHOD_NAMES) {
                    for (String methodName : methodNames) {
                        line.append(method_evaluation_project_rank.get(methodName).get(evaluationName).get(project) + ",");
                    }
                }
                PrintUtil.appendResult(line.toString(), saveFileName);
            }
        }
    }

    private static void writePaperTableAccordBase(Map<String, Map<String, Map<String, Double>>>
                                                          method_evaluation_project_rank, String baseLearner) throws IOException {
        String savePath = PAPER_TABLE_SAVE_PATH + "/" + baseLearner + "_paperRank.csv";
        File saveFile = new File(savePath);
        StringBuffer line = new StringBuffer();
        line.append(",,");
        for (String project : PropertyUtil.PROJECTS) {
            line.append(project + ",");
        }
        line.append(AVERAGE_NAME);
        PrintUtil.appendResult(line.toString(), savePath);
        for (String[] methodNames : PropertyUtil.METHOD_NAMES) {
            for (String methodName : methodNames) {
                for (int i = 0; i < Classification.EVALUATION_NAMES.size(); i++) {
                    line = new StringBuffer();
                    if (i == 0) {
                        line.append(methodName);
                    }
                    line.append("," + Classification.EVALUATION_NAMES.get(i));
                    for (String project : PropertyUtil.PROJECTS) {
                        line.append("," + method_evaluation_project_rank.get(methodName).get(Classification.EVALUATION_NAMES
                                .get(i)).get(project));
                    }
                    line.append("," + method_evaluation_project_rank.get(methodName).get(Classification.EVALUATION_NAMES
                            .get(i)).get(AVERAGE_NAME));
                    PrintUtil.appendResult(line.toString(), savePath);
                }
            }
        }
    }

    private static Map<String, Map<String, Map<String, Double>>> getPaperTable(String baseLearner) throws IOException {
        Map<String, Map<String, Map<String, Double>>> method_evaluation_project_rank = initialPaperTableMap();
        for (String evaluation : Classification.EVALUATION_NAMES) {
            String fileName = SK_RESULT_FOLDER + "/" + Character.toUpperCase(baseLearner.charAt(0)) + FILENAME_DELIMITER +
                    evaluation + FILENAME_DELIMITER + "Rank";
            BufferedReader bReader = new BufferedReader(new FileReader(new File(fileName)));
            String line;
            while ((line = bReader.readLine()) != null) {
                String projectName = "";
                String methodName = "";
                String evaluationName = evaluation;
                Double rankValue = 0.0;
                for (String project : PropertyUtil.PROJECTS) {
                    if (!line.equals(project)) {
                        continue;
                    }
                    projectName = project;
                    line = bReader.readLine();
                    for (int i = 0; i < PropertyUtil.METHOD_NAMES.length; i++) {
                        line = bReader.readLine();
                        for (int j = 0; j < PropertyUtil.METHOD_NAMES.length; j++) {
                            for (int k = 0; k < PropertyUtil.METHOD_NAMES[0].length; k++) {
                                if (!line.split(",")[0].replace("\"", "").equals(PropertyUtil.METHOD_NAMES[j][k])) {
                                    continue;
                                }
                                methodName = PropertyUtil.METHOD_NAMES[j][k];
                                rankValue = Double.parseDouble(line.split(",")[1]);
                                method_evaluation_project_rank.get(methodName).get(evaluation).put(projectName, rankValue);
                                break;
                            }

                        }
                    }
                    break;
                }
            }

        }
        AddAverageRankAccordProject(method_evaluation_project_rank);
        return method_evaluation_project_rank;
    }

    private static void AddAverageRankAccordProject(Map<String, Map<String, Map<String, Double>>>
                                                            method_evaluation_project_rank) {
        for (String[] methodNames : PropertyUtil.METHOD_NAMES) {
            for (String methodName : methodNames) {
                for (String evaluationName : Classification.EVALUATION_NAMES) {
                    double avgRank = 0.0;
                    int projectNum = 0;
                    Map<String, Double> project_rank = method_evaluation_project_rank.get(methodName).get(evaluationName);
                    for (String projectName : project_rank.keySet()) {
                        avgRank += project_rank.get(projectName);
                        projectNum++;
                    }
                    avgRank /= projectNum;
                    method_evaluation_project_rank.get(methodName).get(evaluationName).put(AVERAGE_NAME, avgRank);
                }
            }
        }
    }

    private static Map<String, Map<String, Map<String, Double>>> initialPaperTableMap() {
        Map<String, Map<String, Map<String, Double>>> method_evaluation_project_rank = new LinkedHashMap<>();
        for (String[] methodNames : PropertyUtil.METHOD_NAMES) {
            for (String methodName : methodNames) {
                Map<String, Map<String, Double>> evaluation_project_rank = new LinkedHashMap<>();
                for (String evaluationName : Classification.EVALUATION_NAMES) {
                    Map<String, Double> project_rank = new LinkedHashMap<>();
                    evaluation_project_rank.put(evaluationName, project_rank);
                }
                method_evaluation_project_rank.put(methodName, evaluation_project_rank);
            }
        }
        return method_evaluation_project_rank;
    }

    public static void covertDetailFileToSK_ESDFile(String detaileFilePath, String SK_ESDFoldPath, int detailNum,
                                                    String[] method_names, List<String> evaluation_names)
            throws Exception {
        Map<String, Map<String, String[]>> evaluation_method_values = new LinkedHashMap<>();
        for (int i = 0; i < evaluation_names.size(); i++) {
            Map<String, String[]> method_values = new LinkedHashMap<>();
            for (int j = 0; j < method_names.length; j++) {
                String[] values = new String[detailNum];
                method_values.put(method_names[j], values);
            }
            evaluation_method_values.put(evaluation_names.get(i), method_values);
        }
        readDetailFileToMap(evaluation_method_values, detaileFilePath, method_names, evaluation_names, detailNum);
        writeDetailMapToFile(evaluation_method_values, SK_ESDFoldPath, detailNum, detaileFilePath.substring
                (detaileFilePath.lastIndexOf("/") + 1));
    }

    private static void writeDetailMapToFile(Map<String, Map<String, String[]>> evaluation_method_values, String
            SK_ESDFoldPath, int detailNum, String originFileName) throws IOException {
        for (String evaluation : evaluation_method_values.keySet()) {
            File sk_detail_file = new File(SK_ESDFoldPath + "/" + evaluation + "_" + originFileName + ".csv");
            if (!sk_detail_file.exists()) {
                System.out.println(sk_detail_file);
                sk_detail_file.createNewFile();
            }
            Map<String, String[]> method_values = evaluation_method_values.get(evaluation);
            BufferedWriter bWrite = new BufferedWriter(new FileWriter(sk_detail_file));
            StringBuffer title = new StringBuffer();
            for (String method : method_values.keySet()) {
                title.append(method + ",");
            }
            bWrite.append(title.substring(0, title.length() - 1) + "\n");
            for (int i = 0; i < detailNum; i++) {
                StringBuffer line = new StringBuffer();
                for (String method : method_values.keySet()) {
                    line.append(method_values.get(method)[i] + ",");
                }
                bWrite.append(line.substring(0, line.length() - 1) + "\n");
            }
            bWrite.flush();
            bWrite.close();
        }
    }

    private static void readDetailFileToMap(Map<String, Map<String, String[]>> evaluation_method_values,
                                            String detaileFilePath, String[] method_names,
                                            List<String> evaluation_names, int detailNum) throws Exception {
        BufferedReader bReader = new BufferedReader(new FileReader(new File(detaileFilePath)));
        int curMethdoIndex = 0;
        String line;
        while ((line = bReader.readLine()) != null) {
            if (!line.equals(method_names[curMethdoIndex])) {
                logger.error("covertDetailFileToMap Error!");
                throw new Exception("covertDetailFileToMap Error!");
            }
            for (int i = 0; i < detailNum; i++) {
                line = bReader.readLine();
                String[] array = line.split(",");
                for (int j = 0; j < evaluation_names.size(); j++) {
                    evaluation_method_values.get(evaluation_names.get(j)).get(method_names[curMethdoIndex])[i] =
                            array[j];
                }
            }
            curMethdoIndex++;
        }
        bReader.close();
    }

    public static void covertAllDetailFileToSK_ESDFile(String detailFloderPath, String SK_ESDFoldPath, int detailNum,
                                                       String[] method_names, List<String> evaluation_names) throws
            Exception {
        File detaildFloder = new File(detailFloderPath);
        if (!detaildFloder.exists()) {
            return;
        }
        File[] detaildFiles = detaildFloder.listFiles();
        for (int i = 0; i < detaildFiles.length; i++) {
            File curFile = detaildFiles[i];
            File saveFloder = new File(SK_ESDFoldPath + "/" + Character.toUpperCase(curFile.getName().charAt(0)) + "_SK_ESD");
            if (!saveFloder.exists()) {
                saveFloder.mkdirs();
            }
            covertDetailFileToSK_ESDFile(curFile.getAbsolutePath(), saveFloder.getAbsolutePath(), detailNum,
                    method_names, evaluation_names);
        }
    }

    public static void getRunTimeCsvFile(String runTimeFile, String saveFolder, String baseLearn) throws IOException {
        Map<String, Map<String, Integer>> project_method_runTime = runTimeOnProjectOfMethod(runTimeFile);
        PrintUtil.printIntegerTable(project_method_runTime, baseLearn, saveFolder, "_Time.csv");
    }

    /**
     * @param runTimeFile Run time file when base classifer is determined.
     * @return project_method_runTime map.
     * @throws IOException
     */
    private static Map<String, Map<String, Integer>> runTimeOnProjectOfMethod(String runTimeFile) throws
            IOException {
        File file = new File(runTimeFile);
        String line;
        Map<String, Map<String, Integer>> res = new LinkedHashMap<>();
        BufferedReader bReader = new BufferedReader(new FileReader(file));
        String projectName = "";
        String methodName = "";
        while ((line = bReader.readLine()) != null) {
            if (projectName.equals("")) {
                for (String project : PropertyUtil.PROJECTS) {
                    if (line.endsWith(project)) {
                        projectName = project;
                        break;
                    }
                }
                continue;
            }
            if (methodName.equals("")) {
                for (String[] names : PropertyUtil.METHOD_NAMES) {
                    for (String name : names) {
                        if (line.substring(line.lastIndexOf(')') + 2).equals(name)) {
                            methodName = name;
                            break;
                        }
                    }
                }
                continue;
            }

            String endS = line.substring(line.lastIndexOf(')') + 2);
            if (endS.startsWith("Time")) {
                if (res.get(projectName) == null) {
                    res.put(projectName, new LinkedHashMap<>());
                }
                String runTime = endS.split(":")[1];
                runTime = runTime.substring(0, runTime.length() - 3);
                res.get(projectName).put(methodName, Integer.parseInt(runTime));
                if (methodName.equals("SmoteBoost")) {
                    projectName = "";
                }
                methodName = "";
            }
        }
        addAvgTime(res);
        return res;
    }

    private static void addAvgTime(Map<String, Map<String, Integer>> res) {
        res.put(PropertyUtil.AVG_NAME, new LinkedHashMap<>());
        for (String[] methodNames : PropertyUtil.METHOD_NAMES) {
            for (String methodName : methodNames) {
                long time = 0;
                for (String project : PropertyUtil.PROJECTS) {
                    time += res.get(project).get(methodName);
                }
                int avg = (int) (time / (long) PropertyUtil.PROJECTS.length);
                res.get(PropertyUtil.AVG_NAME).put(methodName, avg);
            }
        }
    }

    public static void getCost20Pb(String costFolderPath) throws IOException {
        for (String baseLearner : PropertyUtil.BASE_LEARNERS) {
            Map<String, Map<String, Double>> curRes = getCost20PbUnderSpecifyBase(costFolderPath, baseLearner);
            PrintUtil.printDoubleTable(curRes, baseLearner, costFolderPath, "_20PbCOST.csv");
        }
    }

    public static Map<String, Map<String, Double>> getCost20PbUnderSpecifyBase(String costFolderPath, String base)
            throws IOException {
        //base = judgeBaseName(base);
        if (base == null) {
            return null;
        }
        Map<String, Map<String, Double>> project_method_cost = new LinkedHashMap<>();
        for (String project : PropertyUtil.PROJECTS) {
            project_method_cost.put(project, new LinkedHashMap<>());
        }
        for (String project : PropertyUtil.PROJECTS) {
            BufferedReader bReader = new BufferedReader(new FileReader(costFolderPath + "/" + base + "_" + project + "_COST"));
            String line;
            while ((line = bReader.readLine()) != null) {
                if (line.equals("")) {
                    break;
                }
                String methodName = line;
                line = bReader.readLine();
                line = bReader.readLine();
                double cost20Pb = Double.parseDouble(line);
                project_method_cost.get(project).put(methodName, cost20Pb);
            }
        }
        AddAvgCost(project_method_cost);
        return project_method_cost;
    }

    private static void AddAvgCost(Map<String, Map<String, Double>> project_method_cost) {
        project_method_cost.put(PropertyUtil.AVG_NAME, new LinkedHashMap<>());
        Map<String, Double> avg_map = project_method_cost.get(PropertyUtil.AVG_NAME);
        for (String[] methodNames : PropertyUtil.METHOD_NAMES) {
            for (String methodName : methodNames) {
                double value = 0.0;
                for (String project : PropertyUtil.PROJECTS) {
                    value += project_method_cost.get(project).get(methodName);
                }
                value /= PropertyUtil.PROJECTS.length;
                avg_map.put(methodName, PrintUtil.formatDouble(2, value));
            }
        }
    }

    public static Map<String, Map<String, Double>> getPaperResultMapForBase(String resultCsvFilePath) throws IOException {
        if (resultCsvFilePath == null || resultCsvFilePath.length() == 0) {
            logger.error("Empty result file to get paper result file.");
            return null;
        }
        File resultFile = new File(resultCsvFilePath);
        Map<String, Map<String, Double>> evaluation_method_value = new LinkedHashMap<>();
        for (String evaluationName : Classification.EVALUATION_NAMES) {
            evaluation_method_value.put(evaluationName, new LinkedHashMap<>());
        }
        BufferedReader bReader = new BufferedReader(new FileReader(resultFile));
        String line;
        int projectNum = 0;

        while ((line = bReader.readLine()) != null) {
            if (line.equals("") || line.startsWith("project")) {
                continue;
            }
            String[] array = line.split(",");
            String methodNmae = array[1];
            if (methodNmae.equals(PropertyUtil.METHOD_NAMES[0])) {
                projectNum++;
            }
            for (int i = 0; i < Classification.EVALUATION_NAMES.size(); i++) {
                addValueOfMethodUnderEvaluation(evaluation_method_value, Classification.EVALUATION_NAMES.get(i),
                        methodNmae, Double.parseDouble(array[i + 2]));
            }
        }
        bReader.close();
        for (String evaluation : Classification.EVALUATION_NAMES) {
            for (String[] methods : PropertyUtil.METHOD_NAMES) {
                for (String method : methods) {
                    double originValue = evaluation_method_value.get(evaluation).get(method);
                    evaluation_method_value.get(evaluation).put(method, PrintUtil.formatDouble(2, originValue / projectNum));
                }
            }
        }
        return evaluation_method_value;
    }

    private static void addValueOfMethodUnderEvaluation(Map<String, Map<String, Double>> evaluation_method_value,
                                                        String evaluation, String methodName, Double value) {
        if (!evaluation_method_value.get(evaluation).containsKey(methodName)) {
            evaluation_method_value.get(evaluation).put(methodName, 0.0);
        }
        double originValue = evaluation_method_value.get(evaluation).get(methodName);
        evaluation_method_value.get(evaluation).put(methodName, originValue + value);
    }

    private static String judgeBaseName(String resultFileName) {
        if (resultFileName == null || resultFileName.length() == 0) {
            logger.error("Can't judge base name by empty name!");
            return null;
        }
        if (resultFileName.startsWith("j") || resultFileName.startsWith("J")) {
            return "J";
        } else if (resultFileName.startsWith("n") || resultFileName.startsWith("N")) {
            return "N";
        } else if (resultFileName.startsWith("s") || resultFileName.startsWith("S")) {
            return "S";
        } else if (resultFileName.startsWith("r") || resultFileName.startsWith("R")) {
            return "RF";
        } else {
            logger.error("Can't judge base name by name " + resultFileName);
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        //getForTwoRankCsv("SK_RESULT");
        //getRunTimeCsvFile("TimeFolder/smo_time_log", "TimeFolder", "s");
        //getCost20Pb("CostFiles");
//        Map<String, Map<String, Double>> eva_method_value = getPaperResultMapForBase
//                ("Arffs_Old_Paper_All_Result/ResultFiles/smoResult.csv");
//        PrintUtil.printDoubleTable(eva_method_value, "s", "Arffs_Old_Paper_All_Result/ResultFiles", "PaperResult" +
//                ".csv");
        //getForTwoRankCsv("SK_RESULT");
        getCost20Pb("/home/niubinbin/ideaProjects/imbalancelearning/JITArff_All_Result/CostFiles");
    }
}
