package ch.epfl.biop.ij2command.stage.general;

import ij.measure.CurveFitter;

public class FunctionA extends SuperFunction{
	
	FunctionA(){
		super("FunctionA");
		super.method=CurveFitter.POLY2;
	}
	

}
