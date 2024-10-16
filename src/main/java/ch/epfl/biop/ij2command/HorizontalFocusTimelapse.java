package ch.epfl.biop.ij2command;

import ij.IJ;

public class HorizontalFocusTimelapse extends HorizontalLineFocusAnalysis{ 
	int frames;
	int slices;
	
	HorizontalLineFocusAnalysis inputHLFA;
	
	HorizontalFocusTimelapse(HorizontalLineFocusAnalysis hlfa){
		super();
		super.inputImage=hlfa.getInputImage();
		inputHLFA=hlfa.duplicate();
		slices=hlfa.inputImage.getNSlices();
		frames=hlfa.inputImage.getNFrames();
	}

	void analyseTimeLapse(){
		inputHLFA.disableStack();
		inputHLFA.ignoreTimelapse();	
		
		for (int t=0;t<frames;t+=1) {
				inputImage.setSlice(inputHLFA.getStackCenter());
				int start=t*slices+1;
				int end=t*slices+slices;
								
				IJ.log("start="+start+"    end="+end);
				inputHLFA.setSliceStart(start);
				inputHLFA.setSliceStop(end);
				inputHLFA.run();
				inputHLFA.setStackCenter(inputHLFA.getStackSlices());
				IJ.run(inputImage, "Select None", "");
			}
	}
	
}
