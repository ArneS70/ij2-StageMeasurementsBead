package ch.epfl.biop.ij2command;

import java.awt.Polygon;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
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
	private static final String [] header= {"z-offset","z-height","z-center","R^2","x-center","y-center"};
	private double xc,yc,zc,r2,zoff,zheight,fwhm;
	private ResultsTable results=new ResultsTable();
	private ResultsTable resultsRefined=new ResultsTable();
	private int gap=1;
	
	
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
			zproject.getProcessor().blurGaussian(2);
			MaximumFinder max=new MaximumFinder();
			Polygon points=max.getMaxima(zproject.getProcessor(), 10, true);
			
			this.xc=points.xpoints[0]*ImageCalibration.pixelWidth;
			this.yc=points.ypoints[0]*ImageCalibration.pixelHeight;
			
			OvalRoi circle=new OvalRoi(points.xpoints[0]-(int)(0.5*diameter),points.ypoints[0]-(int)(0.5*diameter),(int)diameter,(int)diameter,toProject);
	
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
	private void fitXY(ImageProcessor ip,int frame,int slice) {
			double x1=(xc-0.75*diameter)/ImageCalibration.pixelWidth;
			double x2=(xc+0.75*diameter)/ImageCalibration.pixelWidth;
			double y1=yc/ImageCalibration.pixelHeight;
			Line toFit=new Line (x1,y1,x2,y1);
			
			ImagePlus imp=new ImagePlus("Frame"+frame+"_slice"+slice,ip);
			imp.setRoi(toFit);
//			imp.show();
			SuperGaussFitter xpos=new SuperGaussFitter(ip,toFit);
//			xpos.showFit();
			double [] results=xpos.getResults();
//			IJ.log(""+results[0]+"//"+results[1]+"//"+results[2]);
			xc=(x1+results[2])*ImageCalibration.pixelWidth;
			x1=xc/ImageCalibration.pixelWidth;
			y1=(yc-0.75*diameter)/ImageCalibration.pixelHeight;
			double y2=(yc+0.75*diameter)/ImageCalibration.pixelHeight;
			
			toFit=new Line (x1,y1,x1,y2);
			imp.setRoi(toFit);
//			imp.show();
			SuperGaussFitter ypos=new SuperGaussFitter(ip,toFit);
//			ypos.showFit();
			results=ypos.getResults();
//			IJ.log(""+results[0]+"//"+results[1]+"//"+results[2]);
			yc=(results[2]+y1)*ImageCalibration.pixelHeight;
			writeResults(resultsRefined,frame);
			imp.close();
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
	//	Plot Zposition=new Plot("Z axis plot", "Position", "Intensity", pos, zIntensity);
	//	Zposition.show();
		GaussFitter gf=new GaussFitter(pos,zIntensity);
		double [] results=gf.getResults();
		return results[2];
	}
	private void writeResults(int frame) {
		results.incrementCounter();
		results.addValue("Frame",frame);
		results.addValue(SimpleBeadTracker.header[4], xc);
		results.addValue(SimpleBeadTracker.header[5], yc);
		results.addValue(SimpleBeadTracker.header[2], zc);
	}
	private void writeResults(ResultsTable table, int frame) {
		table.incrementCounter();
		table.addValue("Frame",frame);
		table.addValue(SimpleBeadTracker.header[4], xc);
		table.addValue(SimpleBeadTracker.header[5], yc);
		table.addValue(SimpleBeadTracker.header[2], zc);
	}
}
