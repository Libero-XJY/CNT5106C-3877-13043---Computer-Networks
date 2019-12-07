import java.net.*;
import java.io.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Owner {
    public static int number_chunk = 0;

	public static void splitFile(File f) throws IOException {
        int partCounter = 1;

        int sizeOfFiles = 1024 * 100;// 1MB
        byte[] buffer = new byte[sizeOfFiles];

        String fileName = f.getName();

        //try-with-resources to ensure closing stream
        try (FileInputStream fis = new FileInputStream(f);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            int bytesAmount = 0;
            
            while ((bytesAmount = bis.read(buffer)) > 0) {
                //write each chunk of data into separate file with different number in name
                String filePartName = String.format("%s.%d", fileName, partCounter++);
                File newFile = new File(f.getParent(), filePartName);
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(buffer, 0, bytesAmount);
                }
                number_chunk++;
            }
           // System.out.println("The file has been splited into "+number_chunk+" chunks");
		}
    }

    public static String file_name = null;
    
	public static void main(String[] args) throws Exception {
		int sPort = Integer.parseInt(args[0]);//The server will be listening on this port number
		System.out.println("The file owner is running."); 
		ServerSocket listener = new ServerSocket(sPort);
        
        File f = new File("test.pdf");
        file_name = f.getName();
		
		splitFile(f);
        System.out.println("The file has been splited into "+number_chunk+" chunks");
		// has not create a list contains this things
		int peerNum = 1;
        	try {
            		while(true) {
                		new Handler(listener.accept(),peerNum).start();
				System.out.println("Peer "  + peerNum + " is connected!");
				peerNum++;
            			}
        	} finally {
            		listener.close();
        	} 
 
    	}

    	private static class Handler extends Thread {
		private Socket connection;
		private int no;		//The index number of the Peer

            public Handler(Socket connection, int no) {
            this.connection = connection;
	    	this.no = no;
		}
		
        public void run() {
 		try{
            int number_of_chunks = number_chunk;
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            
            double avarage = number_of_chunks/5;
            double avarage_chunks_d = Math.ceil(avarage);
            int avarage_chunks = (int)avarage_chunks_d;
            int peer5_chunks = number_of_chunks;
            out.writeUTF(file_name);
            out.writeInt(number_of_chunks);
            if(no<5){
                int chunk_number_peer = no*avarage_chunks - (avarage_chunks*no-(avarage_chunks));
                System.out.println("chunks to send = "+chunk_number_peer);
                out.writeInt(chunk_number_peer);
                
                for(int i=avarage_chunks*(no-1)+1; i<=no*avarage_chunks;i++){
                    String transferFilename=file_name+"."+i;
                    File transferFile = new File(transferFilename);
                    long transferFileLength = transferFile.length();
                    int length = Math.toIntExact(transferFileLength);
                    out.writeInt(length);
                    out.writeUTF(transferFilename);
                    sendFile(connection,transferFilename);
                }
                
                
            }
            if (no==5){
                int chunk_number_peer = number_of_chunks - 4*avarage_chunks;
                System.out.println("chunks to send = "+chunk_number_peer);
                out.writeInt(chunk_number_peer);
                
                for(int i=avarage_chunks*(no-1)+1; i<=number_of_chunks;i++){
                    String transferFilename=file_name+"."+i;
                    File transferFile = new File(transferFilename);
                    long transferFileLength = transferFile.length();
                    int length = Math.toIntExact(transferFileLength);
                    out.writeInt(length);
                    out.writeUTF(transferFilename);
                    sendFile(connection,transferFilename);
                }
                
            }
        }
		catch(IOException ioException){
			System.out.println("Disconnect with Peer " + no);
		}
		finally{
			try{
				connection.close();
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Peer " + no);
			}
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
