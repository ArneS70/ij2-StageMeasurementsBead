package ch.epfl.biop.ij2command;

import java.awt.Window;
import java.io.File;
import java.io.IOException;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Line;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import loci.formats.FormatException;
import net.imagej.ImageJ;

		
	@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF Horizontal Foccus")
		public class USAF_HorizontalFocus implements Command {

//		@Parameter(style="open")
//	    File fileInput;
			
		@Parameter(label="number of focus points")
		int repetition;
		
//		@Parameter(label="z-stack Start")
		int start;
		
//		@Parameter(label="z-stack End")
		int end;
		
		@Parameter(label="z-stack Step")
		int step;
		
		@Parameter(label="Analysis line length (vetical)")
		int lineLength;
		
		@Parameter(label="Show Fit window?")
		boolean showFit;
		
		@Parameter(label="Save Fit window?")
		boolean saveFitWindow;

		@Parameter(label="Save result tables?")
		boolean save;
		
//		@Parameter(label="Results Index")
//		String text;
		
//		@Parameter(label="Variable Line length?")
//		boolean lineOptimize;
		
		Calibration cal;
		ResultsTable focusResults;
		ResultsTable fitResults=null;
		FocusAnalyser fa=null;
		ImageStack stack;
		String fileName, filePath;
		int counter;
		Plot focusFit;
		Line focusLine;
		
		@Override
		public void run() {
				
				
				
				
				this.getResultsTable();
				
				ImagePlus imp=WindowManager.getCurrentImage();
				
				if (imp==null) {
					//IJ.run("Bio-Formats", "open="+fileInput+" color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
					//imp=WindowManager.getCurrentImage();
					IJ.log("Please provide an image");
					return;
				}
				
				if (imp.getNSlices()==1) {
					IJ.log("Please provide an z-stack");
					return;
				}
				stack=imp.getStack();
				cal=imp.getCalibration();
				
				IJ.log("===============================================================");
				this.filePath=IJ.getDirectory("file");
				this.fileName=imp.getTitle();
				
				if (fileName.startsWith(filePath)) 
					this.fileName=imp.getTitle().substring(filePath.length());
				
				
				IJ.log("File: "+fileName);
				IJ.log("Path: "+filePath);
				
				if (imp!=null) {
					FocusAnalyser fa=new FocusAnalyser();
					HorizontalLineAnalyser hla=new HorizontalLineAnalyser(imp);
					hla.setHorizontalLine();
					
					
					int stack=imp.getImageStackSize();
					int z=imp.getNSlices();
					int frames=imp.getNFrames();
					
					if (imp.getRoi()==null) fa=new FocusAnalyser(imp,hla.getHorizontalLIne());
					Roi roi=imp.getRoi();
					
					
					if (roi!=null ) {
						if(roi.isLine()) {
							this.focusLine=(Line)roi;
							fa=new FocusAnalyser(imp,(Line)roi);
//							int[] param=setStackSize(imp);
//							fa.setStart(param[0]);
//							fa.setEnd(param[1]);
							fa.setStart(1);
							fa.setEnd(z);
							fa.setStep(step);
							LogToTable(fileName);
							fa.analyseHorizontalLine(repetition,lineLength);
							fitTableResults(fa);
							if (save) saveResults();
							
						
						}
					}	
						
					
				} 
		    
			
		}
		
		int [] setStackSize(ImagePlus stack) {

			
			Line line= (Line)stack.getRoi();
			int width=stack.getWidth();
			int stackSize=stack.getStackSize();
			double difMin=0;
			int min=0;
			for (int i=1;i<=stackSize-1;i+=20) {
				stack.setSliceWithoutUpdate(i);
				ImageProcessor ip=stack.getProcessor();
				double [] profileLeft=ip.getLine(0, line.y1d, 20, line.y2d);
				double [] profileRight=ip.getLine(width-20, line.y1d, width-1, line.y2d);
				double diff=Math.abs(new ArrayStatistics(profileLeft).getMean()-new ArrayStatistics(profileRight).getMean());
				if (i==1) {difMin=diff;}
				else {
					if (diff<difMin) 
						{difMin=diff; min=i;}
				}
				
			}
			if (difMin<stackSize/2) {
				start=1;end=2*min;
			} else {
				
				end=stackSize-1;start=stackSize-2*min;
			}
			
	
			return new int []{start,end};
		}
		
		private void getResultsTable() {
			if (WindowManager.getWindow("Focus Results")==null) {
				ResultsTable focusResults=new ResultsTable();
				focusResults.show("Focus Results");
				this.counter=0;
				return;
			};
			this.focusResults=ResultsTable.getResultsTable("Focus Results");
			this.counter=this.focusResults.getCounter();
		}
		void LogToTable(String file) {
			
			ResultsTable focus=ResultsTable.getResultsTable("Focus Results");
			focus.addRow();
			focus.addValue("#", this.counter);
			focus.addValue("File", file);
			focus.addValue("Repetition", this.repetition);
			focus.addValue("z step", this.step);
			focus.addValue("line length", this.lineLength);
			focus.addValue("x1", this.focusLine.x1d);
			focus.addValue("y1", this.focusLine.y1d);
			focus.addValue("x2", this.focusLine.x2d);
			focus.addValue("y2", this.focusLine.y2d);
			
			
			focus.show("Focus Results");
			
		}
		void fitTableResults(FocusAnalyser fa) {		
			
			TableFitter tableFit=new TableFitter(fa.getFocusMap());
			tableFit.fitTable(CurveFitter.POLY5);
			tableFit.getFitResults().show("Table Fit Results");
			
			int last=tableFit.getFitResults().getLastColumn();
			
			CurveFitter cf=new CurveFitter(tableFit.getFitResults().getColumnAsDoubles(0),tableFit.getFitResults().getColumnAsDoubles(last));
			cf.doFit(CurveFitter.STRAIGHT_LINE);
			double [] param=cf.getParams();
			
			ResultsTable focus=ResultsTable.getResultsTable("Focus Results");
			
			IJ.log("Focus shift z-axis  per slice: "+param[1]);
			focus.addValue("Focus shift per slice/um", param[1]);
			
			double zShift=param[1]*cal.pixelDepth;
			IJ.log("Focus shift z-axis  absolut: "+zShift);
			focus.addValue("Focus shift absolut", zShift);
			
			
			double slope=param[1]*fa.getZstep();
			IJ.log("Slope: "+slope);
			focus.addValue("Slope", slope);
			double angle=180*Math.atan(slope)/Math.PI;
			IJ.log("angle/deg: "+angle);
			focus.addValue("angle/deg", angle);
			IJ.log("R^2: "+cf.getFitGoodness());
			focus.addValue("R^2", cf.getFitGoodness());
			
			focus.show("Focus Results");
			ImagePlus fitWin;
			if (showFit) {
				this.focusFit=cf.getPlot();
				fitWin=focusFit.show().getImagePlus();
				fitWin.setTitle(fileName+" "+IJ.pad(counter, 3)+".tif");
				fitWin.show();
				
			}
			if (saveFitWindow) {
				
				IJ.save(filePath+fileName+" "+IJ.pad(counter, 3)+".tif");
				WindowManager.getFrame(fileName+" "+IJ.pad(counter, 3)+".tif").dispose();
				
			}
		}
		
		void saveResults() {
			
			int n=this.fileName.indexOf(".");
			String saveName=this.fileName.substring(0, n);
			
//			n=this.filePath.indexOf(saveName);
//			String savePath=this.filePath.substring(0, n);
			ResultsTable rt=ResultsTable.getResultsTable("Table Fit Results");
			rt.save(filePath+saveName+"_TableFits_"+IJ.pad(counter, 3)+".csv");
			WindowManager.getFrame("Table Fit Results").dispose();
			
			
			rt=ResultsTable.getResultsTable("Horizontal Focus");
			rt.save(filePath+fileName+"_HorizontalFocus_"+IJ.pad(counter, 3)+".csv");
			WindowManager.getFrame("Horizontal Focus").dispose();
			
			
			
			
			
		}
		void closeNonImageWindows() {
			Window [] win=WindowManager.getAllNonImageWindows();
			int num=win.length;
			
			for (int i=0;i<num;i++) {
				win[i].dispose();
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
			IJ.run("Bio-Formats", "open=D:/01-Data/StageMeasurements/240812/USAF_10x_Tilt05_horizizontal.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");

			ij.command().run(USAF_HorizontalFocus.class, true);
		}
		
}
