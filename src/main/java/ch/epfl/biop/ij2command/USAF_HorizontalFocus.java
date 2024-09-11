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
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import loci.formats.FormatException;
import net.imagej.ImageJ;

		
	@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF Horizontal Foccus")
		public class USAF_HorizontalFocus implements Command {
//			@Parameter(style="open")
//		    File fileInput;
			
//			@Parameter(label="Save result tables?")
//			boolean save;
		
		@Parameter(label="number of focus points")
		int repetition;
		
		@Parameter(label="Show Fit window?")
		boolean showFit;

		@Override
		public void run() {
			
				ResultsTable fitResults=null;
				TableFitter tableFit=null;
				FocusAnalyser fa=null;
				ImagePlus imp=WindowManager.getCurrentImage();
				IJ.log("===============================================================");
				IJ.log("File: "+imp.getTitle());
				
				if (imp!=null) {
					
					Roi roi=imp.getRoi();
					
					if(roi.isLine()) {
						fa=new FocusAnalyser(imp,(Line)roi);
						fa.analyseLine(repetition);
					
					} else IJ.showMessage("Line selection required");
				} else IJ.showMessage("Please provide an image");
		    
				
				tableFit=new TableFitter(fa.getFocusResults());
				tableFit.fitTable();
				tableFit.getFitResults().show("Table Fit Results");
				
				int last=tableFit.getFitResults().getLastColumn();
				
				CurveFitter cf=new CurveFitter(tableFit.getFitResults().getColumnAsDoubles(0),tableFit.getFitResults().getColumnAsDoubles(last));
				cf.doFit(CurveFitter.STRAIGHT_LINE);
				double [] param=cf.getParams();
				IJ.log("Focus shift z-axis per slice: "+param[1]);
				IJ.log("R^2: "+cf.getFitGoodness());
				
				if (showFit) cf.getPlot().show();
		}
		
/*		void saveResults() {
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
			
		}	*/
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
			ij.command().run(USAF_HorizontalFocus.class, true);
		}
		
}
