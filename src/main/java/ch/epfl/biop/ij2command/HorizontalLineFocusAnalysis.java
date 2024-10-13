package ch.epfl.biop.ij2command;

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

public class HorizontalLineFocusAnalysis {
	final static String titleResults="Horizontal Focus Results";
	final static String titleSummary="Summary Horizontal Focus Results";
	private ImagePlus inputImage;
	
	Calibration cal;
	private Line horizontalLine;
	
	private ResultsTable analysisTable=new ResultsTable();
	private ResultsTable focusShift=new ResultsTable();
	
	private String filePath,fileName;
	private int start,stop,zstep,lineWidth,counter,stackCenter;
	private boolean showFit,savePlot,saveTable,allStack,ignoreTime=false,isTimeLapse=false;
	private ResultsTable summaryResults;
	private int repetition;
	private Plot focusFitPlot;
	private ImageStack analysisStack;

	HorizontalLineFocusAnalysis(ImagePlus imp,int rep,int start,int stop,int step,int length,boolean allStack, boolean show,boolean savePlot,boolean saveTable){
		this.inputImage=imp;
		this.repetition=rep; this.start=start;this.stop=stop;this.zstep=step;this.lineWidth=length;
		this.allStack=allStack;	this.showFit=show; this.savePlot=savePlot; this.saveTable=saveTable;
		checkStack();
		this.fileName=imp.getTitle();
	}
	void checkStack(){
		int max=inputImage.getNSlices();
		if (allStack) {start=1;stop=max;stackCenter=max/2;}
		if (stop<start) {
			do {
				start--;
			}while (stop<start);
		}
		
		
		if (start<1)start=1;
		if (stop>max)stop=max;
		if (stop<start)start=stop;
		
	}
	void run(){
		if (this.checkInputImage()) {
			if (hasLine()) this.setHorizontalLine(this.getLine());
			else {
					this.horizontalLine= new HorizontalLine(getCenterIP()).findHorizontalLine();
					inputImage.setRoi(horizontalLine);
			}
		}
		getSummaryTable(titleSummary);
		LogToTable();
		cal=inputImage.getCalibration();
		logFileNames();
		if (inputImage.getNFrames()>1&&!ignoreTime) {
			
//			HorizontalFocusTimelapse hft=new HorizontalFocusTimelapse(this);
//			hft.analyseTimeLapse();
		} else {
		
			LineFocusAnalyser focusAnalyser =new LineFocusAnalyser(this);
			TableFitter tableFit=new TableFitter(focusAnalyser.analyseHorizontalLine());
			tableFit.fitTable(CurveFitter.POLY5);
			focusShift=tableFit.getFitResults();
			this.fitFocusShift();
		}
	}
	void LogToTable() {
			
		ResultsTable focus=ResultsTable.getResultsTable(HorizontalLineFocusAnalysis.titleSummary);
		if (focus==null) focus=new ResultsTable();
		focus.addRow();
		focus.addValue("#", this.counter);
		focus.addValue("File", fileName);
//			focus.addValue("Repetition", this.repetition);
		focus.addValue("z step", this.zstep);
		focus.addValue("z start", this.start);
		focus.addValue("z stop", this.stop);
		focus.addValue("line length", this.lineWidth);
		focus.addValue("x1", this.horizontalLine.x1d);
		focus.addValue("y1", this.horizontalLine.y1d);
		focus.addValue("x2", this.horizontalLine.x2d);
		focus.addValue("y2", this.horizontalLine.y2d);
		
		focus.show(HorizontalLineFocusAnalysis.titleSummary);
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
	
	void fitFocusShift() {		
		
		
//		TableFitter tableFit=new TableFitter(rt);
//		tableFit.showFit();
//		tableFit.fitTable(CurveFitter.POLY5);
//		tableFit.GlobalTableFit(CurveFitter.POLY5);
//		tableFit.getFitResults().show(HorizontalFocus.titleResults);
		
	
		int last=focusShift.getLastColumn();
		
		CurveFitter cf=new CurveFitter(focusShift.getColumnAsDoubles(0),focusShift.getColumnAsDoubles(last));
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		
		double [] param=cf.getParams();
		double slope=param[1];
//		double zShift=param[1]*cal.pixelDepth;
		
		double angle=180*Math.atan(slope)/Math.PI;
		
		IJ.log("Focus shift z-axis  per slice: "+slope*zstep);
		IJ.log("Focus shift z-axis  absolut: "+slope);
		
		IJ.log("angle/deg: "+angle);
		IJ.log("R^2: "+cf.getFitGoodness());
		
		
		
		ResultsTable focus=ResultsTable.getResultsTable(HorizontalLineFocusAnalysis.titleSummary);
		focus.setPrecision(5);
		focus.addValue("Focus shift per slice/um", param[1]);
//		focus.addValue("Focus shift absolut", zShift);
		focus.addValue("Slope", slope);
		focus.addValue("angle/deg", angle);
		focus.addValue("R^2", cf.getFitGoodness());
		focus.show(HorizontalLineFocusAnalysis.titleSummary);
		
		
		ImagePlus fitWin;
		
		if (showFit) {
			this.focusFitPlot=cf.getPlot();
			fitWin=focusFitPlot.show().getImagePlus();
			fitWin.setTitle(fileName+" "+IJ.pad(counter, 3)+".tif");
			fitWin.show();
			
		}
		if (savePlot) {
			this.focusFitPlot=cf.getPlot();
			fitWin=focusFitPlot.getImagePlus();
			IJ.save(fitWin, filePath+fileName+" "+IJ.pad(counter, 3)+".tif");
		}
	}
	
	void saveResults() {
		
		int n=this.fileName.indexOf(".");
		String saveName=this.fileName.substring(0, n);
		
//		n=this.filePath.indexOf(saveName);
//		String savePath=this.filePath.substring(0, n);
		ResultsTable rt=ResultsTable.getResultsTable(HorizontalLineFocusAnalysis.titleResults);
		rt.save(filePath+saveName+"_TableFits_"+IJ.pad(counter, 4)+".csv");

		
//		rt=ResultsTable.getResultsTable("Horizontal Focus");
//		rt.save(filePath+fileName+"_HorizontalFocus_"+IJ.pad(counter, 4)+".csv");
//		WindowManager.getFrame("Horizontal Focus").dispose();
		
		
		
		
		
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
				this.start=1;
				this.stop=slices;
			}
			for (int s=start;s<=stop;s+=zstep) {
				inputImage.setSlice(s);
				stack.addSlice(inputImage.getProcessor());
						
			}
		}
		return stack;
	
	}
	int getRepetition() {
		return repetition;
	}
	ResultsTable getResultsTable() {
		return this.analysisTable;
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
	//	Line defineLine() {
	//		
	//	}
	int getZStep() {
		return this.zstep;
	}
	Line setLine(){
		if (hasLine()) return getLine();
		else return defineLine();
	}
	void setHorizontalLine(Line horizontal) {
		this.horizontalLine=horizontal;
	}
	void setStart(int value) {
		this.start=value;
	}
	void setStop(int value) {
		this.stop=value;
	}	

}

/*			
HorizontalLineAnalyser horizontalLineAnalyser=new HorizontalLineAnalyser(this.inputImage);

int z=inputImage.getNSlices();
					
if (inputImage.getRoi()==null) 
	{horizontalLineAnalyser.setHorizontalLine(this.stackCenter);
	 focusAnalyser=new FocusAnalyser(inputImage,horizontalLineAnalyser.getHorizontalLine());
	}
Roi roi=inputImage.getRoi();

roi=horizontalLineAnalyser.optimizeHorizontalMaxima((Line) roi);

if (roi!=null ) {
	if(roi.isLine()) {
		focusAnalyser=new FocusAnalyser(inputImage,(Line)roi);
		this.horizontalLine=(Line)roi;
		
	} else {
		horizontalLineAnalyser.setHorizontalLine(this.stackCenter);
		focusAnalyser=new FocusAnalyser(inputImage,horizontalLineAnalyser.getHorizontalLine());
	}
}
//fa=new FocusAnalyser(inputImage,(Line)roi);
//int[] param=setStackSize(imp);
//fa.setStart(param[0]);
//fa.setEnd(param[1]);
//if (allStack) {start=1;stop=inputImage.getNSlices();}
//focusAnalyser.setStart(start);
//focusAnalyser.setEnd(stop);
//focusAnalyser.setStep(zstep);
//LogToTable(titleSummary);
//focusAnalyser.analyseHorizontalLine(repetition,lineLength);
fitTableResults(focusAnalyser);
if (saveTable) saveResults();
}}*/
/*		void findHorizontalLine() {

//Roi[] rois=new LineAnalyser(getCenterIP()).findVerticalMaxima(5,400);
//
//
//inputImage.setSlice(slice);
ImageProcessor ip_edge=getCenterIP().duplicate().convertToFloat();
ip_edge.findEdges();
LineAnalyser la=new LineAnalyser(ip_edge);
Roi [] lines=la.findVerticalMaxima(10,400);
int pos=1+lines.length/2;
ImageProcessor ip=inputImage.getProcessor();
ip.setRoi(lines[pos]);
double mean1=ip.getStatistics().mean;

ip.setRoi(lines[pos+1]);
double mean2=ip.getStatistics().mean;
//IJ.log("m1="+mean1+"    m2="+mean2);

if (mean1>mean2) {inputImage.setRoi(lines[pos]);horizontalLine=(Line)lines[pos];}
else {inputImage.setRoi(lines[pos+1]);horizontalLine=(Line)lines[pos+1];}

inputImage.updateAndDraw();



}*/
/*	
void LogToTable() {
		
	ResultsTable lineMax=ResultsTable.getResultsTable("Line Maxima Results");
	if ((lineMax)==null) lineMax=new ResultsTable();
//	ResultsTable lineMax=new ResultsTable();
	int counter=lineMax.getCounter();
	lineMax.addRow();
	lineMax.addValue("#", counter);
	lineMax.addValue("File", fileName);
	
	lineMax.addValue("z step", this.zstep);
	
	lineMax.addValue("x1", this.horizontalLine.x1d);
	lineMax.addValue("y1", this.horizontalLine.y1d);
	lineMax.addValue("x2", this.horizontalLine.x2d);
	lineMax.addValue("y2", this.horizontalLine.y2d);
	
	
	lineMax.show("Line Maxima Results");
	
	}
*/