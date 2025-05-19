package ch.epfl.biop.ij2command.stage.bead;

import java.awt.Polygon;
import java.io.File;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.measure.CurveFitter;
import ij.plugin.Projector;
import ij.plugin.ZAxisProfiler;
import ij.plugin.ZProjector;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.frame.Fitter;
import ij.process.ImageStatistics;
import net.imagej.ImageJ;



/**
 *  
 * The pom file of this project is customized for the PTBIOP Organization (biop.epfl.ch)
 * <p>
 * The code here is tracking a fluorescent bead in 3D.
 * </p>
 */

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>Localize Bead 3D")
public class LocalizeBead3D implements Command {

	private int width;
	private int height;
	private int channels;
	private int slices;
	private int frames;
	private double zRes;
	
	@Parameter(label="Bead size in um")
    int sizeBead;
	
	@Parameter(label="Time gap")
    int gap;
	
	@Parameter (choices= {SimpleBeadLocalizer.methodSimple,SimpleBeadLocalizer.methodEllipse,SimpleBeadLocalizer.methodGauss,SimpleBeadLocalizer.method2DGauss}, style="listBox") 
	String method;
	
	@Parameter(label="show Rois")
    boolean showRois;
	
	@Parameter(label="Summarize results")
    boolean summarize;
	
	@Parameter(label="show Fit Window(s)")
    boolean showFit;
	
	
	
	
    
	@Override
    
    public void run() {
    	
    	ImagePlus imp=WindowManager.getCurrentImage();
    	
 		
 		imp.show();
 		zRes=imp.getCalibration().pixelDepth;
 		double diameterBead= (sizeBead/imp.getCalibration().pixelWidth);
 		// calculate the bead diameter in pixels
 		SimpleBeadLocalizer track=new SimpleBeadLocalizer(imp,diameterBead,method,gap);
 		track.setGap(gap);
 		if (showFit) track.showFit();
 		track.run();
 		if (showRois) track.showRois("Bead Localizing Results--"+method);
 		if (summarize) track.summarizeResults("Bead Localizing Results--"+method).show("Bead Localizing Results--Summary");
 		
 		
 		
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
