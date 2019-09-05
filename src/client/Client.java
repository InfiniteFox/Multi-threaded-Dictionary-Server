package client;
import server.Server;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.activation.UnknownObjectException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/*** Client
 * @author Sihan Peng
 */

public class Client {
	
	// IP and port
	private static String ip = "localhost";
	private static int port = 3005;
	private static Socket socket;
	private static List<String> Actions = new ArrayList<String>();
	private static DataInputStream input;
	private static DataOutputStream output;


	public static void main(String[] args)
	{
		Client myClient = new Client();
		myClient.setActions();
		try{
			myClient.establishSocket();
		}
		catch (UnknownHostException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String msg;
		myClient.sendMsg("Query", "Hello");
		System.out.println(1);
		myClient.sendMsg("Delete", "Hello");
		myClient.sendMsg("Add", "Hello", "Greeting");

		try{
			myClient.sendMsg("ERROR", "Hello");
		}catch (IllegalArgumentException e){
			System.out.println(e.getMessage());
		}

		myClient.closeSocket();

	}


	public static void establishSocket() throws IOException {
		try
		{
			socket = new Socket(ip, port);
			// Output and Input Stream
			input = new DataInputStream(socket.getInputStream());

			output = new DataOutputStream(socket.getOutputStream());
			String sendData ="I want to connect";
			output.writeUTF(sendData);
			System.out.println("Data sent to Server--> " + sendData);
			output.flush();
			while (true){
				if (input.available()>0){
					String message = input.readUTF();
					System.out.println(message);
					break;
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw e;
		}
	}

	public static void closeSocket(){
		try{
			socket.close();
			//System.out.println("Socket Close");
		}
		catch (IOException e) {
			e.printStackTrace();
		}


	}

	public void setActions(){
		Actions.add("Query");
		Actions.add("Add");
		Actions.add("Delete");
	}

	public static String[] sendMsg(String... msg){
		String[] result = new String[2];
		if (msg.length <= 1){
			throw new IllegalArgumentException("Message Missing");
		}
		String action = msg[0];
		String send = parseSendMsg(msg);
		try{
			output.writeUTF(send);
			return receiveMsg(action);
		} catch (IOException e) {
			e.printStackTrace();
			result[0] = "Failure";
			result[1] = e.getMessage();
			return result;
		}
	}

	private static String parseSendMsg(String... msg){
		//Use JSON to pass command
		JSONObject newCommand = new JSONObject();
		switch (msg[0]){
			case "Query":
			case "Delete":
				newCommand.put("Action", msg[0]);
				newCommand.put("Word",  msg[1]);
				System.out.println(newCommand.toJSONString());
				return newCommand.toJSONString();
			case "Add":
				if (msg.length <= 2){
					throw new IllegalArgumentException("Meaning Missing");
				}
				newCommand.put("Action", msg[0]);
				newCommand.put("Word",  msg[1]);
				newCommand.put("Meaning", msg[2]);
				//System.out.println(newCommand.toJSONString());
				return newCommand.toJSONString();
		}
		throw new IllegalArgumentException("Message Missing");
	}

	private static String[] receiveMsg(String action){
		String[] result = new String[2];
		try{
			while (true){
				if (input.available()>0){
					String msg = input.readUTF();
					result = parseReceiveMsg(msg, action);
					//System.out.println(msg);
					return result;
				}
			}
		}
		catch (IOException e){
			e.printStackTrace();
			result[0] = "Failure";
			result[1] = e.getMessage();
			return result;
		}
	}

	private static String[] parseReceiveMsg(String msg, String action){
		// The JSON Parser
		JSONParser parser = new JSONParser();
		JSONObject newCommand = new JSONObject();
		String[] result = new String[2];
		result[0] = "Failure";
		try {
			JSONObject command = (JSONObject) parser.parse(msg);
			if (command.containsKey("Result")){
				switch ((String) command.get("Result")){
					//Case 0: Action Success
					case "Success":
						result[0] = "Success";
						switch (action){
							case "Query":
								result[1] = (String) command.get("Meaning");
								return result;
							case "Add":
							case "Delete":
								return result;
						}
					default:
						result[0] = "Failure";
						result[1] = (String) command.get("Result");
						return result;
				}
			}
		}catch (ParseException e){
			e.printStackTrace();
			result[1] = e.getMessage();
		}
		return result;
	}


}
