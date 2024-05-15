package ch.epfl.biop.ij2command;

import java.io.File;
import java.io.IOException;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
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

		@Override
		public void run() {
			BioformatsReader bfr=new BioformatsReader(fileInput.getAbsolutePath());
			try {
				ImagePlus [] imps=bfr.open();
				int num=imps.length;
				
				for (int n=0;n<num;n++) {
					USAF_FocusAnalyser ufm=new USAF_FocusAnalyser(imps[n],lineLength,repetitionX,repetitionY);
					ufm.run();
				}
				
			} catch (FormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
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
