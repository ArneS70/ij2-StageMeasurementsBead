package ch.epfl.biop.ij2command.stage.general;

import ij.measure.CurveFitter;

public class FunctionB extends SuperFunction{
	
	FunctionB(){
		super("FunctionB");
		super.method=CurveFitter.POLY3;
	}

}