package ch.epfl.biop.ij2command.stage.general;


public class ArrayStatistics {
	private double [] array;
	public ArrayStatistics(double [] input){
		this.array=input;
	}
	
	ArrayStatistics(){
		
	}
	
	public double []getMeanMinMax(){
		return new double [] {getMean(),getMin(),getMax()};
	}
	public double getMean() {
		int size=this.array.length;
		double mean=0;
		for (int i=0; i<size;i++) {
			mean+=array[i]/size;
		}
		return mean;
	}
	public double getSTDEV() {
		int size=this.array.length;
		double mean=getMean();
		double stdev=0;
		for (int i=0; i<size;i++) {
			stdev+=Math.pow((array[i]-mean),2);
		}
		return Math.sqrt(stdev/(size));
	}
	public double getMin() {
		int size=this.array.length;
		double min=array[0];
		
		for (int i=1; i<size;i++) {
			if (array[i]<min) min=array[i];
		}
		return min;
	}
	public int getMaxPos() {
		int size=this.array.length;
		int pos=0;
		double max=array[0];
		
		for (int i=1; i<size;i++) {
			if (array[i]>max) {
				max=array[i];
				pos=i;
			}
			
		}
		return pos;
	}
	public double getMax() {
		int size=this.array.length;
		double max=array[0];
		
		for (int i=1; i<size;i++) {
			if (array[i]>max) max=array[i];
		}
		return max;
	}
	public int getMax(int [] array) {
		int size=array.length;
		int max=array[0];
		
		for (int i=1; i<size;i++) {
			if (array[i]>max) max=array[i];
		}
		return max;
	}
	public int getMax(byte [] array) {
		int size=array.length;
		int max=array[0];
		
		for (int i=1; i<size;i++) {
			if (array[i]>max) max=array[i];
		}
		return max;
	}
	public static double[] arrayDivide(double[] a, double[] b) {
        double[] out = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = a[i] / b[i];
        }
        return out;
    }
	public static double[] arrayDivide(double[] a, double b) {
        double[] out = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = a[i] / b;
        }
        return out;
    }
	public static double[] arrayDifference(double[] a, double b) {
        double[] out = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = a[i]-b;
        }
        return out;
    }
}

