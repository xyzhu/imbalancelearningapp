package classification.boosting;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import evaluation.MyEvaluation;
import org.apache.log4j.Logger;
import util.PrintUtil;
import util.PropertyUtil;
import weka.classifiers.Classifier;
import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instance;
import weka.core.Instances;
import classification.BasicClassification;

public class BoostingClassification extends BasicClassification {
    private static Logger logger = Logger.getLogger(BoostingClassification.class);

    public BoostingClassification(Instances data, Map<Instance, List<Integer>> ins_Loc) {
        super(data, ins_Loc);
    }

    public String getClassificationResult(Classifier classifier,
                                          String classifier_name, int times) throws Exception {
        if (!PropertyUtil.METHOD_USE_MAP[2][0]) {
            return "";
        }
        AdaBoostM1 boost_classifier = new AdaBoostM1();
        boost_classifier.setClassifier(classifier);
        String methodName = PropertyUtil.METHOD_NAMES[2][0];
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
        return getResult("," + methodName, classifier_name, validationResult, times);
    }

}
