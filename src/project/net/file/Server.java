package project.net.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
	/*
	 * It simply creates a ServerSocket on port 4445 and 
	 * waiting for incoming socket connections
	 * 
	 * Once a connection comes, it accepts a the connection
	 * 
	 * Then it reads the FileEvent Object
	 * 
	 * Destination directory and file are creating
	 * 
	 * Data is writing to the output file too
	 */
	
	private ServerSocket serverSocket = null;
	private Socket socket = null;
	private ObjectInputStream inputStream = null;
	private FileEvent fileEvent;
	private File dstFile = null;
	private FileOutputStream fileOutputStream = null;
	
	public Server() {
		
	}
	
	// accepts socket connection
	
	public void doConnect() {
		try {
			// connect client
			serverSocket = new ServerSocket(4445);
			socket = serverSocket.accept();
			inputStream = new ObjectInputStream(socket.getInputStream());
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	// reading the FileEvent object and copying the file to disk
	public void downloadFile() {
		try {
			fileEvent = (FileEvent) inputStream.readObject();
			
			if (fileEvent.getStatus().equalsIgnoreCase("Error")) {
				System.out.println("Error Occured");
				System.exit(0);
			}
			String outputFile = fileEvent.getDestinationDirectory() + fileEvent.getFilename();
			
			if (!new File(fileEvent.getDestinationDirectory()).exists()) {
				new File(fileEvent.getDestinationDirectory()).mkdirs();
			}
			
			dstFile = new File(outputFile);
			
			fileOutputStream = new FileOutputStream(dstFile);
			fileOutputStream.write(fileEvent.getFileData());
			fileOutputStream.flush();
			fileOutputStream.close();
			
			System.out.println("Output File : " + outputFile + " is successfully saved");
			socket.close();
			serverSocket.close();
			inputStream.close();
			Thread.sleep(3000);
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Server server = new Server();
		server.doConnect();
		server.downloadFile();
	}
}
