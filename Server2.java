import java.io.*;
import java.net.*;
import java.util.*;

public class Server2{
	public ServerSocket server2 = null;
	public Socket client = null;
	public Socket server1=null;
	public DataInputStream is= null;
	public DataOutputStream os = null;
	public FileInputStream fis = null;
	public HashMap<String,Socket> clientList = new  HashMap<String,Socket>();
 	public HashMap<String,String> users = new HashMap<String,String>();
	
	public static void main(String[] arg){
		Server2 s = new Server2();
		s.doConnection();
	}

	public void doConnection(){
		try{
			//reading server2.db
			int flag = 0; // denotes that thread not started yet
			// Read the users database file
			fis = new FileInputStream("server2.db");
			byte[] file = new byte[fis.available()];
			fis.read(file);
			String usersList = new String(file);
			StringTokenizer strTokenzier = new StringTokenizer(usersList,"#");
			while(strTokenzier.hasMoreTokens())
			{
				users.put(strTokenzier.nextToken(),strTokenzier.nextToken());
			}

			server2 = new ServerSocket(9999); // Server 2 Listens to for clients
			System.out.println("Listening for Client ... ");
			 
			server1 = new Socket("127.0.0.1",6666); // connect to Server1
		    System.out.println("Connected To Server1");
		 	// Servers connected
			// Now handle Clients 
			
			DataOutputStream sos = new DataOutputStream(server1.getOutputStream());
			DataInputStream sis = new DataInputStream(server1.getInputStream());
			Server2HandleMsg server2Handler = new Server2HandleMsg(sis,sos);
			System.out.println("Servers are Connect");

			while(true){
				client = server2.accept();
				os = new DataOutputStream(client.getOutputStream());
				is = new DataInputStream(client.getInputStream());
				String req = is.readUTF(); // username#password <- this format is used
				String username = req.substring(0,req.indexOf("#"));
              	String password = req.substring(req.indexOf("#")+1);
				if(users.containsKey(username) && users.get(username).equals(password))
				{
					server2Handler.clientList.put(username,client);
					os.writeUTF("SUCCESS");
					if(flag == 0)
					{
						server2Handler.start();
					}
				}
				else
				{
					os.writeUTF("FAILED");
				}
			}
		}
		catch(Exception e)
        {

		}
	}
}

class Server2HandleMsg extends Thread{
	public HashMap<String,Socket> clientList = new HashMap<String,Socket>();
	public DataInputStream is,sis;
	public DataOutputStream os,sos;
	public String toClientName="",msg="";
	public Server2HandleMsg(DataInputStream serverInputStream,DataOutputStream serverOutputStream)
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