package ch.epfl.biop.ij2command.stage.general;



import ij.measure.CurveFitter;

public class Poly8Fitter extends FitterFunction{
	
	public final static String [] header= {"a","b","c","d","e","f","g","h","i","sqdiff"};
	double [] parameters;
	public Poly8Fitter(double [] x,double [] y) {
		super(x,y,FitterFunction.Poly8);
//		this.parameters=super.getParameter();
		
	}

}