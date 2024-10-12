package ch.epfl.biop.ij2command;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;

public class HorizontalLine extends HorizontalAnalysis {
	final static String titleResults="Horizontal Line Analysis Results";
	final static String titleSummary="Summary Horizontal Line Analysis Results";
	protected LineAnalyser analyseLine;
	
	HorizontalLine(ImagePlus imp){
		super(imp);
		analyseLine=new LineAnalyser(super.inputImage);
	}
	
	void run() {
		if (checkInputImage()) {
			getSummaryTable(HorizontalLine.titleSummary);
			
			cal=inputImage.getCalibration();
			logFileNames();
			if (inputImage.getNFrames()>1&&!ignoreTime) {
				HorizontalLineTimelapse hlt=new HorizontalLineTimelapse(this);
				hlt.analyseTimelapse();
			} else {
			
				FocusAnalyser fa=new FocusAnalyser();
				HorizontalLineAnalyser hla=new HorizontalLineAnalyser(inputImage);
				
				int z=inputImage.getNSlices();
									
				if (inputImage.getRoi()==null) {hla.setHorizontalLine(this.stackCenter);fa=new FocusAnalyser(inputImage, hla.getHorizontalLine());}
				Roi roi=inputImage.getRoi();

//				this.analyseLine=new LineAnalyser(this.inputImage);
//				this.analyseLine=new LineAnalyser(this.inputImage);

				roi=super.getHorizontalLineAnalyser().optimizeHorizontalMaxima((Line)roi);
				if (roi!=null ) {
					if(roi.isLine()) {
						fa=new FocusAnalyser(inputImage,(Line)roi);
						this.horizontalLine=(Line)roi;
						
					} else {
						hla.setHorizontalLine(this.stackCenter);
						fa=new FocusAnalyser(inputImage,hla.getHorizontalLine());
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
//				fa.analyseHorizontalLine(repetition,lineLength);
//				fitTableResults(fa);
//				if (saveTable) saveResults();
			}
		 
		}
	}
}
