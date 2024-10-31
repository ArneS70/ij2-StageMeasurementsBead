package ch.epfl.biop.ij2command.stage.general;

import java.util.List;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

/**
 * This is a class for Quadratic Function.
 * Codes are derived from the user guide of Apache Commons Math -12 Optimization-
 * @author arai yoshiyuki
 * @data 05/01/2015
 * @version 1.0
 *
 */
public class AsymGaussFunction {
	
	// Member variables
	List<Double> x;
	List<Double> y;
	
	/**
	 * Constructor of QuadraticFunction
	 * @param x	input data
	 * @param y	target data
	 */
	public AsymGaussFunction(List<Double> x, List<Double> y) {
		this.x=x;
		this.y=y;
	}

	/**
	 * set raw data
	 * @param xin	raw input data
	 * @param yin	raw target data
	 */
    public void addPoint(double xin, double yin) {
        this.x.add(xin);
        this.y.add(yin);
    }

    /**
     * return target data as double array by target data
     * @return target	double arrya of target data
     */
    public double[] calculateTarget() {
        double[] target = new double[y.size()];
        for (int i = 0; i < y.size(); i++) {
            target[i] = y.get(i).doubleValue();
        }
        return target;
    }
    
    /**
     * Define model function and return values
     * @return	return the values of model function by input data
     */
    public MultivariateVectorFunction retMVF() {
		return new MultivariateVectorFunction() {
			@Override
			public double[] value(double[] variables)
					throws IllegalArgumentException {
		        double[] values = new double[x.size()];
		        for (int i = 0; i < values.length; ++i) {
		        	
		        	double term1=Math.pow(Math.E,-1*(x.get(i)-variables[2]+variables[3])/variables[4]);
		        	double term2=Math.pow(Math.E,-1*(x.get(i)-variables[2]-variables[3])/variables[5]);
		        	
		        	values[i] = variables[0]+variables[1]*(1/(1+term1))*(1-(1/(1+term2)));
//		            values[i] = (variables[0] * x.get(i) + variables[1]) * x.get(i) + variables[2];
		        }
		        return values;
		    }			
		};
    	
    }
    
    /**
     * Return the jacobian of the model function
     * @return	return the jacobian
     */
    public MultivariateMatrixFunction retMMF() {
    	return new MultivariateMatrixFunction() {

			@Override
			public double[][] value(double[] point)
					throws IllegalArgumentException {
				// TODO Auto-generated method stub
                return jacobian(point);
			}

			/**
			 * calculate and retjacobian
			 * @param	variables	parameters of model function
			 * @return	jacobian	jacobian of the model function
			 */
		    private double[][] jacobian(double[] variables) {
		        double[][] jacobian = new double[x.size()][6];
		        for (int i = 0; i < jacobian.length; ++i) {
		            
		        	double term1=Math.pow(Math.E,-1*(x.get(i)-variables[2]+variables[3])/variables[4]);
		        	double term2=Math.pow(Math.E,-1*(x.get(i)-variables[2]-variables[3])/variables[5]);
		        	double nom,denom;
		        	
		        	jacobian[i][0] = 1.0;
		            	nom=1-(1/(term2+1));
		            	denom=term1+1;
		        	jacobian[i][1] = nom/denom;
		        		nom=variables[1]*((variables[4]-variables[5])*term2+variables[4]*term1*term2-variables[5])*term1;
		        		denom=variables[4]*variables[5]*Math.pow(term2+1, 2)*Math.pow(term1+1, 2);
		            jacobian[i][2] = nom/denom;
		            	nom=variables[1]*term1*(variables[4]*term1*term2+(variables[5]+variables[4])*term2+variables[5]);
		            	denom=variables[5]*variables[4]*Math.pow(term2+1, 2)*Math.pow(term1+1, 2);
		            jacobian[i][3] =nom/denom;
		            	nom=variables[1]*(-variables[2]-x.get(i)-variables[3])*term1;
		            	denom=(term2+1)*variables[4]*variables[4]*(term1+1)*(term1+1);
		            jacobian[i][4] =nom/denom;
		            	nom=variables[1]*(-variables[2]+x.get(i)-variables[3])*term2;
		            	denom=(term1+1)*variables[5]*variables[5]*(term2+1)*(term2+1);
		            jacobian[i][5] =nom/denom;
		        }
		        return jacobian;
		    }
			
		};
    }
}
