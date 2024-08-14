package ch.epfl.biop.ij2command;

import java.io.File;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Line;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import net.imagej.ImageJ;

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF Vertical Tilt Analyser")
public class USAF_VerticalTiltAnalyser implements Command{
	
		@Parameter(label="Save result tables?")
		boolean save;
		@Parameter(label="Grating lp/mm")
		int grating;
	
		int repetition;
		int distance;
		
		@Override
		public void run() {
			
				ResultsTable fitResults,lineResults;
				ImagePlus imp=WindowManager.getCurrentImage();
				
				if (imp!=null) {
					
					Roi roi=imp.getRoi();
					
					if(roi.isLine()) {
						calculateRepetition(imp,(Line)roi);
						lineResults=new ResultsTable();
						lineResults.addValue("Slice", "");
						lineResults.show("LineResults");
						
						
						
						int num=imp.getImageStackSize();
						for (int n=0;n<5;n++) {
							
							imp.setSlice(n);
							lineResults.addValue("Slice", n);
							lineResults.show("LineResults");
							for (int r=0;r<repetition;r++) {
								
								if (n==0) {
									lineResults.addValue("Line "+r,"");
									//if (r==0) lineResults.addRow();
								}
								Line analyse=(Line)roi;
								analyse.x1d=analyse.x1d+r*distance;
								analyse.x2d=analyse.x2d+r*distance;
								LineAnalyser lineAnalyser=new LineAnalyser(imp,analyse);
								
								
								lineResults.addValue("Line "+r,lineAnalyser.getMean());
								
								
							}
							lineResults.addRow();						
	//						if (save) saveResults();
						}
						lineResults.show("LineResults");
					} else IJ.showMessage("Line selection required");
				} else IJ.showMessage("Please provide an image");
				
		}
		
		void calculateRepetition(ImagePlus imp, Line line) {
			
			int width=imp.getWidth();
			int xmax=0;
			if (line.x1>line.x2) xmax=line.x1; else xmax=line.x2;
			double gap=(1000.0/(double)grating)/imp.getCalibration().pixelWidth;
			repetition=(int)((width-xmax)/gap);
			
			
		}
	
	public static void main(final String... args) throws Exception {
		// create the ImageJ application context with all available services
				
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
		ij.command().run(USAF_VerticalTiltAnalyser.class, true);
	}
}
