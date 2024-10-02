package ch.epfl.biop.ij2command;

import java.io.File;
import java.io.IOException;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Line;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import loci.formats.FormatException;
import net.imagej.ImageJ;

		
	@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF Horizontal Line")
		public class USAF_HorizontalLine implements Command {

			ImagePlus fileInput;
			String fileName, filePath;

			//			@Parameter(style="open")
//		    File fileInput;
			
			@Parameter(label="z-step")
			int zstep;
			
			@Parameter(label="Save result tables?")
			boolean save;

		@Override
		public void run() {
			
				ResultsTable fitResults;
				this.fileInput=WindowManager.getCurrentImage();
				
				if (fileInput!=null) {
					
					this.filePath=IJ.getDirectory("file");
					this.fileName=fileInput.getTitle();
					
					if (fileName.startsWith(filePath)) 
						this.fileName=fileInput.getTitle().substring(filePath.length());
					Roi roi=fileInput.getRoi();
					
					if (roi!=null ) {
						if(roi.isLine()) {
							
							int num=fileInput.getImageStackSize();
							for (int n=1;n<=num;n+=zstep) {
								fileInput.setSlice(n);
								IJ.log("===================================");
								IJ.log("Slice: "+n);
								HorizontalLineAnalyser hla=new HorizontalLineAnalyser(fileInput,(Line)roi);
								hla.writeFitResultsTable(FitterFunction.Poly3, true);
								if (save) saveResults();
							}
						}
					} else IJ.showMessage("Line selection required");
				} else IJ.showMessage("Please provide an image");
		    
		}
		
		void saveResults() {
			
			int n=fileName.indexOf(".");
			fileName=fileName.substring(0, n);
			
			n=filePath.indexOf(fileName);
			filePath=filePath.substring(0, n);
			ResultsTable rt=ResultsTable.getResultsTable("Results x-Axis");
			rt.save(filePath+fileName+"_xAxis.csv");
			
			rt=ResultsTable.getResultsTable("Results y-Axis");
			rt.save(filePath+fileName+"_yAxis.csv");
			WindowManager.closeAllWindows();
			
		}
		/**
		* This main function serves for development purposes.
		* It allows you to run the plugin immediately out of
		* your integrated development environment (IDE).
		*
		* @param args whatever, it's ignored
		* @throws Exception
		*/
		public static void main(final String... args) throws Exception {
			// create the ImageJ application context with all available services
					
			final ImageJ ij = new ImageJ();
			ij.ui().showUI();
			
			IJ.run("Bio-Formats", "open=N:/temp-Arne/StageTest/240923/USAF_30LP.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
			//IJ.run("Bio-Formats", "open=D:/01-Data/StageMeasurements/240812/USAF_10x_Tilt05_horizizontal.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
			ij.command().run(USAF_HorizontalLine.class, true);
		}
		
}
