package ch.epfl.biop.ij2command.USAF;
import java.io.IOException;
import loci.formats.FormatException;
import loci.formats.ImageReader;


public class SimpleImageReader {
	private String fileName;
	private int channels;
	private int frames;
	
	SimpleImageReader(String toOpen){
		this.fileName=toOpen;
		try {
			openImage();
		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void openImage() throws FormatException, IOException {
		try (ImageReader reader = new ImageReader()) {
//			reader.openBytes(0);
			this.channels=reader.getSizeC();
			this.frames=reader.getSizeT();
		}
	}
	int getChannels() {
		return channels;
	}
	int getFrames() {
		return frames;
	}
}
