import java.net.*;
import java.io.*;
import java.nio.*;
import java.util.*;

public class Server {

	private static final int sPort = 8000;   //The server will be listening on this port number
	private static final ArrayList<String> arr=new ArrayList<String>();

	public static void main(String[] args) throws Exception {
		System.out.println("The server is running."); 
        	ServerSocket listener = new ServerSocket(sPort);
		int clientNum = 1;
        	try {
            		while(true) {
                		new Handler(listener.accept(),clientNum).start();
				System.out.println("Client "  + clientNum + " is connected!");
				clientNum++;
            			}
        	} finally {
            		listener.close();
        	} 
 
    	}

	/**
     	* A handler thread class.  Handlers are spawned from the listening
     	* loop and are responsible for dealing with a single client's requests.
     	*/
    	private static class Handler extends Thread {
		private Socket connection;
		private int no;		//The index number of the client

        	public Handler(Socket connection, int no) {
            		this.connection = connection;
	    		this.no = no;
        	}

        public void run() {
 		try{
			//initialize Input and Output streams
 			DataInputStream input = new DataInputStream(connection.getInputStream());
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.writeBoolean(true);
			String username;
			while(true) {
				String unpw = input.readUTF();
				String [] c = unpw.split("@");
				if("poloyo".equals(c[0])&&"xjy".equals(c[1])) {
					username = c[0];
					out.writeBoolean(true);
					break;
				}
				else {
					out.writeBoolean(false);
					continue;
				}
			}
				System.out.println("hello, "+username+"!");
				while(true) {
					String command = input.readUTF();
					switch(command) {
					case"dir":
						int num=0;
						showDir(1, new File(System.getProperty("user.dir")),num);
						ObjectOutputStream out1 = new ObjectOutputStream(connection.getOutputStream());
						out1.writeObject(arr);
						continue;
					case"get":
						String transferfilename;
						File testfile;
						while(true) {
							transferfilename = input.readUTF();
							testfile = new File(transferfilename);
							if(testfile.exists()) {
								out.writeBoolean(true);
 								break;
							}		
							else {
								out.writeBoolean(false);
								continue;
								}
							}
						long testfilelength = testfile.length();
						int length = Math.toIntExact(testfilelength);
						out.writeInt(length);
						sendFile(connection,transferfilename);
						continue;
					case"upload":
						String savefilename = input.readUTF();
						saveFile(connection,savefilename);
						out.writeBoolean(true);
						continue;
				}
				}
		}
		catch(IOException ioException){
			System.out.println("Disconnect with Client " + no);
		}
		finally{
			try{
				connection.close();
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Client " + no);
			}
		}
	}
    static void showDir(int indent, File file,int num) throws IOException {
    	arr.add(num++,file.getName());
        if (file.isDirectory()) {
        	File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++)
            	showDir(indent + 4, files[i],num);
            }
        }
	void saveFile(Socket clientSock, String filename) throws IOException {
		DataInputStream dis = new DataInputStream(clientSock.getInputStream());
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
		}

	}
		void sendFile(Socket clientSock, String file) throws IOException {
		DataOutputStream dos = new DataOutputStream(clientSock.getOutputStream());
		FileInputStream fis = new FileInputStream(file);
		byte[] buffer = new byte[4096];
		
		while (fis.read(buffer) > 0) {
			dos.write(buffer);
		}
			
	}
    }

}
