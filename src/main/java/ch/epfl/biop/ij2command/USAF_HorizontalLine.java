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
			@Parameter(style="open")
		    File fileInput;
			
			@Parameter(label="Save result tables?")
			boolean save;

		@Override
		public void run() {
			
				ResultsTable fitResults;
				ImagePlus imp=WindowManager.getCurrentImage();
				
				if (imp!=null) {
					
					Roi roi=imp.getRoi();
					
					if(roi.isLine()) {
						
						int num=imp.getImageStackSize();
						for (int n=0;n<num;n++) {
							imp.setSlice(n);
							HorizontalLineAnalyser hla=new HorizontalLineAnalyser(imp,(Line)roi);
							hla.writeResultsTable();
							if (save) saveResults();
						}
					} else IJ.showMessage("Line selection required");
				} else IJ.showMessage("Please provide an image");
		    
		}
		
		void saveResults() {
			String fileName=fileInput.getName();
			int n=fileName.indexOf(".");
			fileName=fileName.substring(0, n);
			String path=fileInput.getAbsolutePath();
			n=path.indexOf(fileName);
			path=path.substring(0, n);
			ResultsTable rt=ResultsTable.getResultsTable("Results x-Axis");
			rt.save(path+fileName+"_xAxis.csv");
			
			rt=ResultsTable.getResultsTable("Results y-Axis");
			rt.save(path+fileName+"_yAxis.csv");
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
			ij.command().run(USAF_HorizontalLine.class, true);
		}
		
}
