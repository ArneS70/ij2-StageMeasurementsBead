package ch.epfl.biop.ij2command.stage.general;

import ij.measure.CurveFitter;

public class SuperFunction {
	private String name;
	protected String globalFunction;
	protected int method;
	protected boolean isCustom;
	
	SuperFunction(String name){
		this.name=name;
	}
	 String getName() {
		 return this.name;
	 }
	 int getMethod() {
		 return method;
	 }
	 String getGlobalFunction() {
		 return globalFunction;
	 }
	 double [] doFit(double []x,double []y) {
		 CurveFitter cf=new CurveFitter(x,y);
		 if (!isCustom) cf.doFit(this.method);
		 else cf.doCustomFit(this.globalFunction, new double []{1, 1,1},false);
		 return cf.getParams();
	 }
}
