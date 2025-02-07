package ch.epfl.biop.ij2command.USAF;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
import ch.epfl.biop.ij2command.stage.general.ZeissStagePositionReader;
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
import ij.process.ImageStatistics;
import loci.formats.FormatException;
import mdbtools.libmdb.file;
import net.imagej.ImageJ;

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF TiltMeasurement")

public class TiltFocusMeasurement implements Command{

	@Parameter(label="Fitting Fuction",choices= {"Nikon","Leica","Zeiss","Evident"})
	String brand;
	
	@Parameter(style="open")							//XML File to analyse (stage positions)
    File fileXML;
	
	@Parameter(style="open")							//Image File to analyse
    File fileImage;
	/*
	@Parameter(label="Show step function fits")			//show fit of step function
    boolean showFit;
	
	@Parameter(label="Line fit to data")				//Line fit stage position vs. step function results
    boolean lineFit;
	
	@Parameter(label="Summarize results?")				//summarize results
    boolean summarize;
	*/
	
	@Override
	public void run() {
		
		ImageStack fit=new ImageStack(696,415);
		StagePositionReader reader=null;
		ImagePlus imp=null;
		
//		IJ.run("Bio-Formats", "open=N:/temp-Arne/StageTest/250203/USAF_Flat01_ZStack.lsm color_mode=Composite rois_import=[ROI manager] split_timepoints view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
		
		if (brand.equals("Nikon")) {
			reader=new NikonStagePositionReader(fileXML.getAbsolutePath());
			String path=fileImage.getPath();
			
		}
		if (brand.equals("Leica")) {
			reader=new LeicaStagePositionReader(fileXML.getAbsolutePath());
			String path=fileImage.getPath();
			
		}
		if (brand.equals("Zeiss")) {
			reader=new ZeissStagePositionReader(fileXML.getAbsolutePath());
			String path=fileImage.getPath();
			
		}
		
		ArrayList <Double> xpos=reader.getList(StagePositionReader.xPos);
		ArrayList <Double> ypos=reader.getList(StagePositionReader.yPos);
		ArrayList <Double> zpos=reader.getList(StagePositionReader.zPos);
		
		ResultsTable summary=new ResultsTable();
		
		int len=xpos.size();
		
		
		summary.show("TiltMeasurement");
 	   	
		BioformatsReader imageReader=new BioformatsReader(fileImage.getAbsolutePath());
		
		ImagePlus[] img=imageReader.openAllSeries();
		
		len=img.length;
		
		for (int i=0;i<len;i++) {
			IJ.log("Image Series"+i+"/"+len);
			imp=img[i];
			int slices=imp.getNSlices();
			double [] stdDev=new double [slices];
			double [] pos=new double [slices];
			for (int s=1;s<=slices;s++) {
				imp.setSliceWithoutUpdate(s);
				ImageProcessor ip=imp.getProcessor();
				ImageStatistics stat=ip.getStats();
				stdDev[s-1]=stat.stdDev;
				pos[s-1]=s;
				
				
			}
			summary.addRow();
			summary.addValue("x",xpos.get(i));
			summary.addValue("y",ypos.get(i));
			summary.addValue("z",zpos.get(i));
			summary.addValue("Focal Plane",new ArrayStatistics(stdDev).getMaxPos());
			summary.show("TiltMeasurement");
//			IJ.log(""+new ArrayStatistics(stdDev).getMaxPos());
//			CurveFitter fitter=new CurveFitter(pos,stdDev);
//			fitter.doFit(CurveFitter.GAUSSIAN);
//			fitter.getPlot().show();
		}
			
				
			
			
		
		
		
		
		
	
	
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
		ij.command().run(TiltFocusMeasurement.class, true);
	}
}

