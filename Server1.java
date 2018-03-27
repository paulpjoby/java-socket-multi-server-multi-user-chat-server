import java.io.*;
import java.net.*;
import java.util.*;

public class Server1{
	public ServerSocket server1 = null;
	public ServerSocket server1c = null;
	public Socket client = null;
	public Socket server2=null;
	public DataInputStream is= null;
	public DataOutputStream os = null;
	public HashMap<String,Socket> clientList = new HashMap<String,Socket>();
	public HashMap<String,String> users = new HashMap<String,String>();
	public FileInputStream fis ;
	public static void main(String[] arg){
		Server1 s = new Server1();
		s.doConnection();
	}
	public void doConnection(){
		try{
			int flag = 0; // denotes that thread not started yet
			// Read the users database file
			fis = new FileInputStream("server1.db");
			byte[] file = new byte[fis.available()];
			fis.read(file);
			String usersList = new String(file);
			StringTokenizer strTokenzier = new StringTokenizer(usersList,"#");
			while(strTokenzier.hasMoreTokens())
			{
				users.put(strTokenzier.nextToken(),strTokenzier.nextToken());
			}
			server1c = new ServerSocket(7777); // Server 1 Listens to Client through this port
			server1= new ServerSocket(6666); // Servers Communicate through these
			System.out.println("Waiting for Server 2 to be Online");
			// This Part is Different for each server 
			server2 = server1.accept();
			
			DataOutputStream sos = new DataOutputStream(server2.getOutputStream());
			DataInputStream sis = new DataInputStream(server2.getInputStream());
			Server1HandleMsg server1Handler = new Server1HandleMsg(sis,sos);
			System.out.println("Servers are Now Connect");
			while(true){
				client = server1c.accept();
				os = new DataOutputStream(client.getOutputStream());
				is = new DataInputStream(client.getInputStream());
				String req = is.readUTF(); // username#password <- this format is used
				String username = req.substring(0,req.indexOf("#"));
              			String password = req.substring(req.indexOf("#")+1);
				if(users.containsKey(username) && users.get(username).equals(password))
				{
					server1Handler.clientList.put(username,client);
					os.writeUTF("SUCCESS");
					if(flag == 0)
					{
						server1Handler.start();
					}
				}
				else
				{
					os.writeUTF("FAILED");
				}
			}
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
}

class Server1HandleMsg extends Thread{
	public HashMap<String,Socket> clientList = new HashMap<String,Socket>();
	public DataInputStream is,sis;
	public DataOutputStream os,sos;
	public String toClientName="",msg="";
	public Server1HandleMsg(DataInputStream serverInputStream,DataOutputStream serverOutputStream)
	{
		 sis=serverInputStream;
		 sos=serverOutputStream;
	}
	
	public void run()
	{
		while(true){
			try{
				if(clientList != null){
					for(String key: clientList.keySet())
					{
					    is= new DataInputStream(clientList.get(key).getInputStream());
					    if(is.available()>0)
					    {
						msg=is.readUTF();
						msg = msg + ";" + key; // msg = to;sub;msg;from;
						//System.out.println(msg);
						sos.writeUTF(msg); // just write the raw message to server 2 ;
					    }
					
					// any message from server 2
					if(sis.available()>0){
						String msgFromServer = sis.readUTF();
						//System.out.println(msgFromServer);
						StringTokenizer strTokenizer = new StringTokenizer(msgFromServer,";");
						toClientName = strTokenizer.nextToken();
						String msgSub = strTokenizer.nextToken();
						String msgBody = strTokenizer.nextToken();
						String from = strTokenizer.nextToken();
						System.out.println("To Client :" + toClientName);
						System.out.println("From :" + from);
						System.out.println("MESSAGE BODY:" + msgBody);
						if(clientList.containsKey(toClientName)){
						  	os = new DataOutputStream(clientList.get(toClientName).getOutputStream());
							os.writeUTF("From: " + from + "\nSub: " + msgSub + "\n" + "Message: " + msgBody);
						}
					 }
					}
				    }
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage());
				System.out.println("Server Recovered!");
			}
		}
	}
}
