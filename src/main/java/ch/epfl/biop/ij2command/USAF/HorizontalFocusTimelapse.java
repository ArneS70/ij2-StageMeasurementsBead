package ch.epfl.biop.ij2command.USAF;

import ij.IJ;

public class HorizontalFocusTimelapse extends HorizontalLineFocusAnalysis{ 
	int frames;
	int slices;
	
	HorizontalLineFocusAnalysis inputHLFA;
	
	HorizontalFocusTimelapse(HorizontalLineFocusAnalysis hlfa){
		super(hlfa);
		
		inputHLFA=hlfa;
		this.analysis=hlfa.analysis;
		slices=analysis.getStackSlices();
		
		frames=analysis.getImage().getNFrames();
	}
	HorizontalFocusTimelapse(HorizontalAnalysis ha){
		super();
		this.analysis=ha;
		slices=analysis.getStackSlices();
		
		frames=analysis.getImage().getNFrames();
	}
	void analyseTimeLapse(){
		inputHLFA.disableStack();
		inputHLFA.ignoreTimelapse();	
		
		for (int t=0;t<frames;t+=1) {
				analysis.getImage().setSlice(inputHLFA.getStackCenter());
				int start=t*slices+1;
				int end=t*slices+slices;
								
				IJ.log("start="+start+"    end="+end);
				inputHLFA.setSliceStart(start);
				inputHLFA.setSliceStop(end);
				inputHLFA.run();
				analysis.shiftStackCenter(slices);
				analysis.deleteRoi();
//				IJ.run(inputImage, "Select None", "");
			}
	}
	
}
