package ch.epfl.biop.ij2command;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Plot;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

public class HorizontalLineAnalyser {
	
/**   
 * Constructors
 */
	HorizontalLineAnalyser(){
		
	}
	HorizontalLineAnalyser(ImagePlus imp, Line line){
		
		Calibration cal=imp.getCalibration();
		ImageProcessor ip=imp.getProcessor();
		double [] profile=ip.getLine((double)line.x1,(double)line.y1,(double)line.x2,(double)line.y2);
		int profileLength=profile.length;
		double [] x=new double [profileLength];
		double pixelSize=cal.pixelWidth;
		
		for (int n=0;n<profileLength;n++) {
			x[n]=n*pixelSize;
		}
		double [] redX =new double [profileLength/10];
		double [] redProf =new double [profileLength/10];
		int count=0;
		for (int n=0;n<profileLength;n+=10) {
			redX[count]=x[n];
			redProf[count]=profile[n];
			count++;
		}
		
		Asym2SigFitter a2s=new Asym2SigFitter(redX,redProf);
		a2s.fit(true);
		
	}
	
}

