package ch.epfl.biop.ij2command;

import ij.IJ;
import ij.measure.CurveFitter;

public class FitterFunction {
	protected static final int Gauss=1,AsymGauss=2,Poly3=3,Poly4=4;
	private static final String [] methodString= {"Gauss","Asymetric Gauss","Polynomal3","Polynomal4"};
	private String functionName;
	protected double [] x;
	protected double [] y;
	private double [] parameters,initParameters;
	protected CurveFitter cf;
	private int method;
	private boolean logResults=true;
	
	FitterFunction(double [] inputX, double [] inputY,int method){
		this.functionName=methodString[method-1];
		this.method=method;
		setX(inputX);
		setY(inputY);
		this.cf=new CurveFitter(x,y);
	}
	private void run() {
		cf.doFit(this.method);
		this.parameters=cf.getParams();
	}
	private void run(String func,double [] param) {
		cf.doCustomFit(func, param, false);
		cf.getPlot().show();
		this.parameters=cf.getParams();
		
	}
	void logParameters() {
		IJ.log("======================================");
		IJ.log(functionName);
		for (int i=0;i<parameters.length;i++) {
			IJ.log(""+IJ.d2s(parameters[i], 5));
		}
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
}
