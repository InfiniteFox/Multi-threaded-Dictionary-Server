package server;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

import client.Client;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.net.SocketTimeoutException;
import java.util.*;

import javax.net.ServerSocketFactory;
import javax.swing.*;

/*** Server
 * @author Sihan Peng
 */

public class Server {

	@Option(name="-p", aliases="--port", usage="Port number", required=true)
	private static int port;

	@Option(name="-f", aliases="--file", usage="Dictionary file dir")
	private static String dictionary_dir;
	
	// Identifies the user number connected
	private static int counter = 0;

	// Create a map to store the dictionary
	private static Map dict = new HashMap<String, String>();
	private static String dir = "dictionary.csv";
	private static List<String> Actions = new ArrayList<String>();

	public static void main(String[] args) throws IOException {
		Server myServer = new Server();
		try{
			myServer.doMain(args);
		} catch (CmdLineException e) {
			e.printStackTrace();
			return;
		}


		//Read the data from the dictionary file
		myServer.loadDictionary(dir);
		System.out.println(dict);

		ServerSocketFactory factory = ServerSocketFactory.getDefault();
		try(ServerSocket server = factory.createServerSocket(port))
		{
			// Wait for connections.
			while(true&counter<10)
			{
				Socket client = server.accept();
				counter++;
				System.out.println("Client "+counter+": Applying for connection!");
				// Start a new thread for a connection
				Thread t = new Thread(() -> serveClient(client, counter, myServer));
				t.start();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		System.out.println("Exit");
		myServer.storeDictionary(dir);
		
	}



	private void doMain(String[] args) throws CmdLineException {
		CmdLineParser parser = new CmdLineParser(this);

		// if you have a wider console, you could increase the value;

		try {
			// parse the arguments.
			parser.parseArgument(args);

			// you can parse additional arguments if you want.
			// parser.parseArgument("more","args");

			// after parsing arguments, you should check
			// if enough arguments are given.
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
			throw e;
		}
	}

	private static void serveClient(Socket client, int counter, Server myServer)
	{
		try(Socket clientSocket = client)
		{
			// Input stream
			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
			// Output Stream
		    DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
		    //Set Time out at 1 second
			client.setSoTimeout(1000);
			System.out.println("Client "+counter+": "+input.readUTF());
		    
		    output.writeUTF("Server: Hi Client "+ counter +" !!!");
		    //output.writeUTF("Please enter the word to search");

			System.out.println("Start Listen");
			while(true)
			{
				if(input.available()>0){
					String msg = input.readUTF();
					System.out.println("[Receive]Client "+ counter +": "+msg);
					String result = myServer.msgReader(msg);
					output.writeUTF(result);
					System.out.println("[Send]Client "+ counter +": "+result);
					output.flush();
					clientSocket.close();
					System.out.println("[Close]Client "+ counter +": Close");
					break;
				}
			}

		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	private void setActions(){
		Actions.add("Query");
		Actions.add("Add");
		Actions.add("Delete");
	}


	private String msgReader(String msg)  {
		// The JSON Parser
		JSONParser parser = new JSONParser();
		JSONObject newCommand = new JSONObject();
		try{
			JSONObject command = (JSONObject) parser.parse(msg);

			if (command.containsKey("Action") & command.containsKey("Word")){
				String action = (String) command.get("Action");
				String word = (String) command.get("Word");
				switch (action)
				{
					case "Query":

						if (dict.containsKey(word)){
							newCommand.put("Result", "Success");
							newCommand.put("Meaning",dict.get(word));
							return newCommand.toJSONString();
						}
						else{
							newCommand.put("Result", "Failure: Word Not Exist");
							return newCommand.toJSONString();
						}
					case "Add":
						if (command.containsKey("Meaning")){
							String meaning = (String) command.get("Meaning");
							if (meaning.length()>0){
								//Add word meaning
								if (dict.containsKey(word)){
									//Existed word, cannot add
									newCommand.put("Result", "Failure: Word Already Exist");
									return newCommand.toJSONString();
								}
								else{
									//New word, add meaning
									dict.put(word, meaning);
									newCommand.put("Result", "Success");
									return newCommand.toJSONString();
								}
							}
						}
						//No meaning attached, cannot add meaning
						newCommand.put("Result", "Failure: No Meaning Attached");
						return newCommand.toJSONString();

					case "Delete":
						if (dict.containsKey(word)){
							//Word exist
							dict.remove(word);
							newCommand.put("Result", "Success");
							return newCommand.toJSONString();
						}
						else{
							//Word not exist
							newCommand.put("Result", "Failure: Word Not Exist");
							return newCommand.toJSONString();
						}
				}
				//For action not found
				newCommand.put("Result", "Failure: Action Not Found");
				return newCommand.toJSONString();
			}
			//For no action or no word
			newCommand.put("Result", "Failure: Action/Word Not Found");
			return newCommand.toJSONString();
		}
		catch (ParseException e){
			e.printStackTrace();
			//Exception
			newCommand.put("Result", "Failure: Message Cannot Read");
			return newCommand.toJSONString();
		}
	}

	private boolean loadDictionary(String dir){
		try (BufferedReader br = new BufferedReader(new FileReader(dir))){
			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				dict.put(values[0], values[1]);
			}
			br.close();
			System.out.println("Dictionary Load Success");
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Error: Dictionary Load Fail: File Not Found");
			storeDictionary(dir);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error: Dictionary Load Fail: IO Exception");
		}
		return false;
	}

	private void storeDictionary(String dir){
		//TODO: Use temp file to store, then cover the original file
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(dir))){
			bw.write("Word,Meaning\n");
			Iterator<Map.Entry<String, String>> entries = dict.entrySet().iterator();
			while (entries.hasNext()){
				Map.Entry<String, String> entry = entries.next();
				bw.write(entry.getKey()+","+entry.getValue());
				bw.write("\n");
			}
			bw.close();
			System.out.println("Dictionary Store Success");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
