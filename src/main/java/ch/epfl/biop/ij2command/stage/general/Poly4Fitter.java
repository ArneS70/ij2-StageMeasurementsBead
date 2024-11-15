package ch.epfl.biop.ij2command.stage.general;

import ij.measure.CurveFitter;

public class Poly4Fitter extends FitterFunction{
	
	public final static String [] header= {"a","b","c","d","sqdiff"};
	double [] parameters;
	public Poly4Fitter(double [] x,double [] y) {
		super(x,y,CurveFitter.POLY4);
//		this.parameters=super.getParameter();
		
	}

}
