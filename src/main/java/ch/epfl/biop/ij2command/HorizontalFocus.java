package ch.epfl.biop.ij2command;

import java.awt.Window;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Line;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;

public class HorizontalFocus {
	final static String titleResults="Horizontal Focus Results";
	final static String titleSummary="Summary Horizontal Focus Results";
	protected ImagePlus inputImage;
	
	private Line horizontalLine;
	private Calibration cal;
	private int repetition,start,end,zstep,lineLength,counter;
	private boolean showFit,savePlot,saveTable,allStack;
	private String filePath,fileName;
	private ResultsTable summaryResults;
	private Plot focusFitPlot;
	
	HorizontalFocus(){
		
	}
	HorizontalFocus(ImagePlus imp,int rep,int start,int end,int step,int length,boolean allStack, boolean show,boolean savePlot,boolean saveTable){
		this.inputImage=imp;
		this.repetition=rep;
		this.start=start;
		this.end=end;
		this.zstep=step;
		this.lineLength=length;
		this.allStack=allStack;
		this.showFit=show;
		this.savePlot=savePlot;
		this.saveTable=saveTable;
		
	}
	void run() {
		if (checkInputImage()) {
			getSummaryTable();
			
			cal=inputImage.getCalibration();
			logFileNames();
			if (inputImage.getNFrames()>1) {
				HorizontalFocusTimelapse hft=new HorizontalFocusTimelapse(inputImage.getZ(),inputImage.getT());
				hft.analyseTimeLapse();
			} else {
			
				FocusAnalyser fa=new FocusAnalyser();
				HorizontalLineAnalyser hla=new HorizontalLineAnalyser(inputImage);
				
				int z=inputImage.getNSlices();
									
				if (inputImage.getRoi()==null) {hla.setHorizontalLine();fa=new FocusAnalyser(inputImage,hla.getHorizontalLIne());}
				Roi roi=inputImage.getRoi();
				
				if (roi!=null ) {
					if(roi.isLine()) {
						fa=new FocusAnalyser(inputImage,(Line)roi);
						this.horizontalLine=(Line)roi;
						
					} else {
						hla.setHorizontalLine();
						fa=new FocusAnalyser(inputImage,hla.getHorizontalLIne());
					}
				}
				fa=new FocusAnalyser(inputImage,(Line)roi);
//				int[] param=setStackSize(imp);
//				fa.setStart(param[0]);
//				fa.setEnd(param[1]);
				if (allStack) {start=1;end=inputImage.getNSlices();}
				fa.setStart(start);
				fa.setEnd(end);
				fa.setStep(zstep);
				LogToTable(fileName);
				fa.analyseHorizontalLine(repetition,lineLength);
				fitTableResults(fa);
				if (saveTable) saveResults();
			}}
	}
	private boolean checkInputImage() {
		boolean check=true;
		if (inputImage==null) {
			//IJ.run("Bio-Formats", "open="+fileInput+" color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
			//imp=WindowManager.getCurrentImage();
			IJ.log("Please provide an image");
			check=false;
		}
		
		if (inputImage.getNSlices()==1) {
			IJ.log("Please provide an z-stack");
			check=false;
		}
		return check;
	}
	void logFileNames() {
		IJ.log("===============================================================");
		this.filePath=IJ.getDirectory("file");
		this.fileName=inputImage.getTitle();
		
		if (fileName.startsWith(filePath)) 
			this.fileName=inputImage.getTitle().substring(filePath.length());
		
		
		IJ.log("File: "+fileName);
		IJ.log("Path: "+filePath);
	}
	private void getSummaryTable() {
		if (WindowManager.getWindow(HorizontalFocus.titleSummary)==null) {
			ResultsTable focusResults=new ResultsTable();
			focusResults.show(HorizontalFocus.titleSummary);
			this.counter=0;
			return;
		};
		this.summaryResults=ResultsTable.getResultsTable(HorizontalFocus.titleSummary);
		this.counter=this.summaryResults.getCounter();
	}
	void LogToTable(String file) {
		
		ResultsTable focus=ResultsTable.getResultsTable(HorizontalFocus.titleSummary);
		if (focus==null) focus=new ResultsTable();
		focus.addRow();
		focus.addValue("#", this.counter);
		focus.addValue("File", file);
		focus.addValue("Repetition", this.repetition);
		focus.addValue("z step", this.zstep);
		focus.addValue("z star", this.start);
		focus.addValue("z stop", this.end);
		focus.addValue("line length", this.lineLength);
		focus.addValue("x1", this.horizontalLine.x1d);
		focus.addValue("y1", this.horizontalLine.y1d);
		focus.addValue("x2", this.horizontalLine.x2d);
		focus.addValue("y2", this.horizontalLine.y2d);
		
		
		focus.show(HorizontalFocus.titleSummary);
		
	}
	void fitTableResults(FocusAnalyser fa) {		
		
		
		TableFitter tableFit=new TableFitter(fa.getFocusMap());
		tableFit.fitTable(CurveFitter.POLY5);
		tableFit.getFitResults().show(HorizontalFocus.titleResults);
		
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
	void closeNonImageWindows() {
		Window [] win=WindowManager.getAllNonImageWindows();
		int num=win.length;
		
		for (int i=0;i<num;i++) {
			win[i].dispose();
		}
	}
	void setStart(int value) {
		this.start=value;
	}
	void setend(int value) {
		this.end=value;
	}


}
