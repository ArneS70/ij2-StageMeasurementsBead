package ch.epfl.biop.ij2command;

import ij.IJ;

public class HorizontalFocusTimelapse extends HorizontalFocus{
	int frames;
	int slices;
	HorizontalFocusTimelapse(int z, int t){
		this.slices=z;
		this.frames=t;
		super.inputImage=null;
	}
	void analyseTimeLapse(){
		
		for (int t=0;t<frames;t++) {
			super.inputImage.getC();
			IJ.log("t="+t+"    "+super.inputImage.getCurrentSlice());
		}
	}
}
