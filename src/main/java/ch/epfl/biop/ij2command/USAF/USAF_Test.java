package ch.epfl.biop.ij2command.USAF;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ch.epfl.biop.ij2command.stage.general.ArrayStatistics;
import ch.epfl.biop.ij2command.stage.general.Asym2SigFitter;
import ch.epfl.biop.ij2command.stage.general.Asym2SigFitterFixed;
import ch.epfl.biop.ij2command.stage.general.BioformatsReader;
import ch.epfl.biop.ij2command.stage.general.FitterFunction;
import ch.epfl.biop.ij2command.stage.general.LeicaStagePositionReader;
import ch.epfl.biop.ij2command.stage.general.NikonStagePositionReader;
import ch.epfl.biop.ij2command.stage.general.StagePositionReader;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Line;
import ij.gui.Plot;
import ij.io.FileInfo;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import loci.formats.FormatException;
import mdbtools.libmdb.file;
import net.imagej.ImageJ;

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF Test")

public class USAF_Test implements Command{

	@Parameter(label="Fitting Fuction",choices= {"Nikon","Leica","Zeiss","Evident"})
	String brand;
	
	@Parameter(style="open")							//XML File to analyse (stage positions)
    File fileXML;
	
	@Parameter(style="open")							//Image File to analyse
    File fileImage;
	
	@Parameter(label="Show step function fits")			//show fit of step function
    boolean showFit;
	
	@Parameter(label="Line fit to data")				//Line fit stage position vs. step function results
    boolean lineFit;
	
	@Parameter(label="Summarize results?")				//summarize results
    boolean summarize;
	
	@Override
	public void run() {
		
		ImageStack fit=new ImageStack(696,415);
		StagePositionReader reader=null;
		ImagePlus imp=null;
		
		if (brand.equals("Nikon")) {
			reader=new NikonStagePositionReader(fileXML.getAbsolutePath());
			String path=fileImage.getPath();
			imp=NikonStagePositionReader.openImage(fileImage.getPath());
		}
		if (brand.equals("Leica")) reader=new LeicaStagePositionReader(fileXML.getAbsolutePath());		
		
		
		ArrayList <Double> xpos=reader.getList(StagePositionReader.xPos);
		ArrayList <Double> ypos=reader.getList(StagePositionReader.yPos);
		
		
		
		int width=imp.getWidth();
		int height=imp.getHeight();
		int slices=imp.getImageStackSize();
		
		ImageProcessor ip=imp.getProcessor();
		ImageProcessor outMean=ip.createProcessor(width, slices);
		ImageProcessor outStdev=ip.createProcessor(width, slices);
		
		double [] x=new double [width];
		ArrayList <double []>lineProfilesMax=new ArrayList <double []>();
		ArrayList <double []>lineProfilesMean=new ArrayList <double []>();
		ArrayList <double []>stagePositions=new ArrayList <double []>();
		
		for (int s=1;s<=slices;s++) {
			imp.setSlice(s);
			ip=imp.getProcessor();
			
			double [] profileMean= new double [width];
			double [] profileMax= new double [width];
			for (int i=0;i<width;i++) {
				
				double [] line=ip.getLine(i, 0, i, height);
				if (s==1) x[i]=i*imp.getCalibration().pixelWidth;
				profileMean[i]=new ArrayStatistics(line).getMean();
				profileMax[i]=new ArrayStatistics(line).getMax();
				outMean.putPixelValue(i,s-1, new ArrayStatistics(line).getMean());
				outStdev.putPixelValue(i,s-1,new ArrayStatistics(line).getMax());
			}
			if (s==1) lineProfilesMax.add(x);
			lineProfilesMax.add(profileMax);
			lineProfilesMean.add(profileMean);
		}
		
		int size=lineProfilesMax.size();
		ResultsTable results=new ResultsTable();
		
		for (int s=1;s<size;s++) {
			CurveFitter cf=new CurveFitter(lineProfilesMax.get(0),lineProfilesMax.get(s));
			double min=new ArrayStatistics(lineProfilesMax.get(s)).getMin();
			double max=new ArrayStatistics(lineProfilesMax.get(s)).getMin();
			double center=new ArrayStatistics(lineProfilesMax.get(0)).getMax()/2;
			cf.setInitialParameters(new double [] {min,10,center,max});
			cf.doFit(CurveFitter.RODBARD);
			if (showFit) {
				fit.addSlice(cf.getPlot().getImagePlus().getProcessor());
			}
			double [] param=cf.getParams();
			results.addRow();
			results.addValue("#",s);
			results.addValue("Stage x",xpos.get(s-1));
			results.addValue("Stage y",ypos.get(s-1));
			for (int p=0;p<param.length;p++) {
				results.addValue("p"+p, param[p]);
			}
			
		}
		double [] deltax=results.getColumn("Stage x");
		double [] deltaShift=ArrayStatistics.arrayDivide(results.getColumn("p2"),1000);
		
		deltaShift=ArrayStatistics.arrayDifference(deltaShift, deltaShift[0]);
		deltax=ArrayStatistics.arrayDifference(deltax, deltax[0]);
		
		
		if (showFit) new ImagePlus("Step Function Fit",fit).show();
		results.show("FitResults");
		
		if (lineFit) {
			CurveFitter cf=new CurveFitter(deltax,deltaShift);
			cf.doFit(CurveFitter.STRAIGHT_LINE);
			cf.getPlot().show();
			summarizeResults(cf.getParams(),cf.getRSquared()).show("Summary Stage Calibration");
		}
		
		
		
		
		
		
//		ArrayList <Double> ypos=reader.getList(NikonStagePositionReader.yPos);
//		ArrayList <Double> zpos=reader.getList(NikonStagePositionReader.zPos);
		
	
	
	}
	ResultsTable summarizeResults(double [] param, double r) {
		
		ResultsTable summary=ResultsTable.getResultsTable("Summary Stage Calibration");
		if (summary==null) summary=new ResultsTable();
		summary.addRow();
		summary.addValue("File",fileImage.getName());
		summary.addValue("Vendor",brand);
		summary.addValue("a",param[0]);
		summary.addValue("b",param[1]);
		summary.addValue("SSD",param[2]);
		summary.addValue("R^2",r);
		summary.setDecimalPlaces(3, 6);
		
		
		
		
		return summary;
	}
	double [] getArray(ArrayList list) {
		int len=list.size();
		double [] out=new double[len];
		for (int i=0;i<len;i++) {
			out[i]=(double) list.get(i);
		}
		return out;
	}

	public static void main(final String... args) throws Exception {
		// create the ImageJ application context with all available services
				
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
		
		//IJ.run("Bio-Formats", "open=N:/temp-Arne/StageTest/240923/USAF_30LP.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
		//IJ.run("Bio-Formats", "open=D:/01-Data/StageMeasurements/240812/USAF_10x_Tilt05_horizizontal.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
		ij.command().run(USAF_Test.class, true);
	}
}
