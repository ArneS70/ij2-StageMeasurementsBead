package ch.epfl.biop.ij2command;

import java.io.File;
import java.io.IOException;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.ResultsTable;
import loci.formats.FormatException;
import net.imagej.ImageJ;

		@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF FocusMap")
		public class USAF_FocusMap implements Command {
			@Parameter(style="open")
		    File fileInput;
			@Parameter(label="Length line")
			int lineLength;
			@Parameter(label="Repetitions x-Axis")
			int repetitionX;
			@Parameter(label="Repetitions y-Axis")
			int repetitionY;
			@Parameter(label="Save result tables?")
			boolean save;

		@Override
		public void run() {
			BioformatsReader bfr=new BioformatsReader(fileInput.getAbsolutePath());
			try {
				ImagePlus [] imps=bfr.open();
				int num=imps.length;
				
				for (int n=0;n<num;n++) {
					USAF_FocusAnalyser ufm=new USAF_FocusAnalyser(imps[n],lineLength,repetitionX,repetitionY);
					ufm.run();
					if (save) saveResults();
				}
				
			} catch (FormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
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
			ij.command().run(USAF_FocusMap.class, true);
		}
		
}
