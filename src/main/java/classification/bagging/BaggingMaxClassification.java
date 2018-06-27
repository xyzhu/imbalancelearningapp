package classification.bagging;

import classification.BasicClassification;
import evaluation.MyEvaluation;
import org.apache.log4j.Logger;
import util.PrintUtil;
import util.PropertyUtil;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import bagging.MaxBag;
import java.util.List;
import java.util.Map;

public class BaggingMaxClassification extends BasicClassification {
    public static Logger logger = Logger.getLogger(BaggingMaxClassification.class);

    public BaggingMaxClassification(Instances data, Map<Instance, List<Integer>> ins_Loc) {
        super(data, ins_Loc);
    }

    public String getClassificationResult(Classifier classifier, String classifier_name, int times) throws Exception {
        if (!PropertyUtil.METHOD_USE_MAP[3][0]) {
            return "";
        }
        MaxBag bag_classifier = new MaxBag();
        bag_classifier.setClassifier(classifier);
        String methodName = PropertyUtil.METHOD_NAMES[3][0];
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
