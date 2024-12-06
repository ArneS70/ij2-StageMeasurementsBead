package ch.epfl.biop.ij2command.USAF;



import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Line;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

public class HorizontalAnalysisMethods {
		protected HorizontalAnalysis analysis;
		protected ImagePlus inputImage;
		protected Line horizontalLine;
		protected Calibration cal;
		protected double [] profile;
		public double [] xvalues;
		private boolean isTimeLapse;
		
		HorizontalAnalysisMethods(){
			
		}
		HorizontalAnalysisMethods(HorizontalAnalysis analysis){
			this.analysis=analysis;
		}
/************************************************************************************************************
 * Output logFile
 *  
 * 		
 ************************************************************************************************************/
		void logFileNames(){
			IJ.log("===============================================================");
			analysis.filePath=IJ.getDirectory("file");
			analysis.fileName=analysis.getImage().getTitle();
			if (analysis.fileName.startsWith(analysis.filePath)) 
				analysis.fileName=analysis.getImage().getTitle().substring(analysis.filePath.length());
				
			analysis.fileName=checkFileName(analysis.fileName);
			IJ.log("File: "+analysis.fileName);
			IJ.log("Path: "+analysis.filePath);
				
		
		}
		String getFilePath(String input) {
			int pos=input.lastIndexOf("/");
			
			return input.substring(0, pos+1);
		}
		String getFileName(String input) {
			int pos=input.lastIndexOf("/");
			int len=input.length();
			return input.substring(pos+1, len);
		}
		String checkFileName(String input) {
			String output=new String(input);
			do   {
				output=output.replaceFirst("/", "_");
			}while (output.contains("/"));
			
			do   {
				output=output.replaceFirst(" ", "");
			}while (output.contains(" "));
			
//			do   {
//				output=output.replaceFirst(".", "_");
//			}while (output.contains("."));
			
			do   {
				output=output.replaceFirst("-", "_");
			}while (output.contains("-"));
			return output;
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
//			String pathName=analysis.filePath+saveName+name+IJ.pad(analysis.counter, 4)+".csv";
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
			
//			if (analysis.getImage().getNSlices()==1) {
//				IJ.log("Please provide an z-stack");
//				check=false;
//			}
			if (analysis.getImage().getNFrames()>1) this.isTimeLapse=true;
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
			
			max=analysis.getImage().getNFrames();
			if (analysis.getStartT()<1)analysis.setStartT(1);
			if (analysis.getStopT()>max)analysis.setStopT(max);
			if (analysis.getStopT()<analysis.getStartT())analysis.setStartT(analysis.getStopT());
			
		}
		public int [] checkStackParameters(ImagePlus imp, boolean entireZStack, boolean entireTStack, int[] param) {
			int nSlices=imp.getNSlices();
			int nFrames=imp.getNFrames();
			
			
			
/*			GenericDialog gd=new GenericDialog("Stack Parameters");
			gd.addNumericField("Z start", 1, 0);
			gd.addNumericField("Z stop", nSlices, 0);
			gd.addNumericField("Z step", 1, 0);
			gd.addNumericField("T start", 1, 0);
			gd.addNumericField("T stop", nFrames, 0);
			gd.addNumericField("T step", 1, 0);
			
			gd.showDialog();
*/			
			
			int startZ=param[0];
			int stopZ=param[1];
			int stepZ=param[2];
			int startT=param[3];
			int stopT=param[4];
			int stepT=param[5];
			
			if (entireZStack) {startZ=1;stopZ=nSlices;};
			if (entireTStack) {startT=1;stopT=nFrames;};
			
			if (startZ<1)startZ=1; if (startZ>nSlices)startZ=nSlices;
			if (stopZ<2)stopZ=2; if (stopZ>nSlices)stopZ=nSlices;
			if (stepZ<1)stepZ=1; if (stepZ>nSlices)stepZ=nSlices;
			if (startZ==stopZ) stopZ++;
			
			if (startT<1)startT=1; if (startT>nFrames)startT=nFrames;
			if (stopT<2)stopT=2; if (stopT>nFrames)stopT=nFrames;
			if (stepT<1)stepT=1; if (stepT>nFrames)stepT=nFrames;
			if (startT==stopT) stopT++;
			
			return new int [] {startZ,stopZ,stepZ,startT,stopT,stepT};
			
			
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
			ImageProcessor getIP(int position){
				int max=analysis.getImage().getImageStackSize();
				if (position>max) position=max;
				if (position <1)position=1;
				analysis.getImage().setSliceWithoutUpdate(position);
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
