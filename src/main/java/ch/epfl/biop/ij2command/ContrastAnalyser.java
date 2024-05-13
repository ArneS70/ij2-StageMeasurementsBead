package ch.epfl.biop.ij2command;

import java.util.Arrays;

import org.scijava.plugin.Parameter;

import ij.ImagePlus;
import ij.gui.Plot;
import ij.plugin.filter.MaximumFinder;

public class ContrastAnalyser {

	int [] points;
	
	ContrastAnalyser(double []line){
		points=MaximumFinder.findMaxima(line, 200, false);
		Arrays.sort(points);
	}
	void addMaxima() {
		
	}
	@SuppressWarnings("deprecation")
	void showPlot(double [] line) {
		Plot p=new Plot("Line Profile", "x", "Intensity");
		p.add("Line", line);
		p.show();
		int num=points.length;
		for (int n=0;n<num;n++) {
			p.addLabel(points[n], 100, "X");
			p.show();
		}
		
		
	}
	Plot returnPlot(double [] line) {
		Plot p=new Plot("Line Profile", "x", "Intensity");
		p.add("Line", line);
		return p;
	}
}
