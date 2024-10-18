package ch.epfl.biop.ij2command;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Line;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

public class HorizontalAnalysisMethods {
		HorizontalAnalysis analysis;
		
		HorizontalAnalysisMethods(){
			
		}
		HorizontalAnalysisMethods(HorizontalAnalysis analysis){
			this.analysis=analysis;
		}
/************************************************************************************************************
 * Output logFile
 * 		
 ************************************************************************************************************/
		void logFileNames() {
			IJ.log("===============================================================");
			analysis.filePath=IJ.getDirectory("file");
			analysis.fileName=analysis.getImage().getTitle();
			
			if (analysis.fileName.startsWith(analysis.filePath)) 
				analysis.fileName=analysis.getImage().getTitle().substring(analysis.filePath.length());
				IJ.log("File: "+analysis.fileName);
				IJ.log("Path: "+analysis.filePath);
		}
		
		void showPlot(ImagePlus fit) {
			fit.show();
		}
		void savePlot(ImagePlus fit) {
			IJ.save(fit, analysis.filePath+analysis.fileName+" "+IJ.pad(analysis.counter, 4)+".tif");
		}
		void saveResultTables(ResultsTable table,String name) {
			
			int n=this.analysis.fileName.indexOf(".");
			String saveName=analysis.fileName.substring(0, n);
			table.save(analysis.filePath+saveName+name+IJ.pad(analysis.counter, 4)+".csv");
//			table.save(analysis.filePath+saveName+"_Profiles_"+IJ.pad(analysis.counter, 4)+".csv");
//			analysis.focusShiftTable.save(analysis.filePath+analysis.fileName+"_FocusShift_"+IJ.pad(analysis.counter, 4)+".csv");
		}

		void showResultsTables(ResultsTable table,String title) {
			
			table.show(title);
			
		}
/************************************************************************************************************
 * Methods to check input and parameter 
 * 		
 ************************************************************************************************************/
		public boolean checkInputImage() {
			boolean check=true;
			if (analysis.getImage()==null) {
				IJ.log("Please provide an image");
				check=false;
			}
			
			if (analysis.getImage().getNSlices()==1) {
				IJ.log("Please provide an z-stack");
				check=false;
			}
//			if (inputImage.getNFrames()>1) this.isTimeLapse=true;
			return check;
			
		}
		
		void checkParameters(){
			int max=analysis.getImage().getNSlices();
			analysis.stackSlices=max;
			if (analysis.getStepZ()<1) analysis.setStepZ(1);
			if (analysis.getStepZ()>max) analysis.setStepZ(max/10);
			if (analysis.allStack) {analysis.setStartZ(1);analysis.setStopZ(max);analysis.stackCenter=max/2;}
			if (analysis.getStopZ()<analysis.getStartZ()) {
				do {
					analysis.setStartZ(analysis.getStartZ() - 1);
				}while (analysis.getStopZ()<analysis.getStartZ());
			}
			
			
			if (analysis.getStartZ()<1)analysis.setStartZ(1);
			if (analysis.getStopZ()>max)analysis.setStopZ(max);
			if (analysis.getStopZ()<analysis.getStartZ())analysis.setStartZ(analysis.getStopZ());
			
		}
		/************************************************************************************************************
		 * Output logFile
		 * 		
		 ************************************************************************************************************/
		void disableStack() {
			analysis.allStack=false;
		}
		
		void ignoreTimelapse() {
			analysis.ignoreTime=true;
		}
		
		boolean  hasLine() {
			Roi roi=analysis.getImage().getRoi();
			if (roi!=null) return roi.isLine();
			else return false;
		}
		
		Line getLine() {
			return (Line)analysis.getImage().getRoi();
		}
		/********************************************************************************************************** 
		 * Getter and Setter
		 **********************************************************************************************************/
			
			int getAnalysisLineWidth() {
				return analysis.lineWidth;
			}
			ImageProcessor getCenterIP(){
				analysis.getImage().setSliceWithoutUpdate(getStackCenter());
				return analysis.getImage().getProcessor();
			}
			Line getHorizontalLine() {
				return analysis.getHorizontalLine();
			}
			ImageStack getImageStack() {
				ImageStack stack=new ImageStack(analysis.getImage().getWidth(),analysis.getImage().getHeight());
				int start=analysis.getStartZ();
				int stop=analysis.getStopZ();
				int step=analysis.getStepZ();
				
				if (!analysis.isTimeLapse) {
					int slices=analysis.getImage().getNSlices();
					if (analysis.allStack) {
						analysis.setStartZ(1);
						analysis.setStopZ(slices);
					}
					for (int s=start;s<=stop;s+=step) {
						analysis.getImage().setSlice(s);
						stack.addSlice(analysis.getImage().getProcessor());
								
					}
				}
				return stack;
			
			}
			ImagePlus getInputImage(){
				return analysis.getImage();
			}
			int getZStep() {
				return analysis.getStepZ();
			}
			void setHorizontalLine(Line horizontal) {
				analysis.setHorizontalLine(horizontal);
			}
			void setSliceStart(int value) {
				analysis.setStartZ(value);
			}
			void setSliceStop(int value) {
				analysis.setStopZ(value);
			}
			
			void shiftStackCenter(int shift) {
				analysis.shiftStackCenter(shift);
			}
			ResultsTable getResultsTable() {
				return analysis.getAnalysisTable();
			}
			int getStackCenter() {
				return analysis.getstackCenter();
			}
			int getStackSlices() {
				return analysis.getStackSlices();
			}
			public void getSummaryTable(String title) {
				if (WindowManager.getWindow(title)==null) {
					ResultsTable summaryResults=new ResultsTable();
					
					summaryResults.show(title);
					analysis.counter=0;
					return;
				};
				analysis.summaryResults=ResultsTable.getResultsTable(title);
				analysis.counter=analysis.summaryResults.getCounter();
			}

}
