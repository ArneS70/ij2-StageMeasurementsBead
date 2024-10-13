package ch.epfl.biop.ij2command;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;
import ij.process.ImageProcessor;

public class HorizontalLine  {
	ImageProcessor inputIP;
	int width;
	
	HorizontalLine(ImageProcessor ip){
		inputIP=ip;
		this.width=ip.getWidth();
	}
	Line findHorizontalLine() {
		
		Line horizontal;
		ImageProcessor ip_edge=inputIP.duplicate().convertToFloat();
		ip_edge.findEdges();
		LineAnalyser la=new LineAnalyser(ip_edge);
		Roi [] lines=la.findVerticalMaxima(10,3*width/8);
		int pos=1+lines.length/2;
		
		inputIP.setRoi(lines[pos]);
		double mean1=inputIP.getStatistics().mean;
		
		inputIP.setRoi(lines[pos+1]);
		double mean2=inputIP.getStatistics().mean;
		//IJ.log("m1="+mean1+"    m2="+mean2);
		
		if (mean1>mean2) {horizontal=(Line)lines[pos];}
		else {horizontal=(Line)lines[pos+1];}
		
		return horizontal;
	}
}
