package project.net;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.ItemSelectable;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import project.net.file.Client;

/*
 * A chat application consists of a chat server and a chat client
 * The server accepts connections from the clients and delivers all
 * messages from each client to other clients
 * This is a tool to communicate with other people over Internet in
 * real time
 * 
 * The client is implemented using two threads
 * one thread to interact with the server and
 * the other with the standard input
 * 
 * Two threads are needed because a client must communicate with the
 * server and, simultaneously, it must be ready to read messages from
 * the standard input to be sent to the server
 * 
 * The server is implemented using threads too, it uses a separate thread
 * for each connection
 * It spawns a new client thread every time a new connection from a client
 * is accepted
 * 
 * Multiple-Threading creates synchronization issues
 */

public class MultiThreadChatClient extends JFrame implements Runnable, WindowListener {
	/*
	 * The code below is the multiple-threaded chat client
	 * It uses two threads: one to read the data from the
	 * standard input and to send it to the server
	 * the other to read the data from the server and print
	 * it on the standard output
	 */
	
	// file sending
	private ArrayList<String[]> users = new ArrayList<String[]>();
	private String[] drop = new String[10];
	
	
	// gui variables
	private JTextArea messageArea = null;
	private TextField sendArea = null;
	private JButton send;
	private JButton file;
	
	// check connection
	private boolean connected = false;
	
	private final Dimension d = new Dimension(480, 600);
	
	MultiThreadChatClient(String s) {
		super(s);
		this.addWindowListener(this);
		this.setSize(480, 600);
		this.setMinimumSize(d);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		messageArea = new JTextArea();
		messageArea.setBackground(new Color(24, 24, 24));
		messageArea.setEditable(false);
		messageArea.setLineWrap(true);
		this.add(messageArea, "Center");
		messageArea.setFont(new Font("Courier", Font.PLAIN, 16));
		messageArea.setForeground(new Color(222, 222, 222));
		
		Panel p = new Panel();
		p.setLayout(new FlowLayout());
		
		// send area
		sendArea = new TextField(30);
		sendArea.setBackground(new Color(199, 249, 148));
		sendArea.setForeground(new Color(76, 117, 35));
		sendArea.setFont(new Font("Courier", Font.BOLD, 16));
		
		// buttons
		p.add(sendArea);
		p.setBackground(new Color(135, 207, 62)); 
		send = new JButton(new ImageIcon(getClass().getResource("/sendtext.png")));
		send.setOpaque(false);
		send.setContentAreaFilled(false);
		send.setBorderPainted(false);
		p.add(send);
		
		file = new JButton(new ImageIcon(getClass().getResource("/sendfile.png")));
		file.setOpaque(false);
		file.setContentAreaFilled(false);
		file.setBorderPainted(false);
		p.add(file);
		
		this.add(p, "South");
		sendArea.setFocusable(true);
		this.setVisible(true);
	}
	
	public TextField getSendArea() {
		return sendArea;
	}
	
	public String getIP(String str) {
		
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i)[1] == str) str = users.get(i)[0];
		}
		return str;
	}
	
	public void closeConnection() {
		try {
			MultiThreadChatClient.os.close();
			MultiThreadChatClient.is.close();
			MultiThreadChatClient.clientSocket.close();
			this.messageArea.append("Client has been Disconnected\n");
			connected = true;
			System.exit(0);
			//this.frame.dispatchEvent(new WindowEvent(this.frame, WindowEvent.WINDOW_CLOSING));
		} catch (IOException e) {
			e.printStackTrace();
			this.messageArea.append("Client failed to Disconnect\n");
		}
	}

	private static final long serialVersionUID = 1L;
	// the client socket
	private static Socket clientSocket = null;
	// the output stream
	private static PrintStream os = null;
	// the input stream
	private static BufferedReader is = null;
	
	public JFrame frame;
	
	// private static BufferedReader inputLine = null;
	// private static boolean closed = false;
	
	public static void main(String[] args) {
		
		// GUI
		final MultiThreadChatClient c = new MultiThreadChatClient("Chat-Fam");
		
		// the default port
		int portNumber = 2222;
		// the default host
		String host = JOptionPane.showInputDialog("Enter IP Address");
		
		if (args.length < 2) {
			System.out.println("Usage: java MultiThreadChatClient <host> <portNumber>\n"
					+ "Now using host:port=[" + host + ":" + portNumber + "]");
		} else {
			host = args[0];
			portNumber = Integer.valueOf(args[1]).intValue();
		}
		
		/*
		 * open a socket on a given host and port
		 * open input and output streams
		 */
		try {
			clientSocket = new Socket(host, portNumber);
			//inputLine = new BufferedReader(new InputStreamReader(System.in));
			os = new PrintStream(clientSocket.getOutputStream());
			is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Unknown Host " + host);
		} catch (IOException e) {
			System.out.println("Couldn't get I/O for the connection to the host " + host);
		}
		
		/*
		 * if everything has been initialized then we want to write some data to the 
		 * socket we have opened a connection to on the port portNumber
		 */
		if (clientSocket != null && os != null && is != null) {
			// create a thread to read from the server
			new Thread(c).start();
			
			
			// send message
			c.send.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					
					if (!c.sendArea.getText().isEmpty()) {
						os.println(c.sendArea.getText().trim());
						c.sendArea.setText("");
					}
				}
					
			});
			
			c.sendArea.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						os.println(c.sendArea.getText().trim());
						c.sendArea.setText("");
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {}
				@Override
				public void keyTyped(KeyEvent e) {}
			});
			
			// open JFileChooser and send File
			c.file.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// open chooser
					//
					
					// select user to send to
					final JComboBox<String> cmb = new JComboBox<String>(c.drop);
					final JFrame j = new JFrame("Send to");
					j.setLayout(new FlowLayout());
					j.setSize(100, 256);
					j.setLocationRelativeTo(null);
					
					cmb.setSelectedIndex(1);
					cmb.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							if (e.getSource() == cmb) {
								JComboBox cb = (JComboBox)e.getSource();
								String msg = (String)cb.getSelectedItem();
								
								// send file
								Client fileClient = new Client(c.getIP(msg));
								fileClient.connect();
								fileClient.sendFile();
								j.setEnabled(false);
								j.setVisible(false);
							}
						}
						
					});
					j.add(cmb);
					j.setVisible(true);
				}
				
			});
		}
	}
	
	//create a thread to read from the server
	@Override
	public void run() {
		/*
		 * keep on reading from the socket till we receive "Bye"
		 * from the server once we receive that then we want to break
		 */
		String responseLine;
		try {
			while ((responseLine = is.readLine()) != null) {
				if (responseLine.charAt(0) != '/') {
					this.messageArea.append(responseLine + '\n');
					this.sendArea.requestFocus();
				}
				else {
					// add name and IP address of users
					String[] s = responseLine.split(":");
					s[0] = s[0].substring(1);
					users.add(s);
					
					// add to string array for drop list
					for (int i = 0; i < drop.length; i++) {
						if (drop[i] == null) {
							drop[i] = s[1];
							break;
						}
					}
				}
				if (responseLine.indexOf("*** Bye") != -1) break;
			}
			this.closeConnection();
			//closed = true;
		} catch (IOException e) {
			System.err.println("IOException: " + e);
		}
	}
	
	
	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (connected) {
			try {
				MultiThreadChatClient.os.close();
				MultiThreadChatClient.is.close();
				MultiThreadChatClient.clientSocket.close();
				System.out.println("Client has been Disconnected");
				this.messageArea.append("Client has been Disconnected\n");
				System.exit(0);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else {
			System.exit(0);
		}
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public JButton getSend() {
		return send;
	}
}
