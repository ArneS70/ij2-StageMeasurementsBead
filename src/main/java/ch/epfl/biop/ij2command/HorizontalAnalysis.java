package ch.epfl.biop.ij2command;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Line;
import ij.measure.Calibration;
import ij.measure.ResultsTable;

public class HorizontalAnalysis {
	protected ImagePlus inputImage;
	protected Calibration cal;
	protected Line horizontalLine;
	protected ResultsTable analysisTable=new ResultsTable();
	protected String filePath,fileName;
	protected int start,end,zstep,lineLength,counter,stackCenter;
	protected boolean showFit,savePlot,saveTable,allStack,ignoreTime=false;
	protected ResultsTable summaryResults;
	
	HorizontalAnalysis(){
		
	}
	HorizontalAnalysis(ImagePlus imp){
		this.inputImage=imp;
		
	}
	public void getSummaryTable(String title) {
		if (WindowManager.getWindow(title)==null) {
			ResultsTable summaryResults=new ResultsTable();
			summaryResults.show(title);
			this.counter=0;
			return;
		};
		this.summaryResults=ResultsTable.getResultsTable(title);
		this.counter=this.summaryResults.getCounter();
	}
	void LogToTable(String title) {
		
		ResultsTable focus=ResultsTable.getResultsTable(title);
		if (focus==null) focus=new ResultsTable();
		focus.addRow();
		focus.addValue("#", this.counter);
		focus.addValue("File", fileName);
//		focus.addValue("Repetition", this.repetition);
		focus.addValue("z step", this.zstep);
		focus.addValue("z start", this.start);
		focus.addValue("z stop", this.end);
		focus.addValue("line length", this.lineLength);
		focus.addValue("x1", this.horizontalLine.x1d);
		focus.addValue("y1", this.horizontalLine.y1d);
		focus.addValue("x2", this.horizontalLine.x2d);
		focus.addValue("y2", this.horizontalLine.y2d);
		
		
		focus.show(title);
		
	}
	public boolean checkInputImage() {
		boolean check=true;
		if (inputImage==null) {
			//IJ.run("Bio-Formats", "open="+fileInput+" color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
			//imp=WindowManager.getCurrentImage();
			IJ.log("Please provide an image");
			check=false;
		}
		
		if (inputImage.getNSlices()==1) {
			IJ.log("Please provide an z-stack");
			check=false;
		}
		return check;
	}
	void logFileNames() {
		IJ.log("===============================================================");
		this.filePath=IJ.getDirectory("file");
		this.fileName=inputImage.getTitle();
		
		if (fileName.startsWith(filePath)) 
			this.fileName=inputImage.getTitle().substring(filePath.length());
		
		
		IJ.log("File: "+fileName);
		IJ.log("Path: "+filePath);
	}
	void setInputImage(ImagePlus imp) {
		inputImage=imp;
	}
	void setHorizontalLine(Line horizontal) {
		this.horizontalLine=horizontal;
	}
	ResultsTable getResultsTable() {
		return this.analysisTable;
	}
}
