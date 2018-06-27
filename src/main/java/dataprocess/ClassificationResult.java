package dataprocess;

public class ClassificationResult {

	double accuracy = 0;
	double auc = 0;
	double recall1 = 0;
	double recall2 = 0;
	double precision1 = 0;
	double precision2 = 0;
	double fMeasure1 = 0;
	double fMeasure2 = 0;

	public ClassificationResult(double a, double b, double c, double d) {
		accuracy = a;
		auc = b;
		recall1 = c;
		recall2 = d;
	}

	public ClassificationResult() {
		accuracy = 0;
		auc = 0;
		recall1 = 0;
		recall2 = 0;
	}

	public void setPrecision1(double p1) {
		precision1 = p1;
	}

	public void setPrecision2(double p2) {
		precision2 = p2;
	}

	public void setAccuracy(double a) {
		accuracy = a;
	}

	public void setAuc(double a) {
		auc = a;
	}

	public void setRecall1(double r1) {
		recall1 = r1;
	}

	public void setRecall2(double r2) {
		recall2 = r2;
	}

	public double getAccuracy() {
		return accuracy;
	}

	public double getAuc() {
		return auc;
	}

	public double getRecall1() {
		return recall1;
	}

	public double getRecall2() {
		return recall2;
	}

	public double getPrecision1() {
		return precision1;
	}

	public double getPrecision2() {
		return precision2;
	}

	public double getfMeasure1() {
		if (precision1 == 0 && recall1 == 0) {
			return 0;
		}
		fMeasure1 = (2 * precision1 * recall1) / (precision1 + recall1);
		return fMeasure1;
	}

	public double getfMeasure2() {
		if (precision2 == 0 && recall2 == 0) {
			return 0;
		}
		fMeasure2 = (2 * precision2 * recall2) / (precision2 + recall2);
		return fMeasure2;
	}
}