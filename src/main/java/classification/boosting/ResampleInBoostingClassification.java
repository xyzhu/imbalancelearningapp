package classification.boosting;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import evaluation.MyEvaluation;
import org.apache.log4j.Logger;
import util.PrintUtil;
import util.PropertyUtil;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import Classifier.OverBoosting;
import Classifier.SmoteBoosting;
import Classifier.UnderBoosting;
import classification.BasicClassification;

public class ResampleInBoostingClassification extends BasicClassification {

    private static Logger logger = Logger.getLogger(ResampleInBoostingClassification.class);

    public ResampleInBoostingClassification(Instances data, Map<Instance, List<Integer>> ins_Loc) {
        super(data, ins_Loc);
    }

    public String getClassificationResult(Classifier classifier,
                                          String classifier_name, int times) throws Exception {
        String predictResult = "";
        if (PropertyUtil.METHOD_USE_MAP[2][1]) {
            predictResult = getOverBoostClassificationResult(classifier, classifier_name, times);
        }
        if (PropertyUtil.METHOD_USE_MAP[2][2]) {
            predictResult += getUnderBoostClassificationResult(classifier, classifier_name, times);
        }
        if (PropertyUtil.METHOD_USE_MAP[2][3]) {
            predictResult += getSmoteBoostClassificationResult(classifier, classifier_name, times);
        }
        return predictResult;
    }

    private String getSmoteBoostClassificationResult(Classifier classifier,
                                                     String classifier_name, int times) throws Exception {
        SmoteBoosting boost_classifier = new SmoteBoosting();
        boost_classifier.setClassifier(classifier);
        boost_classifier.setUseResampling(true);
        String methodName = PropertyUtil.METHOD_NAMES[2][3];
        logger.info(methodName);
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_DETAIL_FILENAME);
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_COST_EFFECTIVE_RECORD);
        startTime = System.currentTimeMillis();
        validationResult = new double[4];
        ratioes = new double[MyEvaluation.COST_EFFECTIVE_RATIO_STEP];
        for (int randomSeed = 1; randomSeed <= times; randomSeed++) {
            MyEvaluation eval = evaluate(boost_classifier, randomSeed, "none");
            updateResult(validationResult, eval);
            updateCostEffective(eval, methodName);
        }
        writeCostEffective(times);
        endTime = System.currentTimeMillis();
        logger.info("Time:" + (endTime - startTime));
        return getResult("," + methodName, classifier_name, validationResult,
                times);
    }

    public String getUnderBoostClassificationResult(Classifier classifier,
                                                    String classifier_name, int times) throws Exception {
        UnderBoosting boost_classifier = new UnderBoosting();
        String methodName = PropertyUtil.METHOD_NAMES[2][2];
        logger.info(methodName);
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_DETAIL_FILENAME);
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_COST_EFFECTIVE_RECORD);
        boost_classifier.setClassifier(classifier);
        boost_classifier.setUseResampling(true);
        startTime = System.currentTimeMillis();
        validationResult = new double[4];
        ratioes = new double[MyEvaluation.COST_EFFECTIVE_RATIO_STEP];
        for (int randomSeed = 1; randomSeed <= times; randomSeed++) {
            MyEvaluation eval = evaluate(boost_classifier, randomSeed, "none");
            updateResult(validationResult, eval);
            updateCostEffective(eval, methodName);
        }
        writeCostEffective(times);
        endTime = System.currentTimeMillis();
        logger.info("Time:" + (endTime - startTime));
        return getResult("," + methodName, classifier_name, validationResult,
                times);
    }

    private String getOverBoostClassificationResult(Classifier classifier,
                                                    String classifier_name, int times) throws Exception {
        OverBoosting boost_classifier = new OverBoosting();
        String methodName = PropertyUtil.METHOD_NAMES[2][1];
        logger.info(methodName);
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_COST_EFFECTIVE_RECORD);
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_DETAIL_FILENAME);
        boost_classifier.setClassifier(classifier);
        boost_classifier.setUseResampling(true);
        startTime = System.currentTimeMillis();
        validationResult = new double[4];
        ratioes = new double[MyEvaluation.COST_EFFECTIVE_RATIO_STEP];
        for (int randomSeed = 1; randomSeed <= times; randomSeed++) {
            MyEvaluation eval = evaluate(boost_classifier, randomSeed, "none");
            updateResult(validationResult, eval);
            updateCostEffective(eval, methodName);
        }
        writeCostEffective(times);
        endTime = System.currentTimeMillis();
        logger.info("Time:" + (endTime - startTime));
        return getResult("," + methodName, classifier_name, validationResult,
                times);
    }

}
