package ch.epfl.biop.ij2command.stage.bead;

import java.awt.Polygon;
import java.io.File;

import ch.epfl.biop.ij2command.stage.general.ArrayStatistics;
import ch.epfl.biop.ij2command.stage.general.GaussFitter;
import ch.epfl.biop.ij2command.stage.general.SuperGaussFitter;
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
import ij.process.EllipseFitter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class SimpleBeadLocalizer {
	
	private static final String [] header= {"x-center/um","y-center/um","z-center/um","x diameter/um","y diameter/um","z-offset","z-height","R^2"};
	public static final String methodSimple= "Simple";
	public static final String methodEllipse= "Ellipse";
	public static final String methodGauss="SuperGauss Fit";
		
	public static final String ResultTableSimple = "Bead Localization Results--Simple";
	public static final String ResultTableEllipse="Bead Localization Results--Ellipse Fit";
	public static final String ResultTableGauss ="Bead Localization Results--SuperGauss Fit";
	
	private String methodSelection;
	
	private ImagePlus toTrack;
	private Calibration ImageCalibration;
	private int width, height,channels,slices,frames;
	private double diameter,zRes;
	ImageStack fitPlots=new ImageStack(600,400);
	
	private double xc,yc,zc,r2,zoff,zheight,fwhm,fitDiameter_x,fitDiameter_y;
	//xc, yc, zc are stored in calibrated coordinates =um
	private ResultsTable results=new ResultsTable();
	
	private ResultsTable summary=new ResultsTable();
	private ResultsTable resultsRefined=new ResultsTable();
	private int gap=1;
	private boolean showFit=false;
	private boolean hasResultsWindow=false;
	private boolean hasSummary=false;
	
	
	SimpleBeadLocalizer(ImagePlus imp,double beadDiameter, String method, int deltat){
		this.toTrack=imp;
		this.diameter=beadDiameter;
		this.methodSelection=method;
		this.gap=deltat;
		if (toTrack==null) return;
		this.ImageCalibration=imp.getCalibration();
		pasteImageDimension(imp.getDimensions());
		zRes=ImageCalibration.pixelHeight;
		
	}
//	String concat(String []) {
		
//	}
	public void run() {
		if (this.frames>0) analyzeTimeStack();
		showResults();
	}
	
	private void showResults() {
		results.show("Bead Localizing Results--"+this.ResultTableSimple);
		if (!methodSelection.contains("Simple")) resultsRefined.show("Bead Localizing Results--"+methodSelection);
		
		
		this.hasResultsWindow=true;
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
/*	public void run() {
		if (methodSelection.contains("Simple")) {
			findMaxima(toTrack);
		}
	}*/
	public void analyzeTimeStack() {
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
			
			OvalRoi circle=new OvalRoi((xc/ImageCalibration.pixelWidth)-0.5*diameter,yc/ImageCalibration.pixelHeight-0.5*diameter,diameter,diameter);
	
			toProject.setRoi(circle);
			this.zc=measureZMax(toProject,circle);
			int zpos=(int)Math.round(zc/zRes);
			if (!methodSelection.contains("Simple")) {
				toTrack.setZ(zpos);
				toTrack.setT(f);
				writeResults(f);
				if (methodSelection.contains("Gauss")) fitXY(toTrack.getProcessor(),f,zpos);
				if (methodSelection.contains("Ellipse"))fitEllipse(toTrack.getProcessor().duplicate(),f);
			} else writeResults(f); 
		}
		
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
		this.xc=points.xpoints[0]*ImageCalibration.pixelWidth;
		this.yc=points.ypoints[0]*ImageCalibration.pixelHeight;
		
		
	}
	private void fitEllipse(ImageProcessor ip,int frame) {
		ip.setAutoThreshold("Li dark");
		ImageProcessor mask=ip.createMask();
		ip.setMask(mask);
		EllipseFitter ef=new EllipseFitter();
		ef.fit(ip, null);
		xc=ef.xCenter*ImageCalibration.pixelWidth;;
		yc=ef.yCenter*ImageCalibration.pixelHeight;;
		fitDiameter_x=ef.major*ImageCalibration.pixelWidth;;
		fitDiameter_y=ef.minor*ImageCalibration.pixelHeight;;
		writeResults(frame);
		
	}
	/*	private void fitXY(ImageProcessor ip,int frame,int slice,int maxIteration, double delta) {
		double x_init=xc;
		double y_init=yc;
		
		
		ImagePlus imp=new ImagePlus("Frame"+frame+"_slice"+slice,ip);
		
		for (int i=0;i<maxIteration;i++) {
			
			double x1=(xc-1.5*diameter);
			double x2=(xc+1.5*diameter);
			double y1=yc;
			double y2=yc;
			Line toFit=new Line (x1,y1,x2,y1);
			imp.setRoi(toFit);
			imp.show();
			
			
			SuperGaussFitter xpos=new SuperGaussFitter(ip,toFit);
			if (showFit) xpos.showFit();
			double [] results=xpos.getResults();
	//		IJ.log(""+results[0]+"//"+results[1]+"//"+results[2]);
			double xc_new=(x1+results[2]);
			x1=xc_new;
			y1=(yc-1.5*diameter);
			y2=(yc+1.5*diameter);
			
			toFit=new Line (xc_new,y1,xc_new,y2);
			imp.setRoi(toFit);
			imp.show();
			SuperGaussFitter ypos=new SuperGaussFitter(ip,toFit);
			if (showFit) ypos.showFit();
			results=ypos.getResults();
	//		IJ.log(""+results[0]+"//"+results[1]+"//"+results[2]);
			double yc_new=(results[2]+y1);
			
			double diff=Math.pow(x_init-xc_new, 2)+Math.pow(y_init-yc_new, 2);
			IJ.log("x: "+xc_new+"    y: "+yc_new+"   "+diff+"  "+results[5]);
			xc=xc_new;
			yc=yc_new;
			
			
		}
		writeResults(resultsRefined,frame);
		imp.close();
}
*/
	private void fitXY(ImageProcessor ip,int frame,int slice) {
			xc/=ImageCalibration.pixelWidth;
			yc/=ImageCalibration.pixelWidth;
			
			double x1=(xc-1.5*diameter);
			double x2=(xc+1.5*diameter);
			double y1=yc;
			Line toFit=new Line (x1,y1,x2,y1);
			
			ImagePlus imp=new ImagePlus("Frame"+frame+"_slice"+slice,ip);
			imp.setRoi(toFit);
//			imp.show();
			SuperGaussFitter xpos=new SuperGaussFitter(ip,toFit);
			if (showFit) xpos.showFit();
			double [] results=xpos.getResults();
//			IJ.log(""+results[0]+"//"+results[1]+"//"+results[2]);
			xc=(x1+results[2]);//*ImageCalibration.pixelWidth;
			x1=xc;
			y1=(yc-1.5*diameter);
			double y2=(yc+1.5*diameter);
			fitDiameter_x=xpos.getDiameter();
			
			toFit=new Line (x1,y1,x1,y2);
			imp.setRoi(toFit);
//			imp.show();
			SuperGaussFitter ypos=new SuperGaussFitter(ip,toFit);
			if (showFit) ypos.showFit();
			results=ypos.getResults();
//			IJ.log(""+results[0]+"//"+results[1]+"//"+results[2]);
			yc=(results[2]+y1)*ImageCalibration.pixelWidth;
			fitDiameter_y=ypos.getDiameter();
			writeResults(resultsRefined,frame);
//			imp.close();
	}
	public void showRois(String tableName) {
			ResultsTable display=ResultsTable.getResultsTable(tableName);
			RoiManager rm=RoiManager.getRoiManager();
			
			if (display==null) return;
			double [] frame=display.getColumn("Frame"); 
			double []x=display.getColumn(SimpleBeadLocalizer.header[0]);
			double []y=display.getColumn(SimpleBeadLocalizer.header[1]);
			double []z=display.getColumn(SimpleBeadLocalizer.header[2]);
			double []diameter_x=null;
			double []diameter_y=null;
			OvalRoi circle=null;
			if (!tableName.contains("Simple")) {
				diameter_x=convert(display.getColumn(SimpleBeadLocalizer.header[3]),1/ImageCalibration.pixelWidth);
				diameter_y=convert(display.getColumn(SimpleBeadLocalizer.header[4]),1/ImageCalibration.pixelHeight);
			}
			int length=x.length;
			
			for (int i=0;i<length;i++) {
				double xpos=x[i]/ImageCalibration.pixelWidth;
				double ypos=y[i]/ImageCalibration.pixelHeight;
				int zpos=(int)Math.round(z[i]/zRes);
//				toTrack.setT(i);
//				toTrack.setZ(zpos);
				if (!tableName.contains("Simple")) {circle=new OvalRoi(xpos-diameter_x[i]/2,ypos-diameter_y[i]/2,diameter_x[i],diameter_y[i]);}
				else {circle=new OvalRoi(xpos-0.5*diameter,ypos-0.5*diameter,diameter,diameter);};
				
				circle.setPosition(1, zpos, (int)frame[i]+1);
				rm.add(circle, 2);
//				Overlay over=new Overlay();
//				over.setStrokeWidth(3.0);
//				over.add((Roi) circle.clone());
//				toTrack.setOverlay(over);
//				toTrack.show();
						
			}
//			toTrack.show();
	}
	private double [] convert(double []input,double convert) {
		int length=input.length;
		
		for (int i=0;i<length;i++) {
			input[i]=input[i]*convert;
		}
		return input;
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
		
		double [] fitResults=gf.getResults();
		return fitResults[2];
	}
	public ResultsTable summarizeResults(String name) {
		
		ResultsTable input=ResultsTable.getResultsTable(name);
		if (input==null) {IJ.showMessage("No results table found");return null;};
		
		double []x=input.getColumn("delta x");
		double []y=input.getColumn("delta y");
		double []z=input.getColumn("delta z");
		ArrayStatistics as=new ArrayStatistics(x);
		summary.incrementCounter();
		summary.addValue("delta x mean/um", as.getMean());
		summary.addValue("delta x stdev/um", as.getSTDEV());
		summary.addValue("delta x min/um", as.getMin());
		summary.addValue("delta x max/um", as.getMax());
		
		as=new ArrayStatistics(y);
		summary.addValue("delta y mean/um", as.getMean());
		summary.addValue("delta y stdev/um", as.getSTDEV());
		summary.addValue("delta y min/um", as.getMean());
		summary.addValue("delta y max/um", as.getMax());
		
		as=new ArrayStatistics(z);
		summary.addValue("delta z mean/um", as.getMean());
		summary.addValue("delta z stdev/um", as.getSTDEV());
		summary.addValue("detla z min/um", as.getMin());
		summary.addValue("delta z max/um", as.getMax());
		
		this.hasSummary=true;
		return summary;
	}
	public void saveResults(String path, File file) {
		
		String name=file.getName();
		int stop=name.lastIndexOf(".");
		String nameResultsFile=null;
		if (stop>0) {
			nameResultsFile=name.substring(0, stop);
//			IJ.log(nameResultsFile);
		} else nameResultsFile=name;
		
		if (this.hasResultsWindow) {
			//path=path.replace(name, nameResultsFile+"_Results.txt");
			results.save(path.replace(name, nameResultsFile+"_Results.txt"));
		}
		if (this.hasSummary) {
			//path=path.replace(name, nameResultsFile+"_Results.txt");
			results.save(path.replace(name, nameResultsFile+"_Results Summary.txt"));
		}
		
	}
	private boolean writeResults(int frame) {
		results.incrementCounter();
		results.addValue("Frame",frame);
		results.addValue(SimpleBeadLocalizer.header[0], xc);
		results.addValue(SimpleBeadLocalizer.header[1], yc);
		results.addValue(SimpleBeadLocalizer.header[2], zc);
		if (this.methodSelection.contains(methodEllipse)||this.methodSelection.contains(methodGauss)) {
			results.addValue(SimpleBeadLocalizer.header[3], fitDiameter_x);
			results.addValue(SimpleBeadLocalizer.header[4], fitDiameter_y);
		}
		double x0=results.getValue(SimpleBeadLocalizer.header[0],0);
		double y0=results.getValue(SimpleBeadLocalizer.header[1],0);
		double z0=results.getValue(SimpleBeadLocalizer.header[2],0);
		results.addValue("delta x",xc-x0);
		results.addValue("delta y",yc-y0);
		results.addValue("delta z",zc-z0);
		return true;
	}
	private boolean writeResults(ResultsTable table, int frame) {
		table.incrementCounter();
		table.addValue("Frame",frame);
		table.addValue(SimpleBeadLocalizer.header[0], xc);
		table.addValue(SimpleBeadLocalizer.header[1], yc);
		table.addValue(SimpleBeadLocalizer.header[2], zc);
		table.addValue(SimpleBeadLocalizer.header[3], fitDiameter_x);
		table.addValue(SimpleBeadLocalizer.header[4], fitDiameter_y);
		return true;
	}
}
