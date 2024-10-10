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
import ij.process.ImageStatistics;

public class FocusAnalyser {
	
	private ImagePlus imps;
	private Calibration cal;
	private int repX,repY,analysisLineLength;
	private boolean isHorizontal;
	private Line horizonetalLine;
	private ResultsTable focusMap;
	private int start,end,step;
	private String titleFocusMap;
	
	FocusAnalyser(){
		
	}
	FocusAnalyser(ImagePlus imp,int line,int nx,int ny){
		setImage(imp);
		setAnalysisLineLength(line);
		setRepetitionX(nx);
		setRepetitionY(ny);
		cal=imp.getCalibration();
	}
	
	FocusAnalyser(ImagePlus imp, Line line){
		setImage(imp);
		cal=imp.getCalibration();
		this.isHorizontal=true;
		this.horizonetalLine=line;
		
	}
	void analyseHorizontalLine(int rep,int lineHeight) {
		this.focusMap=new ResultsTable();
		this.titleFocusMap="Focus Map Horizontal Line";
		
		int h=imps.getHeight();
		int w=imps.getWidth();
		double x1=this.horizonetalLine.x1d;
		double x2=this.horizonetalLine.x2d;
		double y1=this.horizonetalLine.y1d;
		double y2=this.horizonetalLine.y2d;
		double length=this.horizonetalLine.getLength();
		int slices=imps.getNSlices();
		
		double dist=length/rep;							//distance between horizontal focus points in pixel
		IJ.log("Distance focus points= "+IJ.d2s(dist*cal.pixelWidth)+" um");
		
		
		for (int s=start;s<=end;s+=step) {
			focusMap.addRow();
			imps.setSliceWithoutUpdate(s);
			focusMap.addValue("z-slice",s);
			ImageProcessor ip=imps.getProcessor();
			for (int r=0;r<rep;r++) {
				Line analyseLine=new Line(x1+r*dist,y1-lineHeight,x2+r*dist,y2+lineHeight);
					ip.setRoi(analyseLine);
					ImageStatistics statsX=ip.getStats();
//					focusMean.addValue(""+IJ.d2s((x1+rep*dist)*cal.pixelWidth), statsX.mean);
					focusMap.addValue(IJ.d2s((x1+r*dist)*cal.pixelWidth),statsX.mean);
					
					
			}
			
		}
//		plotFocusMap();
	}
	
	void plotFocusMap() {
		ImageStack plots=new ImageStack(696,415);
		
		int row=this.focusMap.getCounter();
		int col=this.focusMap.getLastColumn();
		for (int c=1;c<col;c++) {
			Plot plot=new Plot("A", "B", "C");
			plot.add("Circle", this.focusMap.getColumnAsDoubles(0), this.focusMap.getColumnAsDoubles(c));
			plots.addSlice(plot.getImagePlus().getProcessor());
		}
		new ImagePlus ("Plots",plots).show();
		
		
	}
	ResultsTable getFocusMap() {
		return this.focusMap;
	}
	void showFocusMap() {
		focusMap.show(this.titleFocusMap);
	}
	void setLine(Line line) {
		this.horizonetalLine=line;
	}
	void run() {
		int h=imps.getHeight()-analysisLineLength;
		int w=imps.getWidth();
		
		int rimX=(w%repX)/2;
		int rimY=analysisLineLength/2+(h%repY)/2;
		
		int distX=w/repX;
		int distY=h/repY;
		
		int slices=imps.getNSlices();
		
		
		ResultsTable rt_xAxis=new ResultsTable();
		ResultsTable rt_yAxis=new ResultsTable();
		
		
		for (int s=start;s<=end;s+=step) {
			IJ.log("Slices:"+s+"/"+slices);
					
			
			rt_xAxis.addRow();
			rt_yAxis.addRow();
			
			rt_xAxis.addValue("X",(s-1)*cal.pixelDepth);
			rt_yAxis.addValue("X",(s-1)*cal.pixelDepth);
			
			imps.setSliceWithoutUpdate(s);
			ImageProcessor ip=imps.getProcessor();
			
			for (int pos=0;pos<repX;pos++) {
				Roi line=new Line(rimX+pos*distX, h/2-analysisLineLength/2, rimX+pos*distX, h/2+analysisLineLength/2);
				ip.setRoi(line);
				ImageStatistics statsX=ip.getStats();
				rt_xAxis.addValue(""+IJ.d2s((rimX+pos*distX)*cal.pixelWidth), statsX.stdDev);
			}	
				//imp.setRoi(line);
				
			for (int pos=0;pos<repY;pos++) {	
				Roi line=new Line(w/2, rimY+pos*distY, w/2, rimY+pos*distY+analysisLineLength);
				ip.setRoi(line);
				ImageStatistics statsY=ip.getStats();
				rt_yAxis.addValue(""+IJ.d2s((rimY+pos*distY)*cal.pixelHeight), statsY.stdDev);
				//imp.setRoi(line);
			}
			
			
		}
		rt_xAxis.show("Results x-Axis");
		rt_yAxis.show("Results y-Axis");
	}
	void setStart(int set) {
		if (set<0) set=1;
		this.start=set;
		
	}
	void setEnd(int set) {
		if (set<=imps.getImageStackSize()) this.end=set;
		else this.end=imps.getNSlices();
	}
	void setStep(int set) {
		this.step=set;
	}
	void setImage(ImagePlus imp) {
		this.imps=imp;
	}
	void setRepetitionX(int rep){
		this.repX=rep;
	}
	void setRepetitionY(int rep){
		this.repY=rep;
	}
	void setAnalysisLineLength(int line){
		this.analysisLineLength=line;
	}
	
	double getZstep() {
		return cal.pixelDepth;
	}
}
