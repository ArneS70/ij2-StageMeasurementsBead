package ch.epfl.biop.ij2command.stage.general;

import ij.measure.CurveFitter;

public class Poly4Fitter extends FitterFunction{
	
	public final static String [] header= {"a0","a1","a2","a3","a4","sqdiff"};
	double [] parameters;
	public Poly4Fitter(double [] x,double [] y) {
		super(x,y,CurveFitter.POLY4);
		super.numParam=5;
		super.globalFunction="y=a+b*(paramparam*Math.pow(x-c,1)param*Math.pow(x-c,2)param*Math.pow(x-c,3)param*Math.pow(x-c,4))";
//		this.parameters=super.getParameter();
		
	}

}
