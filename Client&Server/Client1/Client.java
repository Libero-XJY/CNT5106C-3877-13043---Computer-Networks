import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
public class Client {
	Socket requestSocket;           //socket connect to the server
	String message;                //message send to the server
	String MESSAGE;                //capitalized message read from the server
	public static void main(String args[])
	{
		Client client = new Client();
		client.run();
	}
	public Client() {}
	void run(){
		try{
			Scanner inn = new Scanner(System.in);
				System.out.println("Please input your IP and port number with this format:ftpclient IP port");
				String str=inn.nextLine();//read ip address and port number from key board
				while(true) {
					if(str.startsWith("ftpclient")) {
						break;
					}
					else if(str.startsWith("get")||str.startsWith("dir")||str.startsWith("upload")){
						System.out.println("Please connect to the socket first");
						String ftpcommand=inn.nextLine();
						str = ftpcommand;
						continue;
					}
					else{
						System.out.println("Please input correct ftpclient command");
						String str1=inn.nextLine();
						str = str1;
						continue;
					}
				}
				int i;
				String ipaddress;
				try {
					String [] c = str.split("\\s+");
					ipaddress = c[1];
					i = Integer.parseInt( c[2] );
					requestSocket = new Socket(ipaddress, i);
				}catch (ConnectException e) {
	    			System.err.println("Connection refused. You need to initiate a server first.");
	    			System.out.println("Please input correct IP/Port Number.");
	    			String retryftp=inn.nextLine();
						String [] c = retryftp.split("\\s+");
						ipaddress = c[1];
						i = Integer.parseInt( c[2] );
						requestSocket = new Socket(ipaddress, i);
					}
				boolean link = false;
				DataInputStream input = new DataInputStream(requestSocket.getInputStream());
				DataOutputStream out = new DataOutputStream(requestSocket.getOutputStream());
				link = input.readBoolean();// make sure link successfully
				if(link) {//link successfully
					System.out.println("Connected to "+ipaddress+" in port "+i);
					while(true) {
						System.out.println("Please input your username and password with this format: username@password");
						String username;
						String password;
						String usernamepassword=inn.nextLine();//read username and password from key board
						while(true) {
							if(usernamepassword.startsWith("ftp")) {
								System.out.println("You already connected to a socket, please login first");
								System.out.println("Please input your username and password with this format: username@password");
								String retry = inn.nextLine();
								usernamepassword = retry;
								continue;
							}
							else if(usernamepassword.startsWith("get")||usernamepassword.startsWith("dir")||usernamepassword.startsWith("upload")) {
								System.out.println("You have not login, please login");
								System.out.println("Please input your username and password with this format: username@password");
								String retry = inn.nextLine();
								usernamepassword = retry;
								continue;
							}
							else {
								while(true) {
									String [] s = usernamepassword.split("@");
									username = s[0];
									password = s[1];
									String unpw = username+"@"+password;
									out.writeUTF(unpw);
									boolean msg = input.readBoolean();
									if(msg) {
										break;
									}
									else {
										System.out.println("Username/Password is incorrect.");
										System.out.println("Please input correct username or password");
										String retry = inn.nextLine();
										usernamepassword = retry;
										continue;
									}
								}
								break;
							}
						}
	 							System.out.println("Login in successfully");
	 							while(true) {
									DataOutputStream out1 = new DataOutputStream(requestSocket.getOutputStream());
	 								System.out.println("Please input your command dir/get filename/upload filename");
	 	 							String command = inn.nextLine();
	 	 							String[] splited = command.split("\\s+");
	 	 							switch(splited[0]) {
	 	 							case"dir":
	 	 							out1.writeUTF(splited[0]);
	 	 							ObjectInputStream in = new ObjectInputStream(requestSocket.getInputStream());
	 	 							@SuppressWarnings("unchecked") ArrayList<String> arr = (ArrayList<String>) in.readObject();
	 	 							for(int length=1; length<arr.size();length++)
	 	 					    		System.out.println(arr.get(length));
	 	 								continue;
	 	 							case"get":
	 	 								out1.writeUTF(splited[0]);
	 	 								while(true) {
		 	 								out1.writeUTF(splited[1]);
		 	 								boolean exist = input.readBoolean();
		 	 								if(exist) {
		 	 									break;
		 	 								}
		 	 								else {
		 	 									System.out.println("The file you want to upload does not exist");
		 	 									System.out.println("Please input valid filename");
		 	 									String reinput = inn.nextLine();
		 	 									String [] retry = reinput.split("\\s+");
		 	 									splited[1]=retry[1];
		 	 									continue;
		 	 								}
	 	 								}
	 	 								int length = input.readInt();
	 	 								saveFile(splited[1], length);
	 	 								continue;
	 	 							case"upload":
	 	 								while(true) {
		 	 								File testfile = new File(splited[1]);
		 	 								if(testfile.exists()) {
		 	 									out1.writeUTF(splited[0]);
			 	 								out1.writeUTF(splited[1]);
			 	 								sendFile(splited[1]);
			 	 								break;
		 	 								}
		 	 								else {
		 	 									System.out.println("The file you want to upload does not exist");
		 	 									System.out.println("Please input valid filename");
		 	 									String reinput = inn.nextLine();
		 	 									String [] retry = reinput.split("\\s+");
		 	 									splited[1]=retry[1];
		 	 									continue;
		 	 								}
	 	 								}
										continue;
	 	 							case"ftpclient":
	 	 								System.out.println("You already connected to a socket");
	 	 								continue;
	 	 							default:
	 	 								System.out.println("The command you input is invalid.");
	 	 								continue;
	 							}
	 							}
					}
				}

		}
		catch(IOException ioException){
			ioException.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally{
			//Close connections
			try{
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}
	void sendFile(String file) throws IOException {
		DataOutputStream dos = new DataOutputStream(requestSocket.getOutputStream());
		FileInputStream fis = new FileInputStream(file);
		byte[] buffer = new byte[4096];
		
		while (fis.read(buffer) > 0) {
			dos.write(buffer);
		}
		System.out.println(file+" is sent successfully.");
	}
		void saveFile(String filename,int length) throws IOException {
		DataInputStream dis = new DataInputStream(requestSocket.getInputStream());
		FileOutputStream fos = new FileOutputStream(filename);
		byte[] buffer = new byte[4096];
		int filesize = 300000; // Send file size in separate msg
		int read = 0;
		int totalRead = 0;
		int remaining = filesize;
		while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
			totalRead += read;
			remaining -= read;
			fos.write(buffer, 0, read);
			if(totalRead >=length)
				break;
			else
				continue;
		}

	}
}
