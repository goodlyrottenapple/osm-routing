import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * @author      Samuel Balco <sb592@le.ac.uk>
 * @version     1.7                 (current version number of program)
 * @since       2014-03-31          (the version of the package this class was first added to)
 */
public class Grapher {
	/**
	 * Creates a list of nodes that appear in more than one MapWay, i.e. they are the interestions of ways
	 */
	public static Map<Long, MapNode> reduce(List<MapWay> ways, Map<Long, MapNode> nodes) {
		//find common nodes ie. intersections
		Map<Long, Integer> mapOfCommonNodes = new HashMap<Long, Integer>();
		for (MapWay way : ways) {
			for (MapNode n : way.nodes) {
				if (mapOfCommonNodes.containsKey(n.id)) mapOfCommonNodes.put(n.id, mapOfCommonNodes.get(n.id) + 1);
				else mapOfCommonNodes.put(n.id, 1);
			}
		}
		//create a map of interesctions
		Map<Long, MapNode> intersectionNodes = new HashMap<Long, MapNode>();
		for (Long id : mapOfCommonNodes.keySet()) {
			if (mapOfCommonNodes.get(id) > 1) intersectionNodes.put(id, nodes.get(id));
		}
		
		return intersectionNodes;
	}
	
	/**
	 * Reduces the MapWays into MapLinks by taking the interestions and the endpoints of ways and removing the intermediate points
	 * This method however ensures that the distances of the intermediate poitns are added up and stored inside MapLink
	 */
	public static List<MapLink> link(List<MapWay> ways, Map<Long, MapNode> reducedNodes) {
		List<MapLink> links = new ArrayList<MapLink>();
		for(MapWay way : ways){
			MapNode prev = way.nodes.get(0); // or first
			if (!reducedNodes.containsKey(prev.id)) reducedNodes.put(prev.id, prev);
			MapNode last = way.nodes.get(0);
			double sum = 0;
			for (int i = 1; i< way.nodes.size();i++) {
				MapNode n = way.nodes.get(i);
				sum += calculateDistance(last.lat, last.lon, n.lat, n.lon);
				if (reducedNodes.containsKey(n.id) || i == way.nodes.size()-1) {
					if (i == way.nodes.size()-1){
						MapNode lastN = way.nodes.get(way.nodes.size()-1);
						reducedNodes.put(lastN.id, lastN);
					}
					links.add( new MapLink(way.id, sum, prev, n, way.oneWay) );
					sum = 0;
					prev = n;
				}
				last = n;
			}
		}
		return links;
	}
	
	public final static double AVERAGE_RADIUS_OF_EARTH = 6371;
	/**
	 * A method for calculating the distances between two coordinate poitns in meters
	 */
	public static double calculateDistance(double userLat, double userLng, double venueLat, double venueLng) {
		double latDistance = Math.toRadians(userLat - venueLat);
	    double lngDistance = Math.toRadians(userLng - venueLng);

	    double a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)) +
	               (Math.cos(Math.toRadians(userLat))) *
	               (Math.cos(Math.toRadians(venueLat))) *
	               (Math.sin(lngDistance / 2)) *
	               (Math.sin(lngDistance / 2));

	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return AVERAGE_RADIUS_OF_EARTH * c;
	}
	
	/**
	 * This method will take coordinates of a poitn and find the closest corresponding Node in a graph
	 */
	public static MapNode closestNodeInGraph(MapNode current, List<MapNode> nodes) {
		MapNode closest = null;
		double distance = 0, coord = current.lat, DELTA = 0.1;
		
		
		//find indices of all nodes with lat between (current - DELTA) and (current + DELTA)
		int min = findMinNode(coord-DELTA, nodes), max = findMaxNode(coord+DELTA, nodes);
		//System.out.println(min);
		//System.out.println(max);
		
		for (int i = min; i <= max; ++i) {
			MapNode n = nodes.get(i);
			if (current.lon-DELTA <= n.lon && n.lon <= current.lon+DELTA){
				if (closest == null) {
					closest = n;
					distance = calculateDistance(current.lat, current.lon, n.lat, n.lon);
				}
				else {
					double tmp = calculateDistance(current.lat, current.lon, n.lat, n.lon);
					if (tmp < distance) {
						closest = n;
						distance = tmp;
					}
				}
			}
		}
		
		
	    //System.out.print("Elapsed time for node: ");				//test this
        //System.out.println(elapsedTime);
		return closest;
	}
	
	public static int findMinNode(double coodinate, List<MapNode> nodes){
		return binarySearchMin(coodinate, nodes, 0, nodes.size()-1);
	}
	
	public static int findMaxNode(double coodinate, List<MapNode> nodes){
		return binarySearchMax(coodinate, nodes, 0, nodes.size()-1);
	}
	
	
	private static int binarySearchMin(double key, List<MapNode> arr, int start, int end) {
        int middle = (end-start+1)/2 + start; //get index of the middle element of a particular array portion

        if (arr.get(middle).lat < key && arr.get(middle+1).lat >= key) {
            return middle;
        }

        if (key < arr.get(middle).lat && middle > 0) {
            return binarySearchMin(key, arr, start, middle-1); //recurse lower half
        }

        if (key > arr.get(middle).lat && middle < arr.size()-1) {
            return binarySearchMin(key, arr, middle+1, end); //recurse higher half
        }

        return 0; 
    }
		
		
	private static int binarySearchMax(double key, List<MapNode> arr, int start, int end) { 
		int middle = (end-start+1)/2 + start; //get index of the middle element of a particular array portion

        if (arr.get(middle).lat < key && middle < arr.size()-2 && arr.get(middle+1).lat >= key) {
            return middle + 1;
        }

        if (key < arr.get(middle).lat && middle > 0) {
            return binarySearchMax(key, arr, start, middle-1); //recurse lower half
        }

        if (key > arr.get(middle).lat && middle < arr.size()-1) {
            return binarySearchMax(key, arr, middle+1, end); //recurse higher half
        }

        return arr.size()-1; 
	}
	
	public static MapGraph createGraph( Map<Long, MapNode> nodes, List<MapLink> links) {
		// constructs a directed graph with the specified vertices and edges
		MapGraph directedGraph = new MapGraph();
		for (MapNode n : nodes.values()) {
			directedGraph.addVertex(n);
		}
		
		
		for (MapLink link : links) {
			//if(link.n1.id != link.n2.id) { //hack!
				MapEdge e1 = new MapEdge(link.id, directedGraph.getVerticesMap().get(link.n1.id), directedGraph.getVerticesMap().get(link.n2.id), link.distance);
				directedGraph.addEdge(e1);
				if(!link.oneWay) {
					MapEdge e2 = new MapEdge(link.id, directedGraph.getVerticesMap().get(link.n2.id), directedGraph.getVerticesMap().get(link.n1.id), link.distance);
					directedGraph.addEdge(e2);
				}
		}
		//}
		return directedGraph;
	}
	
	public static List<MapNode> getNodesBetween(MapEdge edge, MapWay way) {
		int b_i = way.nodes.indexOf(edge.getSource().node), e_i = way.nodes.indexOf(edge.getDestination().node);
		List<MapNode> ret = new ArrayList<MapNode>();
		if(b_i+1 < e_i){
			//System.out.println("not in reverse");
			for (int i = b_i+1; i < e_i ; ++i) {
				ret.add(way.nodes.get(i));
			}
			//System.out.println(ret);
		}
		else{
			//System.out.println("in reverse, e:" + e_i + " b: " + b_i);
			for (int i = b_i-1; i > e_i ; --i) {
				ret.add(way.nodes.get(i));
			}
			//System.out.println(ret);
		}
		return ret;
	}
}

class MapLink {
	long id;
	double distance;
  	MapNode n1, n2;
 	boolean oneWay;
  
  	public MapLink(long id, double d, MapNode n1, MapNode n2, boolean oneWay) {
		this.id = id;
		this.distance = d;
	  	this.n1 = n1;
	  	this.n2 = n2;
	  	this.oneWay = oneWay;
	}
}