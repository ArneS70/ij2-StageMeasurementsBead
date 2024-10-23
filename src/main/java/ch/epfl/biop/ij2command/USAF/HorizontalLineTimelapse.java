package ch.epfl.biop.ij2command.USAF;

import ch.epfl.biop.ij2command.stage.general.FitterFunction;
import ij.IJ;

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
		
		for (int t=0;t<frames;t+=1) {
				analysis.getImage().setSlice(analysis.getstackCenter());
				int start=t*slices+1;
				int end=t*slices+slices;
				HorizontalLineAnalysis lineAnalysis=new HorizontalLineAnalysis(analysis);				
				IJ.log("start="+start+"    end="+end);
				lineAnalysis.setStartSlice(start);
				lineAnalysis.setEndSlice(end);
				lineAnalysis.writeFitResultsTable(FitterFunction.Poly3, true);
				analysis.shiftStackCenter(slices);
				analysis.deleteRoi();
//				IJ.run(inputImage, "Select None", "");
			}
	}
}
