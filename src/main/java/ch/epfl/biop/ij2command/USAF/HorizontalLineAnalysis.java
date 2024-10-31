package ch.epfl.biop.ij2command.USAF;

import java.io.IOException;
import java.util.Arrays;

import ch.epfl.biop.ij2command.stage.general.*;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.plugin.filter.MaximumFinder;
import ij.process.ImageProcessor;
import loci.formats.FileInfo;


public class HorizontalLineAnalysis extends HorizontalAnalysisMethods{
	
	final static String titleSummary="Summary Horizontal Line Analysis Results";
	final static String titleProfiles="Horizontal Line Profiles";
	final static String titleFitResults="Analysis Fit Results";
	final static String saveProfiles="Horizontal_Line_Profiles";
	final static String saveFitResults="Analysis_Fit_Results";
	
		
	protected int method,counter;
	protected FitterFunction fitFunc;
	protected ResultsTable fitResults;
	protected ResultsTable lineProfiles;
	protected ResultsTable summary;
	private int startSlice,endSlice;
	private ImageStack fitPlots=new ImageStack(696,415);
/**   
 * Constructors
 */
	HorizontalLineAnalysis(){
		
	}
	HorizontalLineAnalysis(HorizontalAnalysis analysis) {
		this.analysis=analysis;
		this.horizontalLine=analysis.getHorizontalLine();
		this.cal=analysis.cal;
		startSlice=1;
		endSlice=analysis.getStackSlices();

//		this.inputImage=analysis.getImage();
//		double pixelSize=this.analysis.cal.pixelWidth;
//		ImageProcessor ip=inputImage.getProcessor();
//		this.setHorizontalLine(inputImage.getNSlices()/2);
//		this.profile=ip.getLine((double)horizontalLine.x1,(double)horizontalLine.y1,(double)horizontalLine.x2,(double)horizontalLine.y2);
//		int profileLength=profile.length;
//		this.xvalues=new double [profileLength];
		
	}
	HorizontalLineAnalysis(ImagePlus imp, Line line){
//		profiles=new ResultsTable();
//		profiles.show("Line Profiles");
		this.inputImage=imp;
		this.horizontalLine=line;
		this.cal=imp.getCalibration();
		ImageProcessor ip=imp.getProcessor();
		this.profile=ip.getLine((double)line.x1,(double)line.y1,(double)line.x2,(double)line.y2);
//		int profileLength=profile.length;
//		this.xvalues=new double [profileLength];
//		double pixelSize=cal.pixelWidth;
		
//		for (int n=0;n<profileLength;n++) {
//			xvalues[n]=n*pixelSize;
//		}
//		fitResults=ResultsTable.getResultsTable("Horizontal Line Fits");
//		if (fitResults==null) fitResults=new ResultsTable();
//		fitResults.show("Horizontal Line Fits");
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
	void run() {
		
		if (checkInputImage()) {
			
			if (hasLine()) this.setHorizontalLine(this.getLine());
			else {
					analysis.setHorizontalLine( new HorizontalLine(getCenterIP()).findHorizontalLine());
					analysis.setHorizontalLine( new HorizontalLine(getCenterIP()).optimizeHorizontalMaxima(analysis.getHorizontalLine()));
					analysis.getImage().setRoi(analysis.getHorizontalLine());
			}
		}
		
		
		if (analysis.getImage().getNFrames()>1&&!analysis.ignoreTime) {
			
			HorizontalLineTimelapse hft=new HorizontalLineTimelapse(analysis);
			hft.analyseTimeLapse();
		} else {
			
			summary=ResultsTable.getResultsTable(HorizontalLineAnalysis.titleSummary);
			if (summary==null) summary=new ResultsTable();
			counter=summary.getCounter();
			analysis.spacing=new HorizontalLine(getCenterIP()).getHorizontalSpacing();
			logFileNames();
			
			analysis.inputImage.getProcessor().setLineWidth(1);
			final int start=analysis.getStartZ();
			final int stop=analysis.getStopZ();
			final int zstep=analysis.getStepZ();
	    	final int iterations=(stop-start)/zstep;
	    	horizontalLine=analysis.getHorizontalLine();
	    	inputImage=analysis.getImage();
	    	
	    	for (int z=start;z<stop;z+=zstep) {
	    		
	    		inputImage.setSliceWithoutUpdate(z);
				profile=inputImage.getProcessor().getLine((double)horizontalLine.x1,(double)horizontalLine.y1,(double)horizontalLine.x2,(double)horizontalLine.y2);
				
		    	if (lineProfiles==null) {
		    		xvalues=new double [profile.length];
		    		for (int n=0;n<profile.length;n++) {
						xvalues[n]=n*cal.pixelWidth;
					}
		    		lineProfiles=new ResultsTable();
					lineProfiles.setValues("x", xvalues);
					lineProfiles.setValues(""+IJ.d2s(z*cal.pixelDepth), profile);
					
		    	} else {	
					
				lineProfiles.setValues(""+IJ.d2s(z*cal.pixelDepth), profile);
		    	};
	    	}
			if (!analysis.getMultiThread()) writeGlobalFitResults();     //non multithreaded fit, slow;			
			
			else {
				MultiThreadFit fastFit=new MultiThreadFit(this);
				fastFit.run();
				this.fitResults=fastFit.getFitResults();
				this.fitPlots=fastFit.getFitPlots();
				LogToSummaryTable();
				getSlope(3);
				analysis.counter=this.counter;
			
			}
			if (analysis.getSavePlot())savePlot(new ImagePlus("FitPlots",fitPlots));
			if (analysis.getSaveTable()) {
				
				saveResultTables(fitResults, saveFitResults);
				saveResultTables(lineProfiles, saveProfiles);
			}
			if (analysis.getShowTable()) {
				lineProfiles.show(HorizontalLineAnalysis.titleProfiles);
				fitResults.show(HorizontalLineAnalysis.titleFitResults);

			
			}
			if (analysis.getShowPlot()) new ImagePlus("FitPlots",fitPlots).show();
	}}
//	HorizontalLineAnalysis hla=new HorizontalLineAnalysis(analysis.getImage(),analysis.getHorizontalLine());
//	hla.setParameters(analysis);
//	hla.analysis=analysis;
//	hla.setZstep(analysis.getStepZ());
//	hla.writeFitResultsTable(FitterFunction.Poly3, true);
//	ij.io.FileInfo fi=analysis.inputImage.getFileInfo();
//	hla.LogToTable();
//	hla.getSlope();
	
	
	void setParameters(HorizontalAnalysis param) {
		this.analysis=param;
	}
	void setStartSlice(int start) {
		this.startSlice=start;
	}
	void setEndSlice(int end) {
		this.endSlice=end;
	}
	void LogToSummaryTable() {
		//	ResultsTable lineMax=ResultsTable.getResultsTable("Line Maxima Results");
		//	if ((lineMax)==null) lineMax=new ResultsTable();
		//	ResultsTable lineMax=new ResultsTable();
		//	int counter=lineMax.getCounter();
		//	lineMax.addRow();
		//	lineMax.addValue("#", counter);
		//	lineMax.addValue("File", fileName);
			
		//	lineMax.addValue("z step", this.zstep);
			
		//	lineMax.addValue("x1", this.toAnalyse.x1d);
		//	lineMax.addValue("y1", this.toAnalyse.y1d);
		//	lineMax.addValue("x2", this.toAnalyse.x2d);
		//	lineMax.addValue("y2", this.toAnalyse.y2d);
			
			
		//	lineMax.show("Line Maxima Results");
			
				
				summary.addRow();
				summary.addValue("#", this.counter);
				summary.addValue("File", analysis.fileName);
				summary.addValue("z depth/um", cal.pixelDepth);
				summary.addValue("z step", analysis.getStepZ());
				summary.addValue("z start", analysis.getStartZ());
				summary.addValue("z stop", analysis.getStopZ());
				double space=1000/(2*analysis.spacing*analysis.cal.pixelWidth);
				summary.addValue("Grid spacing LP/mm", space);
				Line line=analysis.getHorizontalLine();
				summary.addValue("x1", line.x1d);
				summary.addValue("y1", line.y1d);
				summary.addValue("x2", line.x2d);
				summary.addValue("y2", line.y2d);
				if (analysis.summarize) summary.show(HorizontalLineAnalysis.titleSummary);
		}
		void getSlope(){
//			ResultsTable rt=ResultsTable.getResultsTable("Horizontal Line Fits");
			
//			int counter=fitResults.getCounter();
			CurveFitter cf=new CurveFitter(fitResults.getColumn("z / um"),fitResults.getColumn("max"));
			cf.doFit(CurveFitter.STRAIGHT_LINE);
			double []param=cf.getParams();
			if (analysis.getShowPlot()) {
				Plot focusFit=cf.getPlot();
				ImagePlus fitWin=focusFit.show().getImagePlus();
				fitWin.setTitle(analysis.fileName+" "+IJ.pad(this.counter, 4)+".tif");
				fitWin.show(); 
			}
//			if (savePlot) {
//			IJ.save(filePath+fileName+" "+IJ.pad(counter, 3)+".tif");
//			WindowManager.getFrame(fileName+" "+IJ.pad(counter, 3)+".tif").dispose();
//		};
			ResultsTable lineMax=ResultsTable.getResultsTable(HorizontalLineAnalysis.titleSummary);
			lineMax.addValue("p1", param[0]);
			lineMax.addValue("p2", param[1]);
			lineMax.addValue("SumResSquare", param[2]);
			lineMax.addValue("R^2", cf.getFitGoodness());
			lineMax.show(HorizontalLineAnalysis.titleSummary);
			
		}
		void getSlope(int col){
//			ResultsTable rt=ResultsTable.getResultsTable("Horizontal Line Fits");
			
//			int counter=fitResults.getCounter();
			double []axisX=fitResults.getColumnAsDoubles(0);
			int length=axisX.length;
			for (int i=0;i<length;i++) {
				axisX[i]*=analysis.cal.pixelDepth*analysis.getStepZ();
			}
			CurveFitter cf=new CurveFitter(axisX,fitResults.getColumn(fitResults.getColumnHeading(col)));
			cf.doFit(CurveFitter.STRAIGHT_LINE);
			double []param=cf.getParams();
			Plot focusFit=cf.getPlot();
			ImagePlus fitWin=focusFit.getImagePlus();
			fitWin.setTitle(analysis.fileName+" "+IJ.pad(counter, 3)+".tif");
			if (analysis.getShowPlot()) fitWin.show(); 
			
			if (analysis.getSavePlot()) {
				IJ.save(fitWin,analysis.filePath+analysis.fileName+"_FitPlots_"+IJ.pad(counter, 4)+".tif");
			}
//			ResultsTable lineMax=ResultsTable.getResultsTable(HorizontalLineAnalysis.titleSummary);
			ResultsTable lineMax=this.summary;
			lineMax.addValue("p1", param[0]);
			lineMax.addValue("p2", param[1]);
			lineMax.addValue("SumResSquare", param[2]);
			lineMax.addValue("R^2", cf.getFitGoodness());
			lineMax.show(HorizontalLineAnalysis.titleSummary);
			
		}
		void setHorizontalLine(int slice) {
		
			
			analysis.setSlice(slice);
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
			
			if (mean1>mean2) {inputImage.setRoi(lines[pos]);analysis.setHorizontalLine((Line)lines[pos]);}
			else {inputImage.setRoi(lines[pos+1]);analysis.setHorizontalLine((Line)lines[pos+1]);}
			
			inputImage.updateAndDraw();
			
			
		
	}
	
	void writeGlobalFitResults() {
		
		horizontalLine=analysis.getHorizontalLine();
		ImagePlus inputImage=analysis.getImage();
		this.cal=inputImage.getCalibration();
		inputImage.getProcessor().setLineWidth(1);
		int start=analysis.getStartZ();
		int stop=analysis.getStopZ();
		int zstep=analysis.getStepZ();
		
		inputImage.setSliceWithoutUpdate(analysis.stackCenter);
		this.profile=inputImage.getProcessor().getLine((double)horizontalLine.x1,(double)horizontalLine.y1,(double)horizontalLine.x2,(double)horizontalLine.y2);

		xvalues=new double [profile.length];
		
		for (int i=0;i<profile.length;i++) {
			xvalues[i]=i*cal.pixelWidth;
		}
		//*************Poly3Fitter****************************
		int length=Poly3Fitter.header.length;
		this.fitFunc=new Poly3Fitter(xvalues,profile);
		fitFunc.setHeader(Poly3Fitter.header);
		this.method=FitterFunction.Poly3;
		double [] results=this.fitFunc.getParameter();
		String function=new GlobalFitter().createFormula(new double[]{results[0],results[1],results[2],results[3]});

//		this.fitFunc=new Asym2SigFitter(xvalues,profile);
//		double [] results=fitFunc.getParameter();
//		String test="test";
//		fitFunc.getPlot().show();
		
		for (int n=start;n<=stop;n+=zstep) {
			
			IJ.log("===================================");
			IJ.log("Slice: "+n);

			if (this.fitResults==null) this.fitResults=new ResultsTable();
			
			inputImage.setSliceWithoutUpdate(n);
			this.profile=inputImage.getProcessor().getLine((double)horizontalLine.x1,(double)horizontalLine.y1,(double)horizontalLine.x2,(double)horizontalLine.y2);
			CurveFitter cf=new CurveFitter(xvalues,profile);
//			cf.doCustomFit(function, new double [] {1, 1,1},false);
			results=cf.getParams();
			IJ.log(results[0]+"  "+results[1]+"   "+results[2]);
			
			fitPlots.addSlice(cf.getPlot().getImagePlus().getProcessor());
			this.fitResults.addRow();
		
//			for (int i=0;i<length-1;i++) {
//				this.fitResults.addValue("z / um", n*cal.pixelDepth);
//				this.fitResults.addValue("p"+i, results[i]);
					//fitResults.addValue(fitFunc.header[i], results[i]);
//			}
			
			this.fitResults.addValue("max", fitFunc.getMax());
			
			if (this.lineProfiles==null) {
					this.lineProfiles=new ResultsTable();
					this.lineProfiles.setValues("x", this.xvalues);
					this.lineProfiles.setValues(""+IJ.d2s(n*cal.pixelDepth), this.profile);
					
			} else {	
					
				this.lineProfiles.setValues(""+IJ.d2s(n*cal.pixelDepth), this.profile);
			};
		}
			
		
		if (analysis.getShowTable()) {
										lineProfiles.show(HorizontalLineAnalysis.titleProfiles);
										fitResults.show(HorizontalLineAnalysis.titleFitResults);
		}
		if (analysis.getShowPlot()) new ImagePlus("FitPlots",fitPlots).show();
	}
//	int length=Poly3Fitter.header.length;
//	this.fitFunc=new Poly3Fitter(xvalues,profile);
//	fitFunc.setHeader(Poly3Fitter.header);
//	this.method=FitterFunction.Poly3;
//	results=this.fitFunc.getParameter();
	
	void writeFitResultsTable(int method, boolean profileTable) {
		
		
		horizontalLine=analysis.getHorizontalLine();
		
		ImagePlus inputImage=analysis.getImage();
		this.cal=inputImage.getCalibration();
		int start=analysis.getStartZ();
		int stop=analysis.getStopZ();
		int zstep=analysis.getStepZ();
//		int lineLength=(int)horizontalLine.getLength();
		
		
		
		for (int n=start;n<=stop;n+=zstep) {
			
			IJ.log("===================================");
			IJ.log("Slice: "+n);
//			fitResults=ResultsTable.getResultsTable("Horizontal Line Fits");
			if (this.fitResults==null) this.fitResults=new ResultsTable();
			
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
			xvalues=new double [profile.length];
			for (int i=0;i<profile.length;i++) {
				xvalues[i]=i*cal.pixelWidth;
			}
			if (method==FitterFunction.AsymGauss) {
//				this.fitFunc=new Asym2SigFitter(xvalues,profile);
				this.method=FitterFunction.AsymGauss;
//				fitResults.updateResults();
			}
			if (method==FitterFunction.Poly3) {
				int length=Poly3Fitter.header.length;
				this.fitFunc=new Poly3Fitter(xvalues,profile);
				fitFunc.setHeader(Poly3Fitter.header);
				this.method=FitterFunction.Poly3;
				double [] results=this.fitFunc.getParameter();
				
				
//				if (fitResults==null) 
				this.fitResults.addRow();
//				int counter=fitResults.getCounter();
				for (int i=0;i<length-1;i++) {
					this.fitResults.addValue("z / um", n*cal.pixelDepth);
					this.fitResults.addValue("p"+i, results[i]);
					//fitResults.addValue(fitFunc.header[i], results[i]);
				}
				this.fitResults.addValue("max", fitFunc.getMax());
		
		
			if (profileTable) {
//				this.profiles=ResultsTable.getResultsTable(HorizontalLineAnalysis.titleProfiles);
				
				if (this.lineProfiles==null) {
					this.lineProfiles=new ResultsTable();
					this.lineProfiles.setValues("x", this.xvalues);
					this.lineProfiles.setValues(""+IJ.d2s(n*cal.pixelDepth), this.profile);
					
				} else {	
					
					this.lineProfiles.setValues(""+IJ.d2s(n*cal.pixelDepth), this.profile);
					
				};
				
				
			}
			}
			
		}
		if (analysis.getShowTable()) lineProfiles.show(HorizontalLineAnalysis.titleProfiles);
	}
	Line optimizeHorizontalMaxima(Line line) {
		
		ImageProcessor ip=inputImage.getProcessor().duplicate();
		ImagePlus imp=new ImagePlus("Maxima",ip);
//		double space=Math.abs(new HorizontalLineAnalyser(imp).calculateHorizontalSpacing(5));
		double space=20;		
		double profile []=new LineAnalyser(imp).getProcessor().getLine(line.x1d+20,line.y1d-space,line.x1d+20,line.y1d+space);
		double [] x=new double[profile.length];
		int profLen=x.length;
		for (int i=0;i<profLen;i++) {
			x[i]=i;
		}
		CurveFitter cf=new CurveFitter(x,profile);
		cf.doFit(CurveFitter.GAUSSIAN);
//		cf.getPlot().show();
		double [] paramLeft=cf.getParams();
		
		profile=new LineAnalyser(imp).getProcessor().getLine(line.x2d-20,line.y2d-space,line.x2d-20,line.y2d+space);
		
		cf=new CurveFitter(x,profile);
		cf.doFit(CurveFitter.GAUSSIAN);
//		cf.getPlot().show();
		double [] paramRight=cf.getParams();
		
		
		
		
		return new Line(10,line.y1d-10+paramLeft[2],line.x2d-10,line.y2d-10+paramRight[2]);
	
}
//	}
//	double [] getResults() {
//		return a2s.getResults(false);
//	}
}

//void setZstep(int step) {
//this.zstep=step;
//}
/*	double calculateHorizontalSpacing(int linewidth){
inputImage.setSlice(super.stackCenter);
LineAnalyser spacing=new LineAnalyser (new ImagePlus("edge",this.inputImage.getProcessor().duplicate()));
spacing.getProcessor().findEdges();
//new ImagePlus("test",spacing.getProcessor()).show();
spacing.setProfile(LineAnalyser.CENTER);
//spacing.getProfilPlot().show();
double [] line=spacing.getProfile();

double max=spacing.getMax();
double min=spacing.getMin();
int prominence=(int)(0.5*(max-min));
int [] points=MaximumFinder.findMaxima(line, prominence, false);
Arrays.sort(points);
int length=points.length;
double []x=new double[length];
double []y=new double[length];
for (int i=0;i<length;i++){
	x[i]=i;
	y[i]=points[i];
}

CurveFitter cf=new CurveFitter(x,y);
cf.doFit(CurveFitter.STRAIGHT_LINE);
cf.getPlot().show();
double []param=cf.getParams();
return param[1];


}*/
