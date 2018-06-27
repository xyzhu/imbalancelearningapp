package Classifier;

import java.util.Random;

import util.PropertyUtil;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdaBoostM1;
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;

public class SmoteBoosting extends AdaBoostM1 {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected void buildClassifierUsingResampling(Instances data)
            throws Exception {

        Instances trainData, sample, training;
        double epsilon, reweight, sumProbs;
        Evaluation evaluation;
        int numInstances = data.numInstances();
        Random randomInstance = new Random(m_Seed);
        int resamplingIterations = 0;

        // Initialize data
        m_Betas = new double[m_Classifiers.length];
        m_NumIterationsPerformed = 0;
        // Create a copy of the data so that when the weights are diddled
        // with it doesn't mess up the weights for anyone else
        training = new Instances(data, 0, numInstances);
        sumProbs = training.sumOfWeights();
        for (int i = 0; i < training.numInstances(); i++) {
            training.instance(i).setWeight(
                    training.instance(i).weight() / sumProbs);
        }

        // Do boostrap iterations
        for (m_NumIterationsPerformed = 0; m_NumIterationsPerformed < m_Classifiers.length; m_NumIterationsPerformed++) {
            if (m_Debug) {
                System.err.println("Training classifier "
                        + (m_NumIterationsPerformed + 1));
            }

            // Select instances to train the classifier on
            if (m_WeightThreshold < 100) {
                trainData = selectWeightQuantile(training,
                        (double) m_WeightThreshold / 100);
            } else {
                trainData = new Instances(training);
            }

            // Resample
            resamplingIterations = 0;
            double[] weights = new double[trainData.numInstances()];
            for (int i = 0; i < weights.length; i++) {
                weights[i] = trainData.instance(i).weight();
            }
            do {
                sample = trainData.resampleWithWeights(randomInstance, weights);
                // use under sample to balance the sample
                Instances tempData = new Instances(sample);
                tempData.randomize(randomInstance);
                AttributeStats as = tempData.attributeStats(tempData
                        .numAttributes() - 1);
                int count[] = as.nominalCounts;
                int max = Math.max(count[0], count[1]);
                int min = Math.min(count[0], count[1]);
                double percent = 0;
                if ((double) max / min >= PropertyUtil.SAMPLE_RATIO) {
                    percent = ((double) 1 / PropertyUtil.SAMPLE_RATIO * max - min) / min * 100;
                }
                SMOTE smote = new SMOTE();
                smote.setPercentage(percent);
                smote.setInputFormat(tempData);// smote resample
                sample = Filter.useFilter(tempData, smote);
                m_Classifiers[m_NumIterationsPerformed].buildClassifier(sample);
                evaluation = new Evaluation(data);
                evaluation.evaluateModel(
                        m_Classifiers[m_NumIterationsPerformed], training);
                epsilon = evaluation.errorRate();
                resamplingIterations++;
            } while (Utils.eq(epsilon, 0) && (resamplingIterations < 10));

            // Stop if error too big or 0
            if (Utils.grOrEq(epsilon, 0.5) || Utils.eq(epsilon, 0)) {
                if (m_NumIterationsPerformed == 0) {
                    m_NumIterationsPerformed = 1; // If we're the first we have
                    // to to use it
                }
                break;
            }

            // Determine the weight to assign to this model
            m_Betas[m_NumIterationsPerformed] = Math.log((1 - epsilon)
                    / epsilon);
            reweight = (1 - epsilon) / epsilon;
            if (m_Debug) {
                System.err.println("\terror rate = " + epsilon + "  beta = "
                        + m_Betas[m_NumIterationsPerformed]);
            }

            // Update instance weights
            setWeights(training, reweight);
        }
    }
}
