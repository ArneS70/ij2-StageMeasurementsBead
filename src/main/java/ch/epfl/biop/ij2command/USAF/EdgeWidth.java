package ch.epfl.biop.ij2command.USAF;

/** This plugin analyses the line width from a Ronchi grating transmission image after the "Find Edges" command. The width is 
 *  obtained via a Gaussian Fit.
 * 	In case the specimen is tilted along the z axis the the width is a function of the y axis in the image. 
 * 	As input it requires a stack of images (z-stack) and a the z-position to analyse. 
 * 	 
 *  Please send comments to arne.seitz@epfl.ch
*/

import java.io.File;
import java.io.IOException;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ch.epfl.biop.ij2command.stage.general.BioformatsReader;
import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.ResultsTable;
import loci.formats.FormatException;
import net.imagej.ImageJ;

		@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF EdgeWidth")
		public class EdgeWidth implements Command {
			@Parameter(style="open")							//File to analyse
		    File fileInput;
			@Parameter(label="Slice")							//Slice to analyse
			int slice;
			@Parameter(label="Analysis window height")			//Height of analysis window
			int analysisHeight;
			
			@Parameter(label="Save result tables?")				
			boolean save;
			
			@Parameter(label="Diplay Fit windows")				//Fit windows of Gaussian fit
			boolean fit;
			
			@Parameter(label="Diplay Edge Detection")			//Show the cropped region that is used for the fitting the Gauss profiles.
			boolean edge;
			
			@Parameter(label="Diplay Virtual Focus Fit windows")	//Virtual focus is defined as the position of the minimal line width. 
			boolean focusFit;
			
			@Parameter(label="Process multiple stacks")			//Process all stacks in an image container 
			boolean multipleStacks;

		@Override
		/**
		 * Opens the image using Bioformats and passes it on the the EdgeWidthAnalyser
		 */
		public void run() {
			BioformatsReader bfr=new BioformatsReader(fileInput.getAbsolutePath());
			try {
				ImagePlus [] imps=bfr.open();
				int num=1;
				if (multipleStacks) num=imps.length;
								
				for (int n=0;n<num;n++) {
					EdgeWidthAnalyser ewa=new EdgeWidthAnalyser(imps[n],slice,analysisHeight);
					if (fit) ewa.setShowFit(true);
					if (edge) ewa.setShowEdges(true);
					ewa.fitEdgeWidth();
					ewa.findVirtualFocus(focusFit);
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
