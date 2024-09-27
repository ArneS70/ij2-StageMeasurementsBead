package ch.epfl.biop.ij2command;

import ij.measure.CurveFitter;

public class Poly3Fitter extends FitterFunction{

	Poly3Fitter(double [] x,double [] y) {
		super(x,y,CurveFitter.POLY3);
		double [] params=super.getParameter();
		
	}

}
