package ch.epfl.biop.ij2command;

import java.awt.Window;
import java.io.File;
import java.io.IOException;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Line;
import ij.gui.Roi;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import loci.formats.FormatException;
import net.imagej.ImageJ;

		
	@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF Horizontal Foccus")
		public class USAF_HorizontalFocus implements Command {

		@Parameter(style="open")
	    File fileInput;
			
		@Parameter(label="number of focus points")
		int repetition;
		
//		@Parameter(label="z-stack Start")
		int start;
		
//		@Parameter(label="z-stack End")
		int end;
		
		@Parameter(label="z-stack Step")
		int step;
		
		@Parameter(label="Analysis line length (vetical)")
		int lineLength;
		
		@Parameter(label="Show Fit window?")
		boolean showFit;

		@Parameter(label="Save result tables?")
		boolean save;
		
//		@Parameter(label="Results Index")
//		String text;
		
		@Parameter(label="Variable Line length?")
		boolean lineOptimize;
		
		@Override
		public void run() {
				ResultsTable fitResults=null;
				
				FocusAnalyser fa=null;
				
				ImagePlus imp=WindowManager.getCurrentImage();
				if (imp==null) {
					IJ.run("Bio-Formats", "open="+fileInput+" color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
					imp=WindowManager.getCurrentImage();
				}
				
				IJ.log("===============================================================");
				IJ.log("File: "+imp.getTitle());
				
				if (imp!=null) {
					if (imp.getRoi()==null) setLine(imp);
					Roi roi=imp.getRoi();
					
					if (roi!=null ) {
						if(roi.isLine()) {
							
							fa=new FocusAnalyser(imp,setLineLength(imp));
							int[] param=setStackSize(imp);
							fa.setStart(param[0]);
							fa.setEnd(param[1]);
							fa.setStep(step);
							fa.analyseLine(repetition,lineLength);
							fitTableResults(fa);
							if (save) saveResults();
						
						}
					}	
						
					
				} 
		    
			
		}
		int [] setStackSize(ImagePlus stack) {

			
			Line line= (Line)stack.getRoi();
			int width=stack.getWidth();
			int stackSize=stack.getStackSize();
			double difMin=0;
			int min=0;
			for (int i=1;i<=stackSize-1;i+=20) {
				stack.setSliceWithoutUpdate(i);
				ImageProcessor ip=stack.getProcessor();
				double [] profileLeft=ip.getLine(0, line.y1d, 20, line.y2d);
				double [] profileRight=ip.getLine(width-20, line.y1d, width-1, line.y2d);
				double diff=Math.abs(new ArrayStatistics(profileLeft).getMean()-new ArrayStatistics(profileRight).getMean());
				if (i==1) {difMin=diff;}
				else {
					if (diff<difMin) 
						{difMin=diff; min=i;}
				}
				
			}
			if (difMin<stackSize/2) {
				start=1;end=2*min;
			} else {
				
				end=stackSize-1;start=stackSize-2*min;
			}
			
/*			stack.setSliceWithoutUpdate(stackSize/2);
			double [] profile=stack.getProcessor().getLine(l.x1d, l.y1, l.x2, l.y2);
			double max=new ArrayStatistics(profile).getMax();
			double value=0;
			int start=0;
			int end=stackSize;
			int pos=0;
			do {
				stack.setSliceWithoutUpdate(pos);
				value=stack.getProcessor().getLine(l.x1d, l.y1d, l.x2d, l.y2d)[0];
				pos+=1;
			}while (value<0.5*max);
			start=pos;
			pos=end;
			do {
				stack.setSliceWithoutUpdate(pos);
				profile=stack.getProcessor().getLine(l.x1d, l.y1d, l.x2d, l.y2d);
				value=profile[profile.length-1];
				pos-=1;
			}while (value<0.9*max);
			end=pos;
*/			
			return new int []{start,end};
		}
		Line setLineLength(ImagePlus imp_l) {
			Line l= (Line)imp_l.getRoi();
			int stackSize=imp_l.getStackSize();
			imp_l.setSlice(stackSize/2);
			double [] profile=imp_l.getProcessor().getLine(l.x1d, l.y1, l.x2, l.y2);
			double max=new ArrayStatistics(profile).getMax();
			double value=0;
			imp_l.setSlice(0);
			ImageProcessor ip=imp_l.getProcessor();
			do {
				l.x1d=l.x1d+10;
				profile=ip.getLine(l.x1d, l.y1d, l.x2d, l.y2d);
				value=ip.getLine(l.x1d, l.y1d, l.x2d, l.y2d)[0];
			}while (value<0.9*max);
			
			imp_l.setSlice(stackSize);
			ip=imp_l.getProcessor();
			do {
				l.x2d=l.x2d-10;
				profile=ip.getLine(l.x1d, l.y1d, l.x2d, l.y2d);
				value=ip.getLine(l.x1d, l.y1d, l.x2d, l.y2d)[profile.length-1];
			}while (value<0.9*max);
			
			return l;
		}
		/*
		*
		*Changing the line length is not recommended as it makes the fitting less reliable. It is better to change the start of the focus points
		*along the line.
		*/
		void setLine(ImagePlus imp) {
			int slice=imp.getImageStackSize();
			imp.setSlice(slice/2);
			ImageProcessor ip_edge=imp.getProcessor().duplicate().convertToFloat();
			ip_edge.findEdges();
			LineAnalyser la=new LineAnalyser(new ImagePlus("Edges",ip_edge),1);
			Roi [] lines=la.findVerticalMaxima(10,400);
			int pos=1+lines.length/2;
			ImageProcessor ip=imp.getProcessor();
			ip.setRoi(lines[pos]);
			double mean1=ip.getStatistics().mean;
			
			ip.setRoi(lines[pos+1]);
			double mean2=ip.getStatistics().mean;
			//IJ.log("m1="+mean1+"    m2="+mean2);
			
			if (mean1>mean2)imp.setRoi(lines[pos]); 
			else imp.setRoi(lines[pos+1]);
			
			imp.updateAndDraw();
			
			
		}
		void LogtoTable() {
			if (WindowManager.getWindow("Focus Results")==null) {
				ResultsTable focus=new ResultsTable();
				focus.show("Focus Results");
			};
			ResultsTable focus=ResultsTable.getResultsTable("Focus Results");
			focus.addRow();
			focus.addValue("File", fileInput.getName());
			focus.addValue("Repetition", this.repetition);
			
		}
		void fitTableResults(FocusAnalyser fa) {		
			
			TableFitter tableFit=new TableFitter(fa.getFocusResults());
			tableFit.fitTable(CurveFitter.POLY5);
			tableFit.getFitResults().show("Table Fit Results");
			
			int last=tableFit.getFitResults().getLastColumn();
			
			CurveFitter cf=new CurveFitter(tableFit.getFitResults().getColumnAsDoubles(0),tableFit.getFitResults().getColumnAsDoubles(last));
			cf.doFit(CurveFitter.STRAIGHT_LINE);
			double [] param=cf.getParams();
			IJ.log("Focus shift z-axis  slice/um: "+param[1]);
			double slope=param[1]*fa.getZstep();
			IJ.log("Slope: "+slope);
			double angle=180*Math.atan(slope)/Math.PI;
			IJ.log("angle/deg: "+angle);
			IJ.log("R^2: "+cf.getFitGoodness());
			
			if (showFit) cf.getPlot().show();
		}
		
		void saveResults() {
			String fileName=fileInput.getName();
			int n=fileName.indexOf(".");
			fileName=fileName.substring(0, n);
			String path=fileInput.getAbsolutePath();
			n=path.indexOf(fileName);
			path=path.substring(0, n);
			ResultsTable rt=ResultsTable.getResultsTable("Table Fit Results");
			rt.save(path+fileName+"_TableFits.csv");
			
			rt=ResultsTable.getResultsTable("Horizontal Focus");
			rt.save(path+fileName+"_HorizontalFocus.csv");
			closeNonImageWindows();
			
			
			
			
		}
		void closeNonImageWindows() {
			Window [] win=WindowManager.getAllNonImageWindows();
			int num=win.length;
			
			for (int i=0;i<num;i++) {
				win[i].dispose();
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
			//IJ.run("Bio-Formats", "open=X:/StageTest/240812/UASF_10x_Tilt05_horizizontal.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_2");
			IJ.run("Bio-Formats", "open=D:/01-Data/StageMeasurements/240812/USAF_10x_Tilt05_horizizontal.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");

			ij.command().run(USAF_HorizontalFocus.class, true);
		}
		
}
