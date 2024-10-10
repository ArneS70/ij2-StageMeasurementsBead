package ch.epfl.biop.ij2command;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;
import ij.measure.ResultsTable;
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
	HorizontalLineAnalyser(ImagePlus imp){
		this.inputImage=imp;
//		this.setHorizontalLine(inputImage.getNSlices()/2);
		this.cal=imp.getCalibration();
		ImageProcessor ip=imp.getProcessor();
//		this.profile=ip.getLine((double)horizontalLine.x1,(double)horizontalLine.y1,(double)horizontalLine.x2,(double)horizontalLine.y2);
//		int profileLength=profile.length;
//		this.xvalues=new double [profileLength];
		double pixelSize=cal.pixelWidth;
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
	Line getHorizontalLIne() {
		return this.horizontalLine;
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
//	}
//	double [] getResults() {
//		return a2s.getResults(false);
//	}
}

