

import java.awt.Polygon;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.MaximumFinder;

public class RoiBeadLocalizer {
	private int squareSize;
	private ImagePlus input;
	private ResultsTable trackData;
	private Calibration cal;
	
	RoiBeadLocalizer(){
	}
	
	public RoiBeadLocalizer(ImagePlus imp,int size) {
		setImage(imp);
		setSquareSize(size);
		this.cal=input.getCalibration();
	}
	
	private void setImage(ImagePlus imp){
		this.input=imp;
	}
	
	private void setSquareSize(int size) {
		this.squareSize=size;
	}
	public void run() {
		trackData=new ResultsTable();
//		input.show();
		Roi rect=new Roi(100,100,100,100);
		int slices=input.getNSlices();
		int frames=input.getNFrames();
		
		for (int t=1;t<=frames;t++) {
			IJ.log(t+"/"+frames);
			double max=0;
			int maxSlice=1;
			for (int s=1;s<=slices;s++) {
				input.setSlice((t-1)*slices+s);
	//			input.setRoi(rect);
	//			input.crop().show();
				
				double mean=input.getProcessor().getStats().mean;
				if (mean>max) {
					max=mean;
					maxSlice=s;
				}
			}
			
			
			input.setSlice((t-1)*slices+maxSlice);
			input.getProcessor().blurGaussian(3);
			
			MaximumFinder maximum=new MaximumFinder();
			Polygon p=maximum.getMaxima(input.getProcessor(), 10000, false);
			
			trackData.addRow();
			trackData.addValue("x", p.xpoints[0]*cal.pixelWidth);
			trackData.addValue("y", p.ypoints[0]*cal.pixelHeight);
			trackData.addValue("z-slice", maxSlice*cal.pixelDepth);
			
		}
		trackData.show("Results");
	}
}
