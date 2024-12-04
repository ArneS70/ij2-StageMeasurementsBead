package ch.epfl.biop.ij2command.stage.general;

import ij.measure.UserFunction;

public class GlobalAsym2SigFitter extends FitterFunction implements UserFunction{

	private double w1,w2,w3;
	public GlobalAsym2SigFitter(double[] inputX, double[] inputY) {
		super(inputX, inputY, 99);
		// TODO Auto-generated constructor stub
	}
	public synchronized double [] getFitResults(double [] initialParam) {
		w1=initialParam[3];
		w2=initialParam[4];
		w3=initialParam[5];
//		cf.doCustomFit(function,new double [] {initParam[0],initParam[1],initParam[2]},false);
		cf.setMaxIterations(10000);
		cf.doCustomFit(this, 3, "", new double [] {initialParam[0],initialParam[1],initialParam[2]},null, false);
//		cf.doCustomFit(function,new double [] {1,1},false);
		double [] results=cf.getParams();
		int num=results.length;
		double [] allParam=new double [num+1];
		
		allParam[num]=cf.getRSquared();
		System.arraycopy(results, 0, allParam, 0, num);
		return allParam;
	
	}
	
	@Override
	public double userFunction(double[] p, double x) {
		double a=(1/(1+Math.pow(Math.E,-1*(x-p[2]+(w1/2))/w2)));
		double b=(1-(1/(1+Math.pow(Math.E,-1*(x-p[2]-(w1/2))/w3))));
		return p[0]+p[1]*a*b; 
		
	}
	
}
