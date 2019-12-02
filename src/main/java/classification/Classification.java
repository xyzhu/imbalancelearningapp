package classification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import classification.bagging.*;
import classification.boosting.ResampleOutBoostingClassification;
import org.apache.log4j.Logger;
import util.DataStorageUtil;
import util.PrintUtil;
import util.PropertyUtil;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import classification.boosting.BoostingClassification;
import classification.boosting.ResampleInBoostingClassification;

public class Classification {

    private Logger logger = Logger.getLogger(Classification.class);

    Instances data;
    String classifier_name;
    Classifier classifier;
    public static List<String> EVALUATION_NAMES = Arrays.asList("R1", "P1", "F1", "AUC", "COST20Pb");
    public static int DETAIL_NUM = 1000;

    public Classification(Instances data) {
        this.data = data;
    }

    public void setClassifier(String classifier_name_input) {
        classifier_name = classifier_name_input;
        switch (classifier_name) {
            case "j48":
                classifier = new J48();
                break;
            case "naivebayes":
                classifier = new NaiveBayes();
                break;
            case "smo":
                classifier = new SMO();
                break;
            case "randomforest":
                classifier = new RandomForest();
                break;
            case "ripper":
                classifier = new JRip();
                break;
            case "IBk":
                classifier = new IBk();
                break;
            case "LR":
                classifier = new LinearRegression();
                break;
            case "RF":
                classifier = new RandomForest();
                break;
        }
    }

    //project, classifier_name, sampling_name, ensemble_name, times, numFolds
    public String predict(String project, String classifier_name, String sampling_name, String ensemble_name, int times, int numFolds, boolean isIn)  throws Exception {
        setClassifier(classifier_name);
        BasicClassification use_classification = null;
        String result;
        DETAIL_NUM = times * 10;

        // row 和 col 为PropertyUtil.METHOD_USE_MAP的下标
        int row = -1, col = -1;
        if (ensemble_name.equals("No ensemble")) {
            // Simple ROS RUS Smote
            row = 0;
            if (sampling_name.equals("Simple")) {
                col = 0;
                use_classification = new SimpleClassification(data);
            } else if (sampling_name.equals("ROS")) {
                col = 1;
                use_classification = new ResampleSimpleClassification(data);
            } else if (sampling_name.equals("RUS")) {
                col = 2;
                use_classification = new ResampleSimpleClassification(data);
            } else if (sampling_name.equals("Smote")) {
                col = 3;
                use_classification = new ResampleSimpleClassification(data);
            }
        } else if (ensemble_name.equals("Bagging")) {
            // row = 1 "Bag", "ROSBag", "RUSBag", "SmoteBag"
            // row = 5 {"Bag", "ROSOutBag", "RUSOutBag", "SmoteOutBag"},
            row = 1;
            if (sampling_name.equals("Simple")) {
                col = 0;
                use_classification = new BaggingClassification(data);
            } else {
                if(isIn) {
                    use_classification = new ResampleInBaggingClassification(data);
                } else {
                    row = 5;
                    use_classification = new ResampleOutBaggingClassification(data);
                }

                if (sampling_name.equals("ROS")){
                    col = 1;
                } else if (sampling_name.equals("RUS")) {
                    col = 2;
                } else if (sampling_name.equals("Smote")) {
                    col = 3;
                }
            }
        } else if (ensemble_name.equals("Boosting")) {
            // row = 2 "Boost", "ROSBoost", "RUSBoost", "SmoteBoost"
            // row = 6 {"Boost", "ROSOutBoost", "RUSOutBoost", "SmoteOutBoost"},
            row = 2;
            if (sampling_name.equals("Simple")) {
                col = 0;
                use_classification = new BoostingClassification(data);
            } else {
                if(isIn) {
                    use_classification = new ResampleInBoostingClassification(data);
                } else {
                    row = 6;
                    use_classification = new ResampleOutBoostingClassification(data);
                }

                if (sampling_name.equals("ROS")) {
                    col = 1;
                } else if (sampling_name.equals("RUS")) {
                    col = 2;
                } else if (sampling_name.equals("Smote")) {
                    col = 3;
                }
            }
        }
        if (row == -1 || col == -1) {
            throw new Exception("Error in ensemble_name!");
        }
        PropertyUtil.METHOD_USE_MAP[row][col] = true;
        result = use_classification.classify(classifier, project, classifier_name, times, numFolds).split("\n")[0];
        PropertyUtil.METHOD_USE_MAP[row][col] = false;
        return result;
    }
        // origin predict
   /*
    public String predict(String classifier_name_input, String project,
                          int times, Map<Instance, List<Integer>> ins_Loc) throws Exception {

        setClassifier(classifier_name_input);
        String predict_result = project;
        BasicClassification use_classification = null;
        DETAIL_NUM = times * 10;
        use_classification = new SimpleClassification(data);
        predict_result += use_classification.classify(times, classifier, classifier_name);
        System.out.println(predict_result);

        use_classification = new ResampleSimpleClassification(data);
        predict_result += use_classification.classify(times, classifier, classifier_name);
        System.out.println(predict_result);

        use_classification = new BaggingClassification(data);
        predict_result += use_classification.classify(times, classifier, classifier_name);
        System.out.println(predict_result);

        use_classification = new ResampleInBaggingClassification(data);
        predict_result += use_classification.classify(times, classifier, classifier_name);
        System.out.println(predict_result);

        use_classification = new BoostingClassification(data);
        predict_result += use_classification.classify(times, classifier, classifier_name);
        System.out.println(predict_result);

        use_classification = new ResampleInBoostingClassification(data);
        predict_result += use_classification.classify(times, classifier, classifier_name);
        System.out.println(predict_result);

        use_classification = new BaggingMaxClassification(data);
        predict_result += use_classification.classify(times, classifier, classifier_name);
        System.out.println(predict_result);

        use_classification = new ResampleInBaggingMaxClassification(data);
        predict_result += use_classification.classify(times, classifier, classifier_name);
        System.out.println(predict_result);

        use_classification = new BaggingVoteClassification(data);
        predict_result += use_classification.classify(times, classifier, classifier_name);
        System.out.println(predict_result);

        use_classification = new ResampleInBaggingVoteClassification(data);
        predict_result += use_classification.classify(times, classifier, classifier_name);
        System.out.println(predict_result);

        PrintUtil.printSKOneMap(DataStorageUtil.method_cost20pbs_skOne_basedOnProject, PropertyUtil
                .CUR_COST_20PB_SK_ONE, 2);
        System.out.println(predict_result);
        return predict_result;
    }
    */

}
