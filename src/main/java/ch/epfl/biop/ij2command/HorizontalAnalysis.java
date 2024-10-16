package ch.epfl.biop.ij2command;

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
	private ImagePlus inputImage;
	private Line horizontalLine;
	//optional parameters
	private int startZ, stopZ,stepZ, startT,stopT,stepT;
	private Builder built;
	
	protected String filePath,fileName;
	protected ResultsTable analysisTable=new ResultsTable();
	protected ResultsTable summaryResults;
	protected int lineWidth, counter,stackCenter, stackSlices;
	protected boolean allStack,ignoreTime=false,isTimeLapse=false;
	protected Calibration cal;
	
	private HorizontalAnalysis (Builder builder) {
		//required parameters
		this.inputImage=builder.inputImage;
		
		//optional parameters
		this.horizontalLine=builder.horizontalLine;
		this.startZ=builder.startZ;
		this.stopZ=builder.stopZ;
	}
	
	public static class Builder{
		//required parameters
		private ImagePlus inputImage;
		
		//optional Parameters
		private Line horizontalLine;
		private int startZ;
		private int stopZ;
		private int stepZ;
		private int startT;
		private int stopT;
		private int stepT;
		private Builder built;
		
		public Builder(ImagePlus imp) {
			this.inputImage=imp;
			
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
		public Builder setBuilder(Builder b){
			this.built=b;
			return this;
		}
		public HorizontalAnalysis build() {
			return new HorizontalAnalysis(this);
		}
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

	public boolean checkInputImage() {
		boolean check=true;
		if (inputImage==null) {
			IJ.log("Please provide an image");
			check=false;
		}
		
		if (inputImage.getNSlices()==1) {
			IJ.log("Please provide an z-stack");
			check=false;
		}
//		if (inputImage.getNFrames()>1) this.isTimeLapse=true;
		return check;
		
	}
	void disableStack() {
		this.allStack=false;
	}
	
	void ignoreTimelapse() {
		this.ignoreTime=true;
	}
	
	boolean  hasLine() {
		Roi roi=inputImage.getRoi();
		if (roi!=null) return roi.isLine();
		else return false;
	}
	
	Line getLine() {
		return (Line)inputImage.getRoi();
	}
	/********************************************************************************************************** 
	 * Getter and Setter
	 **********************************************************************************************************/
		
		int getAnalysisLineWidth() {
			return this.lineWidth;
		}
		ImageProcessor getCenterIP(){
			inputImage.setSliceWithoutUpdate(stackCenter);
			return inputImage.getProcessor();
		}
		Line getHorizontalLine() {
			return this.horizontalLine;
		}
		ImageStack getImageStack() {
			ImageStack stack=new ImageStack(inputImage.getWidth(),inputImage.getHeight());
			if (!isTimeLapse) {
				int slices=inputImage.getNSlices();
				if (allStack) {
					startZ=1;
					stopZ=slices;
				}
				for (int s=startZ;s<=stopZ;s+=stepZ) {
					inputImage.setSlice(s);
					stack.addSlice(inputImage.getProcessor());
							
				}
			}
			return stack;
		
		}
		ImagePlus getInputImage(){
			return this.inputImage;
		}
		int getZStep() {
			return stepZ;
		}
		void setHorizontalLine(Line horizontal) {
			this.horizontalLine=horizontal;
		}
		void setSliceStart(int value) {
			startZ=value;
		}
		void setSliceStop(int value) {
			stopZ=value;
		}
		
		void setStackCenter(int shift) {
			this.stackCenter+=shift;
		}
		ResultsTable getResultsTable() {
			return this.analysisTable;
		}
		int getStackCenter() {
			return this.stackCenter;
		}
		int getStackSlices() {
			return this.stackSlices;
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

