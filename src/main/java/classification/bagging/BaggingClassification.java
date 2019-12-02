package classification.bagging;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import evaluation.MyEvaluation;
import org.apache.log4j.Logger;
import util.PrintUtil;
import util.PropertyUtil;
import weka.classifiers.Classifier;
import weka.classifiers.meta.Bagging;
import weka.core.Instance;
import weka.core.Instances;
import classification.BasicClassification;

public class BaggingClassification extends BasicClassification {
    public static Logger logger = Logger.getLogger(BaggingClassification.class);

    public BaggingClassification(Instances data) {
        super(data);
    }

    public String getClassificationResult(Classifier classifier, String classifier_name, int times, int numFolds) throws Exception {
        if (!PropertyUtil.METHOD_USE_MAP[1][0]) {
            return "";
        }
        Bagging bag_classifier = new Bagging();
        bag_classifier.setClassifier(classifier);
        String methodName = PropertyUtil.METHOD_NAMES[1][0];
        logger.info(methodName);
        PrintUtil.appendResult(methodName, PropertyUtil.CUR_DETAIL_FILENAME);

        startTime = System.currentTimeMillis();
        validationResult = new double[4];
        ratioes = new double[MyEvaluation.COST_EFFECTIVE_RATIO_STEP];
        for (int randomSeed = 1; randomSeed <= times; randomSeed++) {
            MyEvaluation eval = evaluate(bag_classifier, randomSeed, "none", numFolds);
            updateResult(validationResult, eval);
        }
        endTime = System.currentTimeMillis();
        logger.info("Time:" + (endTime - startTime));
        return getResult("," + methodName, classifier_name, validationResult, times);
    }

}
