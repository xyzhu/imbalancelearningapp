package evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dataprocess.ClassificationResult;
import org.apache.log4j.Logger;
import util.InstanceUtil;
import util.PrintUtil;
import util.PropertyUtil;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public abstract class MyEvaluation extends Evaluation {
    private static Logger logger = Logger.getLogger(MyEvaluation.class);
    public static int COST_EFFECTIVE_RATIO_STEP = 101;
    public static int INSTANCE_CHANGE_LINE_INDEX = 4;
    public static int EVALUATION_INDEX_NUM = 4;
    int num_inst = 0;
    double[] num_correct = null;
    double[] num_tp1 = null;
    double[] num_tp2 = null;
    double[] numclass1 = null;
    double[] numclass2 = null;
    double[] numPredictClass1 = null;
    double[] numPredictClass2 = null;
    double[] costEffectiveness = null;
    FastVector cur_predictions = null;
    FastVector crs = null;
    Map<Instance, double[]> ins_actual_predict = null;
    Map<Instance, List<Integer>> ins_loc = null;

    public MyEvaluation(Instances data, Map<Instance, List<Integer>> ins_loc)
            throws Exception {
        super(data);
        this.ins_loc = ins_loc;
    }

    public double areaUnderROC(int classIndex, FastVector predictions) {

        // Check if any predictions have been collected
        if (predictions == null) {
            return Instance.missingValue();
        } else {
            ThresholdCurve tc = new ThresholdCurve();
            Instances result = tc.getCurve(predictions, classIndex);
            return ThresholdCurve.getROCArea(result);
        }
    }

    public FastVector getCrossValidateResult() {
        return crs;
    }

    public double get20PbCost() {
        return 0;
    }

    public double[] getCostEffectiveness() {
        int total_actual_bug_num = 0;
        double total_changedLine_num = 0;
        List<List<Double>> rankTable = new ArrayList<>();
        List<Instance> instancesList = new ArrayList<>();
        instancesList.addAll(ins_actual_predict.keySet());

        for (Instance ins : ins_loc.keySet()) {
            int matchIndex = -1;
            for (int i = 0; i < instancesList.size(); i++) {
                if (InstanceUtil.instanceEquel(ins, instancesList.get(i))) {
                    matchIndex = i;
                    break;
                }
            }
            if (matchIndex == -1) {
                logger.error("Error! The mismatch between the instance in ten fold cross validation and the instance "
                        + "in changedLine");
                return null;
            }

            double[] actual_predict = ins_actual_predict.get(instancesList.get(matchIndex));
            instancesList.remove(matchIndex);

            int changedLine = ins_loc.get(ins).get(INSTANCE_CHANGE_LINE_INDEX);
            int commit_id = ins_loc.get(ins).get(0);
            int file_id = ins_loc.get(ins).get(1);
            total_changedLine_num += changedLine;

            if (actual_predict[0] == 1) {
                total_actual_bug_num++;
            }
            List<Double> actual_predict_change = new ArrayList<Double>();
            actual_predict_change.add(actual_predict[0]);
            actual_predict_change.add(actual_predict[1]);
            actual_predict_change.add((double) changedLine);
            if (PropertyUtil.CALCULATION_FILE_TO_HUNK_COST) {
                actual_predict_change.add((double) commit_id);
                actual_predict_change.add((double) file_id);
            }
            rankTable.add(actual_predict_change);
        }
        if (PropertyUtil.CALCULATION_FILE_TO_HUNK_COST) {
            total_actual_bug_num = (int) PropertyUtil.TOTAL_ACTUAL_HUNK_BUG_NUM;
            total_changedLine_num = PropertyUtil.TOTAL_CHANGED_HUNK_LINE_NUM;
        }
        Collections.sort(rankTable, (o1, o2) -> {
            if (o1.get(1).doubleValue() != o2.get(1).doubleValue()) {
                return (int) (o2.get(1) - o1.get(1));
            } else {
                return (int) (o1.get(2) - o2.get(2));
            }
        });
        double alreadyFind = 0.0;
        double alreadyCheckLine = 0;
        for (int i = 0; i < rankTable.size(); i++) {
            List<Double> actual_predict_change = rankTable.get(i);
            double findRatio = 0;
            double x = 0;
            double upper = 0;
            if (Math.abs(actual_predict_change.get(0).doubleValue() - 1) < 0.01) {
                if (PropertyUtil.CALCULATION_FILE_TO_HUNK_COST) {
                    List<Integer> key = new ArrayList<>();
                    key.add((int) actual_predict_change.get(3).doubleValue());
                    key.add((int) actual_predict_change.get(4).doubleValue());
                    List<List<Integer>> changedLine_isBugs = PropertyUtil.COMMITID_FILEID_CHANGEDLINE_ISBUGS.get(key);
                    for (List<Integer> changedLine_isBug : changedLine_isBugs) {
                        alreadyCheckLine += changedLine_isBug.get(0);
                        if (changedLine_isBug.get(1) == 1) {
                            alreadyFind += 1;
                            findRatio = alreadyFind / total_actual_bug_num * 100;
                            x = alreadyCheckLine / total_changedLine_num * 100;
                            upper = Math.ceil(x);
                            costEffectiveness[(int) upper] = findRatio;
                        }
                    }
                } else {
                    alreadyFind += 1;
                    alreadyCheckLine += actual_predict_change.get(2);
                    findRatio = alreadyFind / total_actual_bug_num * 100;
                    x = alreadyCheckLine / total_changedLine_num * 100;
                    upper = Math.ceil(x);
                    costEffectiveness[(int) upper] = findRatio;
                }
            } else {
                if (PropertyUtil.CALCULATION_FILE_TO_HUNK_COST) {
                    List<Integer> key = new ArrayList<>();
                    key.add((int) actual_predict_change.get(3).doubleValue());
                    key.add((int) actual_predict_change.get(4).doubleValue());
                    List<List<Integer>> changedLine_isBugs = PropertyUtil.COMMITID_FILEID_CHANGEDLINE_ISBUGS.get(key);
                    for (List<Integer> changedLine_isBug : changedLine_isBugs) {
                        alreadyCheckLine += changedLine_isBug.get(0);
                        if (changedLine_isBug.get(1) == 1) {
                            alreadyFind += 1;
                            findRatio = alreadyFind / total_actual_bug_num * 100;
                            x = alreadyCheckLine / total_changedLine_num * 100;
                            upper = Math.ceil(x);
                            costEffectiveness[(int) upper] = findRatio;
                        }
                    }
                } else {
                    alreadyCheckLine += actual_predict_change.get(2);
                }
            }
        }

        // smooth
        for (int i = 1; i < costEffectiveness.length; i++) {
            if (costEffectiveness[i] == 0) {
                costEffectiveness[i] = costEffectiveness[i - 1];
            }
        }
        return costEffectiveness;
    }

    public void setCr(ClassificationResult cr, double num_correct, double num_inst, double numclass1, double num_tp1,
                      double numclass2, double num_tp2, double numPredictClass1, double numPredictClass2, FastVector
                              cur_predictions) {
        cr.setAccuracy(num_correct / num_inst);
        if (numclass1 == 0) {
            cr.setRecall1(0);
        } else {
            cr.setRecall1(num_tp1 / numclass1);
        }
        if (numclass2 == 0) {
            cr.setRecall2(0);
        } else {
            cr.setRecall2(num_tp2 / numclass2);
        }
        if (numPredictClass1 == 0) {
            cr.setPrecision1(0);
        } else {
            cr.setPrecision1(num_tp1 / numPredictClass1);
        }
        if (numPredictClass2 == 0) {
            cr.setPrecision2(0);
        } else {
            cr.setPrecision2(num_tp2 / numPredictClass2);
        }
        cr.setAuc(areaUnderROC(0, cur_predictions));
    }

    //Fix me
    public String getCrDetailString(ClassificationResult cr) {
        if (cr == null) {
            return "null";
        }
        String detailString = PrintUtil.formatDouble(
                PrintUtil.CROSSVAILD_OUTPUT_DECIMAL, cr.getRecall2())
                + ","
                + PrintUtil.formatDouble(
                PrintUtil.CROSSVAILD_OUTPUT_DECIMAL,
                cr.getPrecision2())
                + ","
                + PrintUtil.formatDouble(
                PrintUtil.CROSSVAILD_OUTPUT_DECIMAL,
                cr.getfMeasure2())
                + ","
                + PrintUtil.formatDouble(
                PrintUtil.CROSSVAILD_OUTPUT_DECIMAL, cr.getAuc());
        return detailString;
    }

    void dealWithTestResult(Instances test, FastVector predictions, int num_inst, FastVector cur_predictions,
                            double[] numclass1, double[] numclass2, double[] numPredictClass1, double[] numPredictClass2,
                            double[] num_tp1, double[] num_tp2, double[] num_correct) {
        NominalPrediction np = null;
        for (int n = predictions.size() - num_inst; n < predictions.size(); n++) {
            cur_predictions.addElement(predictions.elementAt(n));
            double[] actual_predict = new double[2];
            np = (NominalPrediction) predictions.elementAt(n);
            actual_predict[0] = np.actual();
            actual_predict[1] = np.predicted();
            ins_actual_predict.put(test.instance(n - (predictions.size() - num_inst)), actual_predict);
        }
        for (int n = 0; n < cur_predictions.size(); n++) {
            np = (NominalPrediction) cur_predictions.elementAt(n);
            if (np.actual() == 0) {
                numclass1[0]++;
            } else {
                numclass2[0]++;
            }
            if (np.predicted() == 0) {
                numPredictClass1[0]++;
            } else {
                numPredictClass2[0]++;
            }
            if (np.actual() == np.predicted()) {
                num_correct[0]++;
                if (np.actual() == 0) {
                    num_tp1[0]++;
                } else {
                    num_tp2[0]++;
                }
            }
        }

    }

    void initialForCrossVaild() {
        num_inst = 0;
        num_correct = new double[1];
        num_tp1 = new double[1];
        num_tp2 = new double[1];
        numclass1 = new double[1];
        numclass2 = new double[1];
        numPredictClass1 = new double[1];
        numPredictClass2 = new double[1];
        costEffectiveness = new double[COST_EFFECTIVE_RATIO_STEP];
        cur_predictions = new FastVector();
        crs = new FastVector();
        ins_actual_predict = new LinkedHashMap<>();
    }

    void clearForNextFold() {
        numclass1[0] = 0;
        numclass2[0] = 0;
        num_correct[0] = 0;
        num_tp1[0] = 0;
        num_tp2[0] = 0;
        numPredictClass1[0] = 0;
        numPredictClass2[0] = 0;
        cur_predictions = new FastVector();
    }
}
