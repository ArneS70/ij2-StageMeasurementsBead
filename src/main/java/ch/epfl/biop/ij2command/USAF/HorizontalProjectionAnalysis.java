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


public class HorizontalProjectionAnalysis extends HorizontalAnalysisMethods{
	
	final static String titleSummary="Summary Horizontal Projection Analysis Results";
	final static String titleProfiles="Horizontal Projection Profiles";
	final static String titleFitResults="Analysis Fit Results";
	final static String saveProfiles="Horizontal_Projection_Profiles";
	final static String saveFitResults="Analysis_Fit_Results";
	
	protected String fileName, filePath;
	
	protected boolean isPureTimeLapse=false;
	protected boolean isSingleZStack=false;
	protected boolean isTimeStack=false;
	
	protected FitterFunction fitFunc;
	protected ResultsTable tableFitResults,tableProfiles;
	protected ResultsTable summary;
	
	protected Vector<double[]> lineProfiles=new Vector<double []>();
	protected Vector<double[]> fitResults=new Vector<double []>();
	protected Vector<Integer>fitOrder=new Vector<Integer>();
	protected Vector <Line>horizontalLines=new Vector<Line>();
	protected Vector<Double> position=new Vector<Double>();
	
	protected int method,counter;
	protected ImageStack fitPlots=new ImageStack(696,415);
	protected double[] fitShift; 
	protected double [] globalFitParam;
	
	/***************************************************************************************************************   
	 * Constructors
	 ***************************************************************************************************************/
	
	HorizontalProjectionAnalysis(){
	}
	
	HorizontalProjectionAnalysis(HorizontalAnalysis analysis) {
		this.analysis=analysis;
		this.horizontalLine=analysis.getHorizontalLine();
		this.cal=analysis.cal;
		String file=analysis.getImage().getTitle();
		this.fileName=getFileName(file);
		this.filePath=analysis.getImage().getOriginalFileInfo().directory;
		
	}
	
	HorizontalProjectionAnalysis(ImagePlus imp, Line line){
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
			profiles.getVerticalProtection();
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
					
					this.globalFitParam=fit.getFitResults();
					

					MultiThreadFitter mtf=new MultiThreadFitter(this.lineProfiles,this.analysis.getFitFunc());
				
					mtf.multiThreadCalculate(this.globalFitParam);
					this.fitResults=mtf.fitResults;
					this.fitOrder=mtf.fitOrder;
					createImageStack(mtf.fitPlots);

				}

				this.createTables();
		    	if (analysis.getSavePlot())savePlot(new ImagePlus("FitPlots",fitPlots));
				if (analysis.getSaveTable()) {
					
//				saveResultTables(tableFitResults, filePath+fileName+saveFitResults);
				fileName=checkFileName(fileName);
				String save=filePath.concat(fileName);
				save=save.concat(saveFitResults);
				save=save.concat(IJ.pad(analysis.counter, 4));
				save=save.concat(".csv");
				
				tableFitResults.save(save);
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
			tableFitResults.addValue("z /um", this.fitOrder.elementAt(i)*this.cal.pixelDepth*this.analysis.getStepZ());
			
			double []param=fitResults.get(i);
			
			for (int j=0;j<col;j++) {
				tableFitResults.addValue("P"+j, param[j]);
			}
			
		}
		CurveFitter cf=new CurveFitter(tableFitResults.getColumn("z /um"),tableFitResults.getColumn("P2"));
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		this.fitShift=Arrays.copyOf(cf.getParams(),cf.getNumParams()+2);
		this.fitShift[cf.getNumParams()+1]=cf.getRSquared();
		cf.getPlot().show();
		if (this.analysis.summarize) LogToSummaryTable();
	}
	void setParameters(HorizontalAnalysis param) {
		this.analysis=param;
	}

	void LogToSummaryTable() {
			
			summary=ResultsTable.getResultsTable(this.titleSummary);
			
			if (summary==null) summary=new ResultsTable();
			
			summary.addRow();
			summary.addValue("#", this.counter);
			summary.addValue("File", analysis.fileName);
			summary.addValue("z depth/um", cal.pixelDepth);
			summary.addValue("z step", analysis.getStepZ());
			summary.addValue("z start", analysis.getStartZ());
			summary.addValue("z stop", analysis.getStopZ());
			summary.addValue("delta z / um", (analysis.getStopZ()-analysis.getStartZ())*cal.pixelDepth);
			double space=1000/(2*analysis.spacing*analysis.cal.pixelWidth);
//			summary.addValue("Grid spacing LP/mm", space);
			summary.addValue("Slope", this.fitShift[1]);
			summary.addValue("R^2", this.fitShift[3]);
			summary.addValue("w1", globalFitParam[3]);
			summary.addValue("w2", globalFitParam[4]);
			summary.addValue("w3", globalFitParam[5]);
			
			if (analysis.summarize) summary.show(this.titleSummary);
		}
		
		
		
	/******************************************************************
	 * Global Fitting of line profiles.
	 */
	void writeGlobalFitResults() {
		
		FitterFunction fit=FitterFunction.getFitFunc(lineProfiles.firstElement(), lineProfiles.get(1), this.analysis.getFitFunc());
		double [] results=fit.getFitResults();
		fit.getPlot().show();

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
			
			
			fitResults.add(fit.getFitResults(results));
			
			
			fitPlots.addSlice(fit.getPlot().getImagePlus().getProcessor());
			double t=System.currentTimeMillis();
			IJ.log("delta t="+IJ.d2s((t-t0)/1000));
			t0=t;
			}
			

		}
	
	
	

	
}

