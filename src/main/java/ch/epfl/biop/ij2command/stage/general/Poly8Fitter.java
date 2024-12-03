package ch.epfl.biop.ij2command.stage.general;


public class Poly8Fitter extends FitterFunction{
	
	public final static String [] header= {"y0","a1","a2","a3","a4","a5","a6","a7","a8","sqdiff","R^2"};
	double [] parameters;
	Poly8Fitter(double [] x,double [] y) {
		super(x,y,FitterFunction.POLY8);
		super.numParam=9;
		super.globalFunction="y=a+b*(param*(x-c)param*Math.pow(x-c,2)param*Math.pow(x-c,3)param*Math.pow(x-c,4)param*Math.pow(x-c,5)param*Math.pow(x-c,6)param*Math.pow(x-c,7)param*Math.pow(x-c,8))";
//		this.parameters=super.getParameter();
		
	}

}