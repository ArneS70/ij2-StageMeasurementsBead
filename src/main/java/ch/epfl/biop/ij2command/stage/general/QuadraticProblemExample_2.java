package ch.epfl.biop.ij2command.stage.general;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

/**
 * This is the example of LevenbergMaaquardt Optimization by using Apache Commons Math (3.4 API)
 * Codes are modified from the eample of Quardratic Problem Example of 
 * the user guide of Apache Commons Math -12 Optimization-
 * http://commons.apache.org/proper/commons-math/userguide/optimization.html
 * @author Yoshiyuki Arai
 * @data 05/01/2015
 * 
 */

public class QuadraticProblemExample_2 {
	
	public static void main(String[] args) {
		// construct input data storage as ArrayList type
		List<Double> x = new ArrayList<Double>();
		List<Double> y = new ArrayList<Double>();
        
        // construct QuadraticFunction
        QuadraticFunction qf = new QuadraticFunction(x,y);
        
        //entry the data
		qf.addPoint(1, 34.234064369);
		qf.addPoint(2, 68.2681162306);
		qf.addPoint(3, 118.6158990846);
		qf.addPoint(4, 184.1381972386);
		qf.addPoint(5, 266.5998779163);
		qf.addPoint(6, 364.1477352516);
		qf.addPoint(7, 478.0192260919);
		qf.addPoint(8, 608.1409492707);
		qf.addPoint(9, 754.5988686671);
		qf.addPoint(10, 916.1288180859);

		//prepare construction of LeastSquresProblem by builder
		LeastSquaresBuilder lsb = new LeastSquaresBuilder();

		//set model function and its jacobian
		lsb.model(qf.retMVF(), qf.retMMF());
		double[] newTarget = qf.calculateTarget();
		
		//set target data
		lsb.target(newTarget);
		double[] newStart = {1,1,1};
		//set initial parameters
		lsb.start(newStart);
		//set upper limit of evaluation time
		lsb.maxEvaluations(100);
		//set upper limit of iteration time
		lsb.maxIterations(1000);

		//construct LevenbergMarquardtOptimizer 
		LevenbergMarquardtOptimizer lmo = new LevenbergMarquardtOptimizer();
		try{
			//do LevenbergMarquardt optimization
			LeastSquaresOptimizer.Optimum lsoo = lmo.optimize(lsb.build());
			
			//get optimized parameters
			final double[] optimalValues = lsoo.getPoint().toArray();			
			//output data
			System.out.println("A: " + optimalValues[0]);
			System.out.println("B: " + optimalValues[1]);
			System.out.println("C: " + optimalValues[2]);
			System.out.println("Iteration number: "+lsoo.getIterations());
			System.out.println("Evaluation number: "+lsoo.getEvaluations());
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
}
