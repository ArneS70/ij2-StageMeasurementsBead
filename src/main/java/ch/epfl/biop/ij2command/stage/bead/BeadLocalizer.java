package ch.epfl.biop.ij2command.stage.bead;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.WindowManager;
import net.imagej.ImageJ;

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>Localize Bead 3D")
public class BeadLocalizer implements Command{
	@Override
	public void run() {
		ImagePlus imp=WindowManager.getCurrentImage();
		if (imp!=null && imp.getNSlices()>1) {
			final ImageJ ij = new ImageJ();
			ij.command().run(BeadLocalizer.class, true); 
		}
	}
	public static void main(final String... args) throws Exception {
	    // create the ImageJ application context with all available services
	    final ImageJ ij = new ImageJ();
	    ij.ui().showUI();

	    ij.command().run(BeadLocalizer.class, true);
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


