package ch.epfl.biop.ij2command.USAF;

import java.util.Arrays;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Roi;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.plugin.filter.MaximumFinder;
import ij.process.ImageProcessor;

public class EdgeWidthAnalyser {
	private static final int TOP=1,MIDDLE=2,BOTTOM=3;
	private int analysisSlice;
	private int[] maxBottom;
	private int[] maxTop;
	private double edgeDistance;
	private double pixelWidth;
	private boolean showFit;
	private boolean showEdges;
	private ImagePlus input, crop;
	private ImageProcessor ip_edge;
	private ResultsTable fitWidth=new ResultsTable();
	private ResultsTable profiles=new ResultsTable();
	private Roi cropRoi;
	private int height;
	/**
 * Constructors	
 */
	EdgeWidthAnalyser(){
		
	}
	EdgeWidthAnalyser(ImagePlus imp){
		this.height=imp.getHeight();
		this.pixelWidth=imp.getCalibration().pixelWidth;
		this.setRoi(imp.getHeight(), MIDDLE,imp);
		this.ip_edge=imp.getProcessor();
		detectEdges();
		
	}
	EdgeWidthAnalyser(ImagePlus imp, int slice, int height){
				
		
		imp.setSliceWithoutUpdate(slice);
//		this.input=imp;
		this.pixelWidth=imp.getCalibration().pixelWidth;
		setSlice(slice);
		this.setRoi(height, MIDDLE,imp);
		
		imp.setRoi(cropRoi);
		this.crop=imp.crop();
		
		
		this.ip_edge=this.crop.getProcessor();
		detectEdges();
		
		
		
	}
	private void detectEdges() {
	//		
			ip_edge=ip_edge.convertToFloat();
			ip_edge.findEdges();
			
					
		}
	void findMaxima(int linewidth){
		//ImageProcessor ip_maxima=ip_edge;
		int w=ip_edge.getWidth();
		int h=ip_edge.getHeight();
		ip_edge.setLineWidth(linewidth);;
//		new ImagePlus("Test",ip_edge).show();
		double [] lineTop=ip_edge.getLine(0, 10, w, 10);
		double [] lineBottom=ip_edge.getLine(0, h-10, w, h-10);
		this.maxTop=MaximumFinder.findMaxima(lineTop, 8000, false);
		this.maxBottom=MaximumFinder.findMaxima(lineBottom, 8000, false);
		Arrays.sort(this.maxTop);
		Arrays.sort(this.maxBottom);
		IJ.log("top: "+maxTop.length);
		IJ.log("bottom: "+maxBottom.length);
		
		
	}
	void findVirtualFocus(boolean plot) {
			double [] x=fitWidth.getColumn("position");
			double [] y=fitWidth.getColumn("p3");
			CurveFitter cf=new CurveFitter(x,y);
			
			
			cf.doFit(CurveFitter.POLY2);
			
			double [] param=cf.getParams();
			if (plot) cf.getPlot().show();
			if (WindowManager.getWindow("VirtualFocusResults")==null) {
				ResultsTable rt=new ResultsTable();
				rt.show("VirtualFocusResults");
			}
			ResultsTable rt=ResultsTable.getResultsTable("VirtualFocusResults");
			double a=param[0];
			double b=param[1];
			double c=param[2];
			double x0=-b/(2*c);
	//		double D=param[3];
			
	//		double p=2.0*C/(3.0*D);
	//		double q=B/(3.0*D);
	//		double x1=(-p/2.0)-Math.sqrt((p*p/4.0)-q);
	//		double x2=(-p/2.0)+Math.sqrt((p*p/4.0)-q);
	
			rt.addRow();
			rt.addValue("File",crop.getTitle());
			rt.addValue("Slice",analysisSlice);
			rt.addValue("a",a);
			rt.addValue("b",b);
			rt.addValue("c",c);
	//		rt.addValue("D",D);
			rt.addValue("R^2", cf.getRSquared());
			rt.addValue("x0",x0);
	//		rt.addValue("q",q);
	//		rt.addValue("x1",x1*pixelWidth);
	//		rt.addValue("x2",x2*pixelWidth);
			
			rt.show("VirtualFocusResults");
			
			
		}
	void fitEdgeWidth() {
		
		ImageStack fitWin=new ImageStack();
		findMaxima(10);
		int length=setFitLineLength(maxTop);
		double [] x=new double [length];
		
		for (int i=0;i<length;i++) {
			x[i]=i*pixelWidth;
		}
		
		profiles.setValues("x/um", x);
				
		int max=maxTop.length;
		
		int width=crop.getWidth();
		for (int n=0;n<max;n+=2) {
			
			if (maxTop[n]-length/2>0 && maxTop[n]+length/2<width) {
				fitWidth.addRow();
					
				double line []=getProfile(ip_edge,10,maxTop[n]-length/2,50,maxTop[n]+length/2,50);
				
				profiles.setValues(""+IJ.d2s(maxTop[n]*pixelWidth,1), line);
				
				CurveFitter cf=new CurveFitter(x,line);
				cf.doFit(CurveFitter.GAUSSIAN);
				double []param=cf.getParams();
				int num=cf.getNumParams();
				
				fitWidth.addValue("position", maxTop[n]*pixelWidth);
				for (int i=0;i<num;i++) {
					fitWidth.addValue("p"+i, param[i]);
					
				}
				fitWidth.addValue("R^2", cf.getRSquared());
				
				if (showFit) fitWin.addSlice(cf.getPlot().getProcessor());
			}
		}
		fitWidth.show("FitResults");
		
		profiles.show("Profiles");
		
		if (showFit) new ImagePlus("Fit Windows",fitWin).show();
		if (showEdges) {
			ImagePlus imp=new ImagePlus("edge",ip_edge);
			imp.show();
		}
	}
	void globalVirtualFocusFit(ResultsTable width) {
			
			ImageStack globalFitWin=new ImageStack();
			ResultsTable rt=ResultsTable.getResultsTable("VirtualFocusResults");
			ResultsTable globalFitResults=new ResultsTable();
			double [] x0=rt.getColumn("x0");
			int numCol=width.getLastColumn();
			int numRow=width.size();
			double [] x=new double [numRow*numCol];
			double [] y=new double [numRow*numCol];
			
			
			
			for (int i=1;i<numCol;i++) {
				
				
				for (int r=0;r<numRow;r++) {
					y[(i-1)*numRow+r]=width.getValueAsDouble(i, r);
					
					x[(i-1)*numRow+r]=width.getValueAsDouble(0, r)-x0[i];
				}
				
				
				
			}
			CurveFitter cf=new CurveFitter(x,y);
			cf.doFit(CurveFitter.POLY2);
			cf.getPlot().show();
			double []param=cf.getParams();
			double s=param[2];
			double xc=param[1]/(-2*s);
			double deltaY=param[0]-s*xc*xc;
			x=width.getColumnAsDoubles(0);
			double [] initParam=new double [2];
			initParam[0]=300;
			initParam[1]=deltaY;
	//		initParam[2]=300;
	//		initParam[0]=0.7;
	//		initParam[1]=-0.15;
	//		initParam[2]=2e-6;;
			for (int i=1;i<numCol;i++) {
				y=width.getColumnAsDoubles(i);
				cf=new CurveFitter (x,y);
				
				cf.doCustomFit("y=a+"+s+"*Math.pow(x-b,2)", initParam, false);
	//			cf.doCustomFit("y=a+b*x+c*x*x", initParam, false);
				
				
				globalFitWin.addSlice(cf.getPlot().getProcessor());
				double [] params=cf.getParams();
				globalFitResults.addRow();
				globalFitResults.addValue("i", i);
				globalFitResults.addValue("delta y", params[0]);
				globalFitResults.addValue("xc", params[1]);
				
			}
			globalFitResults.show("Global Fit Results");
			new ImagePlus("Global Fits",globalFitWin).show();
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
	ImageProcessor getProcessor() {
		return this.ip_edge;
	}
	void setSlice(int slice) {
		IJ.log("Slice:"+slice);
//		if (slice>input.getStackSize()) slice=input.getStackSize();
		this.analysisSlice=slice;
		
	}
	
	void setRoi(int size,int position, ImagePlus imp) {
		int w=imp.getWidth();
		int h=imp.getHeight();
		if (size>h)size=h;
		Roi roi=new Roi(0,0,0,0);
		
		if (position==EdgeWidthAnalyser.TOP) {
			roi=new Roi(0,h/4,w,size);
		}
		if (position==EdgeWidthAnalyser.MIDDLE) {
			roi=new Roi(0,h/2,w,size);
		}
		if (position==EdgeWidthAnalyser.BOTTOM) {
			roi=new Roi(0,3*h/4,w,size);
		}
		this.cropRoi=roi;
	}
	void setShowFit(boolean fit) {
		this.showFit=fit;
	}
	void setShowEdges(boolean edge) {
		this.showEdges=edge;
	}
	int setFitLineLength(int[]maxima) {
		int length=maxima.length;
		double [] x=new double [length];
		double [] y=new double [length];
		
		for (int i=0;i<length;i++){
			x[i]=i;
			y[i]=maxima[i];
		}
		CurveFitter cf=new CurveFitter(x,y);
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		double [] param=cf.getParams();
		//cf.getPlot().show();
		IJ.log("Edge distance:"+IJ.d2s(param[1],1)+" pixel");
		this.edgeDistance=param[1]*pixelWidth;
		return (int)(0.95*param[1]);
		
	}
	
	int [] getTopMaxima() {
		return maxTop;
	}
	int [] getBottomMaxima() {
		return maxBottom;
	}
	int getHeight() {
		return this.height;
		
	}
}
