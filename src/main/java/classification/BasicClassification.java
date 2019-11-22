package classification;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import util.DataStorageUtil;
import util.PrintUtil;
import util.PropertyUtil;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import evaluation.MyEvaluation;
import evaluation.NonesampleEvaluation;
import evaluation.OversampleEvaluation;
import evaluation.SmotesampleEvaluation;
import evaluation.UndersampleEvaluation;

/*
 * This is the super class of other classification methodName
 */
public class BasicClassification {

    private Logger logger = Logger.getLogger(BasicClassification.class);

    protected Instances data;
    DecimalFormat df;
    public long startTime;
    public long endTime;
    protected double validationResult[] = new double[4];
    public static double[] ratioes;

    public BasicClassification(Instances data) {
        this.data = data;
    }

    public String classify(Classifier classifier, String project, String classifier_name, int times, int numFolds) throws Exception {
        String predictResult = getClassificationResult(classifier,
                classifier_name, times, numFolds);// get the result without bagging
        return predictResult;
    }

    public MyEvaluation evaluate(Classifier classifier, int randomSeed,
                                 String sample, int numFolds) throws Exception {
        Random rand = new Random(randomSeed);
        MyEvaluation eval = null;
        if (sample.equals("under")) {
            eval = new UndersampleEvaluation(data);
            eval.crossValidateModel(classifier, data, numFolds, rand);
        } else if (sample.equals("over")) {
            eval = new OversampleEvaluation(data);
            eval.crossValidateModel(classifier, data, numFolds, rand);
        } else if (sample.equals("smote")) {
            eval = new SmotesampleEvaluation(data);
            eval.crossValidateModel(classifier, data, numFolds, rand);
        } else {
            eval = new NonesampleEvaluation(data);
            eval.crossValidateModel(classifier, data, numFolds, rand);
        }
        return eval;
    }

    // save the interested result of the classification
    public String getResult(String methodNamename, String classifiername,
                            double validationResult[], int times) throws Exception {
        df = (DecimalFormat) NumberFormat.getInstance();
        df.applyPattern("0.00");
        double recall_1 = validationResult[0] * 100 / times;
        double precison_1 = validationResult[1] * 100 / times;
        double fmeasure_1 = validationResult[2] * 100 / times;
        double auc = validationResult[3] * 100 / times;
        return methodNamename + ", " + PrintUtil.formatDouble(PropertyUtil.NUMBER_PRECISION, recall_1)
                + ", " + PrintUtil.formatDouble(PropertyUtil.NUMBER_PRECISION, precison_1) + ", "
                + PrintUtil.formatDouble(PropertyUtil.NUMBER_PRECISION, fmeasure_1) + ","
                + PrintUtil.formatDouble(PropertyUtil.NUMBER_PRECISION, auc) + "\n";
    }

    public void updateResult(double validationResult[], Evaluation eval) {
        // double accuracy = eval.pctCorrect();
        // double recall_0 = eval.recall(0);
        double recall_1 = eval.recall(1);
        // double precison_0 = eval.precision(0);
        double precison_1 = eval.precision(1);
        // double fmeasure_0 = eval.fMeasure(0);
        double fmeasure_1 = eval.fMeasure(1);
        // double gmean = Math.sqrt(recall_0 * recall_1);
        double auc = eval.areaUnderROC(0);
        validationResult[0] += recall_1;
        validationResult[1] += precison_1;
        validationResult[2] += fmeasure_1;
        validationResult[3] += auc;
        return;
    }



    // save the interested result of the classification
    protected String getResultMatrix(Evaluation eval) throws Exception {
        return eval.toMatrixString() + "\n";
    }

    public String getClassificationResult(Classifier classifier,
                                          String classifier_name, int times, int numFolds) throws Exception {
        return "";
    }

}
