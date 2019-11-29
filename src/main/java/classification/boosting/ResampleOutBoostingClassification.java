package classification.boosting;

import Classifier.OverBoosting;
import Classifier.SmoteBoosting;
import Classifier.UnderBoosting;
import classification.BasicClassification;
import evaluation.MyEvaluation;
import org.apache.log4j.Logger;
import util.PrintUtil;
import util.PropertyUtil;
import weka.classifiers.Classifier;
import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instances;

public class ResampleOutBoostingClassification extends BasicClassification {

    private static Logger logger = Logger.getLogger(ResampleOutBoostingClassification.class);

    public ResampleOutBoostingClassification(Instances data) {
        super(data);
    }

    public String getClassificationResult(Classifier classifier,
                                          String classifier_name, int times, int numFolds) throws Exception {
        String predictResult = "";
        if (PropertyUtil.METHOD_USE_MAP[6][1]) {
            predictResult = getOverBoostClassificationResult(classifier, classifier_name, times, numFolds);
        }
        if (PropertyUtil.METHOD_USE_MAP[6][2]) {
            predictResult += getUnderBoostClassificationResult(classifier, classifier_name, times, numFolds);
        }
        if (PropertyUtil.METHOD_USE_MAP[6][3]) {
            predictResult += getSmoteBoostClassificationResult(classifier, classifier_name, times, numFolds);
        }
        return predictResult;
    }

    private String getSmoteBoostClassificationResult(Classifier classifier,
                                                     String classifier_name, int times, int numFolds) throws Exception {
        AdaBoostM1 boost_classifier = new AdaBoostM1();
        boost_classifier.setClassifier(classifier);
        String methodName = PropertyUtil.METHOD_NAMES[6][3];
        logger.info(methodName);
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_DETAIL_FILENAME);
        startTime = System.currentTimeMillis();
        validationResult = new double[4];
        ratioes = new double[MyEvaluation.COST_EFFECTIVE_RATIO_STEP];
        for (int randomSeed = 1; randomSeed <= times; randomSeed++) {
            MyEvaluation eval = evaluate(boost_classifier, randomSeed, "smote", numFolds);
            updateResult(validationResult, eval);
        }
        endTime = System.currentTimeMillis();
        logger.info("Time:" + (endTime - startTime));
        return getResult("," + methodName, classifier_name, validationResult,
                times);
    }

    public String getUnderBoostClassificationResult(Classifier classifier,
                                                    String classifier_name, int times, int numFolds) throws Exception {
        AdaBoostM1 boost_classifier = new AdaBoostM1();
        boost_classifier.setClassifier(classifier);
        String methodName = PropertyUtil.METHOD_NAMES[6][2];
        logger.info(methodName);
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_DETAIL_FILENAME);
        startTime = System.currentTimeMillis();
        validationResult = new double[4];
        ratioes = new double[MyEvaluation.COST_EFFECTIVE_RATIO_STEP];
        for (int randomSeed = 1; randomSeed <= times; randomSeed++) {
            MyEvaluation eval = evaluate(boost_classifier, randomSeed, "under", numFolds);
            updateResult(validationResult, eval);
        }
        endTime = System.currentTimeMillis();
        logger.info("Time:" + (endTime - startTime));
        return getResult("," + methodName, classifier_name, validationResult,
                times);
    }

    private String getOverBoostClassificationResult(Classifier classifier,
                                                    String classifier_name, int times, int numFolds) throws Exception {
        AdaBoostM1 boost_classifier = new AdaBoostM1();
        boost_classifier.setClassifier(classifier);
        String methodName = PropertyUtil.METHOD_NAMES[6][1];
        logger.info(methodName);
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_DETAIL_FILENAME);
        startTime = System.currentTimeMillis();
        validationResult = new double[4];
        ratioes = new double[MyEvaluation.COST_EFFECTIVE_RATIO_STEP];
        for (int randomSeed = 1; randomSeed <= times; randomSeed++) {
            MyEvaluation eval = evaluate(boost_classifier, randomSeed, "over", numFolds);
            updateResult(validationResult, eval);
        }
        endTime = System.currentTimeMillis();
        logger.info("Time:" + (endTime - startTime));
        return getResult("," + methodName, classifier_name, validationResult,
                times);
    }

}
