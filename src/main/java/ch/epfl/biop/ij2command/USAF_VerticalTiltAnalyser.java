package ch.epfl.biop.ij2command;

import java.awt.Rectangle;
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
		double distance;
		
		@Override
		public void run() {
			
				ResultsTable fitResults,lineResults;
				ImagePlus imp=WindowManager.getCurrentImage();
				
				
				if (imp!=null) {
					Roi roi=imp.getRoi();
					Rectangle r=roi.getBounds();
					
					
					EdgeWidthAnalyser ewa=new EdgeWidthAnalyser(imp.crop());
					ewa.findMaxima(10);
					
					int []maxTop=ewa.getTopMaxima();
					int []maxBottom=ewa.getBottomMaxima();
					
					int length=maxTop.length;
					int height=ewa.getHeight();
					lineResults=new ResultsTable();
					
					int num=imp.getImageStackSize();
					for (int n=0;n<num;n++) {
						imp.setSlice(n);
						for (int i=0;i<length-1;i+=2) {
							Line analyse=new Line(r.x+(maxTop[i+1]+maxTop[i])/2, r.y+5, r.x+(maxBottom[i+1]+maxBottom[i])/2, r.y+height-5);
							imp.setRoi(analyse);
							LineAnalyser lineAnalyser=new LineAnalyser(imp,analyse);
							lineResults.addValue("Line "+i,lineAnalyser.getMean());
							
							
						}
						lineResults.addRow();
						lineResults.show("LineResults");
					}
/*					if(roi.isLine()) {
						Line startLine=(Line)roi;
						calculateRepetition(imp,(Line)roi);
						lineResults=new ResultsTable();
						lineResults.addValue("Slice", "");
						lineResults.show("LineResults");
						
						
						
						int num=imp.getImageStackSize();
						for (int n=0;n<num;n++) {
							Line analyse=startLine;
							imp.setSlice(n);
							lineResults.addValue("Slice", n);
							lineResults.show("LineResults");
							for (int r=0;r<repetition;r++) {
								
								if (n==0) {
									lineResults.addValue("Line "+r,"");
									//if (r==0) lineResults.addRow();
								}
								
								analyse.setLocation(analyse.x1d+distance, analyse.y1d);
//								analyse.x1d=analyse.x1d+r*distance;
//								analyse.x2d=analyse.x2d+r*distance;
								LineAnalyser lineAnalyser=new LineAnalyser(imp,analyse);
								
								
								lineResults.addValue("Line "+r,lineAnalyser.getMean());
								
								
							}
							lineResults.addRow();						
	//						if (save) saveResults();
						}
						lineResults.show("LineResults");
					} else IJ.showMessage("Line selection required");*/
				} else IJ.showMessage("Please provide an image");
				
		}
		
		void calculateRepetition(ImagePlus imp, Line line) {
			
			int width=imp.getWidth();
			int xmax=0;
			if (line.x1>line.x2) xmax=line.x1; else xmax=line.x2;
			distance=(1000.0/(double)grating)/imp.getCalibration().pixelWidth;
			repetition=(int)((width-xmax)/distance);
			
			
		}
	
	public static void main(final String... args) throws Exception {
		// create the ImageJ application context with all available services
				
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
		ij.command().run(USAF_VerticalTiltAnalyser.class, true);
	}
}
