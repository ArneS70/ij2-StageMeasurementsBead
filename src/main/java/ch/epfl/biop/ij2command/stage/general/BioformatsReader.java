package ch.epfl.biop.ij2command.stage.general;

import java.io.IOException;

import ij.ImagePlus;
import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;

public class BioformatsReader{
	String filepath;
	
	public BioformatsReader(String path) {
		this.filepath=path;
	}
	public ImagePlus [] open() throws FormatException, IOException{
		
		ImporterOptions options = new ImporterOptions();
		options.setId(filepath);
		options.setAutoscale(true);
		options.setCrop(true);
		options.setVirtual(true);
		options.setColorMode(ImporterOptions.COLOR_MODE_COMPOSITE);
		options.setSplitTimepoints(true);
		
		
		ImagePlus[] imps = BF.openImagePlus(options);
		
		return imps;
	}
}