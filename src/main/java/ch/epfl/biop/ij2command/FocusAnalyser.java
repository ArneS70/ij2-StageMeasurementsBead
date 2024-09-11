package ch.epfl.biop.ij2command;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class FocusAnalyser {
	
	private ImagePlus imps;
	private Calibration cal;
	private int repX,repY,lineLength;
	private boolean isHorizontal;
	private Line horizonetalLine;
	private ResultsTable focusMean;
	
	FocusAnalyser(ImagePlus imp,int line,int nx,int ny){
		setImage(imp);
		setLineLength(line);
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
	void analyseLine(int rep) {
		this.focusMean=new ResultsTable();
		
		int h=imps.getHeight();
		int w=imps.getWidth();
		double x1=this.horizonetalLine.x1d;
		double x2=this.horizonetalLine.x2d;
		double y1=this.horizonetalLine.y1d;
		double y2=this.horizonetalLine.y1d;
		double length=this.horizonetalLine.getLength();
		int slices=imps.getNSlices();
		
		double dist=length/rep;							//distance between horizontal focus points in pixel
		IJ.log("Distance focus points= "+IJ.d2s(dist*cal.pixelWidth)+" um");
		
		
		for (int s=1;s<=slices;s+=20) {
			focusMean.addRow();
			imps.setSlice(s);
			focusMean.addValue("z-slice",s);
			ImageProcessor ip=imps.getProcessor();
			for (int r=0;r<rep;r++) {
				Line analyseLine=new Line(x1+r*dist,y1-5,x2+r*dist,y2+5);
					ip.setRoi(analyseLine);
					ImageStatistics statsX=ip.getStats();
//					focusMean.addValue(""+IJ.d2s((x1+rep*dist)*cal.pixelWidth), statsX.mean);
					focusMean.addValue(IJ.d2s((x1+r*dist)*cal.pixelWidth),statsX.mean);
					
					
			}
			
		}
		//focusMean.show("Horizontal Focus");
		
	}
	void run() {
		int h=imps.getHeight()-lineLength;
		int w=imps.getWidth();
		
		int rimX=(w%repX)/2;
		int rimY=lineLength/2+(h%repY)/2;
		
		int distX=w/repX;
		int distY=h/repY;
		
		int slices=imps.getNSlices();
		
		
		ResultsTable rt_xAxis=new ResultsTable();
		ResultsTable rt_yAxis=new ResultsTable();
		
		
		for (int s=1;s<=slices;s++) {
			IJ.log("Slices:"+s+"/"+slices);
					
			
			rt_xAxis.addRow();
			rt_yAxis.addRow();
			
			rt_xAxis.addValue("X",(s-1)*cal.pixelDepth);
			rt_yAxis.addValue("X",(s-1)*cal.pixelDepth);
			
			imps.setSliceWithoutUpdate(s);
			ImageProcessor ip=imps.getProcessor();
			
			for (int pos=0;pos<repX;pos++) {
				Roi line=new Line(rimX+pos*distX, h/2-lineLength/2, rimX+pos*distX, h/2+lineLength/2);
				ip.setRoi(line);
				ImageStatistics statsX=ip.getStats();
				rt_xAxis.addValue(""+IJ.d2s((rimX+pos*distX)*cal.pixelWidth), statsX.stdDev);
			}	
				//imp.setRoi(line);
				
			for (int pos=0;pos<repY;pos++) {	
				Roi line=new Line(w/2, rimY+pos*distY, w/2, rimY+pos*distY+lineLength);
				ip.setRoi(line);
				ImageStatistics statsY=ip.getStats();
				rt_yAxis.addValue(""+IJ.d2s((rimY+pos*distY)*cal.pixelHeight), statsY.stdDev);
				//imp.setRoi(line);
			}
			
			
		}
		rt_xAxis.show("Results x-Axis");
		rt_yAxis.show("Results y-Axis");
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
	void setLineLength(int line){
		this.lineLength=line;
	}
	ResultsTable getFocusResults() {
		return focusMean;
	}
}
