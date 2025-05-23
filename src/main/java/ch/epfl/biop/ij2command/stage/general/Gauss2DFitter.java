package ch.epfl.biop.ij2command.stage.general;

import ij.measure.UserFunction;

public class Gauss2DFitter extends FitterFunction implements UserFunction {
	
	private double [] parameters;
	private double diameter;
	private double boxwidth=0;
	
	public final static String [] header= {"y0","height","center","sigma","super","R^2","Diameter"};
	String fitFunction="y=a+b*exp(-1*pow(abs(x-c),e)/(2*pow(d,e)))";
	
	public Gauss2DFitter(double [] xInput, double [] yInput,int width) {
		super(xInput,yInput,FitterFunction.Gauss2D);
		this.boxwidth=width;
	}
	
	public synchronized double [] getFitResults() {
		cf.doCustomFit(this, 6, "", getInitParameters(),new double [] {0,0,0,0,0,0}, false);
		return cf.getParams();
	}
	
	public synchronized double [] getFitResults(double [] init) {
		cf.doCustomFit(this, 6, "", init,new double [] {0,0,0,0,0,0}, false);
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
	
	public void fixSuper(int fixSuper) {
		fitFunction.replace("e", ""+fixSuper);
	}
	
	public void unfixAll() {
		fitFunction="y=a+b*exp(-1*pow(abs(x-c),e)/(2*pow(d,e)))";
	}
	
	double [] getInitParameters(){
		double [] param=new double [6];
		ArrayStatistics statX=new ArrayStatistics(super.x);
		ArrayStatistics statY=new ArrayStatistics(super.y);
		param[0]=statY.getMin();
		param[1]=statY.getMax();
		param[2]=0.04;
		param[3]=20;
		param[4]=0.04;
		param[5]=20;
		
		
		return param;
		
	}
	@Override
	public double userFunction(double[] p, double x) {
		double a1=p[2]*Math.pow(x-Math.floor(x/boxwidth)*boxwidth-p[3], 2);
		double a2=p[4]*Math.pow(Math.floor(x/boxwidth)-p[5], 2);
		return p[0]+p[1]*Math.exp(-(a1+a2));
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