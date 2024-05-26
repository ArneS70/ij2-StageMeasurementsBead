package ch.epfl.biop.ij2command;

import java.io.File;
import java.io.IOException;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.ResultsTable;
import loci.formats.FormatException;
import net.imagej.ImageJ;

		@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF EdgeWidthGlobal")
		public class EdgeWidthGlobal implements Command {
			@Parameter(style="open")
		    File fileInput;
			@Parameter(label="Slice start")
			int startSlice;
			
			@Parameter(label="Slice end")
			int endSlice;
			
			@Parameter(label="Analysis window heigt")
			int analysisHeight;
			
						@Parameter(label="Diplay Virtual Focus Fit windows")
			boolean focusFit;

		@Override
		public void run() {
			ResultsTable rt=new ResultsTable();
			BioformatsReader bfr=new BioformatsReader(fileInput.getAbsolutePath());
			try {
				ImagePlus [] imps=bfr.open();
				int num=imps.length;
				
				for (int n=0;n<num;n++) {
					EdgeWidthAnalyser ewa=new EdgeWidthAnalyser();
					for (int i=startSlice;i<endSlice;i++) {
						ewa =new EdgeWidthAnalyser(imps[n],i,analysisHeight);
						ewa.fitEdgeWidth();
						ewa.findVirtualFocus(false);
						ResultsTable param=ResultsTable.getResultsTable("FitResults");
						if (i==startSlice) {
							rt.setValues("x",param.getColumn("position"));
						}
						rt.setValues(""+i, param.getColumn("p3"));
					}
					ewa.globalVirtualFocusFit(rt);
					
					
					
					
					//if (save) saveResults();
				}
				
			} catch (FormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		}
		public static void main(final String... args) throws Exception {
			// create the ImageJ application context with all available services
					
			final ImageJ ij = new ImageJ();
			ij.ui().showUI();
			ij.command().run(EdgeWidthGlobal.class, true);
		}
	}
