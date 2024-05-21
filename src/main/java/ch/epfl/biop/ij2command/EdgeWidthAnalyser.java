package ch.epfl.biop.ij2command;

import java.lang.reflect.Array;
import java.util.Arrays;

import ij.IJ;
import ij.ImagePlus;
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
	private ImagePlus edgeWidth;
	private ImageProcessor ip_ew;
	private int[] maxTop,maxBottom;
	
	EdgeWidthAnalyser(ImagePlus imp){
		imp.setSliceWithoutUpdate(200);
		
		Roi roi=new Roi(0,470,1388,100);
		imp.setRoi(roi);
		//imp.show();
		
		this.edgeWidth=imp.crop();
		//this.edgeWidth.show();
		
		this.ip_ew=this.edgeWidth.getProcessor();
		
		
	}
	
	private ImageProcessor detectEdges() {
		ImageProcessor ip_edge=ip_ew.duplicate();
		ip_edge=ip_edge.convertToFloat();
		
		
		ip_edge.findEdges();
		//ImagePlus imp=new ImagePlus("edge",ip_edge);
		//imp.show();
		return ip_edge;
	}
	void fitEdgeWidth(int length) {
		ResultsTable rt=new ResultsTable();
		ResultsTable profiles=new ResultsTable();
		
		double [] x=new double [length];
		double scale=edgeWidth.getCalibration().pixelWidth;
		for (int i=0;i<length;i++) {
			x[i]=i*scale;
		}
		findMaxima();
		ImageProcessor ip_maxima=detectEdges();
		int max=maxTop.length;
		
		
		
		
		ImagePlus imp=new ImagePlus("edge",ip_maxima);
		imp.show();
		
		for (int n=0;n<max;n+=2) {
			
			
			
			rt.addRow();
			
			//profiles.addColumns();
			IJ.log("max"+n+"    "+maxTop[n]);
			ip_maxima.setLineWidth(10);
			double line []=getProfile(ip_maxima,10,maxTop[n]-length/2,50,maxTop[n]+length/2,50);
					//ip_maxima.getLine(maxTop[n]-length/2,50,maxTop[n]+length/2,50);
			
			//imp.setRoi(maxTop[n]-length/2,50,length,10);
			//imp.updateAndDraw();
			
			profiles.setValues(""+n, line);
			
			
			CurveFitter cf=new CurveFitter(x,line);
			cf.doFit(CurveFitter.GAUSSIAN);
			double []param=cf.getParams();
			int num=cf.getNumParams();
			
			for (int i=0;i<num;i++) {
				rt.addValue("p"+i, param[i]);
				
			}
			rt.addValue("R^2", cf.getRSquared());
			cf.getPlot().show();
			
		}
		rt.show("FitResults");
		profiles.show("Profiles");
	}
	double [] getProfile(ImageProcessor ip, int lineWidth,double x1,double y1,double x2,double y2) {
		double [] profile=ip.getLine(x1,y1-lineWidth/2.0,x2,y2-lineWidth/2.0);
		IJ.log(""+(y1-lineWidth/2.0));
		int length=profile.length;
		
		for (int n=1;n<lineWidth;n++) {
			double [] line =ip_ew.getLine(x1,y1-(lineWidth/2.0)+n,x2,y2-(lineWidth/2.0)+n);
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
		this.maxTop=MaximumFinder.findMaxima(lineTop, 12000, false);
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
