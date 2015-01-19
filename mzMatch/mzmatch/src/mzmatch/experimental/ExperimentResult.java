package mzmatch.experimental;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple class to represent results of identification performance evaluation experiments
 * 
 * @author joewandy
 * 
 */
public class ExperimentResult {

	private String path;
	private int std;
	private String mode;
	private double alpha; // DP concentration parameter
	private int iterId;
	private int dbSize;
	private int idLevel;
	
	private int tp;
	private int fp;
	private int tn;
	private int fn;
	
	private double threshold;
	private double f1;
	private double tpr;
	private double fpr;
	
	private boolean[] labels;
	private double[] scores;
	private double[] tprArr;
	private double[] fprArr;
	private double[] f1Arr;
	
	/* Set true to format the output nicely */
	boolean prettyPrint;
	
	/* stores each point along the roc curve */
	List<RocData> allRocData;

	public ExperimentResult(String path, double alpha, int iterId, int dbSize, int idLevel) {
		this.path = path;
		parsePath(path);
		this.alpha = alpha;
		this.iterId = iterId;
		this.dbSize = dbSize;
		this.idLevel = idLevel;
		this.prettyPrint = false;
		this.allRocData = new ArrayList<RocData>();
	}

	public void setPrettyPrint(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
	}

	public void storeSingleResult(int tp, int fp, int tn, int fn, double threshold) {
		
		this.tp = tp;
		this.fp = fp;
		this.tn = tn;
		this.fn = fn;
		this.threshold = threshold;
		
		this.tpr = computeTpr(tp, fn);
		this.fpr = computeFpr(fp, tn);
		this.f1 = computeF1(tp, fp, fn);
		
	}
	
	public void printSingleResult() {
		
		String tprStr = String.format("%.4f", tpr);
		String fprStr = String.format("%.4f", fpr);
		String f1Str = String.format("%.4f", f1);
		
		String thresholdStr = String.format("%.2f", threshold);
		String alphaStr = String.format("%.2f", alpha);
		
		if (prettyPrint) {
			
			System.out.println("ExperimentResult:\n" + 
					"\tinput=" + path + "\n" +
					"\talpha=" + alphaStr + "\n" +
					"\titerId=" + iterId + "\n" +
					"\tthreshold=" + thresholdStr + "\n" +
					"\tdbSize=" + dbSize + "\n" +
					"\tidLevel=" + idLevel + "\n" +
					"\ttp=" + tp + "\n" + 
					"\tfp=" + fp + "\n" + 
					"\ttn=" + tn + "\n" + 
					"\tfn=" + fn + "\n" + 
					"\ttpr=" + tprStr + "\n" + 
					"\tfpr=" + fprStr + "\n" + 
					"\tf1=" + f1Str);		
			
		} else {

			System.out.println("ExperimentResult:" + 
					path + "," +
					alphaStr + "," + 
					iterId + "," + 
					thresholdStr + "," +
					dbSize + "," +
					idLevel + "," +
					tp + "," + 
					fp + "," + 
					tn + "," + 
					fn + "," + 
					tprStr + "," + 
					fprStr + "," + 
					f1Str);		
			
		}
	}

	public RocData storeRocData(int tp, int fp, int tn, int fn) {
		
		double tpr = computeTpr(tp, fn);
		double fpr = computeFpr(fp, tn);
		double f1 = computeF1(tp, fp, fn);

		RocData rocData = new RocData(tpr, fpr, f1);
		this.allRocData.add(rocData);
		return rocData;
		
	}
	
	public void printRocData(boolean[] labels, double[] scores) {

		double[] tprArr = new double[allRocData.size()];
		double[] fprArr = new double[allRocData.size()];
		
		for (int i = 0; i < allRocData.size(); i++) {
			RocData data = allRocData.get(i);
			tprArr[i] = data.getTpr();
			fprArr[i] = data.getFpr();
		}
				
		System.out.println("labels = " + Arrays.toString(labels) + ";");
		System.out.println();
		System.out.println("scores = " + Arrays.toString(scores) + ";");		
		System.out.println();
		System.out.println("tpr = " + Arrays.toString(tprArr) + ";");
		System.out.println();
		System.out.println("fpr = " + Arrays.toString(fprArr) + ";");		
		System.out.println();

	}
	
	private void parsePath(String path) {

		// lazy to write regex just to handle these few cases ...
		
		if (path.toLowerCase().contains("std1")) {
			std = 1;
		} else if (path.toLowerCase().contains("std2")) {
			std = 2;
		} else if (path.toLowerCase().contains("std3")) {
			std = 3;
		}
		
		if (path.toLowerCase().contains("positive") || path.toLowerCase().contains("pos")) {
			mode = "positive";
		} else if (path.toLowerCase().contains("negative") || path.toLowerCase().contains("neg")) {
			mode = "negative";
		}

	}
	
	private double computeTpr(int tp, int fn) {
		return ((double) tp) / (tp + fn);
	}

	private double computeFpr(int fp, int tn) {
		return ((double) fp) / (fp + tn);
	}

	private double computeF1(int tp, int fp, int fn) {
		double f1 = ((double) (2*tp)) / ( (2*tp) + fp + fn );
		return f1;
	}
	
}

class RocData {
	
	private double tpr;
	private double fpr;
	private double f1;
	
	public RocData(double tpr, double fpr, double f1) {
		this.tpr = tpr;
		this.fpr = fpr;
		this.f1 = f1;
	}

	public double getTpr() {
		return tpr;
	}

	public double getFpr() {
		return fpr;
	}

	public double getF1() {
		return f1;
	}
	
}
