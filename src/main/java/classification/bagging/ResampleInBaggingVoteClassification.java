package classification.bagging;

import bagging.*;
import classification.BasicClassification;
import evaluation.MyEvaluation;
import org.apache.log4j.Logger;
import util.PrintUtil;
import util.PropertyUtil;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.List;
import java.util.Map;

public class ResampleInBaggingVoteClassification extends BasicClassification {
    public static Logger logger = Logger.getLogger(ResampleInBaggingVoteClassification.class);

    public ResampleInBaggingVoteClassification(Instances data, Map<Instance, List<Integer>> ins_Loc) {
        super(data, ins_Loc);
    }

    public String getClassificationResult(Classifier classifier, String classifier_name, int times) throws Exception {
        String predictResult = "";
        if (PropertyUtil.METHOD_USE_MAP[4][1]) {
            predictResult += getROSVoteBagClassificationResult(classifier, classifier_name, times);
        }
        if (PropertyUtil.METHOD_USE_MAP[4][2]) {
            predictResult += getRUSVoteBagClassificationResult(classifier, classifier_name, times);
        }
        if (PropertyUtil.METHOD_USE_MAP[4][3]) {
            predictResult += getSmoteVoteBagClassificationResult(classifier, classifier_name, times);
        }
        return predictResult;
    }

    public String getSmoteVoteBagClassificationResult(Classifier classifier,
                                                     String classifier_name, int times) throws Exception {
        SmoteVoteBag bag_classifier = new SmoteVoteBag();
        bag_classifier.setClassifier(classifier);
        String methodName = PropertyUtil.METHOD_NAMES[4][3];
        logger.info(methodName);
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_DETAIL_FILENAME);
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_COST_EFFECTIVE_RECORD);
        startTime = System.currentTimeMillis();
        validationResult = new double[4];
        ratioes = new double[MyEvaluation.COST_EFFECTIVE_RATIO_STEP];
        for (int randomSeed = 1; randomSeed <= times; randomSeed++) {
            MyEvaluation eval = evaluate(bag_classifier, randomSeed, "none");
            updateResult(validationResult, eval);
            updateCostEffective(eval, methodName);
        }
        writeCostEffective(times);
        endTime = System.currentTimeMillis();
        logger.info("Time:" + (endTime - startTime));
        return getResult("," + methodName, classifier_name, validationResult, times);
    }

    public String getRUSVoteBagClassificationResult(Classifier classifier,
                                                   String classifier_name, int times) throws Exception {
        RUSVoteBag bag_classifier = new RUSVoteBag();
        bag_classifier.setClassifier(classifier);
        String methodName = PropertyUtil.METHOD_NAMES[4][2];
        logger.info(methodName);
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_DETAIL_FILENAME);
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_COST_EFFECTIVE_RECORD);
        startTime = System.currentTimeMillis();
        validationResult = new double[MyEvaluation.EVALUATION_INDEX_NUM];
        ratioes = new double[MyEvaluation.COST_EFFECTIVE_RATIO_STEP];
        for (int randomSeed = 1; randomSeed <= times; randomSeed++) {
            MyEvaluation eval = evaluate(bag_classifier, randomSeed, "none");
            updateResult(validationResult, eval);
            updateCostEffective(eval, methodName);
        }
        writeCostEffective(times);
        endTime = System.currentTimeMillis();
        logger.info("Time:" + (endTime - startTime));
        return getResult("," + methodName, classifier_name, validationResult, times);

    }


    private String getROSVoteBagClassificationResult(Classifier classifier,
                                                    String classifier_name, int times) throws Exception {
        ROSVoteBag bag_classifier = new ROSVoteBag();
        bag_classifier.setClassifier(classifier);
        String methodName = PropertyUtil.METHOD_NAMES[4][1];
        logger.info(methodName);
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_DETAIL_FILENAME);
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_COST_EFFECTIVE_RECORD);
        startTime = System.currentTimeMillis();
        validationResult = new double[4];
        ratioes = new double[MyEvaluation.COST_EFFECTIVE_RATIO_STEP];
        for (int randomSeed = 1; randomSeed <= times; randomSeed++) {
            MyEvaluation eval = evaluate(bag_classifier, randomSeed, "none");
            updateResult(validationResult, eval);
            updateCostEffective(eval, methodName);
        }
        writeCostEffective(times);
        endTime = System.currentTimeMillis();
        logger.info("Time:" + (endTime - startTime));
        return getResult("," + methodName, classifier_name, validationResult, times);
    }
}
