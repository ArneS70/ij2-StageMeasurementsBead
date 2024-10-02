package ch.epfl.biop.ij2command;

import ij.IJ;
import ij.measure.CurveFitter;

public class FitterFunction {
	protected static final int Gauss=0,AsymGauss=1,Poly3=2,Poly4=3;
	private static final String [] methodString= {"Gauss","Asymetric Gauss","Polynomal3","Polynomal4"};
	private String functionName;
	private String [] header;
	protected double [] x;
	protected double [] y;
	private double [] parameters,initParameters;
	protected CurveFitter cf;
	private int method;
	private boolean logResults=true;
	private double max;
	
	FitterFunction(double [] inputX, double [] inputY,int method){
		this.functionName=methodString[method];
		this.method=method;
		setX(inputX);
		setY(inputY);
		this.cf=new CurveFitter(x,y);
	}
	private void run() {
		cf.doFit(this.method);
		this.parameters=cf.getParams();
		this.max=findMax();
	}
	private void run(String func,double [] param) {
		cf.doCustomFit(func, param, false);
		cf.getPlot().show();
		this.parameters=cf.getParams();
		this.max=findMax();
	}
	void logParameters() {
		
		IJ.log(functionName);
		for (int i=0;i<parameters.length-1;i++) {
			IJ.log(this.header[i]+"   :"+IJ.d2s(parameters[i], 5));
		}
		IJ.log("max  :"+this.max);
	}
	void setInitParameters(double []param) {
		this.initParameters=param;
	}
	
	void setX(double []values) {
		this.x=values;
	}
	void setY(double []values) {
		this.y=values;
	}
	void setHeader(String [] names) {
		this.header=names;
	}
	double [] getMeanMinMax(double [] values) {
		ArrayStatistics as=new ArrayStatistics(values);
		return new double [] {as.getMean(),as.getMin(),as.getMax()};
	}
	String getFunctionName(){
		return this.functionName;
	}
	double [] getParameter() {
		run();
		if (logResults) logParameters();
		return cf.getParams();
	}
	double [] getParameter(String fitFunc) {
		run(fitFunc,initParameters);
		if (logResults) logParameters();
		return cf.getParams();
	}
	double findMax() {
		int length=x.length;
		double maxValue=0;
		double max=0;
		for (int i=0;i<length;i++) {
//			IJ.log(""+cf.f(x[i]));
			if (cf.f(x[i])>maxValue) {max=x[i];maxValue=cf.f(x[i]);};
		}
		return max;
		
	}
	public double getMax() {
		// TODO Auto-generated method stub
		return max;
	}
}
