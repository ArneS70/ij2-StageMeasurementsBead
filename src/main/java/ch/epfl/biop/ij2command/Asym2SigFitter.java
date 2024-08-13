package ch.epfl.biop.ij2command;


	import ij.gui.Line;
	import ij.measure.CurveFitter;
	import ij.process.ImageProcessor;


	public class Asym2SigFitter {

			private double [] x;
			private double [] y;
			private double [] parameters;
			private double fwhm;
			
			public final static String [] header= {"y0","height","center","w1","w2","w3","R^2","FWHM"};
			private String fitFunction="y=10000+b*(1/(1+exp(-1*x-c+(d/2)/e)))*(1-(1/(1+exp(-1*x-c-(d/2)/f))))";
					
			
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
				double [] intParam= {10000,510,600,80,150};
				
				CurveFitter cf=new CurveFitter(x, y);
				
				cf.doCustomFit(fitFunction, intParam, true);
				this.parameters=cf.getParams();
				this.parameters[5]=cf.getRSquared();
				this.fwhm=calcFWHM(this.parameters[3]);
				
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
		}

