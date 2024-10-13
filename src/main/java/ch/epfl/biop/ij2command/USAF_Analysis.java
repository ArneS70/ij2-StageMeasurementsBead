package ch.epfl.biop.ij2command;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;


public class USAF_Analysis {
	ImagePlus inputImage;
	HorizontalAnalysis horianalysis;
	HorizontalLineAnalysis horifocus;
	LineAnalyser lineanalyser;
	FocusAnalyser focusanalyser;
	
	public USAF_Analysis(ImagePlus imp){
		
		this.inputImage=imp;
		horianalysis=new HorizontalAnalysis(imp);
		lineanalyser=new LineAnalyser(imp);
		focusanalyser=new FocusAnalyser(imp);
	}
	void run(){
		if (horianalysis.checkInputImage()) {
			horianalysis.getSummaryTable(HorizontalLineAnalysis.titleSummary);
			
//			cal=inputImage.getCalibration();
			horianalysis.logFileNames();
			if (horianalysis.inputImage.getNFrames()>1&&!horianalysis.ignoreTime) {
				
//				HorizontalFocusTimelapse hft=new HorizontalFocusTimelapse(this);
//				hft.analyseTimeLapse();
			} else {
			
//				FocusAnalyser focusAnalyser =new FocusAnalyser(this.inputImage);
				HorizontalLineAnalyser horizontalLineAnalyser=new HorizontalLineAnalyser(inputImage);
				
				int z=inputImage.getNSlices();
									
				if (inputImage.getRoi()==null) 
					{horizontalLineAnalyser.setHorizontalLine(horianalysis.stackCenter);
					 focusanalyser=new FocusAnalyser(inputImage,horizontalLineAnalyser.getHorizontalLine());
					}
				Roi roi=inputImage.getRoi();
				
				roi=horizontalLineAnalyser.optimizeHorizontalMaxima((Line) roi);
				
				if (roi!=null ) {
					if(roi.isLine()) {
						focusanalyser=new FocusAnalyser(inputImage,(Line)roi);
						horianalysis.horizontalLine=(Line)roi;
						
					} else {
						horizontalLineAnalyser.setHorizontalLine(horianalysis.stackCenter);
						focusanalyser=new FocusAnalyser(inputImage,horizontalLineAnalyser.getHorizontalLine());
					}
				}
//				fa=new FocusAnalyser(inputImage,(Line)roi);
//				int[] param=setStackSize(imp);
//				fa.setStart(param[0]);
//				fa.setEnd(param[1]);
				if (horianalysis.allStack) {horianalysis.start=1;horianalysis.end=inputImage.getNSlices();}
				focusanalyser.setStart(horianalysis.start);
				focusanalyser.setEnd(horianalysis.end);
				focusanalyser.setStep(horianalysis.zstep);
//				LogToTable(horianalysis.titleSummary);
				focusanalyser.analyseHorizontalLine(horifocus.repetition,horianalysis.lineLength);
//				horianalysis.fitTableResults(focusanalyser);
//				if (horianalysis.saveTable) //saveResults();
			}
		 
		}
	}
	}
//	class HorizontalFocusAnalysis extends USAF_Analysis{
		
//		HorizontalFocusAnalysis(ImagePlus imp,int repetition,int start,int end,int step,int lineLength,boolean allStack, boolean showFit,boolean savePlot,boolean saveTable){
//			super(imp);
//			horifocus=new HorizontalFocus(imp,repetition,start,end,step,lineLength,allStack, showFit,savePlot,saveTable);
	
//		}
//	}
//}

