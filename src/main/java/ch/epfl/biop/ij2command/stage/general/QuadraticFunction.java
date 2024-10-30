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
public class QuadraticFunction {
	
	// Member variables
	List<Double> x;
	List<Double> y;
	
	/**
	 * Constructor of QuadraticFunction
	 * @param x	input data
	 * @param y	target data
	 */
	public QuadraticFunction(List<Double> x, List<Double> y) {
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
		            values[i] = (variables[0] * x.get(i) + variables[1]) * x.get(i) + variables[2];
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
		        double[][] jacobian = new double[x.size()][3];
		        for (int i = 0; i < jacobian.length; ++i) {
		            jacobian[i][0] = x.get(i) * x.get(i);
		            jacobian[i][1] = x.get(i);
		            jacobian[i][2] = 1.0;
		        }
		        return jacobian;
		    }
			
		};
    }
}