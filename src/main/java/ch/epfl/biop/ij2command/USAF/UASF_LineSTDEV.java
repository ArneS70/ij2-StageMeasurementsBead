package ch.epfl.biop.ij2command.USAF;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import ch.epfl.biop.ij2command.stage.general.ArrayStatistics;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Line;
import ij.measure.ResultsTable;
import net.imagej.ImageJ;

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF Line STDEV")
public class UASF_LineSTDEV implements Command{
	
	@Override
	public void run() {
		ImagePlus imp=WindowManager.getCurrentImage();
		ResultsTable table=new ResultsTable();
		if (imp!=null) {
			Line line=(Line)imp.getRoi();
			double slope=(line.y2d-line.y1d)/(line.x2d-line.x1d);
			double []profile;
			for (int i=0;i<line.x2d;i++) {
				profile=imp.getProcessor().getLine(line.x1d+i, line.y1d+slope*i-5, line.x1d+i, line.y1d+slope*i+5);
				imp.setRoi(new Line(line.x1d+i, line.y1d+slope*i-10, line.x1d+i, line.y1d+slope*i+10), true);
				table.addValue("#", i);
				table.addValue("Mean", new ArrayStatistics(profile).getMean());
				table.addValue("STDEV", new ArrayStatistics(profile).getSTDEV());
				table.addRow();
			}
			table.show("Line Profile");
		}
		
		
	}
	public static void main(final String... args) throws Exception {
		// create the ImageJ application context with all available services
				
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
		
		IJ.run("Bio-Formats", "open=N:/temp-Arne/StageTest/240923/USAF_30LP.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
		//IJ.run("Bio-Formats", "open=D:/01-Data/StageMeasurements/240812/USAF_10x_Tilt05_horizizontal.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
		ij.command().run(USAF_HorizontalLine.class, true);
	}
	
}
