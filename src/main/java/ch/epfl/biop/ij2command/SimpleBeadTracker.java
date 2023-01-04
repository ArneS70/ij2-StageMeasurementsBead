package ch.epfl.biop.ij2command;

import java.awt.Polygon;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.measure.Calibration;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.plugin.ZProjector;
import ij.plugin.filter.MaximumFinder;
import ij.process.ImageStatistics;

public class SimpleBeadTracker {
	private ImagePlus toTrack;
	private Calibration ImageCalibration;
	private int width, height,channels,slices,frames;
	private double diameter,zRes;
	private static final String [] header= {"z-offset","z-height","z-center","R^2","x-center","y-center"};
	private double xc,yc,zc,r2,zoff,zheight,fwhm;
	private ResultsTable results=new ResultsTable();
	
	
	SimpleBeadTracker(ImagePlus imp,double beadDiameter){
		this.toTrack=imp;
		if (toTrack==null) return;
		this.ImageCalibration=imp.getCalibration();
		pasteImageDimension(imp.getDimensions());
		zRes=ImageCalibration.pixelHeight;
		analyzeStack();
		results.show("BeadTrackingResults");
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
		for (int f=0;f<frames;f+=1) {
			
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
			Overlay over=new Overlay();
			over.add(circle);
			toProject.setOverlay(over);
			toProject.show();
			toProject.setRoi(circle);
	//		ZAxisProfiler zap=new ZAxisProfiler();
	//	    zap.run("");
			this.zc=measureZMax(toProject,circle);
			writeResults();
		}
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
	private void writeResults() {
		results.incrementCounter();
		results.addValue(SimpleBeadTracker.header[2], zc);
		results.addValue(SimpleBeadTracker.header[4], xc);
		results.addValue(SimpleBeadTracker.header[5], yc);
	}
}
