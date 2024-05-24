package ch.epfl.biop.ij2command;

import java.lang.reflect.Array;
import java.util.Arrays;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.Plot;
import ij.gui.ProfilePlot;
import ij.gui.Roi;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.plugin.Profiler;
import ij.plugin.filter.Filters;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;

public class EdgeWidthAnalyser {
	private static final int TOP=1,MIDDLE=2,BOTTOM=3;
	private ImagePlus crop,input;
	private ImageProcessor ip_ew;
	private int[] maxTop,maxBottom;
	private boolean showFit,showEdges;
	private Roi cropRoi;
	private double pixelWidth;
	
	EdgeWidthAnalyser(ImagePlus imp, int slice, int height){
				
		
		this.input=imp;
		this.pixelWidth=input.getCalibration().pixelWidth;
		setSlice(slice);
		this.setRoi(height, MIDDLE);
		
		imp.setRoi(cropRoi);
		this.crop=imp.crop();
				
		this.ip_ew=this.crop.getProcessor();
		
		
	}
	
	private ImageProcessor detectEdges() {
		ImageProcessor ip_edge=ip_ew.duplicate();
		ip_edge=ip_edge.convertToFloat();
		ip_edge.findEdges();
		
		if (showEdges) {
			ImagePlus imp=new ImagePlus("edge",ip_edge);
			imp.show();
		}
		return ip_edge;
	}
	void setSlice(int slice) {
		if (slice>input.getStackSize()) slice=input.getStackSize();
		input.setSliceWithoutUpdate(slice);
	}
	
	void setRoi(int size,int position) {
		int w=input.getWidth();
		int h=input.getHeight();
		if (size>h)size=h;
		Roi roi=new Roi(0,0,0,0);
		
		if (position==EdgeWidthAnalyser.TOP) {
			roi=new Roi(0,h/4,w,size);
		}
		if (position==EdgeWidthAnalyser.MIDDLE) {
			roi=new Roi(0,h/2,w,size);
		}
		if (position==EdgeWidthAnalyser.MIDDLE) {
			roi=new Roi(0,3*h/4,w,size);
		}
		this.cropRoi=roi;
	}
	void showFit() {
		this.showFit=true;
	}
	void showEdges() {
		this.showEdges=true;
	}
	void fitEdgeWidth(int slice) {
		
		int length=20;
		ResultsTable rt=new ResultsTable();
		ResultsTable profiles=new ResultsTable();
		
		ImageStack fitWin=new ImageStack();
		
		double [] x=new double [length];
		
		for (int i=0;i<length;i++) {
			x[i]=i*pixelWidth;
		}
		profiles.setValues("x/um", x);
		findMaxima();
		ImageProcessor ip_maxima=detectEdges();
		int max=maxTop.length;
		
		
		for (int n=0;n<max;n+=2) {
			
			rt.addRow();
				
			double line []=getProfile(ip_maxima,10,maxTop[n]-length/2,50,maxTop[n]+length/2,50);
			
			profiles.setValues(""+IJ.d2s(maxTop[n]*pixelWidth,1), line);
			
			CurveFitter cf=new CurveFitter(x,line);
			cf.doFit(CurveFitter.GAUSSIAN);
			double []param=cf.getParams();
			int num=cf.getNumParams();
			
			rt.addValue("position", maxTop[n]*pixelWidth);
			for (int i=0;i<num;i++) {
				rt.addValue("p"+i, param[i]);
				
			}
			rt.addValue("R^2", cf.getRSquared());
			
			if (showFit) fitWin.addSlice(cf.getPlot().getProcessor());
			
		}
		rt.show("FitResults");
		profiles.show("Profiles");
		
		if (showFit) new ImagePlus("Fit Windows",fitWin).show();
	}
	double [] getProfile(ImageProcessor ip, int lineWidth,double x1,double y1,double x2,double y2) {
		y1=y1-lineWidth/2.0;
		y2=y2-lineWidth/2.0;
		
		double [] profile=ip.getLine(x1,y1,x2,y2);
		
		int length=profile.length;
		
		for (int n=1;n<lineWidth;n++) {
			double [] line =ip.getLine(x1,y1+n,x2,y2+n);

			
			//IJ.log(""+(y1-lineWidth/2.0+n));
			for (int l=0;l<length;l++) {
				profile[l]+=line[l];
			}
		}
		return profile;
	}
	void findMaxima(){
		ImageProcessor ip_maxima=detectEdges();
		int w=ip_maxima.getWidth();
		int h=ip_maxima.getHeight();
		ip_maxima.setLineWidth(10);;
		double [] lineTop=ip_maxima.getLine(0, 10, w, 10);
		double [] lineBottom=ip_maxima.getLine(0, h-10, w, h-10);
		this.maxTop=MaximumFinder.findMaxima(lineTop, 14000, false);
		this.maxBottom=MaximumFinder.findMaxima(lineBottom, 12000, false);
		Arrays.sort(this.maxTop);
		Arrays.sort(this.maxBottom);
		IJ.log("top: "+maxTop.length);
		IJ.log("bottom: "+maxBottom.length);
		
		
	}
	ImageProcessor getProcessor() {
		return this.ip_ew;
	}
}
