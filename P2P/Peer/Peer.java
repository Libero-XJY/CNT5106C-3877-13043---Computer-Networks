import java.net.*;
import java.io.*;
import java.nio.*;
import java.util.*;

public class Peer {
	public static String finalname;
	public static int total;
	public static int getLineNumbers(String fileName) {
		Scanner fileScanner = null;
		File file = new File(fileName);
		try {
			fileScanner = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.out.printf("The file %s could not be found.\n",
			file.getName());
		}
		int lines = 0;
		while (fileScanner.hasNextLine()) {
			lines++;
			// Go to next line in file
			fileScanner.nextLine();
		}
		fileScanner.close();
		return lines;
	}

	public static void saveFile(String filename,int length,Socket ownerSocket) throws IOException {
		DataInputStream dis = new DataInputStream(ownerSocket.getInputStream());
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

	public static void merge(String file,String tempFile,int tempCount){
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(new File(file), "rw");
            for (int i = 1; i <= tempCount; i++) {
                RandomAccessFile reader = new RandomAccessFile(new File(tempFile + "." + i), "r");
                byte[] b = new byte[1024];
                int n = 0;
                while((n = reader.read(b)) != -1){
                    raf.write(b, 0, n);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

}


	public static String getLackChunkName() throws Exception {
		BufferedReader br1 = null;
		BufferedReader br2 = null;
		String sCurrentLine;
		List<String> list1 = new ArrayList<String>();
		List<String> list2 = new ArrayList<String>();
		br2 = new BufferedReader(new FileReader("peerFileList.txt"));
		br1 = new BufferedReader(new FileReader("NFL.txt"));
		while ((sCurrentLine = br1.readLine()) != null) {
			list1.add(sCurrentLine);
		}
		while ((sCurrentLine = br2.readLine()) != null) {
			list2.add(sCurrentLine);
		}
		List<String> tmpList = new ArrayList<String>(list1);
		tmpList.removeAll(list2);
		if(tmpList.isEmpty())
		return null;
		//System.out.println("content from peerFileList.txt which is not there in NFL.txt");
		int actualSize = tmpList.size()-1;
		int i = (int)(Math.random()*actualSize);
		return tmpList.get(i); //content from test.txt which is not there in test2.txt
	}
	public static void main(String[] args) throws Exception {

		int ownerport = Integer.parseInt(args[0]);
		Socket ownerSocket = new Socket("localhost", ownerport);
		DataInputStream input = new DataInputStream(ownerSocket.getInputStream());
		DataOutputStream out = new DataOutputStream(ownerSocket.getOutputStream());
		finalname = input.readUTF();
		total = input.readInt();
		int ChunkNeed = input.readInt();
		for(int i=1; i<=ChunkNeed; i++){
			int length = input.readInt();
			String filename = input.readUTF();
			saveFile(filename, length, ownerSocket);
			writeLog.method("peerFileList.txt", filename + "\n");
            System.out.println("Recieve a chunk from owner!");
		}
		
		// receive chunks
		int selfport = Integer.parseInt(args[1]);
		ServerSocket listener = new ServerSocket(selfport);
		int clientNum = 1;
		new Handler(listener,clientNum).start();

		int neighborport = Integer.parseInt(args[2]);
		Socket neighborSocket = null;
		while (true) {
			try {
				neighborSocket = new Socket("localhost", neighborport);
				break;
			} catch (Exception e) {
				System.err.println("Connection refused. You need to initiate a server first.");
				Thread.sleep(1000);
				continue;
			}
		}
		Send send = new Send(neighborSocket);
		send.start();
		
    } 

		private static class Handler extends Thread {// this class is used to send file
			private ServerSocket listener;
			private Socket selfSocket;
			private int no;		//The index number of the Peer
	
				public Handler(ServerSocket listener, int no) {
						this.listener = listener;
						this.no = no;
				}
	
			public void run() {
			 try{
				selfSocket = listener.accept();
				//initialize Input and Output streams
				 DataInputStream input1 = new DataInputStream(selfSocket.getInputStream());
				DataOutputStream out1 = new DataOutputStream(selfSocket.getOutputStream());
				out1.writeUTF("You are now connected your download neighbor");
				while(true){
					String filename = input1.readUTF();
                    if (filename.equals("peerFileList.txt")){
                        System.out.println("Send a chunk ID list to neighbor");
                        out1.writeInt(Math.toIntExact(new File(filename).length()));
                        sendFile(selfSocket, filename, Math.toIntExact(new File(filename).length()));
                    }
                    else{
                        System.out.println("Recieve a chunk request");
                        out1.writeInt(Math.toIntExact(new File(filename).length()));
                        sendFile(selfSocket, filename, Math.toIntExact(new File(filename).length()));
                        System.out.println("Send a chunk to neighbor");
                    }
				}
				
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Peer " + no);
			}
			finally{
				try{
					selfSocket.close();
				}
				catch(IOException ioException){
					System.out.println("Disconnect with Peer " + no);
				}
			}
		}
		void sendFile(Socket clientSock, String file, int length) throws IOException {
			DataOutputStream dos = new DataOutputStream(clientSock.getOutputStream());
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[length];// avoid many black space when transfer the filelist.txt whose length is much less than 4096
			
			while (fis.read(buffer) > 0) {
				dos.write(buffer);
			}
				
		}
		}

		private static class Send extends Thread {// this class is used to request file
			private Socket selfSocket;
	
				public Send(Socket selfSocket) {
					this.selfSocket = selfSocket;
				}
	
			public void run() {
			 try{
				DataInputStream input2 = new DataInputStream(selfSocket.getInputStream());
				DataOutputStream out2 = new DataOutputStream(selfSocket.getOutputStream());
				System.out.println("The Send thread is running");
				System.out.println(input2.readUTF());
				while(true){
					Thread.sleep(100);
					out2.writeUTF("peerFileList.txt");
					int ListLength = input2.readInt();
					saveFile("NFL.txt", ListLength, selfSocket);
                    System.out.println("Get the file list from dowload neighbor");
					String ChunkName = getLackChunkName();
					if(ChunkName == null){
						if(getLineNumbers("peerFileList.txt")!=total)
						continue;
						else{
							System.out.println("You have already get all the chunks!");
							merge(finalname, finalname, total);
							break;
						}
						
					}
					out2.writeUTF(ChunkName);
                    System.out.println("Request " + ChunkName+" from the download neighbor");
					int ChunkLength = input2.readInt();
					saveFile(ChunkName, ChunkLength, selfSocket);
                    System.out.println("Recieve a chunk from download neighbor");
					writeLog.method("peerFileList.txt", ChunkName + "\n");
				}
				} catch (IOException e) {
					e.printStackTrace();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			finally{
				try{
					selfSocket.close();
				}
				catch(IOException ioException){
					System.out.println("Disconnect with Peer ");
				}
			}
		}
		}

}
