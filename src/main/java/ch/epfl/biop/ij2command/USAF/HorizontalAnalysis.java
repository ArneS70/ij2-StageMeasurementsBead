package ch.epfl.biop.ij2command.USAF;

import java.awt.Window;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Line;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

public class HorizontalAnalysis {
	
	//required parameters
	ImagePlus inputImage;
	private Line horizontalLine;
	//optional parameters
	private int repetition, startZ, stopZ,stepZ, startT,stopT,stepT;
	private boolean saveTables,showTables,savePlot,showPlot,showProfile;
	boolean summarize;
	
	//derived parameters (no input)
	protected String filePath,fileName;
	protected int lineWidth, counter,stackCenter, stackSlices; 
	double spacing;
	protected boolean allStack,ignoreTime=false,isTimeLapse=false;
	protected Calibration cal;
	protected ResultsTable analysisTable;
	
	
	
	
	protected ResultsTable summaryResults;
	
	
	private HorizontalAnalysis (Builder builder) {
		//required parameters
		this.inputImage=builder.inputImage;
		this.horizontalLine=builder.horizontalLine;
		
		//optional parameters
		this.repetition=builder.repetition;
		this.startZ=builder.startZ;
		this.stopZ=builder.stopZ;
		this.stepZ=builder.stepZ;
		this.startT=builder.startT;
		this.stopT=builder.stopT;
		this.stepT=builder.stepT;
		this.stackCenter=builder.stackCenter;
		this.allStack=builder.allStack;
		this.saveTables=builder.saveTables;
		this.showTables=builder.showTables;
		this.savePlot=builder.savePlot;
		this.showPlot=builder.showPlot;
		this.showProfile=builder.showProfile;
		this.summarize=builder.summarize;
		this.cal=builder.cal;
	}
	public ImagePlus getImage(){
		return inputImage;
	}
	public static class Builder{
		

		//required parameters
		private ImagePlus inputImage;
		
		//optional Parameters
		private Line horizontalLine;
		private int repetition;
		private int startZ, stopZ, stepZ, startT, stopT, stepT,stackCenter;
		private boolean allStack, saveTables,showTables,savePlot,showPlot,showProfile,summarize;
		private Calibration cal;
		private Builder built;
		
		public Builder(ImagePlus imp) {
			this.inputImage=imp;
			
		}
		public Builder repetition(int rep) {
			this.repetition=rep;
			return this;
		}
		public Builder setCalibration(Calibration cal) {
			this.cal=cal;
			return this;
		};
		public Builder showTables(boolean show) {
			this.showTables=show;
			return this;
		}
		public Builder summarize(boolean summarize) {
			this.summarize=summarize;
			return this;
		}
		public Builder showProfile(boolean profile) {
			this.showProfile=profile;
			return this;
		}
		public Builder showPlot(boolean show) {
			this.showPlot=show;
			return this;
		}
		public Builder saveTables(boolean save) {
			this.saveTables=save;
			return this;
		}
		public Builder savePLot(boolean save) {
			this.savePlot=save;
			return this;
		}
		public Builder setEntireStack(boolean all) {
			this.allStack=all;
			return this;
		}
		public Builder setRepetition(int rep) {
			this.repetition=rep;
			return this;
		}
		public Builder setStartZ(int start) {
			this.startZ=start;
			return this;
		}
		public Builder setStopZ(int stop) {
			this.stopZ=stop;
			return this;
		}
		public Builder setStepZ(int step) {
			this.stepZ=step;
			return this;
		}
		public Builder setStartT(int start) {
			this.startT=start;
			return this;
		}
		public Builder setStopT(int stop) {
			this.stopT=stop;
			return this;
		}
		public Builder setStepT(int step) {
			this.stepT=step;
			return this;
		}
		public void setSlice(int slice) {
			inputImage.setSlice(slice);
		}
		public Builder setStackCenter(int c) {
			this.stackCenter=c;
			return this;
		}
		public Builder setCalibration() {
			this.cal=inputImage.getCalibration();
			return this;
		}
		public HorizontalAnalysis build() {
			if (inputImage!=null) {
				setCalibration();
//				setStartZ(1);
//				setStopZ(inputImage.getNSlices());
				setStackCenter(startZ+((stopZ-startZ)/2));
			}
			return new HorizontalAnalysis(this);
		}
		
	}
	public void deleteRoi(){
		IJ.run(inputImage, "Select None", "");
	}
	
	public void shiftStackCenter(int shift) {
		// TODO Auto-generated method stub
		this.stackCenter+=shift;
	}
	
	/*************************************************************************************************************
	 * getter and setter methods
	 * @return
	 *************************************************************************************************************/
	
	public Line getHorizontalLine() {
		// TODO Auto-generated method stub
		return horizontalLine;
	}
	public ResultsTable getAnalysisTable() {
		// TODO Auto-generated method stub
		return this.analysisTable;
	}
	public int getstackCenter() {
		// TODO Auto-generated method stub
		return this.stackCenter;
	}
	public int getStackSlices() {
		// TODO Auto-generated method stub
		stackSlices=this.inputImage.getNSlices();
		return this.stackSlices;
	}
	public int getStartZ() {
		// TODO Auto-generated method stub
		return this.startZ;
	}
	public int getStepZ() {
		// TODO Auto-generated method stub
		return this.stepZ;
	}
	public int getStopZ() {
		// TODO Auto-generated method stub
		return stopZ;
	}
	public void setHorizontalLine(Line horizontal) {
		// TODO Auto-generated method stub
		this.horizontalLine=horizontal;
	}
	public void setRepetition(int i) {
		// TODO Auto-generated method stub
		this.repetition=i;
	}
	public void setStartZ(int i) {
		// TODO Auto-generated method stub
		this.startZ=i;
	}
	public void setStepZ(int value) {
		// TODO Auto-generated method stub
		this.stepZ=value;
	}
	public void setStopZ(int slices) {
		// TODO Auto-generated method stub
		this.stopZ=slices;
	}
	public boolean getSaveTable() {
		// TODO Auto-generated method stub
		return this.saveTables;
	}
	public boolean getSavePlot() {
		// TODO Auto-generated method stub
		return this.savePlot;
	}
	public boolean getShowTable() {
		// TODO Auto-generated method stub
		return this.showTables;
	}
	public boolean getShowPlot() {
		// TODO Auto-generated method stub
		return this.showPlot;
	}
	public int getRepetition() {
		// TODO Auto-generated method stub
		return this.repetition;
	}
	public void setSlice(int slice) {
		inputImage.setSlice(slice);
	}	
	
}	
/*	
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

	
	void logFileNames() {
		IJ.log("===============================================================");
		this.filePath=IJ.getDirectory("file");
		this.fileName=inputImage.getTitle();
		
		if (fileName.startsWith(filePath)) 
			this.fileName=inputImage.getTitle().substring(filePath.length());
		
		
		IJ.log("File: "+fileName);
		IJ.log("Path: "+filePath);
	}
	ResultsTable getResultsTable() {
		return this.analysisTable;
	}
	void closeNonImageWindows() {
		Window [] win=WindowManager.getAllNonImageWindows();
		int num=win.length;
		
		for (int i=0;i<num;i++) {
			win[i].dispose();
		}
	}
	void setInputImage(ImagePlus imp) {
		inputImage=imp;
	}
	void setHorizontalLine(Line horizontal) {
		this.horizontalLine=horizontal;
	}
	void setStart(int value) {
		this.start=value;
	}
	void setEnd(int value) {
		this.end=value;
	}
	Line getHorizontalLine() {
		return this.horizontalLine;
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
	void disableStack() {
		this.allStack=false;
	}
	void ignoreTimelapse() {
		this.ignoreTime=true;
	}
	ImageProcessor getCenterIP(){
		inputImage.setSliceWithoutUpdate(stackCenter);
		return inputImage.getProcessor();
	}
	boolean  hasLine() {
		Roi roi=inputImage.getRoi();
		if (roi!=null) return roi.isLine();
		else return false;
	}
	Line getLine() {
		return (Line)inputImage.getRoi();
	}
	Line defineLine() {
		HorizontalLine horizontal=new HorizontalLine (getCenterIP());
		return horizontal.optimizeHorizontalMaxima(horizontal.findHorizontalLine());
	}
	Line setLine(){
		if (hasLine()) return getLine();
		else return defineLine();
	}
*/	

