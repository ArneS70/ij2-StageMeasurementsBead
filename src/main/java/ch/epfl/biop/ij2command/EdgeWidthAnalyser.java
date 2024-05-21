package ch.epfl.biop.ij2command;

import java.lang.reflect.Array;
import java.util.Arrays;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.plugin.filter.Filters;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;

public class EdgeWidthAnalyser {
	private ImagePlus edgeWidth;
	private ImageProcessor ip_ew;
	private int[] maxTop,maxBottom;
	EdgeWidthAnalyser(ImagePlus imp){
		this.edgeWidth=imp;
		imp.setSliceWithoutUpdate(200);
		this.ip_ew=imp.getProcessor();
	}
	ImageProcessor detectEdges() {
		ImageProcessor ip_edge=ip_ew.duplicate();
		ip_edge=ip_edge.convertToFloat();
		Filters f=new Filters();
		
		ip_edge.findEdges();
		ImagePlus imp=new ImagePlus("edge",ip_edge);
		imp.show();
		return ip_edge;
	}
	void fitEdgeWidth(int length) {
		ResultsTable rt=new ResultsTable();
		
		double [] x=new double [length];
		double scale=edgeWidth.getCalibration().pixelWidth;
		for (int i=0;i<length;i++) {
			x[i]=i*scale;
		}
		findMaxima();
		ImageProcessor ip_maxima=detectEdges();
		int max=maxTop.length;
		for (int n=1;n<max;n+=2) {
			rt.addRow();
			double line []=ip_maxima.getLine(maxTop[n]-length/2,520,maxTop[n]+length/2,520);
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
	}
	void findMaxima(){
		ImageProcessor ip_maxima=detectEdges();
		int w=ip_maxima.getWidth();
		int h=ip_maxima.getHeight();
		
		double [] lineTop=ip_maxima.getLine(10, 10, w-10, 10);
		double [] lineBottom=ip_maxima.getLine(10, h-10, w-10, h-10);
		this.maxTop=MaximumFinder.findMaxima(lineTop, 12000, false);
		this.maxBottom=MaximumFinder.findMaxima(lineBottom, 12000, false);
		Arrays.sort(this.maxTop);
		Arrays.sort(this.maxBottom);
		//IJ.log("top: "+maxTop.length);
		//IJ.log("bottom: "+maxBottom.length);
		
		
	}
	ImageProcessor getProcessor() {
		return this.ip_ew;
	}
}
