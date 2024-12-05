package ch.epfl.biop.ij2command.stage.general;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.CurveFitter;
import ij.process.ImageProcessor;

public class MultiThreadFitter extends Thread{
	// ===============used for multithreading only ========================== 
	//start and stop for the individual threads
	private int start=0;
	private int stop=0;
	private int multiStackSize=0;
	private int method=1;
	private String functionName;
	private String fitFunction;
	//number of processors
	private final static int n_cpus=Runtime.getRuntime().availableProcessors();
	Vector <double []>lines;
	double [] globalParam;
	public Vector <double []>fitResults=new Vector <double []>();
	public Vector <ImageProcessor>fitPlots=new Vector <ImageProcessor>();
	public Vector <Integer>fitOrder=new Vector<Integer>();
	
	private int numLines;
	private long t0=System.currentTimeMillis();
	
	public MultiThreadFitter(Vector <double []>input){
		lines=input;
		numLines=lines.size();
		
	}
	public MultiThreadFitter(Vector <double []>input,String name){
		lines=input;
		numLines=lines.size();
		this.functionName=name;
		
	}
	MultiThreadFitter(Vector <double []>input,int start,int stop,String function){
		this.lines=input;
		this.start=start;
		this.stop=stop;
		this.functionName=function;
	
	}
	
	public void run() {
		
		int size=this.lines.size();
		
		for (int i=1;i<size;i++) {
			FitterFunction fit=FitterFunction.getGlobalFitFunc(lines.elementAt(0),lines.elementAt(i),functionName);
			double [] results=fit.getFitResults(this.globalParam);
			fitResults.add(results);
			fitPlots.add(fit.getPlot().getImagePlus().getProcessor());
			
			fitOrder.add(this.start+i-1);
			IJ.log(Thread.currentThread()+"     "+(i+this.start-1)+"      "+((System.currentTimeMillis()-t0)/1000.0));
			

			
		}
	}
	public MultiThreadFitter [] getArray(double [] param) {
//			IJ.log("Start: getArray");
	    	MultiThreadFitter array []=new MultiThreadFitter[n_cpus];
			
			
			int [] start=new int [n_cpus];
			int [] stop=new int[n_cpus];
			
			int mod=(numLines-1) % n_cpus;
			int delta=(numLines-1)/n_cpus;
			
//			if (stackSize%n_cpus==0) delta=stackSize/n_cpus;
//			else delta=(int)Math.round(0.5+((double)stackSize/n_cpus));
			
			
			
			for (int i=0;i<n_cpus;i++){
				start[i]=delta*(i)+1;
				stop[i]=delta*(i+1);
				if (i>=n_cpus-mod) {
					start[i]=stop[i-1]+1;
					stop[i]=start[i]+delta;
				}
				IJ.log("i="+i+"start="+start[i]+"   stop"+stop[i]);
				
				array[i]=new MultiThreadFitter(getLines(start[i],stop[i]),start[i],stop[i],this.fitFunction);
				array[i].functionName=this.functionName;
				array[i].fitFunction=this.fitFunction;
				array[i].globalParam=param;
				
			}
			return array;
	} 
	Vector<double []> getLines(int start,int stop){
		Vector<double []> shortVec=new Vector<double[]>();
		shortVec.add(this.lines.elementAt(0));
		for (int i=start;i<=stop;i++) {
			shortVec.add(this.lines.elementAt(i));
		}
		return shortVec;
	}
	public void multiThreadCalculate(double [] param){
		
//		this.fitResults.add(param);
//		this.fitFunction=function;
//		IJ.log("Start multiThread");
		long start=System.currentTimeMillis();
		
		final MultiThreadFitter [] calculate = new MultiThreadFitter(this.lines,this.functionName).getArray(param); 
//		final ImageStack resultStack=new ImageStack(this.width/scaleFactor,this.height/scaleFactor);  
//		IJ.log("startAndJoin");
		startAndJoin(calculate);
		int size=calculate.length;
		
		for (int i=0;i<size;i++) {
			int num=calculate[i].fitResults.size();
			for (int j=0;j<num;j++) {
				this.fitOrder.add(calculate[i].fitOrder.elementAt(j));
				this.fitResults.add(calculate[i].fitResults.elementAt(j));
				this.fitPlots.add(calculate[i].fitPlots.elementAt(j));
			}
			
		}
		long end=System.currentTimeMillis();    
		IJ.log("Processing time for curve fitting in sec: "+(end-start)/1000);

//	    for (int i=0;i<nFrames;i++){
//	        	resultStack.addSlice(res_ip[i]);
//	    }  
	    

	    
	                	
	    
//	    return new ImagePlus("Conv",DiffractionGenerator.getResultStack(calculate));
	}
	public static void startAndJoin(MultiThreadFitter [] profileArray)  
	    {  
	        for (int ithread = 0; ithread < n_cpus; ++ithread)  
	        {  
	            profileArray[ithread].setPriority(Thread.NORM_PRIORITY);  
	            profileArray[ithread].start();  
	        }  
	  
	        try  
	        {     
	            for (int ithread = 0; ithread < n_cpus; ++ithread)  
	                profileArray[ithread].join();  
	        } catch (InterruptedException ie)  
	        {  
	            throw new RuntimeException(ie);  
	        }  
	    }
}
