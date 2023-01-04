package ch.epfl.biop.ij2command;
import ij.gui.Line;
import ij.measure.CurveFitter;
import ij.process.ImageProcessor;


public class GaussFitter {

		private double [] x;
		private double [] y;
		private double [] parameters;
		private double fwhm;
		
		public final static String [] header= {"y0","height","center","sigma","R^2","FWHM"}; 
		
		GaussFitter(double []xvalues,double [] yvalues){
			this.x=xvalues;
			this.y=yvalues;
			fitGauss();
		}
		private void fitGauss() {
			
			String fitFunction="y=a+b*exp(-1*pow(abs(x-c),2)/(2*pow(d,2)))";
			
			ArrayStatistics as=new ArrayStatistics(y);
			double [] intParam= {
					as.getMean(),
					as.getMax()-as.getMin(),
					new ArrayStatistics(x).getMean(),
					2,2};
			
			CurveFitter cf=new CurveFitter(x, y);
			
			cf.doCustomFit(fitFunction, intParam, false);
			this.parameters=cf.getParams();
			this.parameters[4]=cf.getRSquared();
			this.fwhm=calcFWHM(this.parameters[3]);
			
//			cf.getPlot().show();
			
			
		}
		public double [] getResults() {
			return parameters;
		}
		
		public double getFWHM() {
			return this.fwhm;
		}
		public double calcFWHM(double sigma) {
			
			return 2*sigma*Math.sqrt(Math.log(4));
		}
	}


