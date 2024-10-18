package ch.epfl.biop.ij2command.USAF;



import org.scijava.command.Command;

import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Line;

import ij.gui.Roi;

import net.imagej.ImageJ;

		
	@Plugin(type = Command.class, menuPath = "Plugins>BIOP>Shift Line")
		public class Shift_Line implements Command {


		@Override
		public void run() {
		   ImagePlus imp=WindowManager.getCurrentImage();
		   Roi roi=imp.getRoi();
		   imp.deleteRoi();
		   Line line=(Line)roi;
		   double x1=line.x1d;double x2=line.x2d;
		   double y1=line.y1d;double y2=line.y2d;
		   for (double i=0;i<21;i++) {
			   WindowManager.toFront(imp.getWindow());
//			   WindowManager.getImage(imp.getTitle());
			   line=new Line(x1,y1,x2,y2+10-i);
			   
			   imp.setRoi(line);
			   IJ.run("USAF Horizontal Foccus", "repetition=15 start=1 end=103 step=20 linelength=3 allstack=true showfit=true savefitwindow=false save=false");
			   imp.deleteRoi();
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
			
			IJ.run("Bio-Formats", "open=D:/01-Data/StageMeasurements/240812/USAF_10x_Tilt06_horizontal.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
	        IJ.run("USAF Horizontal Foccus", "repetition=25 start=1 end=103 step=10 linelength=3 allstack=true showfit=false savefitwindow=false save=false");
			ij.command().run(Shift_Line.class, true);
		}
		
}