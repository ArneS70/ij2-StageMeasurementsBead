package ch.epfl.biop.ij2command.stage.general;

import ij.measure.CurveFitter;

public class Poly3Fitter extends FitterFunction{
	
	public final static String [] header= {"a","b","c","d","sqdiff"};
	
	double [] parameters;
	
	public Poly3Fitter(double [] x,double [] y) {
		super(x,y,CurveFitter.POLY3);
		super.numParam=4;
		super.globalFunction="y=a+p1*x+p2*Math.pow(x-a,2)";
//		this.parameters=super.getParameter();
		
	}
	
}
