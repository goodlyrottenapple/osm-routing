import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

/**
 * @author      Samuel Balco <sb592@le.ac.uk>
 * @version     1.1                 (current version number of program)
 * @since       2014-04-30          (the version of the package this class was first added to)
 */
public class RouteRunnable implements Runnable{

    protected Socket clientSocket = null;
    protected String serverText   = null;
	private MapGraph routingGraph;
	private List<MapNode> sortedNodes;
	Map<Long, MapWay> ways;
	
    public RouteRunnable(Socket clientSocket, String serverText, MapGraph graph, List<MapNode> sortedNodes, Map<Long, MapWay> ways) {
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
		routingGraph = graph;
		this.sortedNodes = sortedNodes;
		this.ways = ways;
    }

    public void run() {
        try {
			PrintWriter out=new PrintWriter(clientSocket.getOutputStream());
			BufferedReader in =new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			String input = in.readLine();
			double[][] coords = new double[2][2];
			
			try{
				String[] coordPairs = input.split(";");
			
				for (int i = 0; i < coordPairs.length; ++i) {
					String[] tmp = coordPairs[i].split(",");
					coords[i][0] = Double.parseDouble(tmp[0]);
					coords[i][1] = Double.parseDouble(tmp[1]);
				}
			} catch (Exception e){
				out.print("invalid_coordinates" + "\n");
				out.flush();
				out.close();
				in.close();
				return;
			}
				
			//find the closest points inside the MapGraph
			MapNode o_closest;
			MapNode d_closest;
			try{
				o_closest = Grapher.closestNodeInGraph(new MapNode(0, coords[0][0], coords[0][1]), sortedNodes);
				d_closest = Grapher.closestNodeInGraph(new MapNode(0, coords[1][0], coords[1][1]), sortedNodes);
			} catch (Exception e){
				out.print("invalid_coordinate_range" + "\n");
				out.flush();
				out.close();
				in.close();
				return;
			}
			//compute the route
							
			/*List<MapEdge> foundPath = DijkstraShortestPath.getShortestPathEdges(routingGraph, routingGraph.getVerticesMap().get(o_closest.id), routingGraph.getVerticesMap().get(d_closest.id));
			if(foundPath != null) {
				String response = "";
				double distance = 0;
				
				//convert the found route to a string
				for(MapEdge e : foundPath){
					//using ';' to separate individual coordinates
					response += e.getSource().node.toString() + ";";
					distance += e.getWeight();
				}
				//returns the route and the distance separated by ':'
				response += ":" + distance;
				System.out.println(response);
				out.print(response + "\n");
				out.flush();
			}*/	
			
			BiDirAStar bidirastar = new BiDirAStar(routingGraph);
			List<MapNode> foundPath;
			try{
				foundPath = bidirastar.getShortestReconstructedPath(routingGraph.getVerticesMap().get(o_closest.id), routingGraph.getVerticesMap().get(d_closest.id), ways);
			} catch(Exception e){
				out.print("no_path_found" + "\n");
				out.flush();
				out.close();
				in.close();
				return;
			}
			
			if(foundPath != null) {
				String response = "";
				double distance = bidirastar.getTotalDistance();
				
				//convert the found route to a string
				for(MapNode n : foundPath){
					//using ';' to separate individual coordinates
					response += n.toString() + ";";
				}
				//returns the route and the distance separated by ':'
				response += ":" + distance;
				System.out.println(response);
				out.print(response + "\n");
				out.flush();
			}
			
			out.close();
			in.close();
        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        }
		System.out.println("Terminated connection");
    }
}

