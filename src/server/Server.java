package server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.FileReader;
import java.io.BufferedReader;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

import client.Client;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import java.util.*;

import javax.net.ServerSocketFactory;

public class Server {

	@Option(name="-p", usage="Port number", required=true)
	private static int port;

	//@Option(name="-d", usage="Dictionary file dir")
	//private static String dictionary_dir;

	@Argument
	private List<String> arguments= new ArrayList<String>();


	
	// Declare the port number
	//private static int port = 3005;
	
	// Identifies the user number connected
	private static int counter = 0;

	// Create a map to store the dictionary

	public static void main(String[] args) throws IOException {
		new Server().doMain(args);
		// Create a map to store the dictionary
		Map dictionary = new HashMap();

		//Read the data from the dictionary file

		ServerSocketFactory factory = ServerSocketFactory.getDefault();
		
		try(ServerSocket server = factory.createServerSocket(port))
		{
			System.out.println("Waiting for client connection-");
			
			// Wait for connections.
			while(true)
			{
				Socket client = server.accept();
				counter++;
				System.out.println("Client "+counter+": Applying for connection!");
							
				// Start a new thread for a connection
				Thread t = new Thread(() -> serveClient(client, counter));
				t.start();
			}
			
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}

	public void doMain(String[] args) throws IOException {
		CmdLineParser parser = new CmdLineParser(this);

		// if you have a wider console, you could increase the value;

		try {
			// parse the arguments.
			parser.parseArgument(args);

			// you can parse additional arguments if you want.
			// parser.parseArgument("more","args");

			// after parsing arguments, you should check
			// if enough arguments are given.
			if( arguments.isEmpty() )
				throw new CmdLineException(parser,"No argument is given");

		} catch( CmdLineException e ) {
			// if there's a problem in the command line,
			// you'll get this exception. this will report
			// an error message.
			System.err.println(e.getMessage());
			System.err.println("java SampleMain [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("  Example: java SampleMain"+parser.printExample(ALL));

			return;
		}

		// access non-option arguments
		System.out.println("other arguments are:");
		for( String s : arguments )
			System.out.println(s);
	}

	private static void serveClient(Socket client, int counter)
	{
		try(Socket clientSocket = client)
		{
			// Input stream
			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
			// Output Stream
		    DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
		    int clinetNum = counter;
		    System.out.println("CLIENT: "+input.readUTF());
		    
		    output.writeUTF("Server: Hi Client "+clinetNum+" !!!");
		    output.writeUTF("Please enter the word to search");

		    //TODO: Set a timeout to disconnect
			while(true)
			{
				try{
					String word = input.readUTF();
					System.out.println("Client "+clinetNum+": "+word);
				}
				catch (Exception e)
				{
					System.out.println(e);
					break;
				}

			}

		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

}
