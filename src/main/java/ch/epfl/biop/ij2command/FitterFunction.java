package ch.epfl.biop.ij2command;

import ij.IJ;
import ij.measure.CurveFitter;

public class FitterFunction {
	private static final int Gauss=1,AsymGauss=2,Poly3=3,Poly4=4;
	private static final String [] methodString= {"Gauss","Asymetric Gauss","Polynomal3","Polynomal4"};
	private String functionName;
	private double [] x,y;
	private double [] parameters;
	protected CurveFitter cf;
	private int method;
	
	FitterFunction(double [] inputX, double [] inputY,int method){
		this.functionName=methodString[method];
		this.method=method;
		setX(inputX);
		setY(inputY);
		cf=new CurveFitter(x,y);
	}
	private void run() {
		cf.doFit(this.method);
	}
	private void run(String func,double [] param) {
		cf.doCustomFit(func, param, false);
	}
	void logParameters() {
		IJ.log("======================================");
		IJ.log(functionName);
		for (int i=0;i<parameters.length;i++) {
			IJ.log(""+IJ.d2s(parameters[i], 5));
		}
	}
	void setX(double []values) {
		this.x=values;
	}
	void setY(double []values) {
		this.x=values;
	}
	String getFunctionName(){
		return this.functionName;
	}
	double [] getParameter() {
		run();
		return cf.getParams();
	}
}
