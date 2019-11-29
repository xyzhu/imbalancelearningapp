package classification.bagging;

import Classifier.OverBagging;
import Classifier.SmoteBagging;
import Classifier.UnderBagging;
import classification.BasicClassification;
import classification.ResampleSimpleClassification;
import evaluation.MyEvaluation;
import org.apache.log4j.Logger;
import util.PrintUtil;
import util.PropertyUtil;
import weka.classifiers.Classifier;
import weka.classifiers.meta.Bagging;
import weka.core.Instances;

public class ResampleOutBaggingClassification extends BasicClassification {
    public static Logger logger = Logger.getLogger(ResampleOutBaggingClassification.class);

    public ResampleOutBaggingClassification(Instances data) {
        super(data);
    }

    public String getClassificationResult(Classifier classifier, String classifier_name, int times, int numFolds) throws Exception {
        String predictResult = "";
        if (PropertyUtil.METHOD_USE_MAP[5][1]) {
            predictResult += getOverBagClassificationResult(classifier, classifier_name, times, numFolds);
        }
        if (PropertyUtil.METHOD_USE_MAP[5][2]) {
            predictResult += getUnderBagClassificationResult(classifier, classifier_name, times, numFolds);
        }
        if (PropertyUtil.METHOD_USE_MAP[5][3]) {
            predictResult += getSmoteBagClassificationResult(classifier, classifier_name, times, numFolds);
        }
        return predictResult;
    }

    public String getSmoteBagClassificationResult(Classifier classifier,
                                                  String classifier_name, int times, int numFolds) throws Exception {
        Bagging bag_classifier = new Bagging();
        bag_classifier.setClassifier(classifier);
        String methodName = PropertyUtil.METHOD_NAMES[5][3];
        logger.info(methodName);
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_DETAIL_FILENAME);

        startTime = System.currentTimeMillis();
        validationResult = new double[4];
        ratioes = new double[MyEvaluation.COST_EFFECTIVE_RATIO_STEP];
        for (int randomSeed = 1; randomSeed <= times; randomSeed++) {
            MyEvaluation eval = evaluate(bag_classifier, randomSeed, "smote", numFolds);
            updateResult(validationResult, eval);
        }
        endTime = System.currentTimeMillis();
        logger.info("Time:" + (endTime - startTime));
        return getResult("," + methodName, classifier_name, validationResult, times);
    }

    public String getUnderBagClassificationResult(Classifier classifier,
                                                  String classifier_name, int times, int numFolds) throws Exception {
        Bagging bag_classifier = new Bagging();
        bag_classifier.setClassifier(classifier);
        String methodName = PropertyUtil.METHOD_NAMES[5][2];
        logger.info(methodName);
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_DETAIL_FILENAME);

        startTime = System.currentTimeMillis();
        validationResult = new double[MyEvaluation.EVALUATION_INDEX_NUM];
        ratioes = new double[MyEvaluation.COST_EFFECTIVE_RATIO_STEP];
        for (int randomSeed = 1; randomSeed <= times; randomSeed++) {
            MyEvaluation eval = evaluate(bag_classifier, randomSeed, "under", numFolds);
            updateResult(validationResult, eval);
        }
        endTime = System.currentTimeMillis();
        logger.info("Time:" + (endTime - startTime));
        return getResult("," + methodName, classifier_name, validationResult, times);

    }


    private String getOverBagClassificationResult(Classifier classifier,
                                                  String classifier_name, int times, int numFolds) throws Exception {
        Bagging bag_classifier = new Bagging();
        bag_classifier.setClassifier(classifier);
        String methodName = PropertyUtil.METHOD_NAMES[5][1];
        logger.info(methodName);
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_DETAIL_FILENAME);

        startTime = System.currentTimeMillis();
        validationResult = new double[4];
        ratioes = new double[MyEvaluation.COST_EFFECTIVE_RATIO_STEP];
        for (int randomSeed = 1; randomSeed <= times; randomSeed++) {
            MyEvaluation eval = evaluate(bag_classifier, randomSeed, "over", numFolds);
            updateResult(validationResult, eval);
        }
        endTime = System.currentTimeMillis();
        logger.info("Time:" + (endTime - startTime));
        return getResult("," + methodName, classifier_name, validationResult, times);
    }
}
