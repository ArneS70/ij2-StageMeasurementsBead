package ch.epfl.biop.ij2command;


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


	public class Asym2SigFitter {

			private double [] x;
			private double [] y;
			private double [] parameters;
			private double fwhm;
			
			public final static String [] header= {"y0","height","center","w1","w2","w3","R^2","FWHM"};
			private String fitFunction="y=a+b*(1/(1+exp(-1*(x-c+(d/2))/e)))*(1-(1/(1+exp(-1*(x-c-(d/2))/f))))";
					
			
			Asym2SigFitter(double []xvalues,double [] yvalues){
				this.x=xvalues;
				this.y=yvalues;
				
			}

			
			void fit(boolean show) {
				
				
				
				ArrayStatistics as=new ArrayStatistics(y);
/*				double [] intParam= {
						as.getMin(),
						as.getMax()-as.getMin(),
						new ArrayStatistics(x).getMean(),
						2,2,2,2};
*/				
				double [] intParam= {10000,10000, 510,600,80,150};
				
				CurveFitter cf=new CurveFitter(x, y);
				
				cf.doCustomFit(fitFunction, intParam, false);
				this.parameters=cf.getParams();
				this.parameters[6]=cf.getRSquared();
//				this.fwhm=calcFWHM(this.parameters[3]);
				
				if (show) cf.getPlot().show();
				
				
			}
			public void fixAmplitude(double amplitude) {
				this.fitFunction.replace("b", ""+amplitude);
			}
			public double [] getResults(boolean show) {
				fit(show);
				return parameters;
			}
			
			public double getFWHM() {
				return this.fwhm;
			}
			public double calcFWHM(double sigma) {
				
				return 2*sigma*Math.sqrt(Math.log(4));
			}
			
			Function fun = new Function(){
			    @Override
			    public double evaluate(double[] values, double[] parameters) {
			        double A = parameters[0];
			        double B = parameters[1];
			        double C = parameters[2];
			        double D = parameters[3];
			        double E = parameters[4];
			        double F = parameters[5];
			        
			        double x = values[0];
			        return A+B*(1/(1+Math.exp(-(1*x-C+(D/2))/E)))*(1-(1/(1+Math.exp(-(1*x-C-(D/2))/F))));
			    }
			    @Override
			    public int getNParameters() {
			        return 6;
			    }

			    @Override
			    public int getNInputs() {
			        return 1;
			    }
			};
			void fit(){
				
				double[][] xs = new double[x.length][1];
				for (int i=0;i<x.length;i++) {
					xs[i][0]=x[i];
				}
				
				Fitter fit=new MarquardtFitter(fun);
				fit.setData(xs, y);
				fit.setParameters(new double[]{10000,11000,500,150,80,150});

				fit.fitData();
				IJ.log(Arrays.toString(fit.getParameters()));
				
			}
		}

