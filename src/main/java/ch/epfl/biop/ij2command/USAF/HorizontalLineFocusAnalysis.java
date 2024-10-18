package ch.epfl.biop.ij2command.USAF;

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

public class HorizontalLineFocusAnalysis extends HorizontalAnalysisMethods{
	final static String titleProfiles="Horizontal Focus Frofiles";
	final static String titleShift="Horizontal Focus Shift";
	final static String titleSummary="Summary Horizontal Focus Results";
	
//	protected HorizontalAnalysis analysis;
	
	private ResultsTable focusShiftTable=new ResultsTable();
	protected ResultsTable analysisTable=new ResultsTable();
	
	private Plot focusFitPlot;
	private ImagePlus fitWin;
//	private Calibration cal;
	
	private int counter;
	
	HorizontalLineFocusAnalysis(HorizontalAnalysis parameters){
//		super();
		this.analysis=parameters;
	}
	HorizontalLineFocusAnalysis (HorizontalLineFocusAnalysis hlfa){
		super();
	}
//	HorizontalLineFocusAnalysis(ImagePlus imp,USAF_HorizontalFocus uhf){
//		
//		this.cal=imp.getCalibration();
//		inputParam=uhf;
//			
//		checkParameters();
//		this.fileName=imp.getTitle();
//		this.lineWidth=(int) setAnalysisLineWidth();
//	}
	
	void run(){
		if (checkInputImage()) {
			
			if (hasLine()) this.setHorizontalLine(this.getLine());
			else {
					analysis.setHorizontalLine( new HorizontalLine(getCenterIP()).findHorizontalLine());
					analysis.setHorizontalLine( new HorizontalLine(getCenterIP()).optimizeHorizontalMaxima(analysis.getHorizontalLine()));
					analysis.getImage().setRoi(analysis.getHorizontalLine());
			}
		}
		
		
		if (analysis.getImage().getNFrames()>1&&!analysis.ignoreTime) {
			
			HorizontalFocusTimelapse hft=new HorizontalFocusTimelapse(this);
			hft.analyseTimeLapse();
		} else {
			
			getSummaryTable(titleSummary);
			analysis.cal=analysis.getImage().getCalibration();
			logFileNames();
			LogToTable();
			LineFocusAnalyser focusAnalyser =new LineFocusAnalyser(this);
			analysisTable=focusAnalyser.analyseHorizontalLine();
			TableFitter tableFit=new TableFitter(analysisTable);
			tableFit.fitTable(CurveFitter.POLY5);
			focusShiftTable=tableFit.getFitResults();
			this.fitFocusShift();
			outputResults();
		}
	}
	void outputResults() {
		if (analysis.getSaveTable()) {
										this.saveResultTables(analysisTable,"_Profile_");
										this.saveResultTables(focusShiftTable,"_Shift_");
		}
		if (analysis.getShowTable()) {
										this.showResultsTables(analysisTable, HorizontalLineFocusAnalysis.titleProfiles);
										this.showResultsTables(focusShiftTable, HorizontalLineFocusAnalysis.titleShift);
		}
		if (analysis.getSavePlot()) this.savePlot(fitWin);
		if (analysis.getShowPlot())this.showPlot(fitWin);
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
		focus.addValue("File", analysis.fileName);
		focus.addValue("Repetition", analysis.getRepetition());
		focus.addValue("z step", analysis.getStepZ());
		focus.addValue("z start", analysis.getStartZ());
		focus.addValue("z stop", analysis.getStopZ());
		double space=1000/(4*analysis.lineWidth*analysis.cal.pixelWidth);
		focus.addValue("Grid spacing LP/mm", space);
		Line line=analysis.getHorizontalLine();
		focus.addValue("x1", line.x1d);
		focus.addValue("y1", line.y1d);
		focus.addValue("x2", line.x2d);
		focus.addValue("y2", line.y2d);
		focus.show(HorizontalLineFocusAnalysis.titleSummary);
	}
	
	void fitFocusShift() {		

//		TableFitter tableFit=new TableFitter(rt);
//		tableFit.showFit();
//		tableFit.fitTable(CurveFitter.POLY5);
//		tableFit.GlobalTableFit(CurveFitter.POLY5);
//		tableFit.getFitResults().show(HorizontalFocus.titleResults);
	
		int last=focusShiftTable.getLastColumn();
		
		ResultsTable focus=ResultsTable.getResultsTable(HorizontalLineFocusAnalysis.titleSummary);
		CurveFitter cf=new CurveFitter(focusShiftTable.getColumnAsDoubles(0),focusShiftTable.getColumnAsDoubles(last));
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		
		double [] param=cf.getParams();
		double slope=param[1];
		double angle=180*Math.atan(slope)/Math.PI;
		
		IJ.log("Focus shift x-axis/pixel per z/um: "+analysis.cal.pixelWidth/slope);
		IJ.log("Focus shift delta z/detla x  : "+slope);
		IJ.log("angle/deg: "+angle);
		IJ.log("R^2: "+cf.getFitGoodness());

		focus.setPrecision(5);
		focus.addValue("Focus shift x-axis pixel/um", analysis.cal.pixelWidth/slope);
		focus.addValue("Focus shift delta z/delta x", slope);
		focus.addValue("angle/deg", angle);
		focus.addValue("R^2", cf.getFitGoodness());
		focus.show(HorizontalLineFocusAnalysis.titleSummary);
		
		this.focusFitPlot=cf.getPlot();
		this.focusFitPlot.setXYLabels("Shift x-axis/um", "Shift z-axis/um");
		fitWin=focusFitPlot.getImagePlus();
	}
	
	
/********************************************************************************************************** 
 * Getter and Setter
 **********************************************************************************************************/
	
	
	

	
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