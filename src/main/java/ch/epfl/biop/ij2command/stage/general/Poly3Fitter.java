package ch.epfl.biop.ij2command.stage.general;

import ij.measure.CurveFitter;

public class Poly3Fitter extends FitterFunction{
	
	public final static String [] header= {"a","b","c","d","sqdiff"};
	
	double [] parameters;
	
	public Poly3Fitter(double [] x,double [] y) {
		super(x,y,CurveFitter.POLY3);
		super.numParam=4;
		super.globalFunction="y=a+b*(paramparam*pow(x-c,1)param*pow(x-c,2)param*pow(x-c,3))";
//		this.parameters=super.getParameter();
		
	}
	
}
