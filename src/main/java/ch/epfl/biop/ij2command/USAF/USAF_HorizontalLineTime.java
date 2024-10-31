package ch.epfl.biop.ij2command.USAF;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ch.epfl.biop.ij2command.stage.general.BioformatsReader;
import ch.epfl.biop.ij2command.stage.general.FitterFunction;
import ch.epfl.biop.ij2command.stage.general.GlobalFitter;
import ch.epfl.biop.ij2command.stage.general.Poly3Fitter;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Line;
import ij.gui.Plot;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import loci.formats.FormatException;
import net.imagej.ImageJ;

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF Horizontal Line timelapse")
public class USAF_HorizontalLineTime implements Command{

	ImagePlus fileInput;
	String fileName, filePath;

//	@Parameter(style="open")
//    File fileOpen;
	
	@Parameter(label="z-slice")
	int startZ;
	
	@Parameter(label="start T")
	int startT;
	
	@Parameter(label="stop T")
	int stopT;
	
	@Parameter(label="Show Plot(s)")
	boolean showPlot;

	@Parameter(label="Show result tables?")
	boolean showTable;
	
	@Parameter(label="Show profile tables?")
	boolean showProfile;
	
	@Parameter(label="Save Plots?")
	boolean savePlot;
	
	@Parameter(label="Save result tables?")
	boolean saveTables;
	
	
	Line toAnalyse;
	@Override
	public void run() {
		ImagePlus imp=WindowManager.getCurrentImage();	
		if (imp!=null){
			HorizontalAnalysis analysis=new HorizontalAnalysis.Builder(imp).setStartZ(startZ).setStartT(startT).setStopT(stopT).
																			savePLot(savePlot).showPlot(showPlot).setCalibration(imp.getCalibration()).
																			saveTables(saveTables).showTables(showTable).showProfile(showProfile).build();
			
			HorizontalLineTimelapse horizontal=new HorizontalLineTimelapse(analysis);
			analysis.setHorizontalLine( new HorizontalLine(imp.getProcessor()).findHorizontalLine());
			analysis.setHorizontalLine( new HorizontalLine(imp.getProcessor()).optimizeHorizontalMaxima(analysis.getHorizontalLine()));
			analysis.getImage().setRoi(analysis.getHorizontalLine());
			
			Vector <double []>profiles=horizontal.getTimeProfiles(startT,stopT);
			IJ.log(""+profiles.size());
			fit(profiles);
		
		}

	}
	void fit(Vector <double []> toFit) {
		ImageStack fitPlots=new ImageStack(696,415);
		final int last=toFit.size();
		final int length=Poly3Fitter.header.length;
		
		int proflength=toFit.get(0).length;
		double [] xvalue=new double [proflength];
		
		for (int n=0; n<proflength;n++) {
			xvalue[n]=n;
		}
		
		
		int method=FitterFunction.Poly3;
		FitterFunction fitFunc=new Poly3Fitter(xvalue,toFit.get(0));
		fitFunc.setHeader(Poly3Fitter.header);
		double [] results=fitFunc.getParameter();
		final String function=new GlobalFitter().createFormula(new double[]{results[0],results[1],results[2],results[3]});
		
		final double [] x=new double [last];
		final double [] p0=new double [last];
		final double [] p1=new double [last];
		final double [] p2=new double [last];
		final double [] p3=new double [last];
		
		for (int i = 0; i < last; i += 1) { 
		
			IJ.log("Stack position: "+i);
			CurveFitter cf=new CurveFitter(xvalue,toFit.get(i));
			cf.doCustomFit(function, new double [] {1, 1,1},false);
			results=cf.getParams();
			x[i]=i;//*fileInput.getCalibration().frameInterval;
			p0[i]=results[0]; p1[i]=results[1]; p2[i]=results[2]; p3[i]=results[3];
						
			IJ.log(i+"  "+results[0]+"  "+results[1]+"   "+results[2]+"   "+results[3]);
			fitPlots.addSlice(cf.getPlot().getImagePlus().getProcessor());
//			fitResults.addRow();
//			for (int n=0;n<length-1;n++) {
//				
//				fitResults.addValue("z / slice", i);
//				fitResults.addValue("p"+n, results[n]);
//					//fitResults.addValue(fitFunc.header[i], results[i]);
		}
		
		if (showTable) {
			ResultsTable table=new ResultsTable();
			table.setValues("x", x);
			table.setValues("p0", p0);
			table.setValues("p1", p1);
			table.setValues("p2", p2);
			table.setValues("p3", p3);
			table.show("Time_Focus_Shift");
			
		}
		if (showPlot) {
			new ImagePlus ("Plots",fitPlots).show();
			Plot plot=new Plot("A", "B", "C");
			plot.addPoints(x, p2, Plot.CIRCLE);
			plot.show();
		}
	}
	
	public static void main(final String... args) throws Exception {
		// create the ImageJ application context with all available services
				
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
		
		//IJ.run("Bio-Formats", "open=N:/temp-Arne/StageTest/240923/USAF_30LP.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
		//IJ.run("Bio-Formats", "open=D:/01-Data/StageMeasurements/240812/USAF_10x_Tilt05_horizizontal.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
		//ij.command().run(USAF_HorizontalLine.class, true);
	}
}