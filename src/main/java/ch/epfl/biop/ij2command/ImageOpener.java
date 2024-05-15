package ch.epfl.biop.ij2command;



import java.io.File;
import java.io.IOException;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import loci.formats.FormatException;
import net.imagej.ImageJ;


/**
 * This example illustrates how to create an ImageJ 2 {@link Command} plugin.
 * The pom file of this project is customized for the PTBIOP Organization (biop.epfl.ch)
 * <p>
 * The code here is opening the biop website. The command can be tested in the java DummyCommandTest class.
 * </p>
 */

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>Open Image")
public class ImageOpener implements Command {

	@Parameter(style="open")
    File fileInput;
	
	    public void run() {
    	
    	//SimpleImageReader reader=new SimpleImageReader(fileInput.getAbsolutePath());
    	//IJ.log(""+reader.getChannels());
	    BioformatsReader bfr=new BioformatsReader(fileInput.getAbsolutePath());
	    try {
			ImagePlus [] imps=bfr.open();
			IJ.log(""+imps.length);
			imps[0].show();
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

        ij.command().run(ImageOpener.class, true);
    }
}

