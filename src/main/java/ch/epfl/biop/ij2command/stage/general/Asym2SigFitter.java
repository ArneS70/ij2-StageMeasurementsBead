package ch.epfl.biop.ij2command.stage.general;


	import java.util.Arrays;

import ij.IJ;
import ij.measure.CurveFitter;
import ij.measure.UserFunction;



	public class Asym2SigFitter  extends FitterFunction implements UserFunction{

	public final static String [] header= {"y0","height","center","w1","w2","w3","R^2","FWHM"};
	private String fitFunction="y=a+b*(1/(1+exp(-1*(x-c+(d/2))/e)))*(1-(1/(1+exp(-1*(x-c-(d/2))/f))))";
			
	
	
	public Asym2SigFitter(double []xInput ,double [] yInput){
		super(xInput,yInput,FitterFunction.ASYMGAUSS);
		super.numParam=4;
		super.function=fitFunction;
		initParam=getInitParameters();
		super.globalFunction=fitFunction;
		
	}
	public synchronized double [] getFitResults() {
		cf.doCustomFit(this, 6, "", initParam,new double [] {0.5,0.5,0.5,0.5,0.5,0.5}, false);
//		cf.doCustomFit(this, 6, "", x, initParam, false);
		return cf.getParams();
	}
	public synchronized double [] getFitResults(double [] paramVariation) {
		
//		cf.doCustomFit(function,new double [] {initParam[0],initParam[1],initParam[2]},false);
		cf.setMaxIterations(10000);
		cf.doCustomFit(this, 6, "", initParam,paramVariation, false);
//		cf.doCustomFit(function,new double [] {1,1},false);
		double [] results=cf.getParams();
		int num=results.length;
		double [] allParam=new double [num+1];
		
		allParam[num]=cf.getRSquared();
		System.arraycopy(results, 0, allParam, 0, num);
		return allParam;
	
}
	public synchronized double [] getFitResults(String function) {
		
//		cf.doCustomFit(function,new double [] {initParam[0],initParam[1],initParam[2]},false);
		cf.setMaxIterations(10000);
		cf.doCustomFit(this, 6, "", initParam,new double [] {0.2,0.2,0.2,0.00001,0.00001,0.000001}, false);
//		cf.doCustomFit(function,new double [] {1,1},false);
		double [] results=cf.getParams();
		int num=results.length;
		double [] allParam=new double [num+1];
		
		allParam[num]=cf.getRSquared();
		System.arraycopy(results, 0, allParam, 0, num);
		return allParam;
	
}
	public String createGlobalFormula(double []param) {
		String global=fitFunction.replace("exp", "xxx");
		global=global.replace("d", IJ.d2s(param[3]));
		global=global.replace("e", IJ.d2s(param[4]));
		global=global.replace("f", IJ.d2s(param[5]));
		global=global.replace("xxx", "exp");
		return global;
	}
	double [] getInitParameters(){
		double [] param=new double [6];
		ArrayStatistics statX=new ArrayStatistics(super.x);
		ArrayStatistics statY=new ArrayStatistics(super.y);
		param[0]=statY.getMin();
		param[1]=statY.getMax();
		param[2]=statX.getMean();
		param[3]=statX.getMax()/4;
		param[4]=statX.getMax()/4;
		param[5]=statX.getMax()/4;
		
		return param;
		
	}
	@Override
	public double userFunction(double[] p, double x) {
		double a=(1/(1+Math.pow(Math.E,-1*(x-p[2]+(p[3]/2))/p[4])));
		double b=(1-(1/(1+Math.pow(Math.E,-1*(x-p[2]-(p[3]/2))/p[5]))));
		return p[0]+p[1]*a*b; 
		
	}
	}

/*	
	void fit(boolean show) {
		ArrayStatistics as=new ArrayStatistics(y);
		double [] intParam= {
				as.getMin(),
				as.getMax()-as.getMin(),
				new ArrayStatistics(x).getMean(),
				2,2,2,2};
		
		double [] intParam= {10000,10000, 510,600,80,150};
		
		CurveFitter cf=new CurveFitter(x, y);
		
		cf.doCustomFit(fitFunction, intParam, false);
//		this.parameters=cf.getParams();
//		this.parameters[6]=cf.getRSquared();
//		this.fwhm=calcFWHM(this.parameters[3]);
		
		if (show) cf.getPlot().show();
		
		
	}
	public void fixAmplitude(double amplitude) {
		this.fitFunction.replace("b", ""+amplitude);
	}
//	public double [] getResults(boolean show) {
//		fit(show);
//		return parameters;
//	}
	
	public double getFWHM() {
		return this.fwhm;
	}
	public double calcFWHM(double sigma) {
		
		return 2*sigma*Math.sqrt(Math.log(4));
	}
*/	
	