import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author      Samuel Balco <sb592@le.ac.uk>
 * @version     1.2                 (current version number of program)
 * @since       2014-03-21          (the version of the package this class was first added to)
 */
public class SAXParser extends DefaultHandler {
	Map<Long, MapNode> nodeidx = new HashMap<Long, MapNode>(); // Node Hash
	Map<Long, MapWay> mapWayMap = new HashMap<Long, MapWay>();
    private List<MapWay> mapWayList = new ArrayList<MapWay>();
	
    private MapNode cNode = null;
    private MapWay cWay = null;
	boolean addWay = false;
    
    
    public void load(InputStream stream) throws Exception {
		try {
			// Create a builder factory
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            // Create the builder and parse the file
            factory.newSAXParser().parse(stream, this);
        } catch(SAXException e) {
			throw new Exception(e);
        } catch(IOException e) {
			throw new Exception(e);
        } catch(ParserConfigurationException e) {
			throw new Exception(e);
        }
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if ("node".equals(qName)) {
			long id = Long.parseLong(attributes.getValue("id"));
            cNode = new MapNode(
					id,
					Double.parseDouble(attributes.getValue("lat")),
					Double.parseDouble(attributes.getValue("lon")));
			nodeidx.put(id, cNode);
		}
        // Parsing Way
        else if ("way".equals(qName)) {
			cWay = new MapWay();
			cWay.id = Long.parseLong(attributes.getValue("id"));
        }
        // Parsing WayNode
        else if ("nd".equals(qName)) {
			long ref = Long.parseLong(attributes.getValue("ref"));
            if (ref != 0) {
				MapNode n = nodeidx.get(ref);
                if (n == null) return;
                // Insert WayNode
                cWay.nodes.add(n);
                cNode = null;
            }
        }
        // Parsing Tags
        else if ("tag".equals(qName)) {
			if (cNode == null && cWay == null) return;
            String k, v;

            k = attributes.getValue("k").intern();
            v = attributes.getValue("v").intern();
			//only store a way if it is accessible by a car
            if ("highway".equals(k)) {
				if (!"footway".equals(v)
					&& !"path".equals(v)
					&& !"service".equals(v)
					&& !"pedestrian".equals(v)
					&& !"steps".equals(v)
					&& !"cycleway".equals(v)
					&& !"bridleway".equals(v)
					&& !"track".equals(v)
					) addWay = true;
            }
            if ("oneway".equals(k)) {
				if ("yes".equals(v) && cWay != null) cWay.oneWay = true;
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
		if ("node".equals(qName)) {
			cNode = null;
		} else if ("way".equals(qName)) {
			//System.out.println("end way: "+cWay.id);
	        if(cWay != null && addWay) {
				mapWayList.add(cWay);
				mapWayMap.put(cWay.id, cWay);
				//System.out.println(cWay);
	        }
			addWay = false;
	        cWay = null;
		}
    }
    
    public Map<Long, MapNode> getNodes(){
    	return nodeidx;
    }

    public List<MapWay> getWays(){
    	return mapWayList;
    }
}

class MapNode implements Comparable<MapNode> {
	long id;
  	double lat, lon;
  	public MapNode(long id, double lat, double lon) {
		this.id = id;
	  	this.lat = lat;
	  	this.lon = lon;
  	}
	@Override
  	public String toString() {
		return lat+","+lon;
  	}
	
    public long getId() {
      return id;
    }

  
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      MapNode other = (MapNode) obj;
      return id == other.id;
    }
	
    @Override
    public int compareTo(MapNode n){
    	return (int)((this.lat - n.lat)*100000);
    }
}

class MapWay {
  	long id;
  	List<MapNode> nodes = new ArrayList<MapNode>();
  	boolean oneWay = false;
  
  	@Override
  	public String toString() {
	  	String ret = id+"'s nodes:\n";
	  	for (MapNode m : nodes){
		  	ret += m + "\n";
	  	}
    	return ret;
  	}
}