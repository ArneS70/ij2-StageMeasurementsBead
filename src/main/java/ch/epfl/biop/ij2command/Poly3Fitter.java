package ch.epfl.biop.ij2command;

import ij.measure.CurveFitter;

public class Poly3Fitter extends FitterFunction{
	
	public final static String [] header= {"a","b","c","d","sqdiff"};
	double [] parameters;
	Poly3Fitter(double [] x,double [] y) {
		super(x,y,CurveFitter.POLY3);
//		this.parameters=super.getParameter();
		
	}

}
