package ch.epfl.biop.ij2command.USAF;
import ch.epfl.biop.ij2command.stage.general.ArrayStatistics;
import ch.epfl.biop.ij2command.stage.general.GlobalFitter;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;

public class TableFitter {
	private ResultsTable inputTable,fitParameters;
	private boolean showFit=false;
	private double xmax;
	
	
	TableFitter(ResultsTable toFit){
		this.inputTable=toFit;
	}
	void fitTable() {
		int columns=inputTable.getLastColumn();
		double [] x=inputTable.getColumnAsDoubles(0);
		this.xmax=x[x.length-1];
		
		fitParameters=new ResultsTable();
		for (int i=1;i<columns;i++) {
			fitParameters.addRow();
			fitParameters.addValue("y/um", Double.parseDouble(inputTable.getColumnHeading(i)));
			double y []=inputTable.getColumnAsDoubles(i);
			
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
		
		ImageStack plots=new ImageStack(696,415);
		int columns=inputTable.getLastColumn();
		double [] x=inputTable.getColumnAsDoubles(0);
		this.xmax=x[x.length-1];
		
		fitParameters=new ResultsTable();
		for (int i=1;i<columns;i++) {
			fitParameters.addRow();
			fitParameters.addValue("y/um", Double.parseDouble(inputTable.getColumnHeading(i)));
			double y []=inputTable.getColumnAsDoubles(i);
			
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
			if (showFit) plots.addSlice(cf.getPlot().getImagePlus().getProcessor());
		}
//		fitParameters.show("Fit Parameters");
		if (showFit) new ImagePlus ("Fit Plots",plots).show();
	}
	void GlobalTableFit(int function) {
		
		GlobalFitter gf=new GlobalFitter(this.inputTable, CurveFitter.POLY4);
		gf.initialFit();
		int pos=gf.selectBestFit();
		gf.runBestFit();
	}
		
/*		function=CurveFitter.POLY4;
		int columns=results.getLastColumn();
		double [] x=results.getColumnAsDoubles(0);
		this.xmax=x[x.length-1];
		
		fitParameters=new ResultsTable();
		ResultsTable residual=new ResultsTable();
//		ResultsTable[] fitParamArray=new ResultsTable[columns];
		
		double resMin=0;
		int resMinPos=0;
		
		for (int i=1;i<=columns;i++) {
			double resTotal=0;
//			fitParameters.addRow();
//			fitParameters.addValue("y/um", Double.parseDouble(results.getColumnHeading(i)));
			double y []=results.getColumnAsDoubles(i);
			
			CurveFitter cf=new CurveFitter(x, y);
			cf.doFit(function);
//			cf.getPlot().show();
			double []parameters=cf.getParams();
			int numParam=parameters.length;
			
			
			
			fitParameters.addRow();
			for (int n=0;n<numParam;n++) {
				
				fitParameters.addValue(""+n, parameters[n]);
				
			}
			String formula=createFormula(parameters);
//			formula=formula.concat(IJ.d2s(parameters[0]));
//			if (parameters[1]>0)formula=formula.concat("+");  
//			formula=formula.concat(IJ.d2s(parameters[1]));
//			formula=formula.concat("*(x-a)");
//			if (parameters[2]>0)formula=formula.concat("+");
//			formula=formula.concat(IJ.d2s(parameters[2]));
//			formula=formula.concat("*pow(x-a,2)");
//			if (parameters[3]>0)formula=formula.concat("+");
//			formula=formula.concat(Double.toString(parameters[3]));
//			formula=formula.concat("*pow(x-a,3)");
//			if (parameters[4]>0)formula=formula.concat("+");
//			formula=formula.concat(Double.toString(parameters[4]));
//			formula=formula.concat("*pow(x-a,4)");
//			if (parameters[5]>0)formula=formula.concat("+");
//			formula=formula.concat(Double.toString(parameters[5]));
//			formula=formula.concat("*pow(x-a,5)");
			
			
			residual.addRow();
			
			for (int j=1;j<=columns;j++) {
				
//				fitParameters.addRow();
//				fitParameters.addValue("y/um", Double.parseDouble(results.getColumnHeading(j)));
				double y1 []=results.getColumnAsDoubles(j);
				
				
				cf=new CurveFitter(x, y1);
				cf.doCustomFit(formula, new double []{0}, false);
				double []res=cf.getParams();
				resTotal+=res[1];
			
				
//				cf.getPlot().show();
			}
			
			residual.addValue("i",i);
			residual.addValue("residual",resTotal);
			if (i==1) {resMin=resTotal;resMinPos=1;} else {
				if (resTotal<resMin) {resMin=resTotal;resMinPos=i;}
			}
			residual.show("Residual");
			fitParameters.addValue("xmax", xmax);
			if (showFit) cf.getPlot().show();
			
		}
		IJ.log(resMinPos+"   "+resMin);
		int numParam=fitParameters.getLastColumn();
		double [] finalParam= new double[numParam];
		fitParameters.show("Test");
		for (int n=0;n<numParam;n++) {
			finalParam[n]=fitParameters.getValue(""+n, resMinPos);
		}
		
		double step=(this.xmax-x[0])/(10*x.length);
		double max=0;
		double xmax=x[0];
//		for (double iter=-0.5*xmax;iter<1.5*this.xmax;iter+=step) {
//			double val=cf.f(iter);
//			if (val>max) {
//				max=val;
//				xmax=iter;
//			}
//		}
		//fitParameters.show("Fit Parameters");
	}
*/	
	String createFormula(double []parameters) {
		String formula="y="+parameters[0];
		int poly=parameters.length-1;
		
		for (int n=1;n<poly;n++) {
		
		if (parameters[n]>0)formula=formula.concat("+");  
		formula=formula.concat(IJ.d2s(parameters[n]));
		formula=formula.concat("*pow(x-a,"+n+")");
		}
		
		return formula;
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
	void showFit() {
		this.showFit=true;
	}
}
