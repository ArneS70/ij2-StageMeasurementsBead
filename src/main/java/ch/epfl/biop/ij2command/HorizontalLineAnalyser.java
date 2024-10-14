package ch.epfl.biop.ij2command;

import java.util.Arrays;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.plugin.filter.MaximumFinder;
import ij.process.ImageProcessor;


public class HorizontalLineAnalyser extends HorizontalAnalysis{
	private double [] profile;
	private double [] xvalues;
	
	private int method;
	private FitterFunction fitFunc;
	private ResultsTable fitResults, profileTableValues;
/**   
 * Constructors
 */
	HorizontalLineAnalyser(){
		
	}
	HorizontalLineAnalyser(ImagePlus imp){
		super(imp);
		this.cal=imp.getCalibration();
		double pixelSize=cal.pixelWidth;
		ImageProcessor ip=imp.getProcessor();
		

//		this.setHorizontalLine(inputImage.getNSlices()/2);
//		this.profile=ip.getLine((double)horizontalLine.x1,(double)horizontalLine.y1,(double)horizontalLine.x2,(double)horizontalLine.y2);
//		int profileLength=profile.length;
//		this.xvalues=new double [profileLength];
		
	}
	HorizontalLineAnalyser(ImagePlus imp, Line line){
		profileTableValues=new ResultsTable();
		profileTableValues.show("Line Profiles");
		this.inputImage=imp;
		this.horizontalLine=line;
		this.cal=imp.getCalibration();
		ImageProcessor ip=imp.getProcessor();
		this.profile=ip.getLine((double)line.x1,(double)line.y1,(double)line.x2,(double)line.y2);
		int profileLength=profile.length;
		this.xvalues=new double [profileLength];
		double pixelSize=cal.pixelWidth;
		
		for (int n=0;n<profileLength;n++) {
			xvalues[n]=n*pixelSize;
		}
		fitResults=ResultsTable.getResultsTable("Horizontal Line Fits");
		if (fitResults==null) fitResults=new ResultsTable();
		fitResults.show("Horizontal Line Fits");
/*		int count=profileLength/10;
		double [] redX =new double [count];
		double [] redProf =new double [count];
		
		for (int n=0;n<count;n++) {
			redX[n]=x[n*10];
			redProf[n]=profile[n*10];
			
		}
*/		
		
		
//		a2s=new Asym2SigFitter(x,profile);
//		a2s.fit();
	}
	void setHorizontalLine(int slice) {
		
			
			inputImage.setSlice(slice);
			ImageProcessor ip_edge=inputImage.getProcessor().duplicate().convertToFloat();
			ip_edge.findEdges();
			LineAnalyser la=new LineAnalyser(new ImagePlus("Edges",ip_edge),1);
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
			
			
		
	}
	
	void writeFitResultsTable(int method, boolean profileTable) {
		
		int slices=inputImage.getNSlices();
		
		for (int n=1;n<=slices;n+=zstep) {
			
			IJ.log("===================================");
			IJ.log("Slice: "+n);
			
			inputImage.setSliceWithoutUpdate(n);
			this.profile=inputImage.getProcessor().getLine((double)horizontalLine.x1,(double)horizontalLine.y1,(double)horizontalLine.x2,(double)horizontalLine.y2);
/*			if (method==FitterFunction.Gauss) {
				int length=GaussFitter.header.length;
				this.fitFunc=new GaussFitter(xvalues,profile);
				this.method=FitterFunction.Gauss;	
				double [] results=((GaussFitter) fitFunc).getResults();
				fitResults=ResultsTable.getResultsTable("Horizontal Line Fits");
				fitResults.addRow();
				for (int i=0;i<length-1;i++) {
					fitResults.addValue(GaussFitter.header[i], results[i]);
				}
				fitResults.updateResults();
			}
*/			
			if (method==FitterFunction.AsymGauss) {
				this.fitFunc=new Asym2SigFitter(xvalues,profile);
				this.method=FitterFunction.AsymGauss;
				fitResults.updateResults();
			}
			if (method==FitterFunction.Poly3) {
				int length=Poly3Fitter.header.length;
				this.fitFunc=new Poly3Fitter(xvalues,profile);
				fitFunc.setHeader(Poly3Fitter.header);
				this.method=FitterFunction.Poly3;
				double [] results=this.fitFunc.getParameter();
				
				fitResults=ResultsTable.getResultsTable("Horizontal Line Fits");
				fitResults.addRow();
				int counter=fitResults.getCounter();
				for (int i=0;i<length-1;i++) {
					fitResults.addValue("z / um", counter*cal.pixelDepth);
					fitResults.addValue("p"+i, results[i]);
					//fitResults.addValue(fitFunc.header[i], results[i]);
				}
				fitResults.addValue("max", fitFunc.getMax());
		
		
			if (profileTable) {
				this.profileTableValues=ResultsTable.getResultsTable("Line Profiles");
				
				if (this.profileTableValues==null) {
					this.profileTableValues=new ResultsTable();
					this.profileTableValues.setValues("x", this.xvalues);
					this.profileTableValues.setValues(""+IJ.d2s(n*cal.pixelDepth), this.profile);
					
				} else {	
					
					this.profileTableValues.setValues(""+IJ.d2s(n*cal.pixelDepth), this.profile);
					
				};
				
				profileTableValues.show("Line Profiles");
				
			}
			
			fitResults.show("Horizontal Line Fits");
		}
	}
	}
	void setZstep(int step) {
		this.zstep=step;
	}
/*	double calculateHorizontalSpacing(int linewidth){
		inputImage.setSlice(super.stackCenter);
		LineAnalyser spacing=new LineAnalyser (new ImagePlus("edge",this.inputImage.getProcessor().duplicate()));
		spacing.getProcessor().findEdges();
//		new ImagePlus("test",spacing.getProcessor()).show();
		spacing.setProfile(LineAnalyser.CENTER);
//		spacing.getProfilPlot().show();
		double [] line=spacing.getProfile();
		
		double max=spacing.getMax();
		double min=spacing.getMin();
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
		cf.getPlot().show();
		double []param=cf.getParams();
		return param[1];
		
		
	}*/
	Line optimizeHorizontalMaxima(Line line) {
		
		ImageProcessor ip=inputImage.getProcessor().duplicate();
		ImagePlus imp=new ImagePlus("Maxima",ip);
//		double space=Math.abs(new HorizontalLineAnalyser(imp).calculateHorizontalSpacing(5));
		double space=20;		
		double profile []=new LineAnalyser(imp).getProcessor().getLine(line.x1d+20,line.y1d-space,line.x1d+20,line.y1d+space);
		double [] x=new double[profile.length];
		int profLen=x.length;
		for (int i=0;i<profLen;i++) {
			x[i]=i;
		}
		CurveFitter cf=new CurveFitter(x,profile);
		cf.doFit(CurveFitter.GAUSSIAN);
//		cf.getPlot().show();
		double [] paramLeft=cf.getParams();
		
		profile=new LineAnalyser(imp).getProcessor().getLine(line.x2d-20,line.y2d-space,line.x2d-20,line.y2d+space);
		
		cf=new CurveFitter(x,profile);
		cf.doFit(CurveFitter.GAUSSIAN);
//		cf.getPlot().show();
		double [] paramRight=cf.getParams();
		
		
		
		
		return new Line(10,line.y1d-10+paramLeft[2],line.x2d-10,line.y2d-10+paramRight[2]);
	
}
//	}
//	double [] getResults() {
//		return a2s.getResults(false);
//	}
}

