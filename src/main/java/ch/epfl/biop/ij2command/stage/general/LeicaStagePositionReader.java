package ch.epfl.biop.ij2command.stage.general;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

public class LeicaStagePositionReader extends StagePositionReader{

  private String filename; 
  private final String xPos="StageXPos";
  private final String yPos="StageYPos";
  private final String zPos="StageZPos";
  
  public LeicaStagePositionReader(String name){
	  
	  this.filename=name;
	  super.xPos=this.xPos;
	  super.yPos=this.yPos;
	  super.zPos=this.zPos;
  }
  public ArrayList<Double> getList(String tag) {
	  ArrayList <Double> pList=new ArrayList<Double>();
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
	  return pList;
  }
  public static ImagePlus openImage(String path) {
	  
	  IJ.run("Bio-Formats", "open="+path+" color_mode=Default concatenate_series open_all_series rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT");
	  ImagePlus imp=WindowManager.getCurrentImage().duplicate();
	  WindowManager.getCurrentWindow().close();
	  return imp;
  }


}
