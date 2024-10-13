package ch.epfl.biop.ij2command;

import ij.IJ;

public class HorizontalFocusTimelapse extends HorizontalAnalysis{ 
	int frames;
	int slices;
	
	HorizontalLineAnalysis inputHF;
	
	HorizontalFocusTimelapse(HorizontalLineAnalysis hf){
		inputImage=hf.inputImage;
		inputHF=hf;
		slices=hf.inputImage.getNSlices();
		frames=hf.inputImage.getNFrames();
	}

	void analyseTimeLapse(){
		inputHF.disableStack();
		inputHF.ignoreTimelapse();	
		for (int t=0;t<frames;t+=1) {
				int start=t*slices+1;
				int end=t*slices+slices;
								
				IJ.log("start="+start+"    end="+end);
				inputHF.setStart(start);
				inputHF.setEnd(end);
				
				inputHF.run();
			}
	}
	
}
