package evaluation;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import resample.OverSubsample;
import util.PrintUtil;
import util.PropertyUtil;
import weka.classifiers.Classifier;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import dataprocess.ClassificationResult;

public class OversampleEvaluation extends MyEvaluation {
    private static Logger logger = Logger.getLogger(OversampleEvaluation.class);

    public OversampleEvaluation(Instances data) throws Exception {
        super(data);
    }

    public void crossValidateModel(Classifier classifier, Instances data,
                                   int numFolds, Random random, Object... forPredictionsPrinting)
            throws Exception {
        initialForCrossVaild();
        // Make a copy of the data we can reorder
        data = new Instances(data);
        data.randomize(random);
        if (data.classAttribute().isNominal()) {
            data.stratify(numFolds);
        }
        // Do the folds
        for (int i = 0; i < numFolds; i++) {
            Instances train = data.trainCV(numFolds, i, random);

            OverSubsample oversubsample;
            oversubsample = new OverSubsample();
            oversubsample.setInputFormat(train);
            oversubsample.setDistributionSpread(1);
            train = Filter.useFilter(train, oversubsample);
            setPriors(train);
            Classifier copiedClassifier = Classifier.makeCopy(classifier);
            copiedClassifier.buildClassifier(train);
            Instances test = data.testCV(numFolds, i);
            // evaluateModel(copiedClassifier, test, forPredictionsPrinting);
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

        /*
         * if (classificationOutput != null) classificationOutput.printFooter();
         */

    }

}
