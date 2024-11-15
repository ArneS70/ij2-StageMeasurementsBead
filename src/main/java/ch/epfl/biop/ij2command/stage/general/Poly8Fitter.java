package ch.epfl.biop.ij2command.stage.general;


public class Poly8Fitter extends FitterFunction{
	
	public final static String [] header= {"y0","a1","a2","a3","a4","a5","a6","a7","a8","sqdiff","R^2"};
	double [] parameters;
	private Poly8Fitter(double [] x,double [] y) {
		super(x,y,FitterFunction.POLY8);
//		this.parameters=super.getParameter();
		
	}

}