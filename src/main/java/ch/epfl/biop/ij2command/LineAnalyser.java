package ch.epfl.biop.ij2command;

import java.util.Arrays;

import ij.ImagePlus;
import ij.gui.Line;
import ij.measure.Calibration;
import ij.process.ImageProcessor;



public class LineAnalyser {
	private double [] profile;
	private int profileLength;
	private Calibration cal;
	private double [] x;
	private double mean, stdev;
	private Line line;
	double pixelSize;
	private int width;
	
	/**   
	 * Constructors
	 */
		LineAnalyser(){
			
		}
		LineAnalyser(ImagePlus imp, Line inputLine){
			
			this.line=inputLine;
			this.width=imp.getWidth();
			cal=imp.getCalibration();
			ImageProcessor ip=imp.getProcessor();
			profile=ip.getLine((double)line.x1,(double)line.y1,(double)line.x2,(double)line.y2);
			profileLength=profile.length;
			x=new double [profileLength];
			pixelSize=cal.pixelWidth;
			for (int n=0;n<profileLength;n++) {
				x[n]=n*pixelSize;
			}
		}
		double [] getProfile() {
			return profile;
		}
		double getMean() {
			return new ArrayStatistics(profile).getMean();
		}
		double getSTEDV() {
			return new ArrayStatistics(profile).getSTDEV();
		}
		double xPosition() {
			return pixelSize*(line.x1+line.x2)/2.0;
		}
}
