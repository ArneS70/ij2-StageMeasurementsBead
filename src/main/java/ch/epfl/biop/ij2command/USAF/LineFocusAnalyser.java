package ch.epfl.biop.ij2command.USAF;

import ij.IJ;
import ij.gui.Line;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class LineFocusAnalyser extends FocusAnalyser{

	LineFocusAnalyser(){
		super();
	}
	LineFocusAnalyser(HorizontalLineFocusAnalysis hlfa){
		super();
		this.horizontalLine=hlfa.getHorizontalLine();
		this.zstack=hlfa.getImageStack();
		this.cal=hlfa.analysis.cal;
		this.analysisLineWidth=hlfa.getAnalysisLineWidth();
		this.rep=hlfa.analysis.getRepetition();
		this.step=hlfa.getZStep();
	}
	ResultsTable analyseHorizontalLine() {
		this.focusMap=new ResultsTable();
		this.titleFocusMap="Focus Map Horizontal Line";
		
		double x1=this.horizontalLine.x1d;
		double x2=this.horizontalLine.x2d;
		double y1=this.horizontalLine.y1d;
		double y2=this.horizontalLine.y2d;
		double length=this.horizontalLine.getLength();
		int slices=zstack.size();
		
		double dist=length/rep;							//distance between horizontal focus points in pixel
		IJ.log("Distance focus points= "+IJ.d2s(dist*cal.pixelWidth)+" um");
		double scale=this.step*this.cal.pixelDepth;
		for (int s=1;s<=slices;s++) {
			focusMap.addRow();
			focusMap.addValue("z-slice/um",s*scale);
			ImageProcessor ip=zstack.getProcessor(s);
			
			for (int r=0;r<rep;r++) {
				Line analyseLine=new Line(x1+r*dist,y1-analysisLineWidth,x2+r*dist,y2+analysisLineWidth);
				ip.setRoi(analyseLine);
					ImageStatistics statsX=ip.getStats();
					focusMap.addValue(IJ.d2s((x1+r*dist)*cal.pixelWidth),statsX.mean);
			}
		}
//		plotFocusMap();
//		focusMap.show("");
		return focusMap;
	}
}
