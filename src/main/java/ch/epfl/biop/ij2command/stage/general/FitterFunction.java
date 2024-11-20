package ch.epfl.biop.ij2command.stage.general;

import ij.IJ;
import ij.gui.Plot;
import ij.measure.CurveFitter;

    public class FitterFunction {

	    protected static final int POLY3=3, POLY4=4,POLY6=6,POLY8=8,GAUSS=12, ASYMGAUSS=25;
	    public static final String [] methodString= {"Poly3","Poly4","Poly6","Poly8","Gauss","AsymGauss"};
		public static final int [] methodInt= {CurveFitter.POLY3,CurveFitter.POLY4,CurveFitter.POLY6,0,CurveFitter.POLY8,CurveFitter.GAUSSIAN,25};
		public FitterFunction fitFunc;
			
//	static final int [] methodParam= {2,3,0,3,4,0,0,0,CurveFitter.POLY8};
	
		private String functionName;
		private String [] header;
		protected CurveFitter cf;
		private int method;
		private double max;
		private static double [] x;
		private static double [] y;
	
	public FitterFunction() {
		
	}
	
	public FitterFunction(double [] inputX, double [] inputY,String function){
//		cf=new CurveFitter(inputX,inputY);
//		this.functionName=getMethodstring()[method];
		this.x=inputX;
		this.y=inputY;
		this.fitFunc=getFitFunc(function);
	}
	
	public FitterFunction(double [] inputX, double [] inputY,int method){
		cf=new CurveFitter(inputX,inputY);
		this.functionName=getMethodstring()[method];
		this.method=methodInt[method];
	}
	synchronized public void fit() {
		if (method<25) cf.doFit(method);
	}
	synchronized public void fit(int method) {
		cf.doFit(methodInt[method]);
	}
	
	synchronized public double [][] getFunctionValues(double [] param) {
		return new double [0][0];
	}
	synchronized private void run() {
		cf.doFit(this.method);
		this.parameters=cf.getParams();
		this.max=findMax();
	}
		
	synchronized private void run(String func,double [] param) {
		cf.doCustomFit(func, param, false);
		cf.getPlot().show();
		this.parameters=cf.getParams();
		this.max=findMax();
	}
	synchronized void logParameters() {
		
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
	public void setHeader(String [] names) {
		this.header=names;
	}
	double [] getMeanMinMax(double [] values) {
		ArrayStatistics as=new ArrayStatistics(values);
		return new double [] {as.getMean(),as.getMin(),as.getMax()};
	}
	String getFunctionName(){
		return this.functionName;
	}
	public double [] getParameter() {
		run();
		if (logResults) logParameters();
		return cf.getParams();
	}
	double [] getParameter(String fitFunc) {
		run(fitFunc,initParameters);
		if (logResults) logParameters();
		return cf.getParams();
	}
	public Plot getPlot() {
		return cf.getPlot();
	}
	double findMax() {
		int length=x.length;
		double x1=x[0];double x2=x[length-1];
		if (x1>x2) {x1=x[length];x2=x[0];}
		double dist=x2-x1;
		x1-=dist/2;
		x2+=dist/2;
		double maxValue=0;
		double max=0;
		for (double i=x1;i<x2;i+=dist/(10*length)) {
//			IJ.log(""+cf.f(x[i]));
			if (cf.f(i)>maxValue) {max=i;maxValue=cf.f(i);};
		}
		return max;
		
	}
	public static FitterFunction getFitFunc(String function) {
		
		FitterFunction fit=new FitterFunction();
		switch (function) {
        case "Poly3":
            fit = new Poly3Fitter(x,y); 
            break;
        case "Poly4":
        	fit = new Poly4Fitter(x,y);
            break;
        case "Poly6":
        	fit = new Poly6Fitter(x,y);
            break;
        default:
        	fit = new Poly3Fitter(x,y);
            break;
    }
		return fit;
		
	}
	int getMethod(int method) {
		return FitterFunction.methodInt[method];
	}
	public double getMax() {
		// TODO Auto-generated method stub
		return max;
	}
	public static String [] getMethodstring() {
		return methodString;
	}
	synchronized public String getGlobalFunction(double [] param) {
		return GlobalFitter.createPolyFormula(param,methodParam[method]);
	}
}
