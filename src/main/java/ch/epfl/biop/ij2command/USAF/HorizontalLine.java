package ch.epfl.biop.ij2command.USAF;

import java.util.Arrays;

import ch.epfl.biop.ij2command.stage.general.ArrayStatistics;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;
import ij.measure.CurveFitter;
import ij.plugin.filter.MaximumFinder;
import ij.process.ImageProcessor;

public class HorizontalLine  {
	ImageProcessor inputIP;
	int width;
	int height;
	
	HorizontalLine(ImageProcessor ip){
		inputIP=ip;
		this.width=ip.getWidth();
		this.height=ip.getHeight();
	}
	Line findHorizontalLine() {
		
		Line horizontal;
		ImageProcessor ip_edge=inputIP.duplicate().convertToFloat();
		ip_edge.findEdges();
//		new ImagePlus("Edge",ip_edge).show();
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
	double getHorizontalSpacing(){
//		inputImage.setSlice(super.stackCenter);
//		LineAnalyser spacing=new LineAnalyser (new ImagePlus("edge",this.inputImage.getProcessor().duplicate()));
//		setProfile(LineAnalyser.CENTER);
//		spacing.getProfilPlot().show();
//		double [] line=spacing.getProfile();		
		
		ImageProcessor ipEdge=this.inputIP.duplicate().convertToFloat();
		ipEdge.findEdges();
//		new ImagePlus("test",ipEdge).show();
		
		
		double [] line=ipEdge.getLine(width/2,0,width/2, height);
		ArrayStatistics stat=new ArrayStatistics(line);

		double max=stat.getMax();
		double min=stat.getMin();
		int prominence=(int)(0.5*(max-min));
		int [] points=MaximumFinder.findMaxima(line, prominence, false);
		Arrays.sort(points);
		int length=points.length;
		double []x=new double[length];
		double []y=new double[length];
		for (int i=0;i<length;i++){
			x[i]=i;
			y[i]=points[i];
		}
		
		CurveFitter cf=new CurveFitter(x,y);
		cf.doFit(CurveFitter.STRAIGHT_LINE);
//		cf.getPlot().show();
		double []param=cf.getParams();
		return param[1];
		
		
	}
	Line optimizeHorizontalMaxima(Line line) {
		
		double space=getHorizontalSpacing();		
		double profile []=inputIP.getLine(line.x1d+20,line.y1d-space,line.x1d+20,line.y1d+space);
		double [] x=new double[profile.length];
		int profLen=x.length;
		for (int i=0;i<profLen;i++) {
			x[i]=i;
		}
		CurveFitter cf=new CurveFitter(x,profile);
		cf.doFit(CurveFitter.GAUSSIAN);
//		cf.getPlot().show();
		double [] paramLeft=cf.getParams();
		
		profile=inputIP.getLine(line.x2d-20,line.y2d-space,line.x2d-20,line.y2d+space);
		
		cf=new CurveFitter(x,profile);
		cf.doFit(CurveFitter.GAUSSIAN);
//		cf.getPlot().show();
		double [] paramRight=cf.getParams();
		return new Line(20,line.y1d-space+paramLeft[2],line.x2d-20,line.y2d-space+paramRight[2]);

	}
}