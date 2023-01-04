package ch.epfl.biop.ij2command;

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
 * This example illustrates how to create an ImageJ 2 {@link Command} plugin.
 * The pom file of this project is customized for the PTBIOP Organization (biop.epfl.ch)
 * <p>
 * The code here is opening the biop website. The command can be tested in the java DummyCommandTest class.
 * </p>
 */

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>Track Bead")
public class TrackBead implements Command {

	private int width;
	private int height;
	private int channels;
	private int slices;
	private int frames;
	private double zRes;
	
	@Parameter(style="open")
    File fileInput;
	
	@Parameter(label="Bead size in um")
    int sizeBead;
	
    @Override
    public void run() {
    	
    	IJ.open(fileInput.getAbsolutePath());
 		ImagePlus imp=WindowManager.getCurrentImage();
 		zRes=imp.getCalibration().pixelDepth;
 		double diameterBead= (sizeBead/imp.getCalibration().pixelWidth);
 		// calculate the bead diameter in pixels
 		SimpleBeadTracker track=new SimpleBeadTracker(imp,diameterBead);
 		track.analyzeStack();
 		
 		
    }
    private void measureStack(ImagePlus imp, OvalRoi roi) {
    	int nSlices=imp.getImageStackSize();
    	
    	double [] zIntensity=new double [nSlices];
    	double [] pos=new double [nSlices];
    	imp.setRoi(roi);
    	for (int s=0;s<nSlices;s++) {
    		pos[s]=s*zRes;
    		imp.setSlice(s);
    		ImageStatistics stat=imp.getProcessor().getStatistics();
    		zIntensity[s]=stat.mean;
    	}
//    	Plot Zposition=new Plot("Z axis plot", "Position", "Intensity", pos, zIntensity);
//    	Zposition.show();
    	CurveFitter zMax=new CurveFitter(pos,zIntensity);
    	double initParam []= {0,400,11,3};
    	zMax.doCustomFit("y=a+b*exp(-1*pow(abs(x-c),2)/(2*pow(d,2)))", initParam, false);
//    	IJ.log(zMax.getResultString());
    	double results[]=zMax.getParams();
    	IJ.log("z max="+results[2]);
    }
    private void pasteImageDimension(int[] dimensions) {
    	int length=dimensions.length;
    	if (dimensions==null) return;
    	this.width=dimensions[0];
    	if (length>0) this.height=dimensions[1]; else return;
    	
    	if (length>1) this.channels=dimensions[2]; else return;
    	if (length>2) this.slices=dimensions[3]; else return;
    	if (length>3) this.frames=dimensions[4]; else return;
    	
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

        ij.command().run(TrackBead.class, true);
    }
}
