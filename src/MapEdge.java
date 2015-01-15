public class MapEdge {
	private final long id; 
  	private final Container source;
  	private final Container destination;
  	private final double weight;
	
	public MapEdge(long id, Container source, Container destination, double weight) {
    	this.id = id;
    	this.source = source;
    	this.destination = destination;
    	this.weight = weight;
 	}
  
  	public long getId() {
		return id;
  	}
	public Container getDestination() {
	return destination;
	}

	public Container getSource() {
		return source;
	}
	public double getWeight() {
		return weight;
	}

	@Override
	public String toString() {
		return source.node + " " + destination.node;
	}
  
  
} 