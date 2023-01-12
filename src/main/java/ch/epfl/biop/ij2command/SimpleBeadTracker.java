package ch.epfl.biop.ij2command;

import java.awt.Polygon;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.plugin.ZProjector;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class SimpleBeadTracker {
	private ImagePlus toTrack;
	private Calibration ImageCalibration;
	private int width, height,channels,slices,frames;
	private double diameter,zRes;
	private static final String [] header= {"z-offset","z-height","z-center/um","R^2","x-center/um","y-center/um"};
	private double xc,yc,zc,r2,zoff,zheight,fwhm;
	private ResultsTable results=new ResultsTable();
	private ResultsTable resultsRefined=new ResultsTable();
	private int gap=1;
	private boolean showFit=false;
	
	
	SimpleBeadTracker(ImagePlus imp,double beadDiameter){
		this.toTrack=imp;
		this.diameter=beadDiameter;
		if (toTrack==null) return;
		this.ImageCalibration=imp.getCalibration();
		pasteImageDimension(imp.getDimensions());
		zRes=ImageCalibration.pixelHeight;
		
	}
	
	private void pasteImageDimension(int[] dimensions) {
    	int length=dimensions.length;
    	if (dimensions==null) return;
    	this.width=dimensions[0];
    	if (length>0) this.height=dimensions[1]; else return;
    	
    	if (length>1) this.channels=dimensions[2]; else return;
    	if (length>2) this.slices=dimensions[3]; else return;
    	if (length>3) this.frames=dimensions[4]; else return;
    	
    }
	public void analyzeStack() {
		for (int f=0;f<frames;f+=gap) {
			
			ImageStack zStack=new ImageStack();
			for (int s=0;s<slices;s++) {
	 			toTrack.setSlice(f*slices+s+1);
	 			zStack.addSlice(toTrack.getProcessor());
	 			
	 		}
			
			ImagePlus toProject=new ImagePlus("Z-stack t="+f,zStack);
	//		toProject.getProcessor().blurGaussian(2);
			ZProjector project=new ZProjector();
			project.setImage(toProject);
			project.setMethod(ZProjector.MAX_METHOD);
			project.doProjection();
			ImagePlus zproject=project.getProjection();
			findMaxima(zproject);
			
			OvalRoi circle=new OvalRoi(xc-0.5*diameter,yc-0.5*diameter,diameter,diameter);
	
			toProject.setRoi(circle);
			this.zc=measureZMax(toProject,circle);
			int zpos=(int)Math.round(zc/zRes);
			toTrack.setZ(zpos);
			toTrack.setT(f);
			writeResults(f);
			fitXY(toTrack.getProcessor(),f,zpos);
		}
		results.show("BeadTrackingResults");
		resultsRefined.show("BeadTrackingResults (update)");
	}
	void findMaxima(ImagePlus maxima) {
		
		maxima.getProcessor().blurGaussian(2);
		MaximumFinder max=new MaximumFinder();
		int numPoints=100;
		int thres=1;
		Polygon points=null;
		while (numPoints>1){
			points=max.getMaxima(maxima.getProcessor(), thres, true);
			numPoints=points.npoints;
			thres*=2;
		}
		this.xc=points.xpoints[0];
		this.yc=points.ypoints[0];
		
		
	}
	private void fitXY(ImageProcessor ip,int frame,int slice) {
			double x1=(xc-0.75*diameter);
			double x2=(xc+0.75*diameter);
			double y1=yc;
			Line toFit=new Line (x1,y1,x2,y1);
			
			ImagePlus imp=new ImagePlus("Frame"+frame+"_slice"+slice,ip);
			imp.setRoi(toFit);
//			imp.show();
			SuperGaussFitter xpos=new SuperGaussFitter(ip,toFit);
			if (showFit) xpos.showFit();
			double [] results=xpos.getResults();
//			IJ.log(""+results[0]+"//"+results[1]+"//"+results[2]);
			xc=(x1+results[2]);
			x1=xc;
			y1=(yc-0.75*diameter);
			double y2=(yc+0.75*diameter);
			
			toFit=new Line (x1,y1,x1,y2);
			imp.setRoi(toFit);
//			imp.show();
			SuperGaussFitter ypos=new SuperGaussFitter(ip,toFit);
			if (showFit) ypos.showFit();
			results=ypos.getResults();
//			IJ.log(""+results[0]+"//"+results[1]+"//"+results[2]);
			yc=(results[2]+y1);
			writeResults(resultsRefined,frame);
//			imp.close();
	}
	public void showRois(String tableName) {
			ResultsTable display=ResultsTable.getResultsTable(tableName);
			RoiManager rm=RoiManager.getRoiManager();
			
			if (display==null) return;
			double [] frame=display.getColumn("Frame"); 
			double []x=display.getColumn(SimpleBeadTracker.header[4]);
			double []y=display.getColumn(SimpleBeadTracker.header[5]);
			double []z=display.getColumn(SimpleBeadTracker.header[2]);
			int length=x.length;
			for (int i=0;i<length;i++) {
				double xpos=x[i]/ImageCalibration.pixelWidth;
				double ypos=y[i]/ImageCalibration.pixelHeight;
				int zpos=(int)Math.round(z[i]/zRes);
//				toTrack.setT(i);
//				toTrack.setZ(zpos);
				OvalRoi circle=new OvalRoi(xpos-diameter/2,ypos-diameter/2,diameter,diameter);
				circle.setPosition(1, zpos, (int)frame[i]);
				rm.add(circle, 2);
//				Overlay over=new Overlay();
//				over.setStrokeWidth(3.0);
//				over.add((Roi) circle.clone());
//				toTrack.setOverlay(over);
//				toTrack.show();
						
			}
//			toTrack.show();
	}
	public void setGap(int delta) {
		if (delta<frames) gap=delta;
		else gap=frames;
	}
	public void showFit() {
		this.showFit=true;
	}
	public void hideFit() {
		this.showFit=false;
	}
	private double measureZMax(ImagePlus imp, OvalRoi roi) {
		int nSlices=imp.getImageStackSize();
		
		double [] zIntensity=new double [nSlices];
		double [] pos=new double [nSlices];
		
		imp.setRoi(roi);
		for (int s=0;s<nSlices;s++) {
			pos[s]=s*zRes;
			imp.setSlice(s);
			ImageStatistics stat=imp.getProcessor().getStatistics();
			zIntensity[s]=stat.mean;
			
		}
//		Plot Zposition=new Plot("Z axis plot", "Position", "Intensity", pos, zIntensity);
//		Zposition.show();
		GaussFitter gf=new GaussFitter(pos,zIntensity);
//		gf.fixAmplitude(nSlices);
		double [] results=gf.getResults();
		return results[2];
	}
	private void writeResults(int frame) {
		results.incrementCounter();
		results.addValue("Frame",frame);
		results.addValue(SimpleBeadTracker.header[4], xc*ImageCalibration.pixelWidth);
		results.addValue(SimpleBeadTracker.header[5], yc*ImageCalibration.pixelHeight);
		results.addValue(SimpleBeadTracker.header[2], zc);
	}
	private void writeResults(ResultsTable table, int frame) {
		table.incrementCounter();
		table.addValue("Frame",frame);
		table.addValue(SimpleBeadTracker.header[4], xc*ImageCalibration.pixelWidth);
		table.addValue(SimpleBeadTracker.header[5], yc*ImageCalibration.pixelHeight);
		table.addValue(SimpleBeadTracker.header[2], zc);
	}
}
