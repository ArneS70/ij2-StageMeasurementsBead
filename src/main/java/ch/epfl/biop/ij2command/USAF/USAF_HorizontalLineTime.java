package ch.epfl.biop.ij2command.USAF;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Line;

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF Horizontal Line timelapse")
public class USAF_HorizontalLineTime implements Command{

	ImagePlus fileInput;
	String fileName, filePath;

//	@Parameter(style="open")
//    File fileInput;
	
	@Parameter(label="z-slice")
	int startZ;
	
	@Parameter(label="Show Focus Shift Plot")
	boolean showPlot;

	@Parameter(label="Show result tables?")
	boolean showTable;
	
	@Parameter(label="Show profile tables?")
	boolean showProfile;
	
	@Parameter(label="Save Plot?")
	boolean savePlot;
	
	@Parameter(label="Save result tables?")
	boolean saveTables;
	
	
	Line toAnalyse;
	@Override
	public void run() {
		ImagePlus imp=WindowManager.getCurrentImage();	
		if (imp!=null){
			HorizontalAnalysis analysis=new HorizontalAnalysis.Builder(imp).setStartZ(startZ).
																			savePLot(savePlot).showPlot(showPlot).setCalibration(imp.getCalibration()).
																			saveTables(saveTables).showTables(showTable).showProfile(showProfile).build();
			
			HorizontalLineAnalysis horizontal=new HorizontalLineAnalysis(analysis);
			horizontal.run();
		
		}

	}
}