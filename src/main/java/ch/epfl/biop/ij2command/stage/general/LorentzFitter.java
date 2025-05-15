package ch.epfl.biop.ij2command.stage.general;

import ij.measure.UserFunction;

public class LorentzFitter extends FitterFunction implements UserFunction {
	
	private double [] parameters;
	private double diameter;
	
	public final static String [] header= {"y0","height","center","sigma","super","R^2","Diameter"};
	String fitFunction="y=a+b*exp(-1*pow(abs(x-c),e)/(2*pow(d,e)))";
	
	public LorentzFitter(double [] xInput, double [] yInput) {
		super(xInput,yInput,FitterFunction.LORENTZ);
	}
	
	public synchronized double [] getFitResults() {
		cf.doCustomFit(this, 4, "", getInitParameters(),new double [] {0.5,0.5,0.5,0.5,0.5,0.5}, false);
		return cf.getParams();
	}

	public double getDiameter() {
		return this.diameter;
	}

	private double calcDiameter(double height) {
		double x1=-this.parameters[3]*Math.pow(2*Math.log(1/height),1/this.parameters[4])+this.parameters[2];
		double x2=this.parameters[3]*Math.pow(2*Math.log(1/height),1/this.parameters[4])+this.parameters[2];
		return x2-x1;
	}
	
		
	double [] getInitParameters(){
		double [] param=new double [4];
		ArrayStatistics statX=new ArrayStatistics(super.x);
		ArrayStatistics statY=new ArrayStatistics(super.y);
		param[0]=statY.getMin();
		param[1]=statY.getMax();
		param[2]=statX.getMean();
		param[3]=statX.getMax()/4;
		
		
		
		return param;
		
	}
	@Override
	public double userFunction(double[] p, double x) {
		double a1=4*Math.pow(x-p[3],2)+Math.pow(p[2], 2);
		return p[0]+(2*p[1]/Math.PI)*p[2]/a1;
				
	}
	
}
/* ********************Not needed anymore********************************	
	
private void fitLine() {
	double []x=xValues(line);
	
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
}

public double [] getResults() {
	fitLine();
	return parameters;
}
	


public SuperGaussFitter(ImageProcessor ip,Line l){
this.line_ip=ip;
this.line=ip.getLine(l.x1d, l.y1d, l.x2d, l.y2d);

}
*/