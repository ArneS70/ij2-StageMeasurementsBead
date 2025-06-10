

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.WindowManager;
import net.imagej.ImageJ;

/**
 *  
 * The pom file of this project is customized for the PTBIOP Organization (biop.epfl.ch)
 * <p>
 * The code here is tracking a fluorescent bead in 3D.
 * </p>
 */

@Plugin(type = Command.class, menuPath = "Plugins>StabilityMeasurement>Localize Bead 3D")
public class LocalizeBead3D implements Command {

	@Parameter(label="Bead size in um")
    int sizeBead;
	
	@Parameter(label="Time gap")
    int gap;
	
	@Parameter (choices= {SimpleBeadLocalizer.methodSimple,SimpleBeadLocalizer.methodEllipse,SimpleBeadLocalizer.methodGauss,SimpleBeadLocalizer.method2DGauss,SimpleBeadLocalizer.method2DSymGauss}, style="listBox") 
	String method;
	
	@Parameter(label="show Rois")
    boolean showRois;
	
	@Parameter(label="Summarize results")
    boolean summarize;
	
	@Parameter(label="show Fit Window(s)")
    boolean showFit;
	
	@Parameter(label="show Drift Plot")
    boolean showDrift;
	
	@Override
    
    public void run() {
    	
    	ImagePlus imp=WindowManager.getCurrentImage();
 		imp.show();
 		
 		double diameterBead= (sizeBead/imp.getCalibration().pixelWidth);
 		// calculate the bead diameter in pixels
 		SimpleBeadLocalizer track=new SimpleBeadLocalizer(imp,diameterBead,method,gap);
 		track.setGap(gap);
 		if (showFit) track.showFit();
 		track.run();
 		if (showRois) track.showRois("Bead Localizing Results--"+method);
 		if (summarize) track.summarizeResults().show("BeadLocalizingResults_"+method+"_Summary");
 		if (showDrift) track.getDriftPLot().show();
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
        ij.command().run(LocalizeBead3D.class, true);
    }
}
