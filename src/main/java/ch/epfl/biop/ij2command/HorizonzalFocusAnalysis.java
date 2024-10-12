package ch.epfl.biop.ij2command;

import ij.ImagePlus;

public class HorizonzalFocusAnalysis extends USAF_Analysis{
	HorizonzalFocusAnalysis(ImagePlus imp,int repetition,int start,int end,int step,int lineLength,boolean allStack, boolean showFit,boolean savePlot,boolean saveTable){
		super(imp);
		super.horifocus=new HorizontalFocus(imp,repetition,start,end,step,lineLength,allStack, showFit,savePlot,saveTable);

	}
//	void run(){
//		super.run();
//	}

}
