package ch.epfl.biop.ij2command.stage.general;


	import java.util.Arrays;

import org.orangepalantir.leastsquares.Fitter;
import org.orangepalantir.leastsquares.Function;
import org.orangepalantir.leastsquares.fitters.LinearFitter;
import org.orangepalantir.leastsquares.fitters.MarquardtFitter;
import org.orangepalantir.leastsquares.fitters.NonLinearSolver;

import ij.IJ;
import ij.gui.Line;
	import ij.measure.CurveFitter;
	import ij.process.ImageProcessor;


	public class Asym2SigFitterFixed{ //extends FitterFunction{

			private double fwhm;
			private double [] parameters;
			private double [] x,y;
			Function func;
			
			public Asym2SigFitterFixed(double []x,double[]y){
				this.x=x;
				this.y=y;
			}	
			
			
			public Function fun = new Function(){
			    
				@Override
			    public double evaluate(double[] values, double[] parameters) {
			        double A = parameters[0];
			        double B = parameters[1];
			        double C = parameters[2];
//			        double D = parameters[3];
//			        double E = parameters[4];
//			        double F = parameters[5];
			        
			        double x = values[0];
			        return A+B*(1/(1+Math.exp(-(1*x-C+(parameters[3]/2))/parameters[4])))*(1-(1/(1+Math.exp(-(1*x-C-(parameters[3]/2))/parameters[5]))));
			    }
			    
			    public double [] getParameters() {
			    	return parameters;
			    }
			    public int getNParameters() {
			        return 6;
			    }

			    @Override
			    public int getNInputs() {
			        return 1;
			    }
			};
			public void fit(){
				
				double[][] xs = new double[x.length][1];
				for (int i=0;i<x.length;i++) {
					xs[i][0]=x[i];
				}
				
				Fitter fit=new MarquardtFitter(fun);
				fit.setData(xs, y);
				fit.setParameters(new double[]{10000,11000,500,150,80,150});

				fit.fitData();
				parameters=fit.getParameters();
				IJ.log(Arrays.toString(parameters));
				
			}
			public double [] getParameters(){
				return parameters;
			}
			public double[][] getFunctionValues(double []x, double[] param) {
				int len=x.length;
				double A=param[0];
				double B=param[1];
				double C=param[2];
				double D=param[3];
				double E=param[4];
				double F=param[5];
				
				double deltax=x[1]-x[0];
				int pos=len*5;
				double [][] function =new double [2][pos];
				
				for (int n=0;n<pos;n++) {
					double xval=n*deltax/5;
					function[0][n]=xval;
					function[1][n]=A+B*(1/(1+Math.exp(-(1*xval-C+(D/2))/E)))*(1-(1/(1+Math.exp(-(1*xval-C-(D/2))/F))));
				}
				return function;
			}
		}
	
	/*			
	public final static String [] header= {"y0","height","center","w1","w2","w3","R^2","FWHM"};
	private String fitFunction="y=a+b*(1/(1+exp(-1*(x-c+(d/2))/e)))*(1-(1/(1+exp(-1*(x-c-(d/2))/f))))";
			
	
	public Asym2SigFitter(double []xvalues,double [] yvalues){
		super(xvalues,yvalues,FitterFunction.AsymGauss);
	}

	void fit(boolean show) {
		ArrayStatistics as=new ArrayStatistics(y);
		double [] intParam= {
				as.getMin(),
				as.getMax()-as.getMin(),
				new ArrayStatistics(x).getMean(),
				2,2,2,2};
		
		double [] intParam= {10000,10000, 510,600,80,150};
		
		CurveFitter cf=new CurveFitter(x, y);
		
		cf.doCustomFit(fitFunction, intParam, false);
//		this.parameters=cf.getParams();
//		this.parameters[6]=cf.getRSquared();
//		this.fwhm=calcFWHM(this.parameters[3]);
		
		if (show) cf.getPlot().show();
		
		
	}
	public void fixAmplitude(double amplitude) {
		this.fitFunction.replace("b", ""+amplitude);
	}
//	public double [] getResults(boolean show) {
//		fit(show);
//		return parameters;
//	}
	
	public double getFWHM() {
		return this.fwhm;
	}
	public double calcFWHM(double sigma) {
		
		return 2*sigma*Math.sqrt(Math.log(4));
	}
*/
	
	