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


public class BiDirDijkstra {
	private MapGraph graph;
	
	double totalPath = Double.MAX_VALUE;
	Container min_v1 = null, min_v2 = null;
	
	 Set<Container> settledNodes_source, settledNodes_target;
	PriorityQueue<Container> unSettledNodes_source, unSettledNodes_target;
	private Map<Container, Container> predecessors_source, predecessors_target;
	private Map<Container, Double> distance_source, distance_target;

	public Comparator<Container> distanceComparator = new Comparator<Container>() {
		@Override
		public int compare(Container c1, Container c2) {
			return (int) ((getShortestDistance(c1) - getShortestDistance(c2))*1000);
		}
	};
	
	public Comparator<Container> reverseDistanceComparator = new Comparator<Container>() {
		@Override
		public int compare(Container c1, Container c2) {
			return (int) ((getShortestReversedDistance(c1) - getShortestReversedDistance(c2))*1000);
		}
	};


	public BiDirDijkstra(MapGraph graph) {
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
		settledNodes_source = new HashSet<Container>();
		settledNodes_target = new HashSet<Container>();
		unSettledNodes_source = new PriorityQueue<Container>(1, distanceComparator);
		unSettledNodes_target = new PriorityQueue<Container>(1, reverseDistanceComparator);
		
		distance_source = new HashMap<Container, Double>();
		distance_target = new HashMap<Container, Double>();
		
		predecessors_source = new HashMap<Container, Container>();
		predecessors_target = new HashMap<Container, Container>();
		
		distance_source.put(source, 0.0);
		unSettledNodes_source.add(source);
		
		distance_target.put(target, 0.0);
		unSettledNodes_target.add(target);
		
		while (unSettledNodes_source.size() > 0 && unSettledNodes_target.size() > 0) {
			//Container node = getMinimum(unSettledNodes);
			
			if(getShortestDistance(unSettledNodes_source.peek()) < getShortestReversedDistance(unSettledNodes_target.peek())){
				Container node_source = unSettledNodes_source.poll();
			    settledNodes_source.add(node_source);
			  	unSettledNodes_source.remove(node_source);
  			
				findMinimalDistances(node_source);
				if (settledNodes_target.contains(node_source)) break;
			}
			else{	
	  	  	
			
				//System.out.println("unsettled source: " + unSettledNodes_source);
			
				Container node_target = unSettledNodes_target.poll();
		  	  	settledNodes_target.add(node_target);
		  	  	unSettledNodes_target.remove(node_target);
		  	  	findMinimalDistancesReverse(node_target);
				if (settledNodes_source.contains(node_target)) break;
			}
			
			//System.out.println("unsettled target: " + unSettledNodes_target);
	  	  	
		}
		
		Container node_source = unSettledNodes_source.poll();
	    settledNodes_source.add(node_source);
	  	unSettledNodes_source.remove(node_source);
	
		findMinimalDistances(node_source);
		
		Container node_target = unSettledNodes_target.poll();
  	  	settledNodes_target.add(node_target);
  	  	unSettledNodes_target.remove(node_target);
  	  	findMinimalDistancesReverse(node_target);
		
	}
  
	/**
	 * Method for calculating the minimal distances and adding all the neigbors of the input node
	 */
	private void findMinimalDistances(Container node) {
		List<Container> adjacentNodes = getNeighbors(node);
		for (Container target : adjacentNodes) {
			
			if (getShortestDistance(target) > getShortestDistance(node) + getDistance(node, target)) {
				//System.out.println("GETTINGHERE\n");
				distance_source.put(target, getShortestDistance(node) + getDistance(node, target));
	    		predecessors_source.put(target, node);
	    		unSettledNodes_source.add(target);
	  	  	}
			
            if(settledNodes_target.contains(target)) {
				double possiblePathLength = getShortestDistance(node) + node.con.get(target).getWeight() + getShortestReversedDistance(target);
				//System.out.println("POSSIBLE PATH IN EXEC: " + possiblePathLength);
				if(possiblePathLength < totalPath){
					min_v1 = node;
					min_v2 = target;
					totalPath = possiblePathLength;
				}
			}
						
		}
	}
	
	private void findMinimalDistancesReverse(Container node) {
		List<Container> adjacentNodes = getReverseNeighbors(node);
		//System.out.println(adjacentNodes);
		for (Container target : adjacentNodes) {
			//System.out.println(getShortestReversedDistance(target));
			//System.out.println(getShortestReversedDistance(node));
			//System.out.println(getReversedDistance(node, target));
			if (getShortestReversedDistance(target) > getShortestReversedDistance(node) + getReversedDistance(node, target)) {
				
				distance_target.put(target, getShortestReversedDistance(node) + getReversedDistance(node, target));
	    		//predecessors_target.put(node, target);
				predecessors_target.put(target, node);
	    		unSettledNodes_target.add(target);
	  	  	}
			
            if(settledNodes_source.contains(target)) {
				double possiblePathLength = getShortestDistance(target) + target.con.get(node).getWeight() + getShortestReversedDistance(node);
				//System.out.println("POSSIBLE PATH IN EXEC REV: " + possiblePathLength);
				if(possiblePathLength < totalPath){
					min_v1 = node;
					min_v2 = target;
					totalPath = possiblePathLength;
				}
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
	
	private double getReversedDistance(Container node, Container target) {
		if (node.reversed_con.containsKey(target)){
			return node.reversed_con.get(target).getWeight();
		}
		throw new RuntimeException("Should not happen");
	}

	/**
	 * Method that returns the neighbors for the input node
	 */
	private List<Container> getNeighbors(Container node) {
		List<Container> neighbors = new ArrayList<Container>();
		for (MapEdge edge : node.con.values()) {
			if (!settledNodes_source.contains(edge.getDestination())) {
				neighbors.add(edge.getDestination());
	  	  	}
		}
		return neighbors;
	}
	
	private List<Container> getReverseNeighbors(Container node) {
		List<Container> neighbors = new ArrayList<Container>();
		for (MapEdge edge : node.reversed_con.values()) {
			if (!settledNodes_target.contains(edge.getSource())) {
				neighbors.add(edge.getSource());
				//System.out.println("target: " + edge.getDestination());
	  	  	}
		}
		//System.out.println("rev neighbors: " + neighbors);
		return neighbors;
	}

	
	/**
	 * Method that returns the distance between of the destination node
	 * If no value is found, Double.MAX_VALUE is returned
	 */
	private double getShortestDistance(Container destination) {
		Double d = distance_source.get(destination);
		if (d == null) return Double.MAX_VALUE;
		else return d;
	}
	
	private double getShortestReversedDistance(Container destination) {
		Double d = distance_target.get(destination);
		if (d == null) return Double.MAX_VALUE;
		else return d;
	}

	/*
	 * This method returns the path from the source to the selected target and
	 * NULL if no path exists (the execute method must be called otherwise no path will be returned)
	 */
	
	
	
	public LinkedList<Container> getPath() {
		LinkedList<Container> path1 = new LinkedList<Container>(), path2 = new LinkedList<Container>();
		Container step = min_v2;
		// check if a path exists
		//System.out.println("Reconstructing...");
		if (predecessors_target.get(step) == null) return null;
		path1.add(step);
		while (predecessors_target.get(step) != null) {
			step = predecessors_target.get(step);
	  	  	//System.out.println("step: "+step);
	  	  	path1.add(step);
			//System.out.println(step);
			
		}
		step = min_v1;
		//System.out.println("moving to source");
		while (predecessors_source.get(step) != null) {
			step = predecessors_source.get(step);
	  	  	//System.out.println("step: "+step);
	  	  	path2.add(step);
			//System.out.println(step);
			
		}
		// Put it into the correct order
		//System.out.println("reversing...");
		Collections.reverse(path2);
		//System.out.println(path);
		path2.addAll(path1);
		return path2;
	}
	
	
	/*
	 * This static method creates a new BiDirDijkstra object, runs the algoritm and
	 * returns a list of MapNode objects representing the path
	 */
	public static LinkedList<MapNode> getShortestPath(MapGraph g, Container source, Container target) {
		BiDirDijkstra dijkstra = new BiDirDijkstra(g);
		long startTime = System.currentTimeMillis(); //speed test
		
		if(g.getVertices().contains(source)) {
			dijkstra.execute(source, target);
	  		long stopTime = System.currentTimeMillis();
	  		long elapsedTime = stopTime - startTime;
	    	System.out.print("Elapsed time for route: ");
	      	System.out.println(elapsedTime);
			
			if(g.getVertices().contains(target)) {
				startTime = System.currentTimeMillis();
				LinkedList<Container> path = dijkstra.getPath();
				
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
	 * This static method creates a new BiDirDijkstra object, runs the algoritm and
	 * returns a list of MapEdge objects representing the path
	 */
	public static LinkedList<MapEdge> getShortestPathEdges(MapGraph g, Container source, Container target) {
		BiDirDijkstra dijkstra = new BiDirDijkstra(g);
		
		if(g.getVertices().contains(source)) {
			long startTime = System.currentTimeMillis(); //speed test
			dijkstra.execute(source, target);
			
	  		long stopTime = System.currentTimeMillis();
	  		long elapsedTime = stopTime - startTime;
			System.out.println("--DijkstraBiDir--");
	    	System.out.print("Elapsed time for route: ");
	      	System.out.println(elapsedTime);
			System.out.print("No of nodes searched: ");
			System.out.println(dijkstra.predecessors_source.size());
			System.out.println(dijkstra.predecessors_target.size());
			
			
			if(g.getVertices().contains(target)) {
				LinkedList<Container> path = dijkstra.getPath();
				
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
	
	
	
	public static List<MapNode> getShortestReconstructedPath(MapGraph g, Container source, Container target, Map<Long, MapWay> ways) {
		List<MapEdge> l = getShortestPathEdges(g, source, target);
		
		List<MapNode> ret = new LinkedList<MapNode>();
		for(MapEdge e : l){
			//System.out.println(e);
			if(e != null) {
				ret.add(e.getSource().node);
				ret.addAll(Grapher.getNodesBetween(e,ways.get(e.getId())));
				ret.add(e.getDestination().node);
			}
			
		}
		return ret;
	}
	
	
	
	
	
	
	public  LinkedList<MapEdge> getShortestPathEdges(Container source, Container target) {

		
		if(graph.getVertices().contains(source)) {
			//long startTime = System.currentTimeMillis(); //speed test
			execute(source, target);
			
	  		/*long stopTime = System.currentTimeMillis();
	  		long elapsedTime = stopTime - startTime;
			System.out.println("--DijkstraBiDir--");
	    	System.out.print("Elapsed time for route: ");
	      	System.out.println(elapsedTime);
			System.out.print("No of nodes searched: ");
			System.out.println(predecessors_source.size());
			System.out.println(predecessors_target.size());
			*/
			
			if(graph.getVertices().contains(target)) {
				LinkedList<Container> path = getPath();
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
			//System.out.println(e);
			if(e != null) {
				ret.add(e.getSource().node);
				ret.addAll(Grapher.getNodesBetween(e,ways.get(e.getId())));
				ret.add(e.getDestination().node);
			}
			
		}
		return ret;
	}
} 