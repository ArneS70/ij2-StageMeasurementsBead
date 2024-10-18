package ch.epfl.biop.ij2command.USAF;

import ij.ImagePlus;
import loci.common.DebugTools;
import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;
import net.imagej.ImageJ;

import java.io.IOException;

public class SimpleIJLaunch {
    // -Dorg.slf4j.simpleLogger.defaultLogLevel=debug
    static public void main(String... args) throws IOException, FormatException {
        // Arrange
        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        DebugTools.enableLogging("DEBUG");
        //DebugTools.enableLogging("INFO");
        //SwingUtilities.invokeAndWait(() ->);
        //System.out.println("bf version = "+VersionUtils.getVersion(ZeissCZIReader.class));
        ij.ui().showUI();
        //

        ImporterOptions options = new ImporterOptions();
        options.setVirtual(true);
        options.setId("http://imagej.net/images/blobs.gif");
        ImagePlus[] images = BF.openImagePlus(options);
        images[0].show();

    }
}

