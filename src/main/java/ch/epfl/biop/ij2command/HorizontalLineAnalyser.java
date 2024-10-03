package ch.epfl.biop.ij2command;

import java.util.Arrays;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Plot;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;


public class HorizontalLineAnalyser {
	double [] profile;
	double [] xvalues;
	Line inputLine;
	private int method,zstep=1;
	private ImagePlus input;
	private Calibration cal;
	private FitterFunction fitFunc;
	private ResultsTable fitResults, profileTableValues;
/**   
 * Constructors
 */
	HorizontalLineAnalyser(){
		
	}
	HorizontalLineAnalyser(ImagePlus imp, Line line){
		this.input=imp;
		this.inputLine=line;
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
	void writeFitResultsTable(int method, boolean profileTable) {
		
		int slices=input.getNSlices();
		
		for (int n=1;n<=slices;n+=zstep) {
			
			IJ.log("===================================");
			IJ.log("Slice: "+n);
			
			input.setSliceWithoutUpdate(n);
			this.profile=input.getProcessor().getLine((double)inputLine.x1,(double)inputLine.y1,(double)inputLine.x2,(double)inputLine.y2);
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

