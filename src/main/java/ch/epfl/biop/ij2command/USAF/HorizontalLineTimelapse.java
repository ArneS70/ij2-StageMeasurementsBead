package ch.epfl.biop.ij2command.USAF;

import ch.epfl.biop.ij2command.stage.general.FitterFunction;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;

public class HorizontalLineTimelapse extends HorizontalLineAnalysis{
	int frames,slices;
	
	HorizontalLineTimelapse(HorizontalAnalysis ha){
		super();
		this.analysis=ha;
		slices=analysis.getImage().getNSlices();
		frames=analysis.getImage().getNFrames();
	}
	void analyseTimeLapse(){
//		analysis.disableStack();
//		inputHLFA.ignoreTimelapse();	
		int initialStart=analysis.getStartZ();
		int initialStop=analysis.getStopZ();
		int delta=initialStop-initialStart;
		
		for (int t=0;t<frames;t+=1) {
				int start=initialStart+t*slices;
				int stop=start+delta;
				
				analysis.ignoreTime=true;
				analysis.setStartZ(start);
				analysis.setStopZ(stop);
				IJ.log("start:"+start);
				IJ.log("stop:"+stop);
				
				analysis.stackCenter=analysis.getStartZ()+(analysis.getStopZ()-analysis.getStartZ())/2;
				analysis.getImage().setSlice(analysis.stackCenter);
				HorizontalLineAnalysis lineAnalysis=new HorizontalLineAnalysis(analysis);
				lineAnalysis.run();
				IJ.run(analysis.getImage(), "Select None", "");
			
/*				this.summary=ResultsTable.getResultsTable(HorizontalLineAnalysis.titleSummary);
				if (this.summary==null) this.summary=new ResultsTable();
				int counter=this.summary.getCounter();
				analysis.getImage().setSlice(analysis.getstackCenter());
				int start=analysis.getStartZ()+t*slices;
				int end=analysis.getStopZ()+t*slices;
				HorizontalLineAnalysis lineAnalysis=new HorizontalLineAnalysis(analysis);				
				IJ.log("start="+start+"    end="+end);
				lineAnalysis.setStartSlice(start);
				lineAnalysis.setEndSlice(end);
//				lineAnalysis.writeFitResultsTable(FitterFunction.Poly3, true);
				lineAnalysis.writeGlobalFitResults();
				analysis.shiftStackCenter(slices);
				analysis.deleteRoi();
				LogToTable();
				getSlope(3);
				
				if (analysis.getSavePlot())savePlot(new ImagePlus("FitPlots",fitPlots));
				if (analysis.getSaveTable()) {
					
					saveResultTables(this.fitResults, saveFitResults);
					saveResultTables(this.profiles, saveProfiles);
				}
				
//				IJ.run(inputImage, "Select None", "");
*/			}
	}

}