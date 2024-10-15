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
	final static String titleProfiles="Horizontal Focus Frofiles";
	final static String titleShift="Horizontal Focus Shift";
	final static String titleSummary="Summary Horizontal Focus Results";
	protected ImagePlus inputImage;
	
	Calibration cal;
	private Line horizontalLine;
	
	private ResultsTable analysisTable=new ResultsTable();
	private ResultsTable focusShift=new ResultsTable();
	private ResultsTable summaryResults;
	private String filePath,fileName;
	private USAF_HorizontalFocus inputParam;
	private int lineWidth,counter,stackCenter, stackSlices;
	private boolean allStack,ignoreTime=false,isTimeLapse=false;
	
	private Plot focusFitPlot;
	private ImagePlus fitWin;
	
	HorizontalLineFocusAnalysis(){
		
	}
	HorizontalLineFocusAnalysis(ImagePlus imp,USAF_HorizontalFocus uhf){
		this.inputImage=imp;
		this.cal=imp.getCalibration();
		inputParam=uhf;
			
		checkParameters();
		this.fileName=imp.getTitle();
		this.lineWidth=(int) setAnalysisLineWidth();
	}
	HorizontalLineFocusAnalysis duplicate() {
		return new HorizontalLineFocusAnalysis(this.inputImage,this.inputParam);
	}
	
	void run(){
		if (this.checkInputImage()) {
			if (hasLine()) this.setHorizontalLine(this.getLine());
			else {
					this.horizontalLine= new HorizontalLine(getCenterIP()).findHorizontalLine();
					this.horizontalLine= new HorizontalLine(getCenterIP()).optimizeHorizontalMaxima(horizontalLine);
					inputImage.setRoi(horizontalLine);
			}
		}
		
		
		if (inputImage.getNFrames()>1&&!ignoreTime) {
			
			HorizontalFocusTimelapse hft=new HorizontalFocusTimelapse(this);
			hft.analyseTimeLapse();
		} else {
		
			getSummaryTable(titleSummary);
			cal=inputImage.getCalibration();
			logFileNames();
			LogToTable();
			LineFocusAnalyser focusAnalyser =new LineFocusAnalyser(this);
			analysisTable=focusAnalyser.analyseHorizontalLine();
			TableFitter tableFit=new TableFitter(analysisTable);
			tableFit.fitTable(CurveFitter.POLY5);
			focusShift=tableFit.getFitResults();
			this.fitFocusShift();
			outputResults();
		}
	}
	
	void outputResults() {
		if (inputParam.saveTable) this.saveResultTables();
		if (inputParam.showTable)this.showResultsTables();
		if (inputParam.savePlot) this.savePlot();
		if (inputParam.showPlot)this.showPlot();
	}
	void checkParameters(){
		int max=inputImage.getNSlices();
		this.stackSlices=max;
		if (inputParam.zstep<1) inputParam.zstep=1;
		if (inputParam.zstep>max) inputParam.zstep=max/10;
		if (inputParam.allStack) {inputParam.start=1;inputParam.stop=max;stackCenter=max/2;}
		if (inputParam.stop<inputParam.start) {
			do {
				inputParam.start--;
			}while (inputParam.stop<inputParam.start);
		}
		
		
		if (inputParam.start<1)inputParam.start=1;
		if (inputParam.stop>max)inputParam.stop=max;
		if (inputParam.stop<inputParam.start)inputParam.start=inputParam.stop;
		
	}

	double setAnalysisLineWidth(){
		HorizontalLine hl=new HorizontalLine(getCenterIP());
		return hl.getHorizontalSpacing()/2;
	}
	
	void LogToTable() {
		ResultsTable focus=ResultsTable.getResultsTable(HorizontalLineFocusAnalysis.titleSummary);
		if (focus==null) focus=new ResultsTable();
		focus.addRow();
		focus.addValue("#", this.counter);
		focus.addValue("File", fileName);
		focus.addValue("Repetition", inputParam.repetition);
		focus.addValue("z step", inputParam.zstep);
		focus.addValue("z start", inputParam.start);
		focus.addValue("z stop", inputParam.stop);
		double space=1000/(4*this.lineWidth*cal.pixelWidth);
		focus.addValue("Grid spacing LP/mm", space);
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
	
	void fitFocusShift() {		

//		TableFitter tableFit=new TableFitter(rt);
//		tableFit.showFit();
//		tableFit.fitTable(CurveFitter.POLY5);
//		tableFit.GlobalTableFit(CurveFitter.POLY5);
//		tableFit.getFitResults().show(HorizontalFocus.titleResults);
	
		int last=focusShift.getLastColumn();
		
		ResultsTable focus=ResultsTable.getResultsTable(HorizontalLineFocusAnalysis.titleSummary);
		CurveFitter cf=new CurveFitter(focusShift.getColumnAsDoubles(0),focusShift.getColumnAsDoubles(last));
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		
		double [] param=cf.getParams();
		double slope=param[1];
		double angle=180*Math.atan(slope)/Math.PI;
		
		IJ.log("Focus shift x-axis/pixel per z/um: "+cal.pixelWidth/slope);
		IJ.log("Focus shift delta z/detla x  : "+slope);
		IJ.log("angle/deg: "+angle);
		IJ.log("R^2: "+cf.getFitGoodness());

		focus.setPrecision(5);
		focus.addValue("Focus shift x-axis pixel/um", cal.pixelWidth/slope);
		focus.addValue("Focus shift delta z/delta x", slope);
		focus.addValue("angle/deg", angle);
		focus.addValue("R^2", cf.getFitGoodness());
		focus.show(HorizontalLineFocusAnalysis.titleSummary);
		
		this.focusFitPlot=cf.getPlot();
		this.focusFitPlot.setXYLabels("Shift x-axis/um", "Shift z-axis/um");
		fitWin=focusFitPlot.getImagePlus();
	}
	void showPlot() {
		fitWin.show();
	}
	void savePlot() {
		IJ.save(fitWin, filePath+fileName+" "+IJ.pad(counter, 4)+".tif");
	}
	void saveResultTables() {
		
		int n=this.fileName.indexOf(".");
		String saveName=this.fileName.substring(0, n);
		
		analysisTable.save(filePath+saveName+"_Profiles_"+IJ.pad(counter, 4)+".csv");
		focusShift.save(filePath+fileName+"_FocusShift_"+IJ.pad(counter, 4)+".csv");
	}

	void showResultsTables() {
		analysisTable.show(HorizontalLineFocusAnalysis.titleProfiles);
		focusShift.show(HorizontalLineFocusAnalysis.titleShift);
		
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
				inputParam.start=1;
				inputParam.stop=slices;
			}
			for (int s=inputParam.start;s<=inputParam.stop;s+=inputParam.zstep) {
				inputImage.setSlice(s);
				stack.addSlice(inputImage.getProcessor());
						
			}
		}
		return stack;
	
	}
	ImagePlus getInputImage(){
		return this.inputImage;
	}
	int getRepetition() {
		return inputParam.repetition;
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

	int getZStep() {
		return inputParam.zstep;
	}
	void setHorizontalLine(Line horizontal) {
		this.horizontalLine=horizontal;
	}
	void setStart(int value) {
		inputParam.start=value;
	}
	void setStop(int value) {
		inputParam.stop=value;
	}
	
	void setStackCenter(int shift) {
		this.stackCenter+=shift;
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