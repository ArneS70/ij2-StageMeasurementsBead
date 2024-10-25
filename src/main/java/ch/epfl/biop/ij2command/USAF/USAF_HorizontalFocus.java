package ch.epfl.biop.ij2command.USAF;



import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ch.epfl.biop.ij2command.USAF.HorizontalAnalysis.Builder;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import net.imagej.ImageJ;

		
	@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF Horizontal Foccus")
		public class USAF_HorizontalFocus implements Command  {
		
		
//		protected USAF_Analysis horizontalFocus;
//		protected HorizontalLineAnalyser horizontalLineAnalyser;
		
		@Parameter(label="number of focus points")
		int repetition;
		
		@Parameter(label="z-stack Start")
		int startZ;
		
		@Parameter(label="z-stack End")
		int stopZ;
		
		@Parameter(label="z-stack Step")
		int stepZ;
		
		@Parameter(label="Use entire stack?")
		boolean allStack;
		
		@Parameter(label="Show Fit window?")
		boolean showPlot;
		
		@Parameter(label="Save Fit window?")
		boolean savePlot;

		@Parameter(label="Show result tables?")
		boolean showTable;
		
		@Parameter(label="Save result tables?")
		boolean saveTable;
		
		@Override
		public void run() {
			
			ImagePlus imp=WindowManager.getCurrentImage();	
			if (imp!=null){
				HorizontalAnalysis analysis=new HorizontalAnalysis.Builder(imp).setStartZ(startZ).setStepZ(stepZ).setStopZ(stopZ).
																				setRepetition(repetition).
																				setEntireStack(allStack).
																				savePLot(savePlot).showPlot(showPlot).
																				saveTables(saveTable).showTables(showTable).build();
				
				HorizontalLineFocusAnalysis horizontal=new HorizontalLineFocusAnalysis(analysis);
				horizontal.checkParameters();
				horizontal.run();
				
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
			IJ.run("Bio-Formats", "open=N:/temp-Arne/StageTest/240923/USAF_30LP.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
//			IJ.run("Bio-Formats", "open=D:/01-Data/StageMeasurements/240812/USAF_10x_Tilt05_horizizontal.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
//			IJ.run("Bio-Formats", "open=D:/01-Data/StageMeasurements/240510/SmallSubstack.tif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack");
			ij.command().run(USAF_HorizontalFocus.class, true);
		}
		
}
//	horizontalFocus=new HorizonzalFocusAnalysis(imp,repetition,start,end,step,lineLength,allStack, showFit,savePlot,saveTable);
//	horizontalFocus.run();
//	HorizontalLineAnalyser hlanalyser=new HorizontalLineAnalyser(imp);
//	hlanalyser.stackCenter=horizontalFocus.stackCenter;
//	horizontalFocus.run();

	/*				
	parameters=new int [] {start,end,repetition};
	this.getSummaryTable();
	this.fileInput=WindowManager.getCurrentImage();
	
	if (fileInput==null) {
		//IJ.run("Bio-Formats", "open="+fileInput+" color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
		//imp=WindowManager.getCurrentImage();
		IJ.log("Please provide an image");
		return;
	}
	
	if (fileInput.getNSlices()==1) {
		IJ.log("Please provide an z-stack");
		return;
	}
	
	stack=fileInput.getStack();
	cal=fileInput.getCalibration();
	logFileNames();
	if (fileInput.getNFrames()>1) {
		HorizontalFocusTimelapse hft=new HorizontalFocusTimelapse(fileInput.getZ(),fileInput.getT());
		hft.analyseTimeLapse();
	} else {
	if (fileInput!=null) {
		FocusAnalyser fa=new FocusAnalyser();
		HorizontalLineAnalyser hla=new HorizontalLineAnalyser(fileInput);
		
		int z=fileInput.getNSlices();
							
		if (fileInput.getRoi()==null) {hla.setHorizontalLine();fa=new FocusAnalyser(fileInput,hla.getHorizontalLIne());}
		Roi roi=fileInput.getRoi();
		
		if (roi!=null ) {
			if(roi.isLine()) {
				fa=new FocusAnalyser(fileInput,(Line)roi);
				this.focusLine=(Line)roi;
				
			} else {
				hla.setHorizontalLine();
				fa=new FocusAnalyser(fileInput,hla.getHorizontalLIne());
			}
		}
		fa=new FocusAnalyser(fileInput,(Line)roi);
//		int[] param=setStackSize(imp);
//		fa.setStart(param[0]);
//		fa.setEnd(param[1]);
		fa.setStart(1);
		fa.setEnd(z);
		fa.setStep(step);
		LogToTable(fileName);
		fa.analyseHorizontalLine(repetition,lineLength);
		fitTableResults(fa);
		if (save) saveResults();
	}}
*/
	/*		void logFileNames() {
	IJ.log("===============================================================");
	this.filePath=IJ.getDirectory("file");
	this.fileName=fileInput.getTitle();
	
	if (fileName.startsWith(filePath)) 
		this.fileName=fileInput.getTitle().substring(filePath.length());
	
	
	IJ.log("File: "+fileName);
	IJ.log("Path: "+filePath);
}

/*		int [] setStackSize(ImagePlus stack) {

	
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

private void getSummaryTable() {
	if (WindowManager.getWindow(this.titleSummary)==null) {
		ResultsTable focusResults=new ResultsTable();
		focusResults.show(this.titleSummary);
		this.counter=0;
		return;
	};
	this.summaryResults=ResultsTable.getResultsTable(this.titleSummary);
	this.counter=this.summaryResults.getCounter();
}
void LogToTable(String file) {
	
	ResultsTable focus=ResultsTable.getResultsTable(this.titleSummary);
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
	
	
	focus.show(this.titleSummary);
	
}


void saveResults() {
	
	int n=this.fileName.indexOf(".");
	String saveName=this.fileName.substring(0, n);
	
//	n=this.filePath.indexOf(saveName);
//	String savePath=this.filePath.substring(0, n);
	ResultsTable rt=ResultsTable.getResultsTable(this.titleResults);
	rt.save(filePath+saveName+"_TableFits_"+IJ.pad(counter, 4)+".csv");

	
//	rt=ResultsTable.getResultsTable("Horizontal Focus");
//	rt.save(filePath+fileName+"_HorizontalFocus_"+IJ.pad(counter, 4)+".csv");
//	WindowManager.getFrame("Horizontal Focus").dispose();
	
	
	
	
	
}
void closeNonImageWindows() {
	Window [] win=WindowManager.getAllNonImageWindows();
	int num=win.length;
	
	for (int i=0;i<num;i++) {
		win[i].dispose();
	}
}
void setStart(int value) {
	this.start=value;
}
void setend(int value) {
	this.end=value;
}
*/
