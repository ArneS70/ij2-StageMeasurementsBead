package ch.epfl.biop.ij2command.USAF;

import ij.*;  
import ij.plugin.PlugIn;  
import ij.process.*;
import net.imagej.ImageJ;
import ij.io.Opener;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;

import java.util.concurrent.atomic.AtomicInteger;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import ch.epfl.biop.ij2command.stage.general.FitterFunction;
import ch.epfl.biop.ij2command.stage.general.GlobalFitter;
import ch.epfl.biop.ij2command.stage.general.Poly3Fitter;  
  

public class MultiThreadHLA extends HorizontalLineAnalysis {

	HorizontalLineAnalysis parameters;
	
	MultiThreadHLA(HorizontalAnalysis ha){
		super(ha);
		this.analysis=super.analysis;
	}
	MultiThreadHLA(HorizontalLineAnalysis hla){
		this.parameters=hla;
		this.analysis=hla.analysis;
	}
  
    public void run() { 
    	//   final int starting_threshold = 190;  
    	//   final int ending_threshold = 255;  
    	//   final int n_tests = ending_threshold - starting_threshold + 1;  
    	//  final AtomicInteger ai = new AtomicInteger(starting_threshold);  
  
    	horizontalLine=analysis.getHorizontalLine();
		ImagePlus inputImage=analysis.getImage();
		this.cal=inputImage.getCalibration();
		inputImage.getProcessor().setLineWidth(1);
		final int start=analysis.getStartZ();
		final int stop=analysis.getStopZ();
		final int zstep=analysis.getStepZ();
    	final int iterations=(stop-start)/zstep;
    	final AtomicInteger ai = new AtomicInteger(iterations);
 
    	final Thread[] threads = newThreadArray();  
  
    		for (int ithread = 0; ithread < threads.length; ithread++) {   
    			// Concurrently run in as many threads as CPUs  
  
    			threads[ithread] = new Thread() {  
  
    				public void run() {  
    					for (int i = ai.getAndIncrement(); i <= iterations; i = ai.getAndIncrement()) { 
    					
    						inputImage.setSliceWithoutUpdate(analysis.stackCenter);
	    					profile=inputImage.getProcessor().getLine((double)horizontalLine.x1,(double)horizontalLine.y1,(double)horizontalLine.x2,(double)horizontalLine.y2);
	
	    					xvalues=new double [profile.length];
	    					
	    					for (int n=0;i<profile.length;n++) {
	    						xvalues[n]=i*cal.pixelWidth;
	    					}
	    					int length=Poly3Fitter.header.length;
	    					fitFunc=new Poly3Fitter(xvalues,profile);
	    					fitFunc.setHeader(Poly3Fitter.header);
	    					method=FitterFunction.Poly3;
	    					double [] results=fitFunc.getParameter();
	    					String function=new GlobalFitter().createFormula(new double[]{results[0],results[1],results[2],results[3]});
            
    					 
    						  
    						IJ.log("===================================");
    						IJ.log("Slice: "+start+i*zstep);

    						if (fitResults==null) fitResults=new ResultsTable();
    						
    						inputImage.setSliceWithoutUpdate(start+i*zstep);
    						profile=inputImage.getProcessor().getLine((double)horizontalLine.x1,(double)horizontalLine.y1,(double)horizontalLine.x2,(double)horizontalLine.y2);
    						CurveFitter cf=new CurveFitter(xvalues,profile);
    						cf.doCustomFit(function, new double [] {1, 1,1},false);
    						results=cf.getParams();
    						IJ.log(results[0]+"  "+results[1]+"   "+results[2]);
    						
    						fitPlots.addSlice(cf.getPlot().getImagePlus().getProcessor());
    						fitResults.addRow();
    					
    						for (int n=0;n<length-1;n++) {
    							fitResults.addValue("z / um", i*cal.pixelDepth);
    							fitResults.addValue("p"+i, results[n]);
    								//fitResults.addValue(fitFunc.header[i], results[i]);
    						}
    						
    						fitResults.addValue("max", fitFunc.getMax());
    						
    						if (profiles==null) {
    								profiles=new ResultsTable();
    								profiles.setValues("x", xvalues);
    								profiles.setValues(""+IJ.d2s(i*cal.pixelDepth), profile);
    								
    						} else {	
    								
    							profiles.setValues(""+IJ.d2s(i*cal.pixelDepth), profile);
    						};
    					}
    						
    					
    					if (analysis.getShowTable()) {
    													profiles.show(HorizontalLineAnalysis.titleProfiles);
    													fitResults.show(HorizontalLineAnalysis.titleFitResults);
    					}
    					if (analysis.getShowPlot()) new ImagePlus("FitPlots",fitPlots).show();
            }  
          
        };  
    }  
    
    startAndJoin(threads);  
  
        // now the results array is full. Just show them in a stack:  
 //   final ImageStack stack = new ImageStack(dot_blot.getWidth(), dot_blot.getHeight());  
 //   for (int i=0; i< results.length; i++) {  
 //           stack.addSlice(Integer.toString(i), results[i]);  
//    }  
 //       new ImagePlus("Results", stack).show();  
    }  
  
    /** Create a Thread[] array as large as the number of processors available. 
    * From Stephan Preibisch's Multithreading.java class. See: 
    * http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD 
    */  
    private Thread[] newThreadArray() {  
        int n_cpus = Runtime.getRuntime().availableProcessors();  
        return new Thread[n_cpus];  
    }  
  
    /** Start all given threads and wait on each of them until all are done. 
    * From Stephan Preibisch's Multithreading.java class. See: 
    * http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD 
    */  
    public static void startAndJoin(Thread[] threads)  
    {  
        for (int ithread = 0; ithread < threads.length; ++ithread)  
        {  
            threads[ithread].setPriority(Thread.NORM_PRIORITY);  
            threads[ithread].start();  
        }  
  
        try  
        {     
            for (int ithread = 0; ithread < threads.length; ++ithread)  
                threads[ithread].join();  
        } catch (InterruptedException ie)  
        {  
            throw new RuntimeException(ie);  
        }  
    }

    public static void main(final String... args) throws Exception {
		// create the ImageJ application context with all available services
				
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
		
		//IJ.run("Bio-Formats", "open=N:/temp-Arne/StageTest/240923/USAF_30LP.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
		//IJ.run("Bio-Formats", "open=D:/01-Data/StageMeasurements/240812/USAF_10x_Tilt05_horizizontal.lif color_mode=Composite rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
		ij.command().run(USAF_HorizontalLine.class, true);
	} 
  
}  