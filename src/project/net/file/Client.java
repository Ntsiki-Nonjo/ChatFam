package project.net.file;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JFileChooser;

public class Client {

	/*
	 * The client simply requests for a socket connection
	 * 
	 * After getting the connection it creates the FileEvent object which
	 * contains the file details along with full content as byte stream
	 * 
	 * Then writing the FileEvent object to the socket and exiting itself
	 */

	private Socket socket = null;
	private ObjectOutputStream outputStream = null;
	private boolean isConnected = false;
	private String sourceFilePath = null; //"/home/ntsiki/Downloads/Chef-Meme1.jpg";
	private FileEvent fileEvent = null;
	private String destinationPath = "/home/ntsiki/Music/";
	private DataInputStream diStream = null;
	private File selectedFile = null;
	private JFileChooser fileChooser = null;
	private String IPAddress = null;

	public Client(String IPAddress) {
		this.IPAddress = IPAddress;
		fileEvent = new FileEvent();
		fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		destinationPath = fileEvent.getDestinationDirectory();
		int result = fileChooser.showOpenDialog(null);
		
		if (result == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile();
			sourceFilePath = selectedFile.getAbsolutePath().trim();
		}
	}

	/*
	 * connect with server code running in local host or in any other host
	 */

	public void connect() {
		while (!isConnected) {
			try {
				socket = new Socket(IPAddress, 4445);
				outputStream = new ObjectOutputStream(socket.getOutputStream());
				isConnected = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// sending FileEvent object
	public void sendFile() {
		
		String fileName = sourceFilePath.substring(
				sourceFilePath.lastIndexOf("/") + 1, sourceFilePath.length());
		System.out.println(fileName);
		
		/*
		 * String path = sourceFilePath.substring(0, sourceFilePath.lastIndexOf("/") + 1);
		 */

		//fileEvent.setDestinationDirectory(destinationPath);
		fileEvent.setFilename(fileName);
		fileEvent.setSourceDirectory(sourceFilePath);

		File file = new File(sourceFilePath);

		if (file.isFile()) {
			try {
				diStream = new DataInputStream(
						new FileInputStream(file));
				long len = (int) file.length();
				byte[] fileBytes = new byte[(int) len];
				int read = 0;
				int numRead = 0;

				while (read < fileBytes.length
						&& (numRead = diStream.read(fileBytes, read, fileBytes.length - read)) >= 0) {
					read = read + numRead;
				}
				fileEvent.setFileSize(len);
				fileEvent.setFileData(fileBytes);
				fileEvent.setStatus("Success");
			} catch (Exception e) {
				e.printStackTrace();
				fileEvent.setStatus("Error");
			}
		} else {
			System.out.println("Path Specified is not Pointing to A File");
			fileEvent.setStatus("Error");
		}
		
		// now writing the FileEvent object to socket
		try {
			outputStream.writeObject(fileEvent);
			System.out.println("Done...Going to Exit");
			Thread.sleep(3000);
			socket.close();
			outputStream.close();
			diStream.close();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	/*
	public static void main(String[] args) {
		Client client = new Client();
		client.connect();
		client.sendFile();
	}
	*/
}
