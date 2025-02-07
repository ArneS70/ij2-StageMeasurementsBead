package ch.epfl.biop.ij2command.stage.general;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;

public class ZeissStagePositionReader extends StagePositionReader{

  private String filename; 
  private final String xPos="X";
  private final String yPos="Y";
  private final String zPos="Z";
  
  public ZeissStagePositionReader(String name){
	  
	  this.filename=name;
	  super.xPos=this.xPos;
	  super.yPos=this.yPos;
	  super.zPos=this.zPos;
  }
  public ArrayList<Double> getList(String tag)  {
	 
	ArrayList <Double> pList=new ArrayList<Double>();

     
	try {
		
		BufferedReader reader;
		boolean read=false;
		boolean positions=false;
		reader = new BufferedReader(new FileReader(filename));
		String currentLine;
		do {
			currentLine=reader.readLine();
			if (currentLine!=null) {
				if (currentLine.contains("NumberPositions")) positions=true;
				if (currentLine.contains("BEGIN")) read=true;
				if (currentLine.contains("END")) read=false;
			
				if (positions && read && currentLine.contains(tag)) {
					double pos=getPos(currentLine,tag);
					pList.add(getPos(currentLine,tag));
				}
			}
		
		}while (currentLine!=null);
		reader.close();
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		
	return pList;
  }
  double getPos(String line,String axis) {
	  
	  int len=line.length();
	  int start=line.indexOf(axis)+4;
	  int stop=line.indexOf(".")+4;
	  String number=line.substring(start, stop);
	  //IJ.log(""+number);
	  return Double.valueOf(number);
	  
  }
		  
		  


	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
/*	  
	  // Instantiate the Factory
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  try {

          // optional, but recommended
          // process XML securely, avoid attacks like XML External Entities (XXE)
          dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

          // parse XML file
          DocumentBuilder db = dbf.newDocumentBuilder();

          Document doc = db.parse(new File(filename));

          // optional, but recommended
          // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
          doc.getDocumentElement().normalize();
//          Element e=doc.getElementById(tag);
          NodeList list=doc.getElementsByTagName("XYZStagePointDefinition");
          int len=list.getLength();
          
          for (int i=0;i<len;i++) {
        	  Node n=list.item(i);
        	  NamedNodeMap map=n.getAttributes();
        	  Node nn=map.getNamedItem(tag);
        	  IJ.log(i+"  "+nn.getTextContent());
        	  pList.add(Double.valueOf(nn.getTextContent())*1000);
          }
          
//         
              
          

      } catch (ParserConfigurationException | SAXException | IOException e) {
          e.printStackTrace();
      }
*/
	 
  
  public static ImagePlus openImage(String path) {
	  
	  IJ.run("Bio-Formats", "open="+path+" color_mode=Default concatenate_series open_all_series rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT");
	  ImagePlus imp=WindowManager.getCurrentImage().duplicate();
	  WindowManager.getCurrentWindow().close();
	  return imp;
  }


}
