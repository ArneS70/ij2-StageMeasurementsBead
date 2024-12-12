package ch.epfl.biop.ij2command.USAF;

import java.util.Vector;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.measure.ResultsTable;

public class HorizontalLineExtractor extends HorizontalLineAnalysis{

	Vector <String> header=new Vector<String>();
	Vector <int []>stackOrder=new Vector<int[]>();
	Line referenceLine;
	double space;
	
	HorizontalLineExtractor(HorizontalAnalysis analysis){
		super(analysis);
		
	}
	void run() {
		ImagePlus imp=analysis.getImage();
		checkParameters();
		
		this.setHorizontalLine(analysis.getstackCenter());
		referenceLine=analysis.getHorizontalLine();
		Line line=referenceLine;
		this.profile=imp.getProcessor().getLine((double)line.x1,(double)line.y1,(double)line.x2,(double)line.y2);
		
		int nSlices=imp.getNSlices();
//		int nFrames=imp.getNFrames();
		int startT=analysis.getStartT();
		int stopT=analysis.getStopT();
		int stepT=analysis.getStepT();
		int startZ=analysis.getStartZ();
		int stopZ=analysis.getStopZ();
		int stepZ=analysis.getStepZ();
		int count=0;
		
		for (int t=startT;t<=stopT;t+=stepT) {
			count++;
			int deltaT=t-startT;
			for (int z=startZ;z<stopZ;z+=stepZ) {
				
				int n=deltaT*nSlices+z;
				imp.setSliceWithoutUpdate(n);
				setHorizontalLine(n);
				this.horizontalLines.add(line);          //to be removed
				line=analysis.getHorizontalLine();
//				IJ.log(line.x1+" "+line.x2+"  "+line.y1+"   "+line.y2);
				this.profile=imp.getProcessor().getLine((double)line.x1,(double)line.y1,(double)line.x2,(double)line.y2);

				getProfile();
				header.add("t="+t+"/z="+z);
				stackOrder.add(new int [] {t,z});
				
				
			}
		}
	}
	void getProfile(){
		if (lineProfiles.size()==0) {
			xvalues=new double [profile.length];
			for (int n=0;n<profile.length;n++) {
				xvalues[n]=n*cal.pixelWidth;
			}
			header.add("x / um");
			lineProfiles.add(xvalues);
			lineProfiles.addElement(profile);
			
			
		} else {	
			
		lineProfiles.addElement(profile);
		};
	}
	void setHorizontalLine(int position) {
		
				HorizontalLine hl=new HorizontalLine(getIP(position));
				Line line=hl.findHorizontalLine();
				
//				Line line=hl.findHorizontalLine(20);
				analysis.setHorizontalLine(line);
//				line=hl.optimizeHorizontalMaxima(line);
				line=hl.optimizeHorizontalMaxima(line,0);
				line=hl.checkHorizontalLine(line);
				analysis.setHorizontalLine(line);
				this.horizontalLines.add(line);
				analysis.getImage().setRoi(analysis.getHorizontalLine());

		
	}
	
	ResultsTable getProfileTable() {
		ResultsTable table= new ResultsTable();
		int length=super.lineProfiles.size();
		for(int n=0; n<length;n++) {
			table.setValues(header.elementAt(n), super.lineProfiles.elementAt(n));
		}
		return table;
	}
}
