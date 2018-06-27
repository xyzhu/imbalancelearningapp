package classification;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    protected Instances data;
    DecimalFormat df;
    public long startTime;
    public long endTime;
    Map<Instance, List<Integer>> ins_Loc;
    protected double validationResult[] = new double[4];
    public static double[] ratioes;

    public BasicClassification(Instances data, Map<Instance, List<Integer>> ins_Loc) {
        this.data = data;
        this.ins_Loc = ins_Loc;
    }

    public String classify(int times, Classifier classifier, String classifier_name) throws Exception {
        String predictResult = getClassificationResult(classifier,
                classifier_name, times);// get the result without bagging
        return predictResult;
    }

    public MyEvaluation evaluate(Classifier classifier, int randomSeed,
                                 String sample) throws Exception {
        Random rand;
        rand = new Random(randomSeed);
        MyEvaluation eval = null;
        if (sample.equals("under")) {
            eval = new UndersampleEvaluation(data, ins_Loc);
            eval.crossValidateModel(classifier, data, 10, rand);
        } else if (sample.equals("over")) {
            eval = new OversampleEvaluation(data, ins_Loc);
            eval.crossValidateModel(classifier, data, 10, rand);
        } else if (sample.equals("smote")) {
            eval = new SmotesampleEvaluation(data, ins_Loc);
            eval.crossValidateModel(classifier, data, 10, rand);
        } else {
            eval = new NonesampleEvaluation(data, ins_Loc);
            eval.crossValidateModel(classifier, data, 10, rand);
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

    public void updateCostEffective(MyEvaluation eval, String methodName_name) throws SQLException {
        if (!PropertyUtil.CALCULATION_COST) {
            return;
        }
        double[] curCrossVaildCostValue = eval.getCostEffectiveness();
        for (int i = 0; i < curCrossVaildCostValue.length; i++) {
            ratioes[i] += curCrossVaildCostValue[i];
        }
        DataStorageUtil.method_cost20pbs_skOne_basedOnProject.get(methodName_name).add(curCrossVaildCostValue[20]);
    }

    public double[] getCostEffective(int times) {
        if (!PropertyUtil.CALCULATION_COST) {
            return null;
        }
        for (int i = 0; i < ratioes.length; i++) {
            ratioes[i] /= times;
        }
        return ratioes;
    }

    // save the interested result of the classification
    protected String getResultMatrix(Evaluation eval) throws Exception {
        return eval.toMatrixString() + "\n";
    }

    public String getClassificationResult(Classifier classifier,
                                          String classifier_name, int times) throws Exception {
        return "";
    }

    protected void writeCostEffective(int times) throws IOException {
        if (PropertyUtil.CALCULATION_COST) {
            double[] cost = getCostEffective(times);
            PrintUtil.appendResult(PrintUtil.arrayStringFormat(cost, PropertyUtil.NUMBER_PRECISION), PropertyUtil
                    .CUR_COST_EFFECTIVE_RECORD);
            PrintUtil.appendResult(PrintUtil.formatDouble(PropertyUtil.NUMBER_PRECISION, cost[PropertyUtil.PENCENTAGE_OF_CONCERN]) +
                    "", PropertyUtil.CUR_COST_EFFECTIVE_RECORD);
        }
    }
}
