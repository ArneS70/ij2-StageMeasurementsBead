package ch.epfl.biop.ij2command;

import java.util.Arrays;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Plot;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;


public class HorizontalLineAnalyser {
	double [] profile;
	double [] xvalues;
	int method;
	FitterFunction fitFunc;
	ResultsTable fitResults=new ResultsTable();
/**   
 * Constructors
 */
	HorizontalLineAnalyser(){
		
	}
	HorizontalLineAnalyser(ImagePlus imp, Line line){
		
		Calibration cal=imp.getCalibration();
		ImageProcessor ip=imp.getProcessor();
		this.profile=ip.getLine((double)line.x1,(double)line.y1,(double)line.x2,(double)line.y2);
		int profileLength=profile.length;
		this.xvalues=new double [profileLength];
		double pixelSize=cal.pixelWidth;
		
		for (int n=0;n<profileLength;n++) {
			xvalues[n]=n*pixelSize;
		}
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
	void writeResultsTable(int method) {
		if (method==FitterFunction.Gauss) {
			int length=GaussFitter.header.length;
			this.fitFunc=new GaussFitter(xvalues,profile);
			
			this.method=FitterFunction.Gauss;	
			
			double [] results=((GaussFitter) fitFunc).getResults();
			
			fitResults.addRow();
			for (int i=0;i<length-1;i++) {
				
				fitResults.addValue(GaussFitter.header[i], results[i]);
			}
			fitResults.updateResults();
		}
		if (method==2) {
//			int length=Asym2SigFitter.header.length;
		
			this.fitFunc=new Asym2SigFitter(xvalues,profile);
			this.method=FitterFunction.AsymGauss;
			
//			double [] results=a2s.getResults(false);
//			for (int i=0;i<length;i++) {
//				fitResults.addValue(Asym2SigFitter.header[i], results[i]);
			}
			fitResults.updateResults();
		}
//	}
//	double [] getResults() {
//		return a2s.getResults(false);
//	}
}

