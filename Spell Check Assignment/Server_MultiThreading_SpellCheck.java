/*
 * Created by JFormDesigner on Sat Jun 10 01:23:30 CDT 2017
 */
/**
 * @author Akshay Sarkar - 1001506793
 */

/* Required Packages */
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;

/* Application implemented using JFrame for Java UI */

public class Server_MultiThreading_SpellCheck extends JFrame {

	private static final long serialVersionUID = 1L;
	/**
	 * Creates Variable for Socket Environment
	 * Port number using 32000
	 */
	ServerSocket serverSocket = null;
	Socket socket = null;
	boolean ServerOn = true;
	static DataInputStream din;
	static DataOutputStream dout;
	int port = 32000;

	/* 
	 * Constructor for JSwing Setup
	 * */
	public Server_MultiThreading_SpellCheck() {
		initComponents();
	}

	/* 
	 * Initiating Server  
	 * */
	private void initiateConnection() {
		textArea1.append("Connection Initiated"
				+ "\nListening on port : "+port
				+ "\nWaiting for the Client to Connect.....\n");
		try {
			serverSocket = new ServerSocket(port);
			while (ServerOn) {
				socket = serverSocket.accept();
				textArea1.append("--------------------------------------\n");
				textArea1.append("Client Request Recieved.\n");
				/* Creating Thread for Multiple Client Architecture */
				ClientServiceThread cliThread = new ClientServiceThread(socket);
				cliThread.start();   
			}

			serverSocket.close();
			textArea1.append("Server Stopped..\n");
			System.out.println("Server Stopped");
		} catch(Exception exec){
			exec.printStackTrace();
		}
	}

	/*
	* Starting point of the application
	* Will initialize the Connection from here and wait for client to connect
	* */
	public static void main(String[] args) {
		Server_MultiThreading_SpellCheck f = new Server_MultiThreading_SpellCheck();
		f.setVisible(true);
		/* Start the Server over a particular socket */
		f.initiateConnection();
	}

	/* 
     * Swing Event Listener 
     * */
	private void exitButtonClicked(MouseEvent e) {
		// TODO add your code here
		System.out.println("Reached Here..now "+ serverSocket);
		try {
			/* In case of Server getting close before any connection could be setup with client */
			if(socket == null ){
				ServerOn = false;
				serverSocket.close();
				System.exit(0);
			}else{
				/* In case of Server getting close before any connection could be setup with client */
				socket.close();
				serverSocket.close();
				ServerOn = false;
				System.exit(0);
			}
		} catch (IOException e1) {
			System.out.println("Force Closed Sockets");
			textArea1.append("Server Stopped..");
		}
	}
	
	/*
	* Swing UI setup
	* */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Akshay Sarkar
		scrollPane1 = new JScrollPane();
		textArea1 = new JTextArea("");
		button2 = new JButton();
		textArea1.setEditable(false);
		textArea1.setLineWrap(true);

		//======== this ========
		setTitle("Server");
		setAlwaysOnTop(true);
		Container contentPane = getContentPane();
		contentPane.setLayout(null);

		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(textArea1);
		}
		contentPane.add(scrollPane1);
		scrollPane1.setBounds(25, 15, 370, 175);

		//---- button2 ----
		button2.setText("Exit");
		button2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				exitButtonClicked(e);
			}
		});
		contentPane.add(button2);
		button2.setBounds(new Rectangle(new Point(325, 195), button2.getPreferredSize()));

		{ // compute preferred size
			Dimension preferredSize = new Dimension();
			for(int i = 0; i < contentPane.getComponentCount(); i++) {
				Rectangle bounds = contentPane.getComponent(i).getBounds();
				preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
				preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
			}
			Insets insets = contentPane.getInsets();
			preferredSize.width += insets.right;
			preferredSize.height += insets.bottom;
			contentPane.setMinimumSize(preferredSize);
			contentPane.setPreferredSize(preferredSize);
		}
		setSize(445, 270);
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Akshay Sarkar
	private JScrollPane scrollPane1;
	private JTextArea textArea1;
	private JButton button2;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	/* Every Request will be handled separately in the server part using this Threading Class */
	class ClientServiceThread extends Thread { 
		Socket myClientSocket;
		boolean m_bRunThread = true;
		BufferedReader in = null; 
		PrintWriter out = null;
		String textFileLine, serverResponse="";
		ArrayList<String> clientWords;

		public ClientServiceThread() { 
			super(); 
		} 
		/* Constructor accpets the socket number on which data tranmission will be happening */
		ClientServiceThread(Socket s) { 
			myClientSocket = s; 
		} 

		@Override
		public void run() { 
			java.util.List<String> stringToRemove = new ArrayList<String>();
			try { 
				in = new BufferedReader(
						new InputStreamReader(myClientSocket.getInputStream()));
				out = new PrintWriter(
						new OutputStreamWriter(myClientSocket.getOutputStream()));

				String clientRequest = in.readLine();
				textArea1.append("Client Request >" + clientRequest+ " \n");
				
				/* Non-Case Sensitive Approach For Comparison */
				clientWords = new ArrayList<String>(Arrays.asList(clientRequest.toLowerCase().split(" ")));

				// Open the Dictionary File for checking all the words
				FileInputStream fstream = new FileInputStream("words.txt");
				BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

				//Read Dictionary File Line By Line
				while ((textFileLine = br.readLine()) != null)   {

					textFileLine = textFileLine.toLowerCase();
					/* Checking if all words are matched from client request */
					if(!clientWords.isEmpty()){

						for (String element : clientWords) {
							int index = textFileLine.indexOf(element);
							if( index != -1){
								stringToRemove.add(element);
							}
						}
						/* Removing Words Which are found - includes duplicate words as well */
						for(String indexes : stringToRemove){
							clientWords.remove(indexes);	                	 
						}
						stringToRemove.clear();

					}else{
						serverResponse = "All words found in Dictionary.";
						break;
					}
				}
				//Close the input stream
				br.close();

				/* Checking if any word exist which is not found in the server file*/
				if(!clientWords.isEmpty()){
					serverResponse = serverResponse + " Error : '"+ clientWords.toString()+"' word not found.";
				}else{
					serverResponse = "All words found in Dictionary.";
				}

				// Sending Response Back to the Client
				textArea1.append("Server Response >" + serverResponse+ " \n");
				textArea1.append("-------------------------------------- \n");
				out.println(serverResponse);
				out.flush();
				/* Clearing up variable Here*/
				serverResponse = "";
				clientWords.clear();
			} catch(Exception e) { 
				e.printStackTrace(); 
			} 
			finally { 
				try {
					in.close(); 
					out.close(); 
					myClientSocket.close(); 
				} catch(IOException ioe) { 
					ioe.printStackTrace(); 
				} 
			} 
		} 
	} 
}
