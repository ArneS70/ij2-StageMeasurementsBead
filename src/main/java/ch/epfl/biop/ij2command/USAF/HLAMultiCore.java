package ch.epfl.biop.ij2command.USAF;

import ch.epfl.biop.ij2command.stage.general.FitterFunction;
import ch.epfl.biop.ij2command.stage.general.GlobalFitter;
import ch.epfl.biop.ij2command.stage.general.Poly3Fitter;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.util.ThreadUtil;

public class HLAMultiCore extends HorizontalLineAnalysis {

	HorizontalLineAnalysis parameters;
	
	HLAMultiCore(HorizontalLineAnalysis hla){
		this.parameters=hla;
		this.analysis=hla.analysis;
	}
	void test() {
		ThreadUtil tu =new ThreadUtil();
		int cores=ThreadUtil.getNbCpus();
		Thread[] thread=ThreadUtil.createThreadArray(cores);
		ThreadUtil.startAndJoin(thread);
	}
	void startAndJoin(Thread thread) {
		run();
	}
	void run() {
		if (checkInputImage()) {
			
			if (hasLine()) this.setHorizontalLine(this.getLine());
			else {
				analysis.setHorizontalLine( new HorizontalLine(getCenterIP()).findHorizontalLine());
				analysis.setHorizontalLine( new HorizontalLine(getCenterIP()).optimizeHorizontalMaxima(analysis.getHorizontalLine()));
				analysis.getImage().setRoi(analysis.getHorizontalLine());
			}
		}
		
		
		if (analysis.getImage().getNFrames()>1&&!analysis.ignoreTime) {
			
			HorizontalLineTimelapse hft=new HorizontalLineTimelapse(analysis);
			hft.analyseTimeLapse();
		} else {
			
			summary=ResultsTable.getResultsTable(HorizontalLineAnalysis.titleSummary);
			if (summary==null) summary=new ResultsTable();
			counter=summary.getCounter();
			analysis.spacing=new HorizontalLine(getCenterIP()).getHorizontalSpacing();

			logFileNames();
			writeGlobalFitResults();
			LogToTable();
			getSlope(3);
			analysis.counter=this.counter;
			if (analysis.getSavePlot())savePlot(new ImagePlus("FitPlots",fitPlots));
			if (analysis.getSaveTable()) {
				
				saveResultTables(fitResults, saveFitResults);
				saveResultTables(profiles, saveProfiles);
			}
			
		}
	}
	void writeGlobalFitResults() {
		
		horizontalLine=analysis.getHorizontalLine();
		ImagePlus inputImage=analysis.getImage();
		this.cal=inputImage.getCalibration();
		inputImage.getProcessor().setLineWidth(1);
		int start=analysis.getStartZ();
		int stop=analysis.getStopZ();
		int zstep=analysis.getStepZ();
		
		inputImage.setSliceWithoutUpdate(analysis.stackCenter);
		this.profile=inputImage.getProcessor().getLine((double)horizontalLine.x1,(double)horizontalLine.y1,(double)horizontalLine.x2,(double)horizontalLine.y2);

		xvalues=new double [profile.length];
		
		for (int i=0;i<profile.length;i++) {
			xvalues[i]=i*cal.pixelWidth;
		}
		int length=Poly3Fitter.header.length;
		this.fitFunc=new Poly3Fitter(xvalues,profile);
		fitFunc.setHeader(Poly3Fitter.header);
		this.method=FitterFunction.Poly3;
		double [] results=this.fitFunc.getParameter();
		String function=new GlobalFitter().createFormula(new double[]{results[0],results[1],results[2],results[3]});
//		fitFunc.getPlot().show();
		
		for (int n=start;n<=stop;n+=zstep) {
			
			IJ.log("===================================");
			IJ.log("Slice: "+n);

			if (this.fitResults==null) this.fitResults=new ResultsTable();
			
			inputImage.setSliceWithoutUpdate(n);
			this.profile=inputImage.getProcessor().getLine((double)horizontalLine.x1,(double)horizontalLine.y1,(double)horizontalLine.x2,(double)horizontalLine.y2);
			CurveFitter cf=new CurveFitter(xvalues,profile);
			cf.doCustomFit(function, new double [] {1, 1,1},false);
			results=cf.getParams();
			IJ.log(results[0]+"  "+results[1]+"   "+results[2]);
			
			fitPlots.addSlice(cf.getPlot().getImagePlus().getProcessor());
			this.fitResults.addRow();
		
			for (int i=0;i<length-1;i++) {
				this.fitResults.addValue("z / um", n*cal.pixelDepth);
				this.fitResults.addValue("p"+i, results[i]);
					//fitResults.addValue(fitFunc.header[i], results[i]);
			}
			
			this.fitResults.addValue("max", fitFunc.getMax());
			
			if (this.profiles==null) {
					this.profiles=new ResultsTable();
					this.profiles.setValues("x", this.xvalues);
					this.profiles.setValues(""+IJ.d2s(n*cal.pixelDepth), this.profile);
					
			} else {	
					
				this.profiles.setValues(""+IJ.d2s(n*cal.pixelDepth), this.profile);
			};
		}
			
		
		if (analysis.getShowTable()) {
										profiles.show(HorizontalLineAnalysis.titleProfiles);
										fitResults.show(HorizontalLineAnalysis.titleFitResults);
		}
		if (analysis.getShowPlot()) new ImagePlus("FitPlots",fitPlots).show();
	}
}
