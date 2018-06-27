package bagging;

import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.REPTree;
import weka.core.Instance;
import weka.core.Utils;

public class VoteBag extends Bagging {
    public VoteBag(){
        this.m_Classifier = new REPTree();
    }

    public double[] distributionForInstance(Instance instance) throws Exception {
        double[] sums = new double[instance.numClasses()];

        for(int i = 0; i < this.m_NumIterations; ++i) {
            if (instance.classAttribute().isNumeric()) {
                sums[0] += this.m_Classifiers[i].classifyInstance(instance);
            } else {
                double[] newProbs = this.m_Classifiers[i].distributionForInstance(instance);
                int vote = Utils.maxIndex(newProbs);
                sums[vote] +=1;
            }
        }

        if (instance.classAttribute().isNumeric()) {
            sums[0] /= (double)this.m_NumIterations;
            return sums;
        } else if (Utils.eq(Utils.sum(sums), 0.0D)) {
            return sums;
        } else {
            Utils.normalize(sums);
            return sums;
        }
    }
}
