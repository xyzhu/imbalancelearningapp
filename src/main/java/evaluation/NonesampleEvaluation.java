package evaluation;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import util.PrintUtil;
import util.PropertyUtil;
import weka.classifiers.Classifier;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Range;
import dataprocess.ClassificationResult;

public class NonesampleEvaluation extends MyEvaluation {
    private static Logger logger = Logger.getLogger(NonesampleEvaluation.class);

    public NonesampleEvaluation(Instances data,
                                Map<Instance, List<Integer>> ins_loc) throws Exception {
        super(data, ins_loc);
    }

    public void crossValidateModel(Classifier classifier, Instances data,
                                   int numFolds, Random random, Object... forPredictionsPrinting)
            throws Exception {
        initialForCrossVaild();
        data = new Instances(data);
        data.randomize(random);
        if (data.classAttribute().isNominal()) {
            data.stratify(numFolds);
        }
        if (forPredictionsPrinting.length > 0) {
            // print the header first
            StringBuffer buff = (StringBuffer) forPredictionsPrinting[0];
            Range attsToOutput = (Range) forPredictionsPrinting[1];
            boolean printDist = ((Boolean) forPredictionsPrinting[2])
                    .booleanValue();
            printClassificationsHeader(data, attsToOutput, printDist, buff);
        }
        // Do the folds
        for (int i = 0; i < numFolds; i++) {
            Instances train = data.trainCV(numFolds, i, random);
            setPriors(train);
            Classifier copiedClassifier = Classifier.makeCopy(classifier);
            copiedClassifier.buildClassifier(train);
            Instances test = data.testCV(numFolds, i);
            evaluateModel(copiedClassifier, test);
            FastVector predictions = predictions();
            num_inst = test.numInstances();
            ClassificationResult cr = new ClassificationResult();
            dealWithTestResult(test, predictions, num_inst, cur_predictions, numclass1, numclass2,
                    numPredictClass1, numPredictClass2, num_tp1, num_tp2, num_correct);
            setCr(cr, num_correct[0], num_inst, numclass1[0], num_tp1[0], numclass2[0], num_tp2[0], numPredictClass1[0],
                    numPredictClass2[0], cur_predictions);
            crs.addElement(cr);
            String detailString = getCrDetailString(cr);
            PrintUtil.appendResult(detailString, PropertyUtil.CUR_DETAIL_FILENAME);
            clearForNextFold();
        }
        m_NumFolds = numFolds;
    }

}
