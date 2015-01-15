import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * @author      Samuel Balco <sb592@le.ac.uk>
 * @version     1.3                 (current version number of program)
 * @since       2014-03-31          (the version of the package this class was first added to)
 */
public class MapGraph {
	Map<Long, Container> vertices;

	public MapGraph() {
		vertices = new HashMap<Long, Container>();
	}

	public void addVertex(MapNode v) {
	  	vertices.put(v.getId(), new Container(v));
	}

	public void addEdge(MapEdge e) {
		//e.getSource/Destination() returns a Container
	  	e.getSource().con.put(e.getDestination(), e);
		e.getDestination().reversed_con.put(e.getSource(), e);
	}

	public List<Container> getVertices() {
	  	return new ArrayList<Container>(vertices.values());
	}

	public Map<Long, Container> getVerticesMap() {
	  	return vertices;
	}

}

class Container {
	long id;
	MapNode node;
	Map<Container, MapEdge> con = new HashMap<Container, MapEdge>(), reversed_con = new HashMap<Container, MapEdge>();

	public Container(MapNode n) {
		id = n.id;
		node = n;
	}
	
    @Override
    public boolean equals(Object obj) {
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Container other = (Container) obj;
      return id == other.id;
    }
}