package ch.epfl.biop.ij2command;

import ij.gui.Line;
import ij.measure.CurveFitter;
import ij.process.ImageProcessor;

public class SuperGaussFitter {
	private double [] line;
	private double [] parameters;
	private double diameter;
	
	private ImageProcessor line_ip;
	public final static String [] header= {"y0","height","center","sigma","super","R^2","Diameter"}; 
	
	SuperGaussFitter(ImageProcessor ip,Line l){
		this.line_ip=ip;
		this.line=ip.getLine(l.x1d, l.y1d, l.x2d, l.y2d);
		fitLine();
	}
	private void setLine(Line l) {
		this.line=line_ip.getLine(l.x1d, l.y1d, l.x2d, l.y2d);
		fitLine();
	}
	private void fitLine() {
		double []x=xValues(line);
		String fitFunction="y=a+b*exp(-1*pow(abs(x-c),e)/(2*pow(d,e)))";
		ArrayStatistics as=new ArrayStatistics(line);
		double [] intParam= {
				as.getMean(),
				as.getMax()-as.getMin(),
				new ArrayStatistics(x).getMean(),
				2,2};
		
		CurveFitter cf=new CurveFitter(xValues(line), line);
		cf.doCustomFit(fitFunction, intParam, false);
		this.parameters=cf.getParams();
		this.parameters[5]=cf.getRSquared();
		this.diameter=calcDiameter(0.5);
		
		cf.getPlot().show();
		
		
	}
	private double [] xValues(double [] yValues) {
		double [] x=new double[yValues.length];
		for (int i=1;i<yValues.length;i++) {
			x[i]=i;
		}
		
		
		return x;
		
	}
	public double [] getResults() {
		return parameters;
	}
	
	public double getDiameter() {
		return this.diameter;
	}
	public double calcDiameter(double height) {
		double x1=-this.parameters[3]*Math.pow(2*Math.log(1/height),1/this.parameters[4])+this.parameters[2];
		double x2=this.parameters[3]*Math.pow(2*Math.log(1/height),1/this.parameters[4])+this.parameters[2];
		return x2-x1;
	}
}

