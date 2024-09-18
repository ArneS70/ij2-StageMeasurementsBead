package ch.epfl.biop.ij2command;

import java.io.File;
import java.io.IOException;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Line;
import ij.gui.Roi;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import loci.formats.FormatException;
import net.imagej.ImageJ;

		
	@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF Horizontal Foccus")
		public class USAF_HorizontalFocus implements Command {
//			@Parameter(style="open")
//		    File fileInput;
			
//			@Parameter(label="Save result tables?")
//			boolean save;
		
		@Parameter(label="number of focus points")
		int repetition;
		
		@Parameter(label="z-stack Start")
		int start;
		
		@Parameter(label="z-stack End")
		int end;
		
		@Parameter(label="z-stack Step")
		int step;
		
		@Parameter(label="Show Fit window?")
		boolean showFit;

		@Override
		public void run() {
			
				ResultsTable fitResults=null;
				TableFitter tableFit=null;
				FocusAnalyser fa=null;
				ImagePlus imp=WindowManager.getCurrentImage();
				IJ.log("===============================================================");
				IJ.log("File: "+imp.getTitle());
				
				if (imp!=null) {
					
					Roi roi=imp.getRoi();
					
					if (roi!=null ) {
						if(roi.isLine()) {
							fa=new FocusAnalyser(imp,(Line)roi);
							fa.setStart(start);
							fa.setEnd(end);
							fa.setStep(step);
							fa.analyseLine(repetition,5);
						
						}
					}else {
						IJ.log("Auto line selection");
						IJ.exit();
						setLine(imp);
						this.run();
						
						
					}
				} else IJ.showMessage("Please provide an image");
		    
				
				tableFit=new TableFitter(fa.getFocusResults());
				tableFit.fitTable(CurveFitter.POLY5);
				tableFit.getFitResults().show("Table Fit Results");
				
				int last=tableFit.getFitResults().getLastColumn();
				
				CurveFitter cf=new CurveFitter(tableFit.getFitResults().getColumnAsDoubles(0),tableFit.getFitResults().getColumnAsDoubles(last));
				cf.doFit(CurveFitter.STRAIGHT_LINE);
				double [] param=cf.getParams();
				IJ.log("Focus shift z-axis  slice/um: "+param[1]);
				double slope=param[1]*fa.getZstep();
				IJ.log("Slope: "+slope);
				double angle=180*Math.atan(slope)/Math.PI;
				IJ.log("angle/deg: "+angle);
				IJ.log("R^2: "+cf.getFitGoodness());
				
				if (showFit) cf.getPlot().show();
		}
		void setLine(ImagePlus imp) {
			int slice=imp.getImageStackSize();
			imp.setSlice(slice/2);
			ImageProcessor ip_edge=imp.getProcessor().duplicate().convertToFloat();
			ip_edge.findEdges();
			LineAnalyser la=new LineAnalyser(new ImagePlus("Edges",ip_edge),1);
			Roi [] lines=la.findVerticalMaxima(10,400);
			int pos=1+lines.length/2;
			ImageProcessor ip=imp.getProcessor();
			ip.setRoi(lines[pos]);
			double mean1=ip.getStatistics().mean;
			
			ip.setRoi(lines[pos+1]);
			double mean2=ip.getStatistics().mean;
			//IJ.log("m1="+mean1+"    m2="+mean2);
			
			if (mean1>mean2)imp.setRoi(lines[pos]); 
			else imp.setRoi(lines[pos+1]);
			
			imp.updateAndDraw();
			
			
		}
		
/*		void saveResults() {
			String fileName=fileInput.getName();
			int n=fileName.indexOf(".");
			fileName=fileName.substring(0, n);
			String path=fileInput.getAbsolutePath();
			n=path.indexOf(fileName);
			path=path.substring(0, n);
			ResultsTable rt=ResultsTable.getResultsTable("Results x-Axis");
			rt.save(path+fileName+"_xAxis.csv");
			
			rt=ResultsTable.getResultsTable("Results y-Axis");
			rt.save(path+fileName+"_yAxis.csv");
			WindowManager.closeAllWindows();
			
		}	*/
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
			//IJ.run("Bio-Formats", "open=X:/StageTest/240812/UASF_10x_Tilt05_horizizontal.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_2");
			IJ.run("Bio-Formats", "open=N:/temp-Arne/StageTest/240812/UASF_10x_Tilt05_horizizontal.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");

			ij.command().run(USAF_HorizontalFocus.class, true);
		}
		
}
