package ch.epfl.biop.ij2command;


public class ArrayStatistics {
	private double [] array;
	ArrayStatistics(double [] input){
		this.array=input;
	}
	
	ArrayStatistics(){
		
	}
	
	double getMean() {
		int size=this.array.length;
		double mean=0;
		for (int i=0; i<size;i++) {
			mean+=array[i]/size;
		}
		return mean;
	}
	double getSTDEV() {
		int size=this.array.length;
		double mean=getMean();
		double stdev=0;
		for (int i=0; i<size;i++) {
			stdev+=Math.pow((array[i]-mean),2);
		}
		return Math.sqrt(stdev/(size-1));
	}
	double getMin() {
		int size=this.array.length;
		double min=array[0];
		
		for (int i=1; i<size;i++) {
			if (array[i]<min) min=array[i];
		}
		return min;
	}
	double getMax() {
		int size=this.array.length;
		double max=array[0];
		
		for (int i=1; i<size;i++) {
			if (array[i]>max) max=array[i];
		}
		return max;
	}
	int getMax(int [] array) {
		int size=array.length;
		int max=array[0];
		
		for (int i=1; i<size;i++) {
			if (array[i]>max) max=array[i];
		}
		return max;
	}
	int getMax(byte [] array) {
		int size=array.length;
		int max=array[0];
		
		for (int i=1; i<size;i++) {
			if (array[i]>max) max=array[i];
		}
		return max;
	}
}

