package ch.epfl.biop.ij2command.USAF;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>USAF Focus Linear Range")

public class CheckLinearRange implements Command{

	@Override
	public void run() {
		final ResultsTable fit=new ResultsTable();
		fit.setPrecision(6);
		final ResultsTable table=ResultsTable.getActiveTable();
		
		if (table==null) return;
		final double []x=table.getColumnAsDoubles(0);
		final double []y=table.getColumnAsDoubles(8);
		
		int len=x.length;
		for (int n=0;n<len/2;n++) {
			for(int m=len;m>len/2;m--) {
				IJ.log(""+n+"   "+m);
				double [] xfit=new double [len-n];
				double [] yfit=new double [len-n];
				for (int i=0;i<len-n;i++) {
					xfit[i]=x[i];
					yfit[i]=y[i];
				}
				CurveFitter cf=new CurveFitter(xfit,yfit);
				cf.doFit(CurveFitter.POLY2);
				double []param=cf.getParams();
				fit.addRow();
				fit.addValue("n", n);
				fit.addValue("m", m);
				fit.addValue("length", m-n);
				fit.addValue("a", param[0]);
				fit.addValue("b", param[1]);
				fit.addValue("c", param[2]);
				fit.addValue("SquareSummResiduals", param[3]);
				fit.addValue("R^2", cf.getRSquared());
			}
		}
		fit.sort("R^2");
		fit.show("Fit Table"); 
		
		
		
	}

}
