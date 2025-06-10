package ch.epfl.biop.ij2command.stage.bead;

import java.awt.Color;
import java.awt.Polygon;
import java.io.File;
import java.util.Vector;

import ch.epfl.biop.ij2command.stage.general.*;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;

import ij.gui.OvalRoi;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.measure.Calibration;

import ij.measure.ResultsTable;
import ij.plugin.ZProjector;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.frame.Fitter;
import ij.plugin.frame.RoiManager;
import ij.process.EllipseFitter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class SimpleBeadLocalizer {
	
	private static final String [] header= {"x-center/um","y-center/um","z-center/um","x diameter/um","y diameter/um","z-offset","z-height","R^2"};
	public static final String [] methods={"Simple","Ellipse","Super Gauss Fit","2D Gauss Fit","2D Gauss Sym"};
	public static final String methodSimple= "Simple";
	public static final String methodEllipse= "Ellipse";
	public static final String methodGauss="Super Gauss Fit";
	public static final String method2DGauss="2D Gauss Fit";
	public static final String method2DSymGauss="2D Gauss Sym";
		
	public static final String [] tableTitles= {"BeadLocalizationResults_Simple","BeadLocalizationResults_Ellipse Fit","BeadLocalizationResults_SuperGaussFit","BeadLocalizationResults_2DGaussFit","BeadLocalizationResults_2DGaussFit(symetric)"};
	
	private String methodSelection;
	private String actualResultsTitle;
	private Vector <Roi>regions=new Vector<Roi>();
	private String fileName;
	
	private ImagePlus toTrack;
	private Calibration ImageCalibration;
	private int width, height,channels,slices,frames;
	private double diameter,zRes;
	ImageStack fitPlots=new ImageStack(694,415);
	
	private double xc,yc,zc,amp,r2,zoff,zheight,fwhm,fitDiameter_x,fitDiameter_y;
	//xc, yc, zc are stored in uncalibrated coordinates
	private ResultsTable results=new ResultsTable();
	
	private ResultsTable summary=new ResultsTable();
	private ResultsTable resultsRefined=new ResultsTable();
	private int gap=1;
	private boolean showFit=false;
	private boolean hasResultsWindow=false;
	private boolean hasSummary=false;
	
	/**
	 * Constructor SimpleBeadLocalizer. 
	 * Contains the parameters for the localization.
	 * @param imp               input image/image stack 
	 * @param beadDiameter		size of the bead (estimate)
	 * @param method			method used for the localization
	 * @param deltat			variable to skip frames
	 */
	SimpleBeadLocalizer(ImagePlus imp,double beadDiameter, String method, int deltat){
		this.toTrack=imp;
		this.fileName=imp.getTitle();
		this.diameter=beadDiameter;
		this.methodSelection=method;
		this.gap=deltat;
		if (toTrack==null) return;
		this.ImageCalibration=imp.getCalibration();
		pasteImageDimension(imp.getDimensions());
		zRes=ImageCalibration.pixelDepth;
		
	}

	public void run() {
		if (this.frames>0) analyzeTimeStack();
		showResults();
		if (showFit) new ImagePlus ("Profie Plots",fitPlots).show();
	}
	
	private void showResults() {
		results.show(tableTitles[getMethodNumber()]);
//		if (!methodSelection.contains("Simple")) resultsRefined.show("Bead Localizing Results--"+methodSelection);
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
/**
 * Tracks a bead in 3D over time.
 * The variable gap is used to skip frames. 
 */
	public void analyzeTimeStack() {
		
		for (int f=0;f<frames;f+=gap) {
			IJ.log("*** Processing frame:"+f+" ***");
			// Create Z-stack per timepoint
			ImageStack zStack=new ImageStack();
			for (int s=0;s<slices;s++) {
	 			int pos=1+(f*slices)+s;
				toTrack.setSliceWithoutUpdate(pos);
	 			zStack.addSlice(toTrack.getProcessor());
	 		}

			// Localizes the bead in a in a z-projected image. 
			ImagePlus toProject=new ImagePlus("Z-stack t="+f,zStack);
			ZProjector project=new ZProjector();
			project.setImage(toProject);
			project.setMethod(ZProjector.MAX_METHOD);
			project.doProjection();
			ImagePlus zproject=project.getProjection();
			findMaxima(zproject);

			// Creates a circular Roi and stores it 
			OvalRoi circle=new OvalRoi(xc-0.5*diameter,yc-0.5*diameter,diameter,diameter);
			regions.add(circle);
			toProject.setRoi(circle);

			// Localizes the bead along the z-axis
			this.zc=measureZMax(toProject,circle);
			int zpos=(int)Math.round(zc);

			if (methodSelection.contains("Simple")) writeSimpleResults(f); 
			else {
				toTrack.setSliceWithoutUpdate(f*slices+zpos);
				preciseZposition(toProject);

				if (methodSelection.equals("Super Gauss Fit")) fitXY(toTrack.getProcessor(),f,zpos);
				if (methodSelection.equals("Ellipse"))fitEllipse(toTrack.getProcessor().duplicate(),f);
				if (methodSelection.equals("2D Gauss Fit"))fit2D(toTrack.getProcessor(),f,zpos);
				if (methodSelection.equals("2D Gauss Sym"))fitSym2D(toTrack.getProcessor(),f,zpos);
			}  
		}
//		results.show(actualResultsTitle);
		
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
		this.amp=maxima.getProcessor().getValue((int)xc, (int)yc);
		
		
	}
	private void preciseZposition (ImagePlus imp) {
		int len = imp.getStackSize();
		double [] x=new double [len];
		double [] y=new double [len];
		
		for (int i=0;i<len;i++) {
			imp.setSliceWithoutUpdate(i);
			x[i]=i;
			y[i]=imp.getStatistics().mean;		
		}
		
		LorentzFitter fitter=new LorentzFitter(x,y);
		double [] results=fitter.getFitResults();
		this.zc=results[3];
//		fitter.getPlot().show();
		
	}
	private void fitEllipse(ImageProcessor ip,int frame) {
	//	new ImagePlus("Test"+frame,ip).show();
		this.showFit=false;
		ip.setAutoThreshold("Li dark");
		ImageProcessor mask=ip.createMask();
		ip.setMask(mask);
		EllipseFitter ef=new EllipseFitter();
		ef.fit(ip, null);
		
		xc=ef.xCenter;
		yc=ef.yCenter;
		fitDiameter_x=ef.major;
		fitDiameter_y=ef.minor;
		regions.add(new OvalRoi(ef.xCenter, ef.yCenter, ef.major, ef.minor));
		
		
		writeResults(results,frame);
		
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
	/**
	 * Super Gauss Fit
	 */
	
	private void fitXY(ImageProcessor ip,int frame,int slice) {

			// Intensity Profile of the bead i x direction
			double x1=(xc-1.5*diameter);			//size of the line profile
			double x2=(xc+1.5*diameter);			//size of the line profile
			double y1=yc;
			

			
			double [] line=ip.getLine(x1, y1, x2, y1);  //get the line profile as array of doubles
			double [] x=new double [line.length];		//create array of doubles for x-values
			
			for (int i=0;i<line.length;i++) {			//populate the array via a loop
				x[i]=i;
			}
			
			FitterFunction maximum=new SuperGaussFitter(x,line);	//Fit function to find the maximum
			double [] fitResults=maximum.getFitResults();				//get the results of the fit
			
			if (showFit)											//show Plots of the fit
				fitPlots.addSlice(maximum.getPlot().getImagePlus().getProcessor());
			
			xc=(x1+fitResults[2]);							//Redefine center in x direction
			fitDiameter_x=2.35*fitResults[4];
			// Intensity Profile of the bead in y-direction
			
			x1=xc;
			y1=(yc-1.5*diameter);
			double y2=(yc+1.5*diameter);
			
			line=ip.getLine(x1, y1, x1, y2);			//get the line profile as array of doubles
			x=new double [line.length];
			
			for (int i=0;i<line.length;i++) {
				x[i]=i;
			}
			
			maximum=new SuperGaussFitter(x,line);
			fitResults=maximum.getFitResults();
			if (showFit) 
				fitPlots.addSlice(maximum.getPlot().getImagePlus().getProcessor());
			
			yc=(fitResults[2]+y1);
			fitDiameter_y=2.35*fitResults[4];
			// Convert maxima to um
			
			
			
			writeResults(results,frame);

//			ImagePlus imp=new ImagePlus("Frame"+frame+"_slice"+slice,ip);
//			imp.show();
//			IJ.log(""+results[0]+"//"+results[1]+"//"+results[2]);
//			fitDiameter_x=xpos.getDiameter();
//			IJ.log(""+results[0]+"//"+results[1]+"//"+results[2]);
//			fitDiameter_y=ypos.getDiameter();
//			imp.close();
	}
	void fit2D(ImageProcessor ip,int frame,int zpos) {
		
		
		
		// Intensity Profile of the bead in the x direction
		double x1=(xc-1.5*diameter);			//size of the line profile
		double x2=(xc+1.5*diameter);			//size of the line profile
		double y1=yc;
		int length=ip.getLine(x1, y1, x2, y1).length;
		double [] line =new double[length*length]; 
	
		for (int i=0;i<length;i++) {
			double [] profile=ip.getLine(x1, y1-1.5*diameter+i, x2, y1-1.5*diameter+i);
			for (int j=0;j<length;j++) {
				line[i*length+j]=profile[j];
			}
			
		}
		
		
		double [] x=new double [length*length];		//create array of doubles for x-values
		
		for (int i=0;i<length*length;i++) {			//populate the array via a loop
			x[i]=i;
		}
		double fitSigma=0.5*Math.pow(2.35/diameter,2);
		FitterFunction maximum=new Gauss2DFitter(x,line, length);	//Fit function to find the maximum
		double [] fitResults=maximum.getFitResults(new double []{0,this.amp,fitSigma,3*diameter/2,fitSigma,3*diameter/2});				//get the results of the fit

		
		if (showFit) 
			fitPlots.addSlice(maximum.getPlot().getImagePlus().getProcessor());
		
		xc=(fitResults[3]+x1);
		yc=(fitResults[5]+y1-1.5*diameter);	//old index 4
		fitDiameter_x=2.35*Math.sqrt(1/(2*fitResults[2]));
		fitDiameter_y=2.35*Math.sqrt(1/(2*fitResults[4])); //old index3
		
		
		writeResults(results,frame);
	}
void fitSym2D(ImageProcessor ip,int frame,int zpos) {
		
		
		
		// Intensity Profile of the bead in the x direction
		double x1=(xc-1.5*diameter);			//size of the line profile
		double x2=(xc+1.5*diameter);			//size of the line profile
		double y1=yc;
		int length=ip.getLine(x1, y1, x2, y1).length;
		double [] line =new double[length*length]; 
	
		for (int i=0;i<length;i++) {
			double [] profile=ip.getLine(x1, y1-1.5*diameter+i, x2, y1-1.5*diameter+i);
			for (int j=0;j<length;j++) {
				line[i*length+j]=profile[j];
			}
			
		}
		
		
		double [] x=new double [length*length];		//create array of doubles for x-values
		
		for (int i=0;i<length*length;i++) {			//populate the array via a loop
			x[i]=i;
		}
		double fitSigma=0.5*Math.pow(2.35/diameter,2);
		FitterFunction maximum=new SymGauss2DFitter(x,line, length);	//Fit function to find the maximum
		double [] fitResults=maximum.getFitResults(new double []{0,this.amp,fitSigma,3*diameter/2,fitSigma,3*diameter/2});				//get the results of the fit

		
		if (showFit) 
			fitPlots.addSlice(maximum.getPlot().getImagePlus().getProcessor());
		
		xc=(fitResults[3]+x1);
		yc=(fitResults[4]+y1-1.5*diameter);	//old index 4
		fitDiameter_x=2.35*Math.sqrt(1/(2*fitResults[2]));
		fitDiameter_y=2.35*Math.sqrt(1/(2*fitResults[2])); //old index3
		
		
		writeResults(results,frame);
	}
	public void showRois(String tableName) {
			
			RoiManager rm=RoiManager.getRoiManager();
			
			ResultsTable display=ResultsTable.getResultsTable(tableTitles[getMethodNumber()]);
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
//
 
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
			pos[s]=s;
			imp.setSliceWithoutUpdate(s);
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
	public ResultsTable summarizeResults() {
		
		if (results==null) {IJ.showMessage("No results table found");return null;};
		
		double []x=results.getColumn("delta x");
		double []y=results.getColumn("delta y");
		double []z=results.getColumn("delta z");
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
	private boolean writeSimpleResults(int frame) {						//should only by used for SimpleResults!
		results.incrementCounter();
		results.addValue("Frame",frame);
		results.addValue("Time/s", frame*ImageCalibration.frameInterval);
		results.addValue(SimpleBeadLocalizer.header[0], xc*ImageCalibration.pixelWidth);
		results.addValue(SimpleBeadLocalizer.header[1], yc*ImageCalibration.pixelHeight);
		results.addValue(SimpleBeadLocalizer.header[2], zc*ImageCalibration.pixelDepth);
//		if (this.methodSelection.contains(methodEllipse)||this.methodSelection.contains(methodGauss)) {
//			results.addValue(SimpleBeadLocalizer.header[3], fitDiameter_x);
//			results.addValue(SimpleBeadLocalizer.header[4], fitDiameter_y);
//		}
		double x0=results.getValue(SimpleBeadLocalizer.header[0],0);
		double y0=results.getValue(SimpleBeadLocalizer.header[1],0);
		double z0=results.getValue(SimpleBeadLocalizer.header[2],0);
		results.addValue("delta x",xc*ImageCalibration.pixelWidth-x0);
		results.addValue("delta y",yc*ImageCalibration.pixelHeight-y0);
		results.addValue("delta z",zc*ImageCalibration.pixelDepth-z0);
		return true;
	}
	Plot getDriftPLot() {
		
		double []x= results.getColumn("delta x");
		double []y= results.getColumn("delta y");
		double []z= results.getColumn("delta z");
		double []t= results.getColumn("Time/s");
		
		double [] conc=ArrayStatistics.concatArrays(ArrayStatistics.concatArrays(x, y),z);
		
		Plot p=new Plot("Drift Plot", "Time/s", "distance/um");
		p.setFontSize(18);
		p.setLineWidth(2);
		p.setColor(new Color(0,255,255));
		p.add("circle", t,x);
		p.add("line", t, x);
		p.setColor(new Color(255,0,255));
		p.add("square", t, y);
		p.add("line", t, y);
		p.setColor(new Color(255,255,0));
		p.add("circle", t, z);
		p.add("line", t, z);
		p.setLimits(new ArrayStatistics(t).getMin(), new ArrayStatistics(t).getMax(), new ArrayStatistics(conc).getMin()*1.1, new ArrayStatistics(conc).getMax()*1.1);
		return p;
	}
	private boolean writeResults(ResultsTable table, int frame) {
		table.incrementCounter();
		
		table.addValue("Frame",frame);
		table.addValue("Time/s", frame*ImageCalibration.frameInterval);
		table.addValue(SimpleBeadLocalizer.header[0], xc*ImageCalibration.pixelWidth);
		table.addValue(SimpleBeadLocalizer.header[1], yc*ImageCalibration.pixelHeight);
		table.addValue(SimpleBeadLocalizer.header[2], zc*ImageCalibration.pixelDepth);
		double x0=results.getValue(SimpleBeadLocalizer.header[0],0);
		double y0=results.getValue(SimpleBeadLocalizer.header[1],0);
		double z0=results.getValue(SimpleBeadLocalizer.header[2],0);
		results.addValue("delta x",xc*ImageCalibration.pixelWidth-x0);
		results.addValue("delta y",yc*ImageCalibration.pixelHeight-y0);
		results.addValue("delta z",zc*ImageCalibration.pixelDepth-z0);
		table.addValue(SimpleBeadLocalizer.header[3], fitDiameter_x*ImageCalibration.pixelWidth);
		table.addValue(SimpleBeadLocalizer.header[4], fitDiameter_y*ImageCalibration.pixelWidth);
		return true;
	}
	int getMethodNumber(){
		int number=0;
		for (int i=0;i<methods.length;i++) {
			if (methods[i].equals(methodSelection)) number=i;
		}
		return number;
	}
}
