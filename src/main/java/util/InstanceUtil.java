package util;

import weka.core.Instance;

public class InstanceUtil {
    public static boolean instanceEquel(Instance ins1, Instance ins2) {
        if (!ins1.equalHeaders(ins2)) {
            return false;
        }
        double[] array1 = ins1.toDoubleArray();
        double[] array2 = ins2.toDoubleArray();
        for (int i = 0; i < ins1.numAttributes(); i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }
}
