package ch.epfl.biop.ij2command.stage.general;

import java.util.Vector;

public class MultiThreadFitter extends Thread{
	// ===============used for multithreading only ========================== 
	//start and stop for the individual threads
	private int start=0;
	private int stop=0;
	private int multiStackSize=0;
	//number of processors
	private final static int n_cpus=Runtime.getRuntime().availableProcessors();
	Vector <double []>lines;
	private int numLines;
	
	MultiThreadFitter(Vector <double []>input){
		lines=input;
		numLines=lines.size();
	}
	 public MultiThreadFitter [] getArray() {
//			IJ.log("Start: getArray");
	    	MultiThreadFitter array []=new MultiThreadFitter[n_cpus];
			
			
			int [] start=new int [n_cpus];
			int [] stop=new int[n_cpus];
			
			int mod=numLines % n_cpus;
			int delta=numLines/n_cpus;
			
//			if (stackSize%n_cpus==0) delta=stackSize/n_cpus;
//			else delta=(int)Math.round(0.5+((double)stackSize/n_cpus));
			
			
			
			for (int i=0;i<n_cpus;i++){
				start[i]=delta*(i);
				stop[i]=delta*(i+1)-1;
				if (i>=n_cpus-mod) {
					start[i]=stop[i-1]+1;
					stop[i]=start[i]+delta;
				}
//				IJ.log("i="+i+"start="+start[i]+"   stop"+stop[i]);
				array[i]=new DiffractionGenerator(this.tg,start[i],stop[i],this.dd);
				array[i].setPhotons(this.getPhotons());
			}
			return array;
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
