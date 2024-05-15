package ch.epfl.biop.ij2command;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class USAF_FocusAnalyser {
	
	private ImagePlus imps;
	private Calibration cal;
	private int repX,repY,lineLength;
	
	USAF_FocusAnalyser(ImagePlus imp,int line,int nx,int ny){
		setImage(imp);
		setLineLength(line);
		setRepetitionX(nx);
		setRepetitionY(ny);
		cal=imp.getCalibration();
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
}
