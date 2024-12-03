package ch.epfl.biop.ij2command.stage.general;

import ij.IJ;
import ij.gui.Plot;
import ij.measure.CurveFitter;
import ij.measure.UserFunction;

    public class FitterFunction {

	    protected static final int POLY3=3, POLY4=4,POLY6=6,POLY8=8,GAUSS=12, ASYMGAUSS=25;
	    public static final String [] methodString= {"Poly3","Poly4","Poly6","Poly8","Gauss","AsymGauss"};
		public static final int [] methodInt= {CurveFitter.POLY3,CurveFitter.POLY4,CurveFitter.POLY6,0,CurveFitter.POLY8,CurveFitter.GAUSSIAN,25};
		public String function,globalFunction;
		public int numParam;
		public double[] initParam; 
			
//	static final int [] methodParam= {2,3,0,3,4,0,0,0,CurveFitter.POLY8};
	
		private String functionName;
		private String [] header;
		protected CurveFitter cf;
		private int method;
		private double max;
		protected double [] x;
		protected double [] y;
	
//	public FitterFunction() {
		
//	}
	
//	public FitterFunction(double [] inputX, double [] inputY,String function){
//		cf=new CurveFitter(inputX,inputY);
//		this.functionName=getMethodstring()[method];
//		this.x=inputX;
//		this.y=inputY;
//		this.fitFunc=getFitFunc(function);
//	}
	
	public FitterFunction(double [] inputX, double [] inputY,int method){
		this.x=inputX;
		this.y=inputY;
		cf=new CurveFitter(inputX,inputY);
//		this.functionName=getMethodstring()[method];
		this.method=method;
	}
//	synchronized public void fit() {
//		if (method<25) cf.doFit(method);
//	}
//	synchronized public void fit(int method) {
//		cf.doFit(methodInt[method]);
//	}
	
//	synchronized public double [][] getFunctionValues(double [] param) {
//		return new double [0][0];
//	}
	public String createGlobalFormula(double[]parameters){
//		Vector <Integer> pos=new Vector<Integer>();
		int i=0;
		
		do {
			String val="";
			int pos=function.indexOf("param");
			char a=function.charAt(pos-1);
			
			if (parameters[i]>0 && a!=40) val="+";
			val=val.concat(Double.toString(parameters[i]));
			
			function=function.replaceFirst("param",val );
			i++;
		}
		while (function.indexOf("param")!=-1);
		return function;
		
	}
	public synchronized double [] getFitResults(double [] paramVariation) {
		return null;
	}
	public synchronized double [] getGlobalFitResults(double [] param) {
		return null;
	}
	public synchronized double [] getFitResults() {
		if (method<25) {
			cf.doFit(this.method);
		} else {
			int len=this.initParam.length;
			double []variation=new double [this.initParam.length];
			for (int i=0;i<this.initParam.length;i++) {
				variation[i]=0.1;
			}
//			cf.doCustomFit(cf, len, function, initParam, variation, false);
			cf.doCustomFit(this.function, this.initParam,  false);
		}
		double [] results=cf.getParams();
		int num=results.length;
		double [] allParam=new double [num+1];
		
		allParam[num]=cf.getRSquared();
		System.arraycopy(results, 0, allParam, 0, num);
		return allParam;
		
	}
	public synchronized double [] getFitResults(String function) {
		
			cf.doCustomFit(function,new double [] {1,1,1},false);
//			cf.doCustomFit(function,new double [] {1,1},false);
			double [] results=cf.getParams();
			int num=results.length;
			double [] allParam=new double [num+1];
			
			allParam[num]=cf.getRSquared();
			System.arraycopy(results, 0, allParam, 0, num);
			return allParam;
		
	}
	public synchronized void updateInput(double []x,double[]y) {
		this.cf=new CurveFitter(x,y);
	}
/*		
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
	
	public void setX(double []values) {
		FitterFunction.x=values;
	}
	public void setY(double []values) {
		FitterFunction.y=values;
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
*/
	public synchronized Plot getPlot() {
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
	public static FitterFunction getFitFunc(double []x,double []y,String function) {
		
		FitterFunction fit;
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
        case "Poly8":
        	fit = new Poly8Fitter(x,y);
            break;
        case "AsymGauss":
        	fit = new Asym2SigFitter(x,y);
            break;
        default:
        	fit = new Poly3Fitter(x,y);
            break;
    }
		return fit;
		
	}
	void setMethod(String function) {
		
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
	synchronized public String getGlobalFunction(double [] param,int num) {
		return GlobalFitter.createPolyFormula(param,num);
	}

	
}
