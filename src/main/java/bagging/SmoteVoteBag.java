package bagging;

import util.PropertyUtil;
import weka.classifiers.Classifier;
import weka.classifiers.trees.REPTree;
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.core.Randomizable;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;

import java.util.Random;

public class SmoteVoteBag extends VoteBag{
    public SmoteVoteBag(){
        this.m_Classifier = new REPTree();
    }

    public void checkClassifier(Instances data) throws Exception {

        if (m_Classifier == null) {
            throw new Exception("A base classifier has not been specified!");
        }
        m_Classifiers = Classifier.makeCopies(m_Classifier, m_NumIterations);
    }

    @Override
    public void buildClassifier(Instances data) throws Exception {

        // can classifier handle the data?
        getCapabilities().testWithFail(data);

        // remove instances with missing class
        data = new Instances(data);
        data.deleteWithMissingClass();

        checkClassifier(data);

        if (m_CalcOutOfBag && (m_BagSizePercent != 100)) {
            throw new IllegalArgumentException("Bag size needs to be 100% if "
                    + "out-of-bag error is to be calculated!");
        }

        int bagSize = data.numInstances() * m_BagSizePercent / 100;
        Random random = new Random(m_Seed);

        boolean[][] inBag = null;
        if (m_CalcOutOfBag)
            inBag = new boolean[m_Classifiers.length][];

        for (int j = 0; j < m_Classifiers.length; j++) {
            Instances bagData = null;

            // create the in-bag dataset
            if (m_CalcOutOfBag) {
                inBag[j] = new boolean[data.numInstances()];
                bagData = data.resampleWithWeights(random, inBag[j]);
            } else {
                Instances tempData = new Instances(data);
                tempData.randomize(random);
                SMOTE smotesample = new SMOTE();
                smotesample.setInputFormat(tempData);
                AttributeStats as = tempData.attributeStats(tempData.numAttributes() - 1);
                int count[] = as.nominalCounts;
                int max = Math.max(count[0], count[1]);
                int min = Math.min(count[0], count[1]);
                double percent = 0;
                if ((double) max / min >= PropertyUtil.SAMPLE_RATIO) {
                    percent = ((double) 1 / PropertyUtil.SAMPLE_RATIO * max - min) / min * 100;
                }
                smotesample.setPercentage(percent);
                bagData = Filter.useFilter(tempData, smotesample);
                if (bagSize < data.numInstances()) {
                    bagData.randomize(random);
                    Instances newBagData = new Instances(bagData, 0, bagSize);
                    bagData = newBagData;
                }
            }

            if (m_Classifier instanceof Randomizable) {
                ((Randomizable) m_Classifiers[j]).setSeed(random.nextInt());
            }

            // build the classifier
            m_Classifiers[j].buildClassifier(bagData);
        }

        // calc OOB error?
        if (getCalcOutOfBag()) {
            double outOfBagCount = 0.0;
            double errorSum = 0.0;
            boolean numeric = data.classAttribute().isNumeric();

            for (int i = 0; i < data.numInstances(); i++) {
                double vote;
                double[] votes;
                if (numeric)
                    votes = new double[1];
                else
                    votes = new double[data.numClasses()];

                // determine predictions for instance
                int voteCount = 0;
                for (int j = 0; j < m_Classifiers.length; j++) {
                    if (inBag[j][i])
                        continue;

                    voteCount++;
                    // double pred = m_Classifiers[j].classifyInstance(data.instance(i));
                    if (numeric) {
                        // votes[0] += pred;
                        votes[0] = m_Classifiers[j].classifyInstance(data.instance(i));
                    } else {
                        // votes[(int) pred]++;
                        double[] newProbs = m_Classifiers[j].distributionForInstance(data
                                .instance(i));
                        // average the probability estimates
                        for (int k = 0; k < newProbs.length; k++) {
                            votes[k] += newProbs[k];
                        }
                    }
                }

                // "vote"
                if (numeric) {
                    vote = votes[0];
                    if (voteCount > 0) {
                        vote /= voteCount; // average
                    }
                } else {
                    if (Utils.eq(Utils.sum(votes), 0)) {
                    } else {
                        Utils.normalize(votes);
                    }
                    vote = Utils.maxIndex(votes); // predicted class
                }

                // error for instance
                outOfBagCount += data.instance(i).weight();
                if (numeric) {
                    errorSum += StrictMath.abs(vote - data.instance(i).classValue())
                            * data.instance(i).weight();
                } else {
                    if (vote != data.instance(i).classValue())
                        errorSum += data.instance(i).weight();
                }
            }

            m_OutOfBagError = errorSum / outOfBagCount;
        } else {
            m_OutOfBagError = 0;
        }
    }
}
