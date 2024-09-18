package ch.epfl.biop.ij2command;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;

public class TableFitter {
	private ResultsTable results,fitParameters;
	private boolean showFit=false;
	private double xmax;
	
	TableFitter(ResultsTable toFit){
		this.results=toFit;
	}
	void fitTable() {
		int columns=results.getLastColumn();
		double [] x=results.getColumnAsDoubles(0);
		this.xmax=x[x.length-1];
		
		fitParameters=new ResultsTable();
		for (int i=1;i<columns;i++) {
			fitParameters.addRow();
			fitParameters.addValue("y/um", Double.parseDouble(results.getColumnHeading(i)));
			double y []=results.getColumnAsDoubles(i);
			
			CurveFitter cf=new CurveFitter(x, y);
			cf.doFit(CurveFitter.POLY3);
			double [] parameters=cf.getParams();
			int numParam=parameters.length;
			
			for (int n=0;n<numParam;n++) {
				fitParameters.addValue(""+n, parameters[n]);
			}
			double p=2*parameters[2]/(3*parameters[3]);
			double q=parameters[1]/(3*parameters[3]);
			double x1=(-p/2)+Math.sqrt((p*p/4)-q);
			double x2=(-p/2)-Math.sqrt((p*p/4)-q);
			
			fitParameters.addValue("x1", x1);
			fitParameters.addValue("x2", x2);
			
			
			
			if (showFit) cf.getPlot().show();
			
		}
		checkResults();
		//fitParameters.show("Fit Parameters");
	}
	void fitTable(int function) {
		
		int columns=results.getLastColumn();
		double [] x=results.getColumnAsDoubles(0);
		this.xmax=x[x.length-1];
		
		fitParameters=new ResultsTable();
		for (int i=1;i<columns;i++) {
			fitParameters.addRow();
			fitParameters.addValue("y/um", Double.parseDouble(results.getColumnHeading(i)));
			double y []=results.getColumnAsDoubles(i);
			
			CurveFitter cf=new CurveFitter(x, y);
			cf.doFit(function);
			double [] parameters=cf.getParams();
			int numParam=parameters.length;
			
			
			double step=(this.xmax-x[0])/(10*x.length);
			double max=0;
			double xmax=x[0];
			for (double iter=x[0];iter<this.xmax;iter+=step) {
				double val=cf.f(iter);
				if (val>max) {
					max=val;
					xmax=iter;
				}
			}
			
			
			
			
			
			
			
			
			for (int n=0;n<numParam;n++) {
				fitParameters.addValue(""+n, parameters[n]);
			}
			
			
			fitParameters.addValue("xmax", xmax);
			
			
			
			
			if (showFit) cf.getPlot().show();
			
		}
		
		//fitParameters.show("Fit Parameters");
	}
	private void checkResults() {
		double [] x1=fitParameters.getColumnAsDoubles(5);
				
		ArrayStatistics as=new ArrayStatistics(x1);
		double x1max=as.getMax();
		if (x1max>xmax) {
			fitParameters.deleteColumn("x1");
		} else {
			fitParameters.deleteColumn("x2");
		}
	}
	ResultsTable getFitResults() {
		return fitParameters;
	}
}
