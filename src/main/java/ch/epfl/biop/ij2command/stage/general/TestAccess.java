package ch.epfl.biop.ij2command.stage.general;

import java.lang.reflect.Method;

public class TestAccess {   
    public static void main(String[] args) {
               
            
        SuperFunction fit=new FunctionB();	
    	System.out.println("Name: "+fit.getName());
    	double []p=fit.doFit(new double[]{1,2,3,4,5},new double[]{1,4,9,16,25});
    	System.out.println("p1: "+p[0]);
    	System.out.println("p2: "+p[1]);
            
            
            
         }     
    }

