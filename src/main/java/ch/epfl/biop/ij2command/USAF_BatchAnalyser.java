package ch.epfl.biop.ij2command;
import java.io.File;
import java.util.Arrays;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Line;
import ij.gui.Roi;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.plugin.filter.MaximumFinder;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.util.ArrayUtil;
import net.imagej.ImageJ;

/**
* 
* The pom file of this project is customized for the PTBIOP Organization (biop.epfl.ch)
* <p>
* The code here is tracking a fluorescent bead in 3D.
* </p>
*/
		@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF Traget Batch Analyser")
		public class USAF_BatchAnalyser implements Command {
		@Parameter(label="Length line")
		int lineLength;
		@Parameter(label="Repetitions")
		int repetitions;
		
		@Override
		public void run() {
			IJ.run("Bio-Formats Importer", "open=N:/temp-Arne/StageTest/USFA_Slide_Exp13.lsm color_mode=Default rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack");
			ImagePlus imp=WindowManager.getCurrentImage();
			ImageProcessor ip=imp.getProcessor();
			ImageStatistics stats=new ImageStatistics();
			for (int pos=0;pos<repetitions;pos++) {
				Roi line=new Line(94+pos*lineLength, 500, 94+pos*lineLength, 540);
				imp.setRoi(line);
				stats=imp.getStatistics();
				IJ.log("Mean: "+stats.mean);
				IJ.log("Max: "+stats.max);
			}
		/* 
		if (analyseStack) slices=imp.getStackSize();
		ImageStack stack=new ImageStack();
		ResultsTable rt=new ResultsTable();
		for (int i=0; i<slices;i++) {
		imp.setSliceWithoutUpdate(i);
		IJ.log(""+i);
		imp.setRoi(rectangle);
		ImageProcessor ip=imp.getProcessor().crop();
		ip.rotate(angle);
		//ip.findEdges();
		double[] line=meanLine(ip);
		int [] points=MaximumFinder.findMaxima(line, 200, false);
		Arrays.sort(points);
		double [] fitResults=fitLine(points);
		rt.addRow();
		rt.addValue("Slice",i);
		rt.addValue("a", fitResults[0]);
		rt.addValue("b", fitResults[1]);
		rt.addValue("R^2", fitResults[2]);
		//stack.addSlice(ip);
		}
		//ImagePlus crop=new ImagePlus("Crop",stack);
		//crop.show();
		//rt.show("FitResults_"+imp.getTitle());
		rt.show("FitResults_"+imp.getTitle());
		*/ 
		}
		double [] meanLine(ImageProcessor ip) {
			int w=ip.getWidth();
			int h=ip.getHeight();
			double []line=new double [ip.getWidth()];
			for (int nx=0;nx<w;nx++) {
				for (int ny=0;ny<h;ny++) {
					line[nx]+=ip.getPixel(nx, ny);
				}
			}
		return line;
		}
		
		double [] fitLine(int []max) {
			int length=max.length;
			final int fitType=CurveFitter.STRAIGHT_LINE;
			final int numParam=CurveFitter.getNumParams(fitType);
			double []results= new double [numParam+1];
			double []x=new double[length];
			double []y=new double[length];
			for (int i=0;i<length;i++) {
				x[i]=i;
				y[i]=(double)max[i];
			}
		
			CurveFitter cf=new CurveFitter (x,y);
			cf.doFit(fitType);
			//cf.getPlot().show();
			for (int i=0;i<numParam;i++) {
				results[i]=cf.getParams()[i];
			}
			results[numParam]=cf.getRSquared();
			return results;
		}
/**
* This main function serves for development purposes.
* It allows you to run the plugin immediately out of
* your integrated development environment (IDE).
*
* @param args whatever, it's ignored
* @throws Exception
*/
		public static void main(final String... args) throws Exception {
			// create the ImageJ application context with all available services
			final ImageJ ij = new ImageJ();
			ij.ui().showUI();
			ij.command().run(USAF_BatchAnalyser.class, true);
		}
}

