import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.PriorityQueue;
import java.util.Comparator;


public class DijkstraShortestPath {
	private MapGraph graph;
	Set<Container> settledNodes;
	//private Set<Container> unSettledNodes;
	PriorityQueue<Container> unSettledNodes;
	private Map<Container, Container> predecessors;
	private Map<Container, Double> distance;

	public Comparator<Container> distanceComparator = new Comparator<Container>() {
		@Override
		public int compare(Container c1, Container c2) {
			return (int) ((getShortestDistance(c1) - getShortestDistance(c2))*1000);
		}
	};

	public DijkstraShortestPath(MapGraph graph) {
		// create a copy of the array so that we can operate on this array
		this.graph = graph;
	}

	/**
	 * This method finds the route through the graph.
	 *
	 * @param source The origin node wrapped in the Container class object
	 * @param target The destination node wrapped in the Container class object
	 * Run this to find the route through the graph.
	 */
	public void execute(Container source, Container target) {
		settledNodes = new HashSet<Container>();
		//unSettledNodes = new HashSet<Container>();
		unSettledNodes = new PriorityQueue<Container>(1, distanceComparator);
		distance = new HashMap<Container, Double>();
		predecessors = new HashMap<Container, Container>();
		distance.put(source, 0.0);
		unSettledNodes.add(source);
		while (unSettledNodes.size() > 0) {
			//Container node = getMinimum(unSettledNodes);
			Container node = unSettledNodes.poll();
  
	  	  	if (node == target) return;
	  	  	settledNodes.add(node);
	  	  	unSettledNodes.remove(node);
  
	  	  	findMinimalDistances(node);
		}
	}
  
	/**
	 * Method for calculating the minimal distances and adding all the neigbors of the input node
	 */
	private void findMinimalDistances(Container node) {
		List<Container> adjacentNodes = getNeighbors(node);
		for (Container target : adjacentNodes) {
			if (getShortestDistance(target) > getShortestDistance(node) + getDistance(node, target)) {
				distance.put(target, getShortestDistance(node) + getDistance(node, target));
	    		predecessors.put(target, node);
	    		unSettledNodes.add(target);
	  	  	}
		}
	}
	
	/**
	 * Method that returns the distance between node and target
	 */
	private double getDistance(Container node, Container target) {
		if (node.con.containsKey(target)){
			return node.con.get(target).getWeight();
		}
		throw new RuntimeException("Should not happen");
	}

	/**
	 * Method that returns the neighbors for the input node
	 */
	private List<Container> getNeighbors(Container node) {
		List<Container> neighbors = new ArrayList<Container>();
		for (MapEdge edge : node.con.values()) {
			if (!isSettled(edge.getDestination())) {
				neighbors.add(edge.getDestination());
	  	  	}
		}
		return neighbors;
	}

	/**
	 * Checks if the vertex is in the settledNodes
	 */
	private boolean isSettled(Container vertex) {
		return settledNodes.contains(vertex);
	}
	
	/**
	 * Method that returns the distance between of the destination node
	 * If no value is found, Double.MAX_VALUE is returned
	 */
	private double getShortestDistance(Container destination) {
		Double d = distance.get(destination);
		if (d == null) return Double.MAX_VALUE;
		else return d;
	}

	/*
	 * This method returns the path from the source to the selected target and
	 * NULL if no path exists (the execute method must be called otherwise no path will be returned)
	 */
	public LinkedList<Container> getPath(Container target) {
		LinkedList<Container> path = new LinkedList<Container>();
		Container step = target;
		// check if a path exists
		if (predecessors.get(step) == null) return null;
		path.add(step);
		while (predecessors.get(step) != null) {
			step = predecessors.get(step);
	  	  	//System.out.println("step: "+step);
	  	  	path.add(step);
		}
		// Put it into the correct order
		Collections.reverse(path);
		return path;
	}

	/*
	 * This static method creates a new DijkstraShortestPath object, runs the algoritm and
	 * returns a list of MapNode objects representing the path
	 */
	public static LinkedList<MapNode> getShortestPath(MapGraph g, Container source, Container target) {
		DijkstraShortestPath dijkstra = new DijkstraShortestPath(g);
		long startTime = System.currentTimeMillis(); //speed test
		
		if(g.getVertices().contains(source)) {
			dijkstra.execute(source, target);
	  		long stopTime = System.currentTimeMillis();
	  		long elapsedTime = stopTime - startTime;
	    	System.out.print("Elapsed time for route: ");
	      	System.out.println(elapsedTime);
			
			if(g.getVertices().contains(target)) {
				startTime = System.currentTimeMillis();
				LinkedList<Container> path = dijkstra.getPath(target);
				
				stopTime = System.currentTimeMillis();
				elapsedTime = stopTime - startTime;
				System.out.print("Elapsed time for route reconstruction: ");
				System.out.println(elapsedTime);
				
				if(path != null && path.size() > 0) {
					LinkedList<MapNode> ret = new LinkedList<MapNode>();
					for (Container c : path) {
						ret.add(c.node);
			  	  	}
					
					return ret;
				}
			}
		}
		
		return null;
	}
	
	/*
	 * This static method creates a new DijkstraShortestPath object, runs the algoritm and
	 * returns a list of MapEdge objects representing the path
	 */
	public static LinkedList<MapEdge> getShortestPathEdges(MapGraph g, Container source, Container target) {
		DijkstraShortestPath dijkstra = new DijkstraShortestPath(g);
		
		if(g.getVertices().contains(source)) {
			long startTime = System.currentTimeMillis(); //speed test
			dijkstra.execute(source, target);
			
	  		long stopTime = System.currentTimeMillis();
	  		long elapsedTime = stopTime - startTime;
			System.out.println("--Dijkstra--");
	    	System.out.print("Elapsed time for route: ");
	      	System.out.println(elapsedTime);
			System.out.print("No of nodes searched: ");
			System.out.println(dijkstra.predecessors.size());
			
			if(g.getVertices().contains(target)) {
				LinkedList<Container> path = dijkstra.getPath(target);
				
				if(path != null && path.size() > 0) {
					LinkedList<MapEdge> ret = new LinkedList<MapEdge>();
					
					for (int i = 0; i < path.size()-1; ++i) {
						Container c = path.get(i);
						Container next = path.get(i+1);
						ret.add(c.con.get(next));
			  	  	}
					
					return ret;
				}
			}
		}
		
		return null;
	}
	
	public LinkedList<MapEdge> getShortestPathEdges(Container source, Container target) {

		
		if(graph.getVertices().contains(source)) {
			execute(source, target);

			if(graph.getVertices().contains(target)) {
				LinkedList<Container> path = getPath(target);
				//System.out.println("PATH: " + path);
				if(path != null && path.size() > 0) {
					LinkedList<MapEdge> ret = new LinkedList<MapEdge>();
					
					for (int i = 0; i < path.size()-1; ++i) {
						Container c = path.get(i);
						Container next = path.get(i+1);
						ret.add(c.con.get(next));
			  	  	}
					
					return ret;
				}
			}
		}
		
		return null;
	}
	
	public List<MapNode> getShortestReconstructedPath(Container source, Container target, Map<Long, MapWay> ways) {
		List<MapEdge> l = getShortestPathEdges(source, target);
		List<MapNode> ret = new LinkedList<MapNode>();
		for(MapEdge e : l){
			ret.add(e.getSource().node);
			ret.addAll(Grapher.getNodesBetween(e,ways.get(e.getId())));
			ret.add(e.getDestination().node);
		}
		return ret;
	}
	
	public static List<MapNode> getShortestReconstructedPath(MapGraph g, Container source, Container target, Map<Long, MapWay> ways) {
		List<MapEdge> l = getShortestPathEdges(g, source, target);
		List<MapNode> ret = new LinkedList<MapNode>();
		for(MapEdge e : l){
			ret.add(e.getSource().node);
			ret.addAll(Grapher.getNodesBetween(e,ways.get(e.getId())));
			ret.add(e.getDestination().node);
		}
		return ret;
	}
} 