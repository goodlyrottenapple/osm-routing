SHELL=bash

map = "path-to-osm-file"

classes: FORCE
	rm -rf bin
	mkdir bin
	javac -d bin -cp lib/JMapViewer.jar:. `find src -maxdepth 1 -name "*.java"`

test: FORCE
	printf "\n\n\nThis is a GUI test environment adapted from a JMapViewer demo. It is purely for testing purposes and won't be used in the final implementation...Enjoy ;)\n\n\n"
	java -Xmx4g -cp bin:lib/JMapViewer.jar:xml:. GUI

tserver: FORCE
	java -Xmx4g -cp bin:lib/JMapViewer.jar:xml:. Server
	
server 1: FORCE
	java -Xmx4g -cp bin:lib/JMapViewer.jar:xml:. Server -f $(map)
	
client: FORCE
	java -cp bin:xml:. Client

full: FORCE
	printf "\n\n\nThis is a GUI test environment adapted from a JMapViewer demo. It is purely for testing purposes and won't be used in the final implementation...Enjoy ;)\n\n\n"
	java -Xmx4g -cp bin:lib/JMapViewer.jar:. GUI -f $(map)

clean: FORCE
	rm -rf bin

FORCE:
