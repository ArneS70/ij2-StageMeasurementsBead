package ch.epfl.biop.ij2command.USAF;

import java.util.Vector;

import ch.epfl.biop.ij2command.stage.general.FitterFunction;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
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
		int initialStartZ=analysis.getStartZ();
		int initialStopZ=analysis.getStopZ();
		int delta=initialStopZ-initialStartZ;
		int startT=analysis.getStartT();
		int stopT=analysis.getStopT();
		int stepT=analysis.getStepT();
		
		for (int t=startT;t<stopT;t+=stepT) {
				int start=initialStartZ+(t-1)*slices;
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
			
			}
	}
	
	Vector<double []> getTimeProfiles() {
		Vector <double []> profiles=new Vector<double[]>();
		Line line=analysis.getHorizontalLine();
	
		int sliceZ=analysis.getStartZ();
		ImagePlus imp=analysis.getImage();
		for (int t=0;t<frames;t+=1) {
			imp.setSliceWithoutUpdate(sliceZ+t*slices);
			analysis.setHorizontalLine( new HorizontalLine(imp.getProcessor()).findHorizontalLine());
			analysis.setHorizontalLine( new HorizontalLine(imp.getProcessor()).optimizeHorizontalMaxima(analysis.getHorizontalLine()));
			analysis.getImage().setRoi(analysis.getHorizontalLine());
			profiles.add(imp.getProcessor().getLine(line.x1d,line.y1d,line.x2d,line.y2d));
						
		}
		return profiles;
	}
	Vector<double []> getTimeProfiles(int startT,int stopT) {
		Vector <double []> profiles=new Vector<double[]>();
		Line line=analysis.getHorizontalLine();
	
		int sliceZ=analysis.getStartZ();
		ImagePlus imp=analysis.getImage();
		for (int t=startT;t<stopT;t+=1) {
			imp.setSliceWithoutUpdate(sliceZ+t);
			analysis.setHorizontalLine( new HorizontalLine(imp.getProcessor()).findHorizontalLine());
			analysis.setHorizontalLine( new HorizontalLine(imp.getProcessor()).optimizeHorizontalMaxima(analysis.getHorizontalLine()));
			analysis.getImage().setRoi(analysis.getHorizontalLine());
			profiles.add(imp.getProcessor().getLine(line.x1d,line.y1d,line.x2d,line.y2d));
						
		}
		return profiles;
	}

}

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
//lineAnalysis.writeFitResultsTable(FitterFunction.Poly3, true);
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

//IJ.run(inputImage, "Select None", "");
*/