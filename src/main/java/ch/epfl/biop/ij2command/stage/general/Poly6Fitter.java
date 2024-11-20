package ch.epfl.biop.ij2command.stage.general;

import ij.measure.CurveFitter;

public class Poly6Fitter extends FitterFunction{
	
	public final static String [] header= {"a","b","c","d","sqdiff"};
	double [] parameters;
	
	public Poly6Fitter(){
		
	}
	public Poly6Fitter(double [] x,double [] y) {
		super(x,y,CurveFitter.POLY3);
//		this.parameters=super.getParameter();
		
	}
	
}
