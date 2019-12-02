package classification;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import util.PrintUtil;
import util.PropertyUtil;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import evaluation.MyEvaluation;

public class ResampleSimpleClassification extends BasicClassification {
    private static Logger logger = Logger.getLogger(ResampleSimpleClassification.class);

    public ResampleSimpleClassification(Instances data) {
        super(data);
    }

    @Override
    // get the classification result without bagging
    public String getClassificationResult(Classifier classifier,
                                          String classifier_name, int times, int numFolds) throws Exception {
        String predictResult = "";
        if (PropertyUtil.METHOD_USE_MAP[0][1]) {
            predictResult += getOverClassificationResult(classifier, classifier_name, times, numFolds);
        }
        if (PropertyUtil.METHOD_USE_MAP[0][2]) {
            predictResult += getUnderClassificationResult(classifier, classifier_name, times, numFolds);
        }
        if (PropertyUtil.METHOD_USE_MAP[0][3]) {
            predictResult += getSmoteClassificationResult(classifier, classifier_name, times, numFolds);
        }
        return predictResult;
    }

    private String getSmoteClassificationResult(Classifier classifier, String classifier_name, int times, int numFolds) throws Exception {
        String methodName = PropertyUtil.METHOD_NAMES[0][3];
        logger.info(methodName);
        validationResult = new double[4];
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_DETAIL_FILENAME);

        startTime = System.currentTimeMillis();
        ratioes = new double[MyEvaluation.COST_EFFECTIVE_RATIO_STEP];
        for (int randomSeed = 1; randomSeed <= times; randomSeed++) {
            MyEvaluation eval = evaluate(classifier, randomSeed, "smote", numFolds);
            updateResult(validationResult, eval);
        }
        endTime = System.currentTimeMillis();
        logger.info("Time:" + (endTime - startTime));
        return getResult("," + methodName, classifier_name, validationResult, times);
    }

    private String getUnderClassificationResult(Classifier classifier, String classifier_name, int times, int numFolds) throws Exception {
        String methodName = PropertyUtil.METHOD_NAMES[0][2];
        validationResult = new double[4];
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_DETAIL_FILENAME);

        startTime = System.currentTimeMillis();
        ratioes = new double[MyEvaluation.COST_EFFECTIVE_RATIO_STEP];
        for (int randomSeed = 1; randomSeed <= times; randomSeed++) {
            MyEvaluation eval = evaluate(classifier, randomSeed, "under", numFolds);
            updateResult(validationResult, eval);
        }
        endTime = System.currentTimeMillis();
        logger.info("Time:" + (endTime - startTime));
        return getResult("," + methodName, classifier_name, validationResult, times);
    }

    private String getOverClassificationResult(Classifier classifier, String classifier_name, int times, int numFolds) throws Exception {
        String methodName = PropertyUtil.METHOD_NAMES[0][1];
        logger.info(methodName);
        validationResult = new double[4];
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_DETAIL_FILENAME);

        startTime = System.currentTimeMillis();
        ratioes = new double[MyEvaluation.COST_EFFECTIVE_RATIO_STEP];
        for (int randomSeed = 1; randomSeed <= times; randomSeed++) {
            MyEvaluation eval = evaluate(classifier, randomSeed, "over", numFolds);
            updateResult(validationResult, eval);
        }
        endTime = System.currentTimeMillis();
        logger.info("Time:" + (endTime - startTime));
        return getResult("," + methodName, classifier_name, validationResult, times);
    }
}