package bagging;

import weka.classifiers.RandomizableIteratedSingleClassifierEnhancer;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.REPTree;
import weka.core.*;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

public class MaxBag extends Bagging {

    /**
     * Constructor.
     */
    public MaxBag() {

        m_Classifier = new REPTree();
    }


    /**
     * Calculates the class membership probabilities for the given test instance.
     *
     * @param instance the instance to be classified
     * @return preedicted class probability distribution
     * @throws Exception if distribution can't be computed successfully
     */
    @Override
    public double[] distributionForInstance(Instance instance) throws Exception {
        double maxProbilities = Double.MIN_VALUE;
        double[] sums = new double[instance.numClasses()], newProbs;

        for (int i = 0; i < m_NumIterations; i++) {
            if (instance.classAttribute().isNumeric() == true) {
                sums[0] += m_Classifiers[i].classifyInstance(instance);
            } else {
                newProbs = m_Classifiers[i].distributionForInstance(instance);
                for (int j = 0; j < newProbs.length; j++){
                    if (newProbs[j]>sums[j]){         //Get maxProbilities
                        sums[j]=newProbs[j];
                    }
                }
            }
        }
        if (instance.classAttribute().isNumeric() == true) {
            sums[0] /= m_NumIterations;
            return sums;
        } else if (Utils.eq(Utils.sum(sums), 0)) {
            return sums;
        } else {
            Utils.normalize(sums);
            return sums;
        }
    }

}
