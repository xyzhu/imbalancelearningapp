package util;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PropertyUtil {
    public static int PENCENTAGE_OF_CONCERN = 20;
    public static int NUMBER_PRECISION = 2;
    public static boolean CALCULATION_COST = true;
    public static boolean CALCULATION_FILE_TO_HUNK_COST = false;
    public static String CUR_DATABASE = "";
    public static String HUNK_TABLE_NAME = "metaHunk";
    public static String FOR_TWO_RANK = "For_Two_Rank";
    public static String DETAIL_FOLDER_PATH = "DetailFiles";
    public static String COST_FOLDER_PATH = "CostFiles";
    public static String RESULT_FOLDER_PATH = "ResultFiles";
    public static String DEFAULT_DETAIL_FOLDER = "DetailFiles";
    public static String DEFAULT_SKESD_FOLDER = "SK_ESD";
    public static String CUR_DETAIL_FILENAME = "";
    public static String CUR_COST_EFFECTIVE_RECORD = "";
    public static String CUR_COST_20PB_SK_ONE = "";
    public static String LOC_FILE_PATH = "FileChange/LOCFiles";
    public static String ARFF_PATH = "FileChange/Arffs_old_paper";
    public static String AVG_NAME = "Avg";
    public static String FILE_NAME_DELIMITER = "_";
    public static String FILE_PATH_DELIMITER = "/";
    public static String[] indicators = {"recall-1", "precision-1", "fMeasure-1", "auc"};
    public static String[] BASE_LEARNERS = {"j48", "RF"};
    //public static String[] BASE_LEARNERS = {"naivebayes", "smo"};
    public static int SAMPLE_RATIO = 1;
    public static String[] PROJECTS = {"MyAnt", "MyBuck", "MyFlink", "MyHadoop", "MyItextpdf", "MyJedit", "MyLucene",
            "MySynapse", "MyTomcat", "MyVoldemort"};
    //public static String[] PROJECTS = {"MyItextpdf", "MyJedit", "MyLucene", "MySynapse", "MyTomcat", "MyVoldemort"};
    //Be careful to change!
    public static final String[][] METHOD_NAMES = {{"Simple", "ROS", "RUS", "Smote"},
            {"Bag", "ROSBag", "RUSBag", "SmoteBag"},
            {"Boost", "ROSBoost", "RUSBoost", "SmoteBoost"},
            {"MaxBag", "ROSMaxBag", "RUSMaxBag", "SmoteMaxBag"},
            {"VoteBag", "ROSVoteBag", "RUSVoteBag", "SmoteVoteBag"}};
    public static boolean[][] METHOD_USE_MAP = {{false, false, false, false},
            {false, false, false, false},
            {false, false, false, false},
            {false, false, true, false},
            {false, false, true, false}};
    public static SQLConnection sqlL = null;
    public static Statement stmt = null;
    public static ResultSet resultSet = null;
    public static double TOTAL_ACTUAL_HUNK_BUG_NUM = 0.0;
    public static int TOTAL_CHANGED_HUNK_LINE_NUM = 0;
    public static Map<List<Integer>, List<List<Integer>>> COMMITID_FILEID_CHANGEDLINE_ISBUGS = null;
}
