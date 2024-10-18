package ch.epfl.biop.ij2command.stage.general;
import ij.gui.Line;
import ij.measure.CurveFitter;
import ij.process.ImageProcessor;


public class GaussFitter extends FitterFunction{

		
		private double fwhm;
		
		public final static String [] header= {"y0","height","center","sigma","R^2","FWHM"};
		private String fitFunction="y=a+b*exp(-1*pow(abs(x-c),2)/(2*pow(d,2)))";
		
		public GaussFitter(double []xvalues,double [] yvalues){
			super(xvalues,yvalues,FitterFunction.Gauss);
		}
		
		public String fixAmplitude(double amplitude) {
			return this.fitFunction.replace("b", ""+amplitude);
		}
		
		public String fixOffset(double amplitude) {
			return this.fitFunction.replace("a", ""+amplitude);
		}
		void initParameters() {
			double []xStat=super.getMeanMinMax(super.x);
			double []yStat=super.getMeanMinMax(super.y);
			super.setInitParameters(new double [] {yStat[1],yStat[2]-yStat[1],xStat[0],2,2});
		}
		public double getFWHM() {
			return this.fwhm;
		}
		public double calcFWHM(double sigma) {
			return 2*sigma*Math.sqrt(Math.log(4));
		}
		public double [] getResults() {
			initParameters();
			return super.getParameter(fitFunction);
		};
		double [] getResults(String custom) {
			initParameters();
			return super.getParameter(custom);
		};
	}


