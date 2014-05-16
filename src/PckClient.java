package pckwlf.java;
/* The method functionality should be proper. Change the way input is processed so that you don't have to put in
 * the pckName every time and it will use the default one thats present in the object.
 */
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.nio.ByteBuffer;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class PckClient {
	private final byte TASK_BYTE = 6;
	private final String MULTICAST_ADDRESS = "228.5.6.7";
	private final int MULTICAST_PORT = 22699;
	private final int TCP_PORT = 22710;
	private final String LOGIN_TAG = "pckwlf-reserved";

	private boolean loggedIn;
	private static String pckName;
	private InetAddress MC_IADDRESS;
	private int uniqueifier = 0;
	private DatagramSocket datagramSocket;
	private ServerSocket servSocket;
	private Socket tcpSocket;
	private ObjectInputStream in;
	private ObjectOutputStream out;


	public static void main(String[] args) throws IOException, ClassNotFoundException {
		PckClient client = new PckClient();
		String input = "";
		Scanner scan = new Scanner(System.in);
		Response response = null;
		for(;;) {
			System.out.println("login, signup or exit");
			input = scan.nextLine();
			if (input.equalsIgnoreCase("login")) {
				System.out.println("Username: ");
				String pckN = scan.nextLine();
				System.out.println("Password: ");
				String pw = scan.nextLine();
				response = client.login(pckN, pw);
				if(response.getType() == RespType.FAILURE) {
					System.out.println("Incorrect username/password");
					continue;
				} else if(response.getType() == RespType.SUCCESS) {
					for (;;) {
						System.out.println("Enter operation (add, update, remove, get, getAll, logout):");
						String op = scan.nextLine();
						if (op.equalsIgnoreCase("add")) {
							System.out.println("Tag:");
							String tag = scan.nextLine();
							System.out.println("Username");
							String un = scan.nextLine();
							System.out.println("Password");
							pw = scan.nextLine();
							response = client.add(tag, un, pw);
							client.processResponse(response);
						} else if (op.equalsIgnoreCase("update")) {
							System.out.println("Tag:");
							String tag = scan.nextLine();
							System.out.println("Username");
							String un = scan.nextLine();
							System.out.println("Password");
							pw = scan.nextLine();
							response = client.update(tag, un, pw);
							client.processResponse(response);
						} else if (op.equalsIgnoreCase("remove")) {
							System.out.println("Tag:");
							String tag = scan.nextLine();
							response = client.remove(tag);
							client.processResponse(response);
						} else if (op.equalsIgnoreCase("get")) {
							System.out.println("Tag:");
							String tag = scan.nextLine();
							response = client.get(tag);
							client.processResponse(response);
						} else if (op.equalsIgnoreCase("getAll")) {
							response = client.getAll();
							client.processResponse(response);
						} else if (op.equalsIgnoreCase("logout")) {
							client.logout();
							break;
						}
					}
				}
			} else if (input.equalsIgnoreCase("signup")) {
				System.out.println("Desired Username:");
					String pckN = scan.nextLine();
				System.out.println("Password:");
				String pw = scan.nextLine();
				response = client.signUp(pckName, pw);
				if(response.getType() == RespType.SUCCESS) {
					System.out.println("Pckwlf account created.");
					continue;
				}
			} else if(response.getType() == RespType.FAILURE) {
				System.out.println("Username already in use");
				continue;
			} else if (input.equalsIgnoreCase("exit")) {
				System.exit(0);
			}
		}
/*
		for(;;) {
			String op = "";
			String tag = "";
			String un = "";
			String pw = "";
			System.out.println("Enter Operation (add, signup, update, remove, get, getAll, logout):");
			input = scan.nextLine();
			System.out.println("ops: sig, add, upd, rem, get, gal");
			System.out.println("pckName,operation,tag,username,password");
			System.out.println("Input String to send:");
			input = scan.nextLine();
			String[] split = input.split(",");
			if (split.length != 5) {
				System.out.println("put all parameters pls");
				continue;
			} else {
				pckName = split[0];
				op = split[1];
				tag = split[2];
				un = split[3];
				pw = split[4];
			}
			if ("log".equalsIgnoreCase(op)) {
				response = client.login(pckName, pw);
			} else if ("sig".equalsIgnoreCase(op)) {
				response = client.signUp(pckName, pw);
			} else if ("add".equalsIgnoreCase(op)) {
				response = client.add(tag, un, pw);
			} else if ("upd".equalsIgnoreCase(op)) {
				response = client.update(tag, un, pw);
			} else if ("rem".equalsIgnoreCase(op)) {
				response = client.remove(tag);
			} else if ("get".equalsIgnoreCase(op)) {
				response = client.get(tag);
			} else if ("gal".equalsIgnoreCase(op)) {
				response = client.getAll();
			} else if("exit".equalsIgnoreCase(op)){
				System.out.println("Goodbye");
				client.logout();
				break;
			} else {
				System.out.println("not a valid op");
				continue;
			}

			if(response.getType() == RespType.FAILURE)
				System.out.println("FAIL");
			else if(response.getType() == RespType.SUCCESS)
				System.out.println("SUCC");
			else if(response.getType() == RespType.RESULTS){
				System.out.println("RESULTS");
				for(Map.Entry entry : response.getResults().entrySet())
					System.out.println(entry.getKey()+","+entry.getValue());
			} else if(response.getType() == RespType.ERROR){
				System.out.println("ERR");
				System.out.println(response.getMessage());
			}
		}
*/
	}

	public void processResponse(Response response) {
		if(response.getType() == RespType.FAILURE)
			System.out.println("FAIL");
		else if(response.getType() == RespType.SUCCESS)
			System.out.println("SUCC");
		else if(response.getType() == RespType.RESULTS){
			System.out.println("RESULTS");
			for(Map.Entry entry : response.getResults().entrySet())
				System.out.println(entry.getKey()+","+entry.getValue());
		} else if(response.getType() == RespType.ERROR){
			System.out.println("ERR");
			System.out.println(response.getMessage());
		}
	}

	public PckClient(){
		this.loggedIn = false;
	}

	public void logout(){
		this.loggedIn = false;
		this.pckName = null;
	}

	public Response login(String pckName, String password) throws IOException, ClassNotFoundException {
		try{
			requestConnection();
			Request req = new Request(LOGIN_TAG, pckName, password, ReqType.VERIFY);
			Response resp = sendRequest(req);
			if (resp.getType() == RespType.SUCCESS) {
				this.pckName = pckName;
				this.loggedIn = true;
			} else if (resp.getType() == RespType.FAILURE) {
				System.out.println("Username/password combination invalid");
			}
			return resp;
		} catch(IOException ioe){
			closeSockets();
			return new Response(RespType.ERROR,"IO Exception error");
		}
	}

	public Response signUp(String pckName, String password) throws IOException, ClassNotFoundException {
		try{
			requestConnection();
			Request req = new Request(LOGIN_TAG, pckName, ReqType.VERIFY);
			Response resp = sendRequest(req);
			if (resp.getType() == RespType.SUCCESS) {
				System.out.println("That username is already taken");
				return new Response(RespType.FAILURE,"Username taken");
			} else if (resp.getType() == RespType.FAILURE) {
				return add(LOGIN_TAG, pckName, password);
			}
			return resp;
		} catch(IOException eio){
			return new Response(RespType.ERROR,"IO Exception error");
		}
	}

	public Response add(String tag, String username, String password) throws IOException, ClassNotFoundException {
		if(!loggedIn) return new Response(RespType.ERROR,"Not Logged in Error");
		try{
			requestConnection();
			Request req = new Request(tag, pckName, username, password, ReqType.ADD);
			return sendRequest(req);
		} catch(IOException oei){
			closeSockets();
			return new Response(RespType.ERROR,"IO Exception error");
		}
	}

	public Response update(String tag, String username, String password) throws IOException, ClassNotFoundException {
		if(!loggedIn) return new Response(RespType.ERROR,"Not Logged in Error");
		try{
			requestConnection();
			Request req = new Request(tag, pckName, username, password, ReqType.UPDATE);
			return sendRequest(req);
		} catch(IOException eoe){
			closeSockets();
			return new Response(RespType.ERROR,"IO Exception error");
		}
	}

	public Response remove(String tag) throws IOException, ClassNotFoundException {
		if(!loggedIn) return new Response(RespType.ERROR,"Not Logged in Error");
		try{
			requestConnection();
			Request req = new Request(tag, pckName, ReqType.REMOVE);
			return sendRequest(req);
		} catch(IOException ioe){
			closeSockets();
			return new Response(RespType.ERROR,"IO Exception error");
		}
	}

	public Response get(String tag) throws IOException, ClassNotFoundException {
		if(!loggedIn) return new Response(RespType.ERROR,"Not Logged in Error");
		try{
			requestConnection();
			Request req = new Request(tag, pckName, ReqType.GET);
			return sendRequest(req);
		} catch(IOException ioe){
			closeSockets();
			return new Response(RespType.ERROR,"IO Exception error");
		}
	}

	public Response getAll() throws IOException, ClassNotFoundException {
		if(!loggedIn) return new Response(RespType.ERROR,"Not Logged in Error");
		try{
			requestConnection();
			Request req = new Request("DOESNTMATTER", pckName, ReqType.GETALL);
			return sendRequest(req);
		} catch(IOException ioe){
			closeSockets();
			return new Response(RespType.ERROR,"IO Exception error");
		}
	}

	public void requestConnection() throws IOException {
		DatagramPacket reqPacket = makeTaskPacket();
		openDatagramSocket();
		datagramSocket.send(reqPacket);
		openTCPSocket();
	}

	public Response sendRequest(Request r) throws IOException, ClassNotFoundException {
		String initResp = (String) in.readObject();
		out.writeObject(r);
		Response response = (Response)in.readObject();
		closeSockets();
		return response;
	}

	public DatagramPacket makeTaskPacket() throws UnknownHostException {
		byte[] buf = new byte[5];	
		buf[0] = TASK_BYTE;
		byte[] uniqueBytes = intToByteArray(uniqueifier); 
		System.arraycopy(uniqueBytes, 0, buf, 1, uniqueBytes.length);
		uniqueifier++;
		MC_IADDRESS = InetAddress.getByName(MULTICAST_ADDRESS);
		return (new DatagramPacket(buf, buf.length, MC_IADDRESS, MULTICAST_PORT));
	}

	public void openDatagramSocket() throws IOException {
		datagramSocket = new DatagramSocket();
	}

	public void openTCPSocket() throws IOException {
		servSocket = new ServerSocket(TCP_PORT);	
		servSocket.setSoTimeout(5000);
		try {
			tcpSocket = servSocket.accept();
			in = new ObjectInputStream(tcpSocket.getInputStream());
			out = new ObjectOutputStream(tcpSocket.getOutputStream());
		} catch (SocketTimeoutException e) {
			servSocket = null;
			tcpSocket = null;
			return;
		}
	}

	public void closeSockets() throws IOException {
		datagramSocket.close();
		tcpSocket.close();
		servSocket.close();
	}

	private byte[] intToByteArray(int i) {
		return ByteBuffer.allocate(4).putInt(i).array();
	}

	private int byteArrayToInt(byte[] b) {
		return ByteBuffer.allocate(4).put(b).getInt(0);
	}
}
