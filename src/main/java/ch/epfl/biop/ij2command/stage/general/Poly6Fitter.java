package ch.epfl.biop.ij2command.stage.general;

import ij.measure.CurveFitter;

public class Poly6Fitter extends FitterFunction{
	
	public final static String [] header= {"a","b","c","d","sqdiff"};
	double [] parameters;
	
	
	public Poly6Fitter(double [] x,double [] y) {
		super(x,y,CurveFitter.POLY6);
		super.numParam=7;
		super.globalFunction="y=a+b*(paramparam*Math.pow(x-c,1)param*Math.pow(x-c,2)param*Math.pow(x-c,3)param*Math.pow(x-c,4)param*Math.pow(x-c,5)param*Math.pow(x-c,6))";
//		this.parameters=super.getParameter();
		
	}
	
}
