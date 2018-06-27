import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import util.InstanceUtil;
import weka.core.Instance;
import weka.core.Instances;

public class InstanceUtilTest {

    @BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testInstanceEquel() throws IOException {
		String testArffFilePathString = "TestFolder/MyVoldemort.arff";
		BufferedReader bReader = new BufferedReader(new FileReader(new File(
				testArffFilePathString)));
		Instances instances1 = new Instances(bReader);
		bReader = new BufferedReader(new FileReader(new File(
				testArffFilePathString)));
		Instances instances2 = new Instances(bReader);
		for (int i = 0; i < instances1.numInstances(); i++) {
			boolean curRes = InstanceUtil.instanceEquel(instances1.instance(i),
					instances2.instance(i));
			Assert.assertTrue(curRes);
		}
		int count = 0;
		Instance compare = instances1.instance(0);
		for (int i = 0; i < instances2.numInstances(); i++) {
			if (InstanceUtil.instanceEquel(compare, instances2.instance(i))) {
				count++;
			}
		}
		Assert.assertEquals(1, count);
	}

	public void testSortDoubleList() {
		List<Double> list0 = Arrays.asList(0.0, 0.0, 99.0);
		List<Double> list1 = Arrays.asList(0.0, 1.0, 99.0);
		List<Double> list2 = Arrays.asList(0.0, 1.0, 40.0);
		List<List<Double>> sortList = new ArrayList<List<Double>>();
		sortList.add(list0);
		sortList.add(list1);
		sortList.add(list2);
		Collections.sort(sortList, new Comparator<List<Double>>() {
			public int compare(List<Double> o1, List<Double> o2) {
				if (o1.get(1).doubleValue() != o2.get(1).doubleValue()) {
					return (int) (o2.get(1) - o1.get(1));
				} else {
					return (int) (o1.get(2) - o2.get(2));
				}
			}

		});
		Assert.assertTrue(sortList.get(0).get(1).doubleValue() - 1 == 0);
		Assert.assertTrue(sortList.get(0).get(2).doubleValue() == 40);
		Assert.assertTrue(sortList.get(2).get(1).doubleValue() == 0);
	}

	@Test
	public void testMathCeil() {
		System.out.println(Math.ceil(3.5));
		System.out.println(74.0 / 151);
		System.out.println(Math.ceil(0.00001));
	}
}
