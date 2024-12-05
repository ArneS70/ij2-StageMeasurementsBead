package ch.epfl.biop.ij2command.USAF;

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

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
	
	protected boolean isPureTimeLapse=false;
	protected boolean isSingleZStack=false;
	protected boolean isTimeStack=false;
		
	protected int method,counter;
	protected FitterFunction fitFunc;
	protected ResultsTable tableFitResults,tableProfiles;
	protected Vector<double[]> lineProfiles=new Vector<double []>();
	protected Vector<double[]> fitResults=new Vector<double []>();
	protected Vector<Integer>fitOrder=new Vector<Integer>();
	Vector <Line>horizontalLines=new Vector<Line>();
	
	protected Vector<Double> position=new Vector<Double>();
	
	
	protected ResultsTable summary;
	private int startSlice,endSlice;
	protected ImageStack fitPlots=new ImageStack(696,415);
	
	/***************************************************************************************************************   
	 * Constructors
	 ***************************************************************************************************************/
	
	HorizontalLineAnalysis(){
	}
	
	HorizontalLineAnalysis(HorizontalAnalysis analysis) {
		this.analysis=analysis;
		this.horizontalLine=analysis.getHorizontalLine();
		this.cal=analysis.cal;
		startSlice=1;
		endSlice=analysis.getStackSlices();
	}
	
	HorizontalLineAnalysis(ImagePlus imp, Line line){
		this.inputImage=imp;
		this.horizontalLine=line;
		this.cal=imp.getCalibration();
		ImageProcessor ip=imp.getProcessor();
		this.profile=ip.getLine((double)line.x1,(double)line.y1,(double)line.x2,(double)line.y2);
	}

	/*************************************************************************************************************************
	 *  Creates line profiles and fits a function to the intensity profile to find the maximum. 
	 *  - Line profiles can be displayed and saved as a ResultsTable.
	 *  - Fit results can be displayed and saved as a ResultsTable. 
	 *************************************************************************************************************************/
	void run() {
		
		if (checkInputImage()) {
			
			HorizontalLineExtractor profiles=new HorizontalLineExtractor(this.analysis);
			profiles.run();
			this.lineProfiles=profiles.getProfileStack();
			this.horizontalLines=profiles.horizontalLines;
			
			//***************************************************************************
			// Fit the line profiles
			//
			//***************************************************************************
		    	
				if (!analysis.getMultiThread()) {
//		    		FitterFunction fit=FitterFunction.getFitFunc(this.analysis.getFitFunc());
//					int m=FitterFunction.getMethod(this.analysis.getFitFunc());
					double t1=System.currentTimeMillis();
					writeGlobalFitResults();     					//non multithreaded fit, slow;			
					double t2=System.currentTimeMillis();
					IJ.log("start="+t1+"   stop="+t2);
					IJ.log("duration="+(t2-t1)/1000);
					this.createTables();
		    		this.tableFitResults.show(titleFitResults);
		    	}
				
				else {
					FitterFunction fit=FitterFunction.getFitFunc(lineProfiles.firstElement(), lineProfiles.get(1), this.analysis.getFitFunc());
					
					double [] results=fit.getFitResults();
					fit.getPlot().show();
//					final String function1=new GlobalFitter().createPolyFormula(results,fit.numParam);
//					final String function=new GlobalFitter().createGlobalFormula(results, fit.globalFunction);
					final String function=fit.createGlobalFormula(results);
//					fit.getFitResults(function);
//					fit.getPlot().show();
					
					MultiThreadFitter mtf=new MultiThreadFitter(this.lineProfiles,this.analysis.getFitFunc());
				
					mtf.multiThreadCalculate(results);
					this.fitResults=mtf.fitResults;
					this.fitOrder=mtf.fitOrder;
					createImageStack(mtf.fitPlots);
					IJ.log("");
					
/*					MultiThreadFit fastFit=new MultiThreadFit(this);
					double t1=System.currentTimeMillis();
					fastFit.run();
					double t2=System.currentTimeMillis();
					IJ.log("start="+t1);
					IJ.log("stop="+t2);
					IJ.log("duration="+(t2-t1)/1000);
//					this.fitResults=fastFit.fitResults;
					this.fitPlots=fastFit.getFitPlots();
					if (isSingleZStack || isTimeStack) {
						LogToSummaryTable();
						if (tableFitResults!=null) getSlope(3);
						analysis.counter=this.counter;
					}*/
				}

				this.createTables();
		    	if (analysis.getSavePlot())savePlot(new ImagePlus("FitPlots",fitPlots));
				if (analysis.getSaveTable()) {
					
				saveResultTables(tableFitResults, saveFitResults);
//				saveResultTables(lineProfiles, saveProfiles);
				}
				
				if (analysis.getShowTable()) {
//					lineProfiles.show(HorizontalLineAnalysis.titleProfiles);
					tableFitResults.show(HorizontalLineAnalysis.titleFitResults);
				}
				
				if (analysis.getShowPlot()) new ImagePlus("FitPlots",fitPlots).show();
			}
		}
		void createImageStack(Vector<ImageProcessor>ips) {
			int size=ips.size();
			for (int i=0;i<size;i++) {
				this.fitPlots.addSlice(ips.elementAt(i));
			}
		}
		void createTables() {
		
		tableFitResults=new ResultsTable();
		int length=fitResults.size();
		int col=fitResults.get(1).length;
		
		for (int i=0;i<length;i++) {
			tableFitResults.addRow();
			tableFitResults.addValue("#", this.fitOrder.elementAt(i));
			
			if (this.horizontalLines.size()>0) {
				Line line=this.horizontalLines.get(i);
				tableFitResults.addValue("x1", line.x1d);
				tableFitResults.addValue("x2", line.x2d);
				tableFitResults.addValue("y1", line.y1d);
				tableFitResults.addValue("y2", line.y2d);
			}
			
			double []param=fitResults.get(i);
			
			for (int j=0;j<col;j++) {
				tableFitResults.addValue("P"+j, param[j]);
			}
			
		}
	}
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
		/**********************************************************************************************
		 * Calculates the slope of the peak x-shift versus the z-displacement
		 **********************************************************************************************/
		void getSlope(){
//			ResultsTable rt=ResultsTable.getResultsTable("Horizontal Line Fits");
			
//			int counter=fitResults.getCounter();
			CurveFitter cf=new CurveFitter(tableFitResults.getColumn("z / um"),tableFitResults.getColumn("max"));
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
			double []axisX=tableFitResults.getColumnAsDoubles(0);
			int length=axisX.length;
			for (int i=0;i<length;i++) {
				axisX[i]*=analysis.cal.pixelDepth*analysis.getStepZ();
			}
			CurveFitter cf=new CurveFitter(axisX,tableFitResults.getColumn(tableFitResults.getColumnHeading(col)));
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
		
//*************Poly8Fitter****************************
//int length=Poly8Fitter.header.length;
//		this.fitFunc=new FitterFunction(lineProfiles.firstElement(),lineProfiles.get(1),FitterFunction.methodInt[method]);
//		fitFunc.setHeader(Poly8Fitter.header);
//		this.method=FitterFunction.Poly8;
//		double [] results=this.fitFunc.getParameter();
//		String function=GlobalFitter.createPolyFormula(this.fitFunc.getParameter(),3);
		

		FitterFunction fit=FitterFunction.getFitFunc(lineProfiles.firstElement(), lineProfiles.get(1), this.analysis.getFitFunc());
		double [] results=fit.getFitResults();
		fit.getPlot().show();
		String function =fit.getGlobalFunction(results,fit.numParam);
		fit=FitterFunction.getGlobalFitFunc(lineProfiles.firstElement(), lineProfiles.get(1), this.analysis.getFitFunc());
		fit.getFitResults(results);
		fit.getPlot().show();
		
		int stop=lineProfiles.size();
		position.add(0.0);
		double t0=System.currentTimeMillis();
		for (int n=1;n<stop;n++) {
			
			
			IJ.log("===================================");
			IJ.log("Slice: "+n);
			
			
			position.add((double)n);
			
			if (this.tableFitResults==null) this.tableFitResults=new ResultsTable();
			
			
			fit.updateInput(lineProfiles.firstElement(),lineProfiles.get(n));
			
//			CurveFitter cf=new CurveFitter(lineProfiles.firstElement(),lineProfiles.get(n));
//			cf.doCustomFit(function, new double [] {1, 1,1},false);
			
//			results=fit.getFitResults(function);
			
			
			
			fitResults.add(fit.getFitResults(results));
			
//			IJ.log(results[0]+"  "+results[1]+"   "+results[2]);
			
			fitPlots.addSlice(fit.getPlot().getImagePlus().getProcessor());
			double t=System.currentTimeMillis();
			IJ.log("delta t="+IJ.d2s((t-t0)/1000));
			t0=t;
			}
			

		}
	
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
			if (this.tableFitResults==null) this.tableFitResults=new ResultsTable();
			
			inputImage.setSliceWithoutUpdate(n);
			this.profile=inputImage.getProcessor().getLine((double)horizontalLine.x1,(double)horizontalLine.y1,(double)horizontalLine.x2,(double)horizontalLine.y2);
		
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
				this.tableFitResults.addRow();
//				int counter=fitResults.getCounter();
				for (int i=0;i<length-1;i++) {
					this.tableFitResults.addValue("z / um", n*cal.pixelDepth);
					this.tableFitResults.addValue("p"+i, results[i]);
					//fitResults.addValue(fitFunc.header[i], results[i]);
				}
				this.tableFitResults.addValue("max", fitFunc.getMax());
		
		
			if (profileTable) {
//				this.profiles=ResultsTable.getResultsTable(HorizontalLineAnalysis.titleProfiles);
				
				
					this.tableProfiles=new ResultsTable();
					this.tableProfiles.setValues("x", lineProfiles.firstElement());
					this.tableProfiles.setValues(""+IJ.d2s(n*cal.pixelDepth), this.profile);
					
				} else {	
					
					this.tableProfiles.setValues(""+IJ.d2s(n*cal.pixelDepth), this.profile);
					
				};
			}
			
			
		}
		if (analysis.getShowTable()) tableProfiles.show(HorizontalLineAnalysis.titleProfiles);
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
	Vector <double []> getProfileStack(){
		return lineProfiles;
	}

	void overflow() {
				
				if (hasLine()) this.setHorizontalLine(this.getLine());
				else {
						HorizontalLine hl=new HorizontalLine(getCenterIP());
						Line line=hl.findHorizontalLine();
						analysis.setHorizontalLine(line);
						line=hl.optimizeHorizontalMaxima(line);
						analysis.setHorizontalLine( line);
						analysis.getImage().setRoi(analysis.getHorizontalLine());
				}
			
			horizontalLine=analysis.getHorizontalLine();	//defines the horizontal line
			inputImage=analysis.getImage();					//defines the input image
	//		analysis.ignoreTime=true;
			
			//********************************************************************************
			// Image Stack is only time-lapse.
			// 
			//**********************************************************************************/
			
			if (analysis.getImage().getNFrames()>1&&analysis.getImage().getNSlices()==1) {		
				
				this.isPureTimeLapse=true;
				
				int startT=analysis.getStartT();			//starting frame
				int stopT=analysis.getStopT();				//stop frame
				int stepT=analysis.getStepT();				//step between frames
				
				for (int t=startT;t<=stopT;t+=stepT) {
					
					inputImage.setSliceWithoutUpdate(t);
					analysis.setHorizontalLine( new HorizontalLine(inputImage.getProcessor()).optimizeHorizontalMaxima(analysis.getHorizontalLine()));
					analysis.getImage().setRoi(analysis.getHorizontalLine());
					
					horizontalLines.add(analysis.getHorizontalLine());
					profile=inputImage.getProcessor().getLine(analysis.getHorizontalLine().x1d,analysis.getHorizontalLine().y1d,analysis.getHorizontalLine().x2d,analysis.getHorizontalLine().y2d);
					
						
					//********************************************************************************
					//Creates x-values (calibrated)
					//********************************************************************************
			    	
					if (lineProfiles.size()==0) {
			    		xvalues=new double [profile.length];
			    		for (int n=0;n<profile.length;n++) {
							xvalues[n]=n*cal.pixelWidth;
						}
			    		
						lineProfiles.add(xvalues);
						lineProfiles.addElement(profile);
						
			    	} else {	
						
					lineProfiles.addElement(profile);
			    	};
				}
				
			}
			//********************************************************************************
			// Image Stack is Z-Stack over time
			//
			//********************************************************************************
			 
			if (analysis.getImage().getNFrames()>1&&!analysis.ignoreTime) {
				
				HorizontalLineTimelapse hft=new HorizontalLineTimelapse(analysis);
				hft.analyseTimeLapse();
			}; 
			
			//********************************************************************************
			// Image Stack is a single Z-Stack
			//
			//********************************************************************************
			
			if (analysis.getImage().getNFrames()==1) {
				
				this.isSingleZStack=true;
				summary=ResultsTable.getResultsTable(HorizontalLineAnalysis.titleSummary);
				if (summary==null) summary=new ResultsTable();
				counter=summary.getCounter();
				analysis.spacing=new HorizontalLine(getCenterIP()).getHorizontalSpacing();
				logFileNames();
				
				analysis.inputImage.getProcessor().setLineWidth(1);
				final int start=analysis.getStartZ();
				final int stop=analysis.getStopZ();
				final int zstep=analysis.getStepZ();
		    
				for (int z=start;z<=stop;z+=zstep) {
		    		
		    		inputImage.setSliceWithoutUpdate(z);
					profile=inputImage.getProcessor().getLine((double)horizontalLine.x1,(double)horizontalLine.y1,(double)horizontalLine.x2,(double)horizontalLine.y2);
					
					if (lineProfiles.size()==0) {
			    		xvalues=new double [profile.length];
			    		for (int n=0;n<profile.length;n++) {
							xvalues[n]=n*cal.pixelWidth;
						}
			    		
						lineProfiles.add(xvalues);
						lineProfiles.addElement(profile);
						
						
			    	} else {	
						
					lineProfiles.addElement(profile);
			    	};
		    	}
			}
				
				}
}

