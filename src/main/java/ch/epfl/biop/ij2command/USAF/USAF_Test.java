package ch.epfl.biop.ij2command.USAF;

import java.awt.Color;
import java.util.Vector;

import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import ch.epfl.biop.ij2command.stage.general.ArrayStatistics;
import ch.epfl.biop.ij2command.stage.general.Asym2SigFitter;
import ch.epfl.biop.ij2command.stage.general.Asym2SigFitterFixed;
import ch.epfl.biop.ij2command.stage.general.FitterFunction;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Line;
import ij.gui.Plot;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import net.imagej.ImageJ;

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF Test")
public class USAF_Test implements Command{

	@Override
	public void run() {
		ImagePlus imp=WindowManager.getCurrentImage();
		ImageProcessor ip=imp.getProcessor();
		
		int width=imp.getWidth();
		int height=imp.getHeight();
		int slices=imp.getNSlices();
		ImageProcessor outMean=ip.createProcessor(width, slices);
		ImageProcessor outStdev=ip.createProcessor(width, slices);
		Vector <double []>lineProfilesMax=new Vector <double []>();
		Vector <double []>lineProfilesMean=new Vector <double []>();
		
		
		double [] x=new double [width];
		
		for (int s=1;s<=slices;s++) {
			imp.setSlice(s);
			ip=imp.getProcessor();
			
			double [] profileMean= new double [width];
			double [] profileMax= new double [width];
			
			for (int i=0;i<width;i++) {
				double [] line=ip.getLine(i, 0, i, height);
				if (s==1) x[i]=i*imp.getCalibration().pixelWidth;
				profileMean[i]=new ArrayStatistics(line).getMean();
				profileMax[i]=new ArrayStatistics(line).getMax();
				outMean.putPixelValue(i,s-1, new ArrayStatistics(line).getMean());
				outStdev.putPixelValue(i,s-1,new ArrayStatistics(line).getMax());
			}
			if (s==1) lineProfilesMax.add(x);
			lineProfilesMax.add(profileMax);
			lineProfilesMean.add(profileMean);
		}
		
		int size=lineProfilesMax.size();
		ResultsTable results=new ResultsTable();
		
		for (int s=1;s<size;s++) {
			CurveFitter cf=new CurveFitter(lineProfilesMax.get(0),lineProfilesMax.get(s));
			double min=new ArrayStatistics(lineProfilesMax.get(s)).getMin();
			double max=new ArrayStatistics(lineProfilesMax.get(s)).getMin();
			double center=new ArrayStatistics(lineProfilesMax.get(0)).getMax()/2;
			cf.setInitialParameters(new double [] {min,10,center,max});
			cf.doFit(CurveFitter.RODBARD);
//			cf.getPlot().show();
			double [] param=cf.getParams();
			results.addRow();
			for (int p=0;p<param.length;p++) {
				results.addValue("p"+p, param[p]);
			}
			
		}
		results.show("FitResults");
		
		
	}
	

	//		
	Plot plot=new Plot("Profile", "x axis", "y axis");
//		plot.addPoints(x, profile, Plot.CIRCLE);
//		plot.show();
/*		
		
		ImageStack stack=new ImageStack(640,480);
		ImagePlus imp=WindowManager.getCurrentImage();
		Line l=(Line)imp.getRoi();
		double []profile=imp.getProcessor().getLine(l.x1d, l.y1d, l.x2d, l.y2d);
		int len=profile.length;
		double []x=new double[len];
		
		for (int i=0;i<len;i++) {
			x[i]=i*imp.getCalibration().pixelWidth;
		}
		
		for (int i=0;i<20;i++) {
			FitterFunction fit=FitterFunction.getFitFunc(x,profile,"AsymGauss");
			double p=Math.pow(1.2, 20-i);
			IJ.log(i+"  p="+p);
			double [] results=fit.getFitResults(new double[] {p,p,p,p,p,p});
			stack.addSlice(fit.getPlot().getImagePlus().getProcessor());
			
		}
		new ImagePlus ("Stack",stack).show();
		
		LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer() {
	 		// Override your objective function here
	 		public void setValues(double[] parameters, double[] values) {
	 			values[0] = parameters[0] * 0.0 + parameters[1];
	 			values[1] = parameters[0] * 2.0 + parameters[1];
	 		}
	 	};
	 
	 	// Set solver parameters
	 	optimizer.setInitialParameters(new double[] { 0, 0 });
	 	optimizer.setWeights(new double[] { 1, 1 });
	 	optimizer.setMaxIteration(100);
	 	optimizer.setTargetValues(new double[] { 5, 10 });
	 
	 	optimizer.run();
	 
	 	double[] bestParameters = optimizer.getBestFitParameters();
		
		
		
		Asym2SigFitter firstFit=new Asym2SigFitter(x, profile);
		
		
		
		for (int s=1;s<=3;s++) {
			IJ.log("Slice   :"+s+"    started:");
			imp.setSlice(s);
			profile=imp.getProcessor().getLine(l.x1d, l.y1d, l.x2d, l.y2d);
			Asym2SigFitterFixed fit=new Asym2SigFitterFixed(x, profile);
			fit.fit();
			IJ.log("Fit Done");
			double [][] func=fit.getFunctionValues(x,fit.getParameters());
			Plot plot=new Plot("A","B","C");
			plot.setSize(640, 480);
			plot.addPoints(x, profile, Plot.CIRCLE);
			plot.setColor(new Color(255,0,0));
			plot.draw();
			plot.setLineWidth(3);
			plot.addPoints(func[0], func[1], Plot.LINE);
			stack.addSlice(plot.getImagePlus().getProcessor());
		}
		new ImagePlus("Fit Plots",stack).show();
	}
*/
	public static void main(final String... args) throws Exception {
		// create the ImageJ application context with all available services
				
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
		
		//IJ.run("Bio-Formats", "open=N:/temp-Arne/StageTest/240923/USAF_30LP.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
		//IJ.run("Bio-Formats", "open=D:/01-Data/StageMeasurements/240812/USAF_10x_Tilt05_horizizontal.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
		ij.command().run(USAF_Test.class, true);
	}}
