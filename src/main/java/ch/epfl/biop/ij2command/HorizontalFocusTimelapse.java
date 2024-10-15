package ch.epfl.biop.ij2command;

import ij.IJ;
import ij.gui.Roi;
import ij.process.ImageProcessor;


public class HorizontalFocusTimelapse extends HorizontalLineFocusAnalysis{ 
	int frames;
	int slices;
	
	HorizontalLineFocusAnalysis inputHLFA;
	
	HorizontalFocusTimelapse(HorizontalLineFocusAnalysis hlfa){
		super();
		super.inputImage=hlfa.getInputImage();
		inputHLFA=hlfa;
		slices=hlfa.inputImage.getNSlices();
		frames=hlfa.inputImage.getNFrames();
	}

	void analyseTimeLapse(){
		inputHLFA.disableStack();
		inputHLFA.ignoreTimelapse();	
		
		for (int t=0;t<frames;t+=1) {
				int start=t*slices+1;
				int end=t*slices+slices;
								
				IJ.log("start="+start+"    end="+end);
				inputHLFA.setStart(start);
				inputHLFA.setStop(end);
				inputHLFA.run();
				IJ.run(inputImage, "Select None", "");
			}
	}
	
}
