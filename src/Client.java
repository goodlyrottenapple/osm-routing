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
public class Client {
	public static void main(String[] args) throws Exception {
		String input="52.6252420,-1.1085937;52.6213092,-1.1245779";
		if (args.length==1) input = args[0];
		System.out.println("input:" + input);
		Socket c = new Socket("localhost",9000);

	    BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
		PrintWriter out=new PrintWriter(c.getOutputStream());
		
		out.print(input+"\n");
		out.flush();
		
	    String response = in.readLine();
	    System.out.println(response);
	  }
	  
  	/**
  	 * Creates a new socket connection, passes the server coordinates for routing and waits + returns the response as string 
  	 */
	public static String route(String coords) {
		try{
			Socket c = new Socket("localhost",9000);
			BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
			PrintWriter out=new PrintWriter(c.getOutputStream());
			out.print(coords+"\n");
			out.flush();
			return in.readLine();
		} catch (IOException e) {}
		return null;
	}
}