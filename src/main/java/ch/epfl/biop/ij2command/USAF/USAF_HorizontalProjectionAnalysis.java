package ch.epfl.biop.ij2command.USAF;

import java.io.File;
import java.io.IOException;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ch.epfl.biop.ij2command.stage.general.FitterFunction;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Line;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import loci.formats.FormatException;
import net.imagej.ImageJ;

		
	@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF Horizontal Line Projection")
		public class USAF_HorizontalProjectionAnalysis implements Command {

			ImagePlus fileInput;
			
			int [] stackParam=new int[6];
			

//			@Parameter(style="open")
//		    File fileInput;
			
			@Parameter(label="Process entire Z-stack") 
			boolean  entireZStack;
			
			@Parameter(label="z-step",min="1")
			int stepZ;
			
			@Parameter(label="Process entire T-stack")
			boolean  entireTStack;
			
			@Parameter(label="T-step",min="1")
			int stepT;
			
			@Parameter(label="z-start",min="1")
			int startZ;
			
			@Parameter(label="z-stop",min="1")
			int stopZ;
			
			@Parameter(label="T-start",min="1")
			int startT;
			
			@Parameter(label="T-stop",min="1")
			int stopT;
			
			@Parameter(label="Fitting Fuction",choices= {"Poly3","Poly4","Poly6","Poly8","AsymGauss"})
			String fitFunc;

			@Parameter(label="Multi Thread Fit?")
			boolean multiThread;
			
			@Parameter(label="Show Focus Shift Plot")
			boolean showPlot;
		
			@Parameter(label="Show result tables?")
			boolean showTable;
			
			@Parameter(label="Show profile tables?")
			boolean showProfile;
			
//			@Parameter(label="Summarize results?")
//			boolean summarize;
			
			@Parameter(label="Save Plot?")
			boolean savePlot;
			
			@Parameter(label="Save result tables?")
			boolean saveTables;
			
			Line toAnalyse;

		@Override
		public void run() {
		
			ImagePlus imp=WindowManager.getCurrentImage();
			IJ.log(imp.getTitle());
			
			if (imp!=null){
				stackParam=new HorizontalAnalysisMethods().checkStackParameters(imp,entireZStack,entireTStack,new int [] {startZ,stopZ,stepZ,startT,stopT,stepT});
				
				HorizontalAnalysis analysis=new HorizontalAnalysis.Builder(imp).setStartZ(stackParam[0]).setStopZ(stackParam[1]).setStepZ(stackParam[2]).
																				setStartT(stackParam[3]).setStopT(stackParam[4]).setStepT(stackParam[5]).
																				savePLot(savePlot).showPlot(showPlot).setCalibration(imp.getCalibration()).
																				saveTables(saveTables).showTables(showTable).showProfile(showProfile).
																				fitFunc(fitFunc).multiThread(multiThread).
																				build();
																					
				HorizontalProjectionAnalysis project=new HorizontalProjectionAnalysis(analysis);
				project.run();
//				MultiThreadHLA horizontal=new MultiThreadHLA(analysis);
//				horizontal.run();
				
			}
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
			
			//IJ.run("Bio-Formats", "open=N:/temp-Arne/StageTest/240923/USAF_30LP.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
			//IJ.run("Bio-Formats", "open=D:/01-Data/StageMeasurements/240812/USAF_10x_Tilt05_horizizontal.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
			
			IJ.run("Bio-Formats", "open=[D:/01-Data/StageMeasurements/New folder/USAF_newHolderTimelapse03.lsm] color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
			ij.command().run(USAF_HorizontalLine.class, true);
		}
	}		

		