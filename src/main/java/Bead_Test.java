

import java.io.File;
import java.util.ArrayList;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import net.imagej.ImageJ;

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>Bead Test")

public class Bead_Test implements Command{

	@Parameter(style="open")							//Image File to analyse
    File fileImage;
	
	
	
	@Override
	public void run() {
		BioformatsReader reader=new BioformatsReader(fileImage.getAbsolutePath());
		ImagePlus [] series=reader.openAllSeries();
		int len= series.length;
		
		RoiBeadLocalizer localize=new RoiBeadLocalizer(series[0],50);
		localize.run();
		
		
		
		
	}
	ImagePlus openImage(String path) {
		  
		  IJ.run("Bio-Formats", "open="+path+" color_mode=Default concatenate_series open_all_series rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT");
		  ImagePlus imp=WindowManager.getCurrentImage().duplicate();
		  WindowManager.getCurrentWindow().close();
		  return imp;
	  }

	public static void main(final String... args) throws Exception {
		// create the ImageJ application context with all available services
				
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
		
		//IJ.run("Bio-Formats", "open=N:/temp-Arne/StageTest/240923/USAF_30LP.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
		//IJ.run("Bio-Formats", "open=D:/01-Data/StageMeasurements/240812/USAF_10x_Tilt05_horizizontal.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
		ij.command().run(Bead_Test.class, true);
	}
}
