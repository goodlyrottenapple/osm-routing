
//License: GPL. Copyright 2008 by Jan Peter Stotz
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Color;
import java.awt.Point;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.FileInputStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOpenAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;



/**
 *
 * Demonstrates the usage of {@link JMapViewer}
 *
 * @author Jan Peter Stotz
 *
 */
public class GUI extends JFrame implements JMapViewerEventListener  {

    private static final long serialVersionUID = 1L;

    private JMapViewerTree treeMap = null;

    private JLabel zoomLabel=null;
    private JLabel zoomValue=null;

    private JLabel mperpLabelName=null;
    private JLabel mperpLabelValue = null;
	
	Map<Long, MapNode> nodes = new HashMap<Long, MapNode>();
	Map<Long, MapWay> ways;
	
	MapGraph routingGraph = new MapGraph();
	MapNode o_closest, d_closest;
	ArrayList<MapNode> sortedNodes;
	String selected_algorithm = "Dijkstra";
	JLabel helpLabel1, helpLabel2;
	List<MapLink> links;
 
    public GUI(String setting, String path) {
        super("JMapViewer Demo");
        setSize(400, 400);
        treeMap = new JMapViewerTree("Zones");

        // Listen to the map viewer for user operations so components will
        // recieve events and update
        map().addJMVListener(this);

        // final JMapViewer map = new JMapViewer(new MemoryTileCache(),4);
        // map.setTileLoader(new OsmFileCacheTileLoader(map));
        // new DefaultMapController(map);

        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        JPanel panel = new JPanel();
        JPanel panelTop = new JPanel();
		JPanel panelMiddle = new JPanel();
        JPanel panelBottom = new JPanel();
        JPanel helpPanel = new JPanel();

        mperpLabelName=new JLabel("Meters/Pixels: ");
        mperpLabelValue=new JLabel(String.format("%s",map().getMeterPerPixel()));

        zoomLabel=new JLabel("Zoom: ");
        zoomValue=new JLabel(String.format("%s", map().getZoom()));

        add(panel, BorderLayout.NORTH);
        add(helpPanel, BorderLayout.SOUTH);
        panel.setLayout(new BorderLayout());
        panel.add(panelTop, BorderLayout.NORTH);
		panel.add(panelMiddle, BorderLayout.CENTER);
        panel.add(panelBottom, BorderLayout.SOUTH);
        
		helpLabel1 = new JLabel();
        helpPanel.add(helpLabel1);
		helpLabel2 = new JLabel();
        helpPanel.add(helpLabel2);
		
        JButton button = new JButton("setDisplayToFitMapMarkers");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                map().setDisplayToFitMapMarkers();
            }
        });
        JComboBox tileSourceSelector = new JComboBox(new TileSource[] { new OsmTileSource.Mapnik(),
                new OsmTileSource.CycleMap(), new BingAerialTileSource(), new MapQuestOsmTileSource(), new MapQuestOpenAerialTileSource() });
        tileSourceSelector.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                map().setTileSource((TileSource) e.getItem());
            }
        });
        JComboBox tileLoaderSelector;
        try {
            tileLoaderSelector = new JComboBox(new TileLoader[] { new OsmFileCacheTileLoader(map()),
                    new OsmTileLoader(map()) });
        } catch (IOException e) {
            tileLoaderSelector = new JComboBox(new TileLoader[] { new OsmTileLoader(map()) });
        }
        tileLoaderSelector.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                map().setTileLoader((TileLoader) e.getItem());
            }
        });
        map().setTileLoader((TileLoader) tileLoaderSelector.getSelectedItem());
        panelTop.add(tileSourceSelector);
        panelTop.add(tileLoaderSelector);
		
		final Layer fullNodes = new Layer("userPoints");
		
		final JTextField o_lat = new JTextField("52.6252420");
		panelMiddle.add(o_lat);
		final JTextField o_lon = new JTextField("-1.1085937");
		panelMiddle.add(o_lon);
		
		final JTextField d_lat = new JTextField("52.6213092");
		panelMiddle.add(d_lat);
		final JTextField d_lon = new JTextField("-1.1245779");
		panelMiddle.add(d_lon);
		
        JButton point = new JButton("Put points on map");
        point.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
				try{
				double o_latitude = Double.parseDouble(o_lat.getText());
				double o_longitude = Double.parseDouble(o_lon.getText());
				map().addMapMarker( new MapMarkerDot(fullNodes, "", new Coordinate( o_latitude, o_longitude), new Style(Color.BLACK, Color.GREEN, null, new Font("Courier", Font.PLAIN,10)) ) );
				
			
				double d_latitude = Double.parseDouble(d_lat.getText());
				double d_longitude = Double.parseDouble(d_lon.getText());
				map().addMapMarker( new MapMarkerDot(fullNodes, "", new Coordinate( d_latitude, d_longitude), new Style(Color.BLACK, Color.GREEN, null, new Font("Courier", Font.PLAIN,10)) ) );
				
				long startTime = System.currentTimeMillis(); //speed test
				
				d_closest = Grapher.closestNodeInGraph(new MapNode(0, d_latitude, d_longitude), sortedNodes);
				o_closest = Grapher.closestNodeInGraph(new MapNode(0, o_latitude, o_longitude), sortedNodes);
				
				long stopTime = System.currentTimeMillis();
				long elapsedTime = stopTime - startTime;
				
				map().addMapMarker( new MapMarkerDot(fullNodes, "destination", new Coordinate( d_closest.lat, d_closest.lon), new Style(Color.BLACK, Color.RED, null, new Font("Courier", Font.PLAIN,10)) ) );
				map().addMapMarker( new MapMarkerDot(fullNodes, "origin", new Coordinate( o_closest.lat, o_closest.lon), new Style(Color.BLACK, Color.RED, null, new Font("Courier", Font.PLAIN,10)) ) );
				
				helpLabel1.setText("Elapsed time for markers (approximate): " + elapsedTime + " ms");
				
			}catch(Exception ex){
				System.out.println("whoops!");
			}
				//calculate and display route here.....
				/*
		        List<MapNode> foundPath = DijkstraShortestPath.getShortestPath(routingGraph, routingGraph.getVerticesMap().get(o_closest.id), routingGraph.getVerticesMap().get(d_closest.id));
			
				//System.out.println(foundPath);
				
				for( int i = 1;i<foundPath.size(); ++i ){
					
					Coordinate one = new Coordinate(foundPath.get(i-1).lat, foundPath.get(i-1).lon);
					Coordinate two = new Coordinate(foundPath.get(i).lat, foundPath.get(i).lon);
					List<Coordinate> route = new ArrayList<Coordinate>(Arrays.asList(one, two, two));
					//map().addMapPolygon(new MapPolygonImpl(""+(l.distance*1000)+" m", route)); //shows distances - messy with a lot of points
					map().addMapPolygon(new MapPolygonImpl("", route));
				}
				//*/
            }
        });
		panelMiddle.add(point);
		
		String[] labels = {"All points", "Intersections", "Links", "Dijkstra", "Bi-dir Dijkstra", "AStar", "Bi-dir AStar"};
        JComboBox algSelect = new JComboBox(labels);
		selected_algorithm = "All points";
        algSelect.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
				selected_algorithm = (String)(e.getItem());
            }
        });
		panelMiddle.add(algSelect);
		
        JButton route = new JButton("Route");
        route.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
				//calculate and display route here.....
				//*
		        List<MapNode> foundPath = new ArrayList<MapNode>();
				
				
				if(selected_algorithm.equals("All points")){
					for (MapWay way : ways.values()){
						for (MapNode node : way.nodes){
							if(node.lat > 52.5813092 && node.lat < 52.675455  && node.lon > -1.2145779 && node.lon < -1.0584038)
								map().addMapMarker( new MapMarkerDot(fullNodes, "", new Coordinate( node.lat, node.lon), new Style(Color.BLACK, Color.RED, null, new Font("Courier", Font.PLAIN,10)) ) );
							//map().addMapMarker( new MapMarkerDot(fullNodes, "", new Coordinate( node.lat, node.lon), new Style(Color.BLACK, Color.RED, null, new Font("Courier", Font.PLAIN,10)) ) );
				  	  	}
			  	  	}	
					
				}
				else if(selected_algorithm.equals("Intersections")){
					for (MapNode node : nodes.values()){
						//System.out.println(node);
		  	  			if(node.lat > 52.5813092 && node.lat < 52.675455  && node.lon > -1.2145779 && node.lon < -1.0584038) //map().addMapMarker( new MapMarkerDot(fullNodes, ""+node.id, new Coordinate( node.lat, node.lon), new Style(Color.BLACK, Color.YELLOW, null, new Font("Courier", Font.PLAIN,10)) ) );
						map().addMapMarker(new MapMarkerDot(node.lat, node.lon));
			  	  	}
				}
				else if(selected_algorithm.equals("Links")){
					for(MapLink l : links){
						if(	l.n1.lat > 52.5813092 && l.n1.lat < 52.675455  && l.n1.lon > -1.2145779 && l.n1.lon < -1.0584038 &&
							l.n2.lat > 52.5813092 && l.n2.lat < 52.675455  && l.n2.lon > -1.2145779 && l.n2.lon < -1.0584038 ){
								Coordinate one = new Coordinate(l.n1.lat, l.n1.lon);
								Coordinate two = new Coordinate(l.n2.lat, l.n2.lon);
								List<Coordinate> r = new ArrayList<Coordinate>(Arrays.asList(one, two, two));
								//map().addMapPolygon(new MapPolygonImpl(""+(l.distance*1000)+" m", r)); //shows distances - messy with a lot of points
								map().addMapPolygon(new MapPolygonImpl("", r));
								
							}
						}
				}
				else if(selected_algorithm.equals("Dijkstra")){
					DijkstraShortestPath dijkstra = new DijkstraShortestPath(routingGraph);
					long startTime = System.currentTimeMillis(); //speed test
					
					foundPath = dijkstra.getShortestReconstructedPath(routingGraph.getVerticesMap().get(o_closest.id), routingGraph.getVerticesMap().get(d_closest.id), ways);
					
					long stopTime = System.currentTimeMillis();
					long elapsedTime = stopTime - startTime;
					helpLabel2.setText("Elapsed time for Dijkstra (approximate): " + elapsedTime + " ms");
					
					for (Container c : dijkstra.settledNodes){
						//System.out.println(node);
		  	  			 map().addMapMarker( new MapMarkerDot(fullNodes, "", new Coordinate( c.node.lat, c.node.lon), new Style(Color.BLUE, Color.BLUE, null, new Font("Courier", Font.PLAIN,10)) ) );
						//map().addMapMarker(new MapMarkerDot(c.node.lat, c.node.lon));
			  	  	}
				}
				else if(selected_algorithm.equals("Bi-dir Dijkstra")){
					BiDirDijkstra dijkstra = new BiDirDijkstra(routingGraph);
					
					long startTime = System.currentTimeMillis(); //speed test

					foundPath = dijkstra.getShortestReconstructedPath(routingGraph.getVerticesMap().get(o_closest.id), routingGraph.getVerticesMap().get(d_closest.id), ways);

					long stopTime = System.currentTimeMillis();
					long elapsedTime = stopTime - startTime;
					helpLabel2.setText("Elapsed time for Bi-dir Dijkstra (approximate): " + elapsedTime + " ms");
										

					for (Container c : dijkstra.settledNodes_source){
						//System.out.println(node);
		  	  			 map().addMapMarker( new MapMarkerDot(fullNodes, "", new Coordinate( c.node.lat, c.node.lon), new Style(Color.BLUE, Color.BLUE, null, new Font("Courier", Font.PLAIN,10)) ) );
						//map().addMapMarker(new MapMarkerDot(c.node.lat, c.node.lon));
			  	  	}

					for (Container c : dijkstra.settledNodes_target){
						//System.out.println(node);
		  	  			 map().addMapMarker( new MapMarkerDot(fullNodes, "", new Coordinate( c.node.lat, c.node.lon), new Style(Color.GREEN, Color.GREEN, null, new Font("Courier", Font.PLAIN,10)) ) );
						//map().addMapMarker(new MapMarkerDot(c.node.lat, c.node.lon));
			  	  	}
				}
				else if(selected_algorithm.equals("AStar")){
			        AStar astar = new AStar(routingGraph);
					long startTime = System.currentTimeMillis(); //speed test

					foundPath = astar.getShortestReconstructedPath(routingGraph.getVerticesMap().get(o_closest.id), routingGraph.getVerticesMap().get(d_closest.id), ways);

					long stopTime = System.currentTimeMillis();
					long elapsedTime = stopTime - startTime;
					helpLabel2.setText("Elapsed time for AStar (approximate): " + elapsedTime + " ms");
					
					for (Container c : astar.settledNodes){
						//System.out.println(node);
		  	  			 map().addMapMarker( new MapMarkerDot(fullNodes, "", new Coordinate( c.node.lat, c.node.lon), new Style(Color.BLUE, Color.BLUE, null, new Font("Courier", Font.PLAIN,10)) ) );
						//map().addMapMarker(new MapMarkerDot(c.node.lat, c.node.lon));
			  	  	}
				
				}
				else if(selected_algorithm.equals("Bi-dir AStar")){
			        BiDirAStar astar = new BiDirAStar(routingGraph);
					long startTime = System.currentTimeMillis(); //speed test

					foundPath = astar.getShortestReconstructedPath(routingGraph.getVerticesMap().get(o_closest.id), routingGraph.getVerticesMap().get(d_closest.id), ways);

					long stopTime = System.currentTimeMillis();
					long elapsedTime = stopTime - startTime;
					helpLabel2.setText("Elapsed time for Bi-dir AStar (approximate): " + elapsedTime + " ms");
					
				
				
					for (Container c : astar.settledNodes_source){
						//System.out.println(node);
		  	  			 map().addMapMarker( new MapMarkerDot(fullNodes, "", new Coordinate( c.node.lat, c.node.lon), new Style(Color.BLUE, Color.BLUE, null, new Font("Courier", Font.PLAIN,10)) ) );
						//map().addMapMarker(new MapMarkerDot(c.node.lat, c.node.lon));
			  	  	}
				
					for (Container c : astar.settledNodes_target){
						//System.out.println(node);
		  	  			 map().addMapMarker( new MapMarkerDot(fullNodes, "", new Coordinate( c.node.lat, c.node.lon), new Style(Color.GREEN, Color.GREEN, null, new Font("Courier", Font.PLAIN,10)) ) );
						//map().addMapMarker(new MapMarkerDot(c.node.lat, c.node.lon));
			  	  	}
				
					map().addMapMarker( new MapMarkerDot(fullNodes, "min_v1", new Coordinate( astar.min_v1.node.lat, astar.min_v1.node.lon), new Style(Color.BLACK, Color.RED, null, new Font("Courier", Font.PLAIN,10)) ) );
					map().addMapMarker( new MapMarkerDot(fullNodes, "min_v2", new Coordinate( astar.min_v2.node.lat, astar.min_v2.node.lon), new Style(Color.BLACK, Color.RED, null, new Font("Courier", Font.PLAIN,10)) ) );
				
				} 
				
				
				
				
				
				for( int i = 1;i<foundPath.size(); ++i ){
					
					Coordinate one = new Coordinate(foundPath.get(i-1).lat, foundPath.get(i-1).lon);
					Coordinate two = new Coordinate(foundPath.get(i).lat, foundPath.get(i).lon);
					List<Coordinate> route = new ArrayList<Coordinate>(Arrays.asList(one, two, two));
					//map().addMapPolygon(new MapPolygonImpl(""+(l.distance*1000)+" m", route)); //shows distances - messy with a lot of points
					map().addMapPolygon(new MapPolygonImpl(fullNodes, "", route, new Style(Color.BLACK, Color.YELLOW, new java.awt.BasicStroke(6f), new Font("Courier", Font.PLAIN,10))   ));

					
				}
				
				//foundPath = DijkstraShortestPath.getShortestReconstructedPath(routingGraph, routingGraph.getVerticesMap().get(o_closest.id), routingGraph.getVerticesMap().get(d_closest.id), ways);
		        //BiDirDijkstra dijkstra = new BiDirDijkstra(routingGraph);
				//foundPath = dijkstra.getShortestReconstructedPath(routingGraph.getVerticesMap().get(o_closest.id), routingGraph.getVerticesMap().get(d_closest.id), ways);
				/*
				for (Container c : dijkstra.settledNodes_source){
					//System.out.println(node);
	  	  			 map().addMapMarker( new MapMarkerDot(fullNodes, "", new Coordinate( c.node.lat, c.node.lon), new Style(Color.BLUE, Color.BLUE, null, new Font("Courier", Font.PLAIN,10)) ) );
					//map().addMapMarker(new MapMarkerDot(c.node.lat, c.node.lon));
		  	  	}
				
				for (Container c : dijkstra.settledNodes_target){
					//System.out.println(node);
	  	  			 map().addMapMarker( new MapMarkerDot(fullNodes, "", new Coordinate( c.node.lat, c.node.lon), new Style(Color.GREEN, Color.GREEN, null, new Font("Courier", Font.PLAIN,10)) ) );
					//map().addMapMarker(new MapMarkerDot(c.node.lat, c.node.lon));
		  	  	}
			
		        BiDirAStar astar = new BiDirAStar(routingGraph);
				foundPath = astar.getShortestReconstructedPath(routingGraph.getVerticesMap().get(o_closest.id), routingGraph.getVerticesMap().get(d_closest.id), ways);
				
				
				for (Container c : astar.settledNodes_source){
					//System.out.println(node);
	  	  			 map().addMapMarker( new MapMarkerDot(fullNodes, "", new Coordinate( c.node.lat, c.node.lon), new Style(Color.BLUE, Color.BLUE, null, new Font("Courier", Font.PLAIN,10)) ) );
					//map().addMapMarker(new MapMarkerDot(c.node.lat, c.node.lon));
		  	  	}
				
				for (Container c : astar.settledNodes_target){
					//System.out.println(node);
	  	  			 map().addMapMarker( new MapMarkerDot(fullNodes, "", new Coordinate( c.node.lat, c.node.lon), new Style(Color.GREEN, Color.GREEN, null, new Font("Courier", Font.PLAIN,10)) ) );
					//map().addMapMarker(new MapMarkerDot(c.node.lat, c.node.lon));
		  	  	}
				
				map().addMapMarker( new MapMarkerDot(fullNodes, "min_v1", new Coordinate( astar.min_v1.node.lat, astar.min_v1.node.lon), new Style(Color.BLACK, Color.RED, null, new Font("Courier", Font.PLAIN,10)) ) );
				map().addMapMarker( new MapMarkerDot(fullNodes, "min_v2", new Coordinate( astar.min_v2.node.lat, astar.min_v2.node.lon), new Style(Color.BLACK, Color.RED, null, new Font("Courier", Font.PLAIN,10)) ) );
				
			
				for( int i = 1;i<foundPath.size(); ++i ){
					
					Coordinate one = new Coordinate(foundPath.get(i-1).lat, foundPath.get(i-1).lon);
					Coordinate two = new Coordinate(foundPath.get(i).lat, foundPath.get(i).lon);
					List<Coordinate> route = new ArrayList<Coordinate>(Arrays.asList(one, two, two));
					//map().addMapPolygon(new MapPolygonImpl(""+(l.distance*1000)+" m", route)); //shows distances - messy with a lot of points
					map().addMapPolygon(new MapPolygonImpl("", route));
					//map().addMapMarker( new MapMarkerDot(fullNodes, "", one, new Style(Color.BLACK, Color.YELLOW, null, new Font("Courier", Font.PLAIN,10)) ) );
					//map().addMapMarker( new MapMarkerDot(fullNodes, "", two, new Style(Color.BLACK, Color.YELLOW, null, new Font("Courier", Font.PLAIN,10)) ) );
					
				}
				*/
            }
        });
		panelMiddle.add(route);
		
		
        JButton clear = new JButton("Clear");
        clear.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
				map().removeAllMapMarkers();
				map().removeAllMapPolygons();
			}
		});
		
		panelMiddle.add(clear);
		
        final JCheckBox showMapMarker = new JCheckBox("Map markers visible");
        showMapMarker.setSelected(map().getMapMarkersVisible());
        showMapMarker.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                map().setMapMarkerVisible(showMapMarker.isSelected());
            }
        });
        panelBottom.add(showMapMarker);
        ///
        final JCheckBox showTreeLayers = new JCheckBox("Tree Layers visible");
        showTreeLayers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                treeMap.setTreeVisible(showTreeLayers.isSelected());
            }
        });
        panelBottom.add(showTreeLayers);
        ///
        final JCheckBox showToolTip = new JCheckBox("ToolTip visible");
        showToolTip.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                map().setToolTipText(null);
            }
        });
        panelBottom.add(showToolTip);
        ///
        final JCheckBox showTileGrid = new JCheckBox("Tile grid visible");
        showTileGrid.setSelected(map().isTileGridVisible());
        showTileGrid.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                map().setTileGridVisible(showTileGrid.isSelected());
            }
        });
        panelBottom.add(showTileGrid);
        final JCheckBox showZoomControls = new JCheckBox("Show zoom controls");
        showZoomControls.setSelected(map().getZoomContolsVisible());
        showZoomControls.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                map().setZoomContolsVisible(showZoomControls.isSelected());
            }
        });
        panelBottom.add(showZoomControls);
        final JCheckBox scrollWrapEnabled = new JCheckBox("Scrollwrap enabled");
        scrollWrapEnabled.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                map().setScrollWrapEnabled(scrollWrapEnabled.isSelected());
            }
        });
        panelBottom.add(scrollWrapEnabled);
        panelBottom.add(button);

        panelTop.add(zoomLabel);
        panelTop.add(zoomValue);
        panelTop.add(mperpLabelName);
        panelTop.add(mperpLabelValue);

        add(treeMap, BorderLayout.CENTER);

        //
       
       // MapMarkerDot eberstadt = new MapMarkerDot(germanyEastLayer, "Eberstadt", 49.814284999, 8.642065999);
        //MapMarkerDot ebersheim = new MapMarkerDot(germanyWestLayer, "Ebersheim", 49.91, 8.24);
        //MapMarkerDot darmstadt = new MapMarkerDot(germanyEastLayer, "Darmstadt", 49.8588, 8.643);
//        map().addMapMarker(ebersheim);
		
   	 	try{
			SAXParser p = new SAXParser();
			if (setting.equals("-t")) p.load( ClassLoader.getSystemResourceAsStream(path) ); //loads up a smaller map for testing
			else if (setting.equals("-f")) p.load( new FileInputStream(path) ); //loads up all of uk!
			
			nodes = Grapher.reduce(p.getWays(), p.getNodes());
			ways = p.mapWayMap;
			links = Grapher.link(p.getWays(), nodes);
			routingGraph = Grapher.createGraph( nodes, links );
			sortedNodes = new ArrayList<MapNode>(nodes.values());
			Collections.sort(sortedNodes);
			/*
			//draw all markers
			for (MapWay way : p.getWays()){
				for (MapNode node : way.nodes){
					map().addMapMarker( new MapMarkerDot(fullNodes, ""+node.id, new Coordinate( node.lat, node.lon), new Style(Color.BLACK, Color.RED, null, new Font("Courier", Font.PLAIN,10)) ) );
					//map().addMapMarker( new MapMarkerDot(fullNodes, "", new Coordinate( node.lat, node.lon), new Style(Color.BLACK, Color.RED, null, new Font("Courier", Font.PLAIN,10)) ) );
		  	  	}
	  	  	}
			
			
			///draw reduced markers	
			for (MapNode node : nodes.values()){
				//System.out.println(node);
  	  			if(node.lat > 52.5813092 && node.lat < 52.675455  && node.lon > -1.2145779 && node.lon < -1.0584038) //map().addMapMarker( new MapMarkerDot(fullNodes, ""+node.id, new Coordinate( node.lat, node.lon), new Style(Color.BLACK, Color.YELLOW, null, new Font("Courier", Font.PLAIN,10)) ) );
				map().addMapMarker(new MapMarkerDot(node.lat, node.lon));
	  	  	}
			
			//draw reduced lines
			
			for(MapLink l : links){
				if(	l.n1.lat > 52.5813092 && l.n1.lat < 52.675455  && l.n1.lon > -1.2145779 && l.n1.lon < -1.0584038 &&
					l.n2.lat > 52.5813092 && l.n2.lat < 52.675455  && l.n2.lon > -1.2145779 && l.n2.lon < -1.0584038 ){
						Coordinate one = new Coordinate(l.n1.lat, l.n1.lon);
						Coordinate two = new Coordinate(l.n2.lat, l.n2.lon);
						List<Coordinate> r = new ArrayList<Coordinate>(Arrays.asList(one, two, two));
						map().addMapPolygon(new MapPolygonImpl(""+(l.distance*1000)+" m", r)); //shows distances - messy with a lot of points
						//if(l.oneWay) map().addMapPolygon(new MapPolygonImpl(">", route));
						//else map().addMapPolygon(new MapPolygonImpl(">", route));
				}
				
			}
			
			//*/
			
		}catch(Exception e){System.out.println(e);}
        // map.setDisplayPositionByLatLon(49.807, 8.6, 11);
        // map.setTileGridVisible(true);

        map().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    map().getAttribution().handleAttribution(e.getPoint(), true);
                }
            }
        });

        map().addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                boolean cursorHand = map().getAttribution().handleAttributionCursor(p);
                if (cursorHand) {
                    map().setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    map().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                if(showToolTip.isSelected()) map().setToolTipText(map().getPosition(p).toString());
            }
        });
    }
    private JMapViewer map(){
        return treeMap.getViewer();
    }
    private static Coordinate c(double lat, double lon){
        return new Coordinate(lat, lon);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // java.util.Properties systemProperties = System.getProperties();
        // systemProperties.setProperty("http.proxyHost", "localhost");
        // systemProperties.setProperty("http.proxyPort", "8008");
		String flag = "-t",path="map.xml";
		if (args.length==1) flag = args[0];
		if (args.length==2) {
			flag = args[0];
			path = args[1];
		}
        new GUI(flag, path).setVisible(true);
    }

    private void updateZoomParameters() {
        if (mperpLabelValue!=null)
            mperpLabelValue.setText(String.format("%s",map().getMeterPerPixel()));
        if (zoomValue!=null)
            zoomValue.setText(String.format("%s", map().getZoom()));
    }

    @Override
    public void processCommand(JMVCommandEvent command) {
        if (command.getCommand().equals(JMVCommandEvent.COMMAND.ZOOM) ||
                command.getCommand().equals(JMVCommandEvent.COMMAND.MOVE)) {
            updateZoomParameters();
        }
    }

}
