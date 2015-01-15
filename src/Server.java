import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import java.io.FileInputStream;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

/**
 * @author      Samuel Balco <sb592@le.ac.uk>
 * @version     1.0                 (current version number of program)
 * @since       2014-03-31          (the version of the package this class was first added to)
 */
public class Server implements Runnable{

    protected int          serverPort   = 5123;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;
	
	Map<Long, MapNode> nodes = new HashMap<Long, MapNode>();
	MapGraph routingGraph = new MapGraph();
	ArrayList<MapNode> sortedNodes;
	Map<Long, MapWay> ways;
	
	/*
	 * The Server instance created will load an osm file and create a MapGraph used for routing
	 */
    public Server(int port, String setting, String path){
        this.serverPort = port;
		
		SAXParser p = new SAXParser();
		try {
			if (setting.equals("-t")) p.load( ClassLoader.getSystemResourceAsStream(path) ); //loads up a smaller map for testing
			else if (setting.equals("-f")) p.load( new FileInputStream(path) ); //loads up all of uk!
		} catch (Exception e) {}
		
		// create the nodes and ways that are used to construct the MapGraph
		ways = p.mapWayMap;
		nodes = Grapher.reduce(p.getWays(), p.getNodes());
		List<MapLink> links = Grapher.link(p.getWays(), nodes);
		routingGraph = Grapher.createGraph( nodes, links );
		sortedNodes = new ArrayList<MapNode>(nodes.values());
		Collections.sort(sortedNodes);
    }

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();
		System.out.println("Server Running");
        while(!isStopped()){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
				//creates a new instance of RouteRunnable that handles a routing request form the use client
	            new Thread( new RouteRunnable(clientSocket, "Multithreaded Server", routingGraph, sortedNodes, ways) ).start();
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
        }
        System.out.println("Server Stopped.") ;
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }
	
    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port 8080", e);
        }
    }
	
	public static void main(String[] args) {
        // systemProperties.setProperty("http.proxyPort", "8008");
		String flag = "-t",path="map.xml";
		if (args.length==1) flag = args[0];
		if (args.length==2) {
			flag = args[0];
			path = args[1];
		}
		Server server = new Server(9000, flag, path);
		new Thread(server).start();
		Scanner s = new Scanner(System.in);
		String in = s.next();
		while(!in.equals("exit")){ in = s.next(); }
		server.stop();
	}
}