package ch.epfl.biop.ij2command.stage.general;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.formats.MetadataTools;
import loci.formats.meta.IMetadata;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.units.unit.Unit;

public class BioformatsReader{
	String filepath;
	
	public BioformatsReader(String path) {
		this.filepath=path;
	}
/*	public ImagePlus [] open() throws FormatException, IOException{
		
		ImagePlus[] imps=null;
		try {
		ImporterOptions options = new ImporterOptions();
				
		options.setId(filepath);
		options.setAutoscale(true);
		options.setCrop(true);
		options.setVirtual(true);
		options.setColorMode(ImporterOptions.COLOR_MODE_COMPOSITE);
		options.setSplitTimepoints(true);
		int l=BF.openImagePlus(filepath).length;
		l=BF.openThumbImagePlus(filepath).length;
		
		
		
		imps = BF.openImagePlus(options);
		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return imps;
	}
*/	
	public ImagePlus [] openAllSeries() {
		ImagePlus [] imps=null;
		try {
			ImporterOptions options = new ImporterOptions();
			options.setId(filepath);
			options.setOpenAllSeries(true);
			options.setVirtual(true);
			
			imps = BF.openImagePlus(options);
	        } catch (FormatException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

		return imps;
	}
}
		/*
		Vector series=new Vector<ImagePlus>();
		final ImageReader reader=new ImageReader();
		 final IMetadata omeMeta = MetadataTools.createOMEXMLMetadata();
		    reader.setMetadataStore(omeMeta);
		    
		try {
			reader.setId(filepath);
			int count=omeMeta.getImageCount();
			int numSeries=reader.getSeriesCount();
			IJ.log("interleaved: "+reader.isInterleaved());
			IJ.log("Littel Endian: "+reader.isLittleEndian());
			
			reader.setSeries(0);
			IJ.log("bits "+reader.getBitsPerPixel());
			reader.open
			int width = reader.getSizeX();
            int height = reader.getSizeY();
            int channels = reader.getSizeC();
            int slices= reader.getSizeZ();
            byte [] imgData=reader.openBytes(0);
            
            
            
         // Convert to ImagePlus
            byte [] singlePLane=Arrays.copyOfRange(imgData, 0, width*height);
            for (int i=0;i<width*height;i++) {
            	singlePLane[i]+=128;
            }
            ByteProcessor bp = new ByteProcessor(width, height, Arrays.copyOfRange(singlePLane, 0, width*height));
            
            ImagePlus imp = new ImagePlus("Series " + 0, bp);
            imp.show();
            
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            img.getRaster().setDataElements(0, 0, width, height, imgData);
            
			
			
		} catch (Exception e) {
            e.printStackTrace();
        }
		return series;
	}
*/
