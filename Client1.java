import java.io.*;
import java.net.*;
import java.util.*;

// SERVER 1 LISTENS AT 7777 FOR CLIENT
// SERVER 2 LISTENS AT 9999 FOR CLIENT
// SERVERS CONNECT THROUGH 6666
public class Client1{
	public Socket client = null;
	public DataInputStream is= null;
	public DataOutputStream os = null;
	public static void main(String[] arg){
		Client1 c = new Client1();
		c.doConnection();
	}

	public void doConnection(){
		try{
			String username;
			String password;
 		        String serverResponse;
			System.out.println("Client");
			client = new Socket("127.0.0.1",7777); // CLIENT 1 CONNECTS TO SERVER 1
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(isr);
			System.out.println("Enter username :"); 
			username = br.readLine();
			System.out.println("Enter password :");
			password=br.readLine();
        		os = new DataOutputStream(client.getOutputStream());
			os.writeUTF(username+"#"+password);
        		is = new DataInputStream(client.getInputStream());
        		Client1MsgReadThread fetchMsg = new Client1MsgReadThread(is);
        		Client1MsgWriteThread sendMsg = new Client1MsgWriteThread(os);
			serverResponse = is.readUTF();
			if(serverResponse.equals("SUCCESS"))
			{
                		// START READING AND WRITING MESSAGE
                		System.out.println("Authentication Successful!");
                		fetchMsg.start();
                		sendMsg.start();
                		fetchMsg.join();
                		sendMsg.join();
			}
			else
			{
				System.out.println("Authentication Failed !");
			}
		}
		catch(Exception e)
                {
			System.out.println(e);
	        }
	}

}
class Client1MsgReadThread extends Thread{
	public DataInputStream is = null;
	public String msg;
	public Client1MsgReadThread(DataInputStream i){
		is=i;
	}
	public void run(){
		while(true){
		try{	
			if(is.available()>0)
		  	{
			msg = is.readUTF();
			System.out.println("\nRecieved A Mail\n---------------");
			System.out.println(msg);
			}
		    }
		catch(Exception e)
		{}
	        }	
	}
}
class Client1MsgWriteThread extends Thread{
	public DataOutputStream os = null;
	public String msg;
	public String mail;
	InputStreamReader isr = null;
	BufferedReader br = null;
	public Client1MsgWriteThread(DataOutputStream o){
		os=o;
		isr = new InputStreamReader(System.in);
		br = new BufferedReader(isr);
	}
	public void run(){
		while(true){
		   try{
			System.out.println("\n\nType 'create' to create a new mail");
			msg = br.readLine();
			if(msg.equals("create"))
			{
			    writeMail();
			}
		   }
		   catch(Exception e)
		   {

		   }
		}
	}
	// THIS METHOD WRITES THE MAIL AND SENDS TO SERVER
	public void writeMail(){
		// MAIL FORMAT : TO;SUB;MSGBODY
		mail="";
		try{
			System.out.println("\nTo :");
			mail = br.readLine();
			System.out.println("\nSub : ");
			mail = mail + ";" + br.readLine();
			System.out.println("\nMessage :");
			mail += ";" + br.readLine();
		//	System.out.println("MAIL PACKET" + mail); // Line used for testing
			os.writeUTF(mail);
		}
		catch(Exception e){

		}
	}
}