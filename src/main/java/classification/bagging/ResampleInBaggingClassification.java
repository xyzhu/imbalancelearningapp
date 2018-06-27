package classification.bagging;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import classification.ResampleSimpleClassification;
import org.apache.log4j.Logger;
import util.PrintUtil;
import util.PropertyUtil;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import Classifier.OverBagging;
import Classifier.SmoteBagging;
import Classifier.UnderBagging;
import classification.BasicClassification;
import evaluation.MyEvaluation;

public class ResampleInBaggingClassification extends BasicClassification {
    public static Logger logger = Logger.getLogger(ResampleSimpleClassification.class);

    public ResampleInBaggingClassification(Instances data, Map<Instance, List<Integer>> ins_Loc) {
        super(data, ins_Loc);
    }

    public String getClassificationResult(Classifier classifier, String classifier_name, int times) throws Exception {
        String predictResult = "";
        if (PropertyUtil.METHOD_USE_MAP[1][1]) {
            predictResult += getOverBagClassificationResult(classifier, classifier_name, times);
        }
        if (PropertyUtil.METHOD_USE_MAP[1][2]) {
            predictResult += getUnderBagClassificationResult(classifier, classifier_name, times);
        }
        if (PropertyUtil.METHOD_USE_MAP[1][3]) {
            predictResult += getSmoteBagClassificationResult(classifier, classifier_name, times);
        }
        return predictResult;
    }

    public String getSmoteBagClassificationResult(Classifier classifier,
                                                  String classifier_name, int times) throws Exception {
        SmoteBagging bag_classifier = new SmoteBagging();
        bag_classifier.setClassifier(classifier);
        String methodName = PropertyUtil.METHOD_NAMES[1][3];
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

    public String getUnderBagClassificationResult(Classifier classifier,
                                                  String classifier_name, int times) throws Exception {
        UnderBagging bag_classifier = new UnderBagging();
        bag_classifier.setClassifier(classifier);
        String methodName = PropertyUtil.METHOD_NAMES[1][2];
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


    private String getOverBagClassificationResult(Classifier classifier,
                                                  String classifier_name, int times) throws Exception {
        OverBagging bag_classifier = new OverBagging();
        bag_classifier.setClassifier(classifier);
        String methodName = PropertyUtil.METHOD_NAMES[1][1];
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
