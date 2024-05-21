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

		@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF EdgeWidth")
		public class EdgeWidth implements Command {
			@Parameter(style="open")
		    File fileInput;
			@Parameter(label="Length line")
			int lineLength;
			@Parameter(label="Analysis window heigt")
			int analysisHeight;
			
			@Parameter(label="Save result tables?")
			boolean save;

		@Override
		public void run() {
			BioformatsReader bfr=new BioformatsReader(fileInput.getAbsolutePath());
			try {
				ImagePlus [] imps=bfr.open();
				int num=imps.length;
				
				for (int n=0;n<num;n++) {
					EdgeWidthAnalyser ewa=new EdgeWidthAnalyser(imps[n]);
					ewa.fitEdgeWidth(20);
					//if (save) saveResults();
				}
				
			} catch (FormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		}
		public static void main(final String... args) throws Exception {
			// create the ImageJ application context with all available services
					
			final ImageJ ij = new ImageJ();
			ij.ui().showUI();
			ij.command().run(EdgeWidth.class, true);
		}
	}
