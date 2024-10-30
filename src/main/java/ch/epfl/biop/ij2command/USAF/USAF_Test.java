package ch.epfl.biop.ij2command.USAF;

import java.awt.Color;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import ch.epfl.biop.ij2command.stage.general.Asym2SigFitter;
import ch.epfl.biop.ij2command.stage.general.Asym2SigFitterFixed;
import ch.epfl.biop.ij2command.stage.general.FitterFunction;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Line;
import ij.gui.Plot;
import net.imagej.ImageJ;

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF Test")
public class USAF_Test implements Command{

	@Override
	public void run() {
		ImageStack stack=new ImageStack(640,480);
		ImagePlus imp=WindowManager.getCurrentImage();
		Line l=(Line)imp.getRoi();
		double []profile=imp.getProcessor().getLine(l.x1d, l.y1d, l.x2d, l.y2d);
		int len=profile.length;
		double []x=new double[len];
		
		for (int i=0;i<len;i++) {
			x[i]=i*imp.getCalibration().pixelWidth;
		}
		Asym2SigFitter fit=new Asym2SigFitter(x, profile);
		
		
		
		for (int s=1;s<=3;s++) {
			IJ.log("Slice   :"+s);
			imp.setSlice(s);
			profile=imp.getProcessor().getLine(l.x1d, l.y1d, l.x2d, l.y2d);
			Asym2SigFitterFixed fit=new Asym2SigFitter(x, profile);
			fit.fit();
			IJ.log("Fit Done");
			double [][] func=fit.getFunctionValues(x,fit.getParameters());
			Plot plot=new Plot("A","B","C");
			plot.setSize(640, 480);
			plot.addPoints(x, profile, Plot.CIRCLE);
			plot.setColor(new Color(255,0,0));
			plot.draw();
			plot.setLineWidth(3);
			plot.addPoints(func[0], func[1], Plot.LINE);
			stack.addSlice(plot.getImagePlus().getProcessor());
		}
		new ImagePlus("Fit Plots",stack).show();
	}

	public static void main(final String... args) throws Exception {
		// create the ImageJ application context with all available services
				
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
		
		//IJ.run("Bio-Formats", "open=N:/temp-Arne/StageTest/240923/USAF_30LP.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
		IJ.run("Bio-Formats", "open=D:/01-Data/StageMeasurements/240812/USAF_10x_Tilt05_horizizontal.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
		ij.command().run(USAF_Test.class, true);
	}}
