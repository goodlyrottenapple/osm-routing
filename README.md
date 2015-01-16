# OpenStreetMap routing implementation in Java

This was my contribution to a university group project. The code includes a client and server side. 
OpenStreetMap data in XML format is loaded from the 'xml' folder. The implementation includes several different routing algorithms (Dijkstra, AStar and bidirectional Dijkstra and AStar) as well as a GUI testing utility using JMapViewer (http://wiki.openstreetmap.org/wiki/JMapViewer)

##Usage
Add the path to an osm map file in the Makefile `map = "path-to-osm-file"`

To compile, cd into the project directory and run: `make classes`

To test using the GUI with the small sample map included in the xml folder, run `make test`

To test the server/client implementation, run `make tserver` or `make server` (depending on which map file you want to use; tserver will use the sample map whilst sever will load the user specified osm file) in one terminal window and `make client` in another
