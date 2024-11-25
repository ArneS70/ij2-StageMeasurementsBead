package ch.epfl.biop.ij2command.USAF;

import ij.*;  
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;


import ch.epfl.biop.ij2command.stage.general.FitterFunction;
import ch.epfl.biop.ij2command.stage.general.GlobalFitter;
import ch.epfl.biop.ij2command.stage.general.Poly3Fitter;
import ch.epfl.biop.ij2command.stage.general.Poly8Fitter;  
  

public class MultiThreadFit extends HorizontalLineAnalysis {

	HorizontalLineAnalysis parameters;
//	private final ResultsTable fitResults=new ResultsTable();
//	private final ImageStack fitPlots=new ImageStack(696,415);
	Vector <double []> fitResults=new Vector<double[]>();
	Vector <ImageProcessor> plots=new Vector<ImageProcessor>();
	
	MultiThreadFit(HorizontalAnalysis ha){
		super(ha);
		this.analysis=super.analysis;
	}
	MultiThreadFit(HorizontalLineAnalysis hla){
		this.parameters=hla;
		this.analysis=hla.analysis;
	}
  
    public void run() { 
    	  
//    	double []x=parameters.lineProfiles.firstElement();
    	
    	horizontalLine=analysis.getHorizontalLine();
		ImagePlus inputImage=analysis.getImage();
		this.cal=inputImage.getCalibration();
		inputImage.getProcessor().setLineWidth(1);
		
    	final AtomicInteger ai = new AtomicInteger(1);
    	
    	final Thread[] threads = newThreadArray();
    	final double t0=System.currentTimeMillis();
  
    		for (int ithread = 0; ithread < threads.length; ithread++) {   
    			// Concurrently run in as many threads as CPUs  
    			IJ.log("ithread="+ithread);
    			FitterFunction fit=FitterFunction.getFitFunc(parameters.lineProfiles.firstElement(), parameters.lineProfiles.get(1), this.analysis.getFitFunc());
    			threads[ithread] = new Thread() {  
    				
    				public void run() {  
    					
    					final int last=parameters.lineProfiles.size();
//    					final int length=Poly8Fitter.header.length;
    					
//    					method=FitterFunction.Poly3;
//    					fitFunc=new Poly3Fitter(parameters.lineProfiles.firstElement(),parameters.lineProfiles.get(last/2));
//    					fitFunc.setHeader(Poly3Fitter.header);
    					
//    					fitFunc=new Poly8Fitter(parameters.lineProfiles.firstElement(),parameters.lineProfiles.get(last/2));
//    					fitFunc.setHeader(Poly8Fitter.header);
    					
    					double [] results=fit.getFitResults();
    					
//    					final String function=new GlobalFitter().createFormula(new double[]{results[0],results[1],results[2],results[3]});
    					final String function=new GlobalFitter().createPolyFormula(results,fit.numParam);
    					
    					
    					for (int i = ai.getAndIncrement(); i < last; i = ai.getAndIncrement()) { 
    					
    						double t=System.currentTimeMillis();
    						IJ.log("Stack position: "+i+"   "+IJ.d2s((t-t0)/1000));
    						
    						fit.updateInput(parameters.lineProfiles.firstElement(),parameters.lineProfiles.get(i));
    						//CurveFitter cf=new CurveFitter(parameters.lineProfiles.firstElement(),parameters.lineProfiles.get(i));
    						
    						//cf.doCustomFit(function, new double [] {1, 1,1},false);
    						
//    						double [] allParam=new double [6];
//    						allParam[0]=(double)i;
//    						allParam[5]=cf.getRSquared();
//    						System.arraycopy(cf.getParams(), 0, allParam, 1, 4);
    						
//    						parameters.fitResults.add(fit.getFitResults(function));
    						fitResults.add(fit.getFitResults(function));
    						plots.add(fit.getPlot().getImagePlus().getProcessor());
    						
//    						fitPlots.addSlice(fit.getPlot().getImagePlus().getProcessor());
    						
    						
    					}
    						
    					
    					
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
    ImageStack getFitPlots() {
    	return fitPlots; 
    }
    ResultsTable getFitResults() {
    	return fitResults;
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

    
  
}  