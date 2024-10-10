package ch.epfl.biop.ij2command;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;

public class HorizontalFocus extends HorizontalAnalysis{
	final static String titleResults="Horizontal Focus Results";
	final static String titleSummary="Summary Horizontal Focus Results";
	private int repetition;
	private Plot focusFitPlot;
	
	HorizontalFocus(){
		super();
	}
	HorizontalFocus(ImagePlus imp,int rep,int start,int end,int step,int length,boolean allStack, boolean show,boolean savePlot,boolean saveTable){
		super.inputImage=imp;
		this.repetition=rep;
		super.start=start;
		super.end=end;
		super.zstep=step;
		super.lineLength=length;
		super.allStack=allStack;
		super.showFit=show;
		super.savePlot=savePlot;
		super.saveTable=saveTable;
		if (!allStack) super.stackCenter=(end-start)/2;
		else stackCenter=imp.getNSlices()/2;
		
	}
	void run() {
		if (checkInputImage()) {
			getSummaryTable(HorizontalFocus.titleSummary);
			
			cal=inputImage.getCalibration();
			logFileNames();
			if (inputImage.getNFrames()>1&&!ignoreTime) {
				HorizontalFocusTimelapse hft=new HorizontalFocusTimelapse(this);
				hft.analyseTimeLapse();
			} else {
			
				FocusAnalyser fa=new FocusAnalyser();
				HorizontalLineAnalyser hla=new HorizontalLineAnalyser(inputImage);
				
				int z=inputImage.getNSlices();
									
				if (inputImage.getRoi()==null) {hla.setHorizontalLine(this.stackCenter);fa=new FocusAnalyser(inputImage,hla.getHorizontalLIne());}
				Roi roi=inputImage.getRoi();
				
				
				if (roi!=null ) {
					if(roi.isLine()) {
						fa=new FocusAnalyser(inputImage,(Line)roi);
						this.horizontalLine=(Line)roi;
						
					} else {
						hla.setHorizontalLine(this.stackCenter);
						fa=new FocusAnalyser(inputImage,hla.getHorizontalLIne());
					}
				}
//				fa=new FocusAnalyser(inputImage,(Line)roi);
//				int[] param=setStackSize(imp);
//				fa.setStart(param[0]);
//				fa.setEnd(param[1]);
				if (allStack) {start=1;end=inputImage.getNSlices();}
				fa.setStart(start);
				fa.setEnd(end);
				fa.setStep(zstep);
				LogToTable(titleSummary);
				fa.analyseHorizontalLine(repetition,lineLength);
				fitTableResults(fa);
				if (saveTable) saveResults();
			}
		 
		}
	}
	
	
	void fitTableResults(FocusAnalyser fa) {		
		
		
		TableFitter tableFit=new TableFitter(fa.getFocusMap());
//		tableFit.showFit();
		tableFit.fitTable(CurveFitter.POLY5);
//		tableFit.GlobalTableFit(CurveFitter.POLY5);
//		tableFit.getFitResults().show(HorizontalFocus.titleResults);
		
		
		int last=tableFit.getFitResults().getLastColumn();
		
		CurveFitter cf=new CurveFitter(tableFit.getFitResults().getColumnAsDoubles(0),tableFit.getFitResults().getColumnAsDoubles(last));
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		
		double [] param=cf.getParams();
		double zShift=param[1]*cal.pixelDepth;
		double slope=param[1]*fa.getZstep();
		double angle=180*Math.atan(slope)/Math.PI;
		
		IJ.log("Focus shift z-axis  per slice: "+param[1]);
		IJ.log("Focus shift z-axis  absolut: "+zShift);
		IJ.log("Slope: "+slope);
		IJ.log("angle/deg: "+angle);
		IJ.log("R^2: "+cf.getFitGoodness());
		
		
		
			ResultsTable focus=ResultsTable.getResultsTable(HorizontalFocus.titleSummary);
			focus.setPrecision(5);
			focus.addValue("Focus shift per slice/um", param[1]);
			focus.addValue("Focus shift absolut", zShift);
			focus.addValue("Slope", slope);
			focus.addValue("angle/deg", angle);
			focus.addValue("R^2", cf.getFitGoodness());
			focus.show(HorizontalFocus.titleSummary);
		
		
		ImagePlus fitWin;
		
		if (showFit) {
			this.focusFitPlot=cf.getPlot();
			fitWin=focusFitPlot.show().getImagePlus();
			fitWin.setTitle(fileName+" "+IJ.pad(counter, 3)+".tif");
			fitWin.show();
			
		}
		if (savePlot) {
			this.focusFitPlot=cf.getPlot();
			fitWin=focusFitPlot.getImagePlus();
			IJ.save(fitWin, filePath+fileName+" "+IJ.pad(counter, 3)+".tif");
		}
	}
	
	void saveResults() {
		
		int n=this.fileName.indexOf(".");
		String saveName=this.fileName.substring(0, n);
		
//		n=this.filePath.indexOf(saveName);
//		String savePath=this.filePath.substring(0, n);
		ResultsTable rt=ResultsTable.getResultsTable(HorizontalFocus.titleResults);
		rt.save(filePath+saveName+"_TableFits_"+IJ.pad(counter, 4)+".csv");

		
//		rt=ResultsTable.getResultsTable("Horizontal Focus");
//		rt.save(filePath+fileName+"_HorizontalFocus_"+IJ.pad(counter, 4)+".csv");
//		WindowManager.getFrame("Horizontal Focus").dispose();
		
		
		
		
		
	}
	void LogToTable() {
		
		ResultsTable lineMax=ResultsTable.getResultsTable("Line Maxima Results");
		if ((lineMax)==null) lineMax=new ResultsTable();
//		ResultsTable lineMax=new ResultsTable();
		int counter=lineMax.getCounter();
		lineMax.addRow();
		lineMax.addValue("#", counter);
		lineMax.addValue("File", fileName);
		
		lineMax.addValue("z step", this.zstep);
		
		lineMax.addValue("x1", this.horizontalLine.x1d);
		lineMax.addValue("y1", this.horizontalLine.y1d);
		lineMax.addValue("x2", this.horizontalLine.x2d);
		lineMax.addValue("y2", this.horizontalLine.y2d);
		
		
		lineMax.show("Line Maxima Results");
		
	}
	

}
