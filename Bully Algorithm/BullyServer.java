/*
 * Created by JFormDesigner on Tue Jul 11 19:21:23 CDT 2017
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

/**
 * @author AKSHAY SARKAR - axs6793 - 1001506793
 */
/**
 * Server purpose is to hold the number clients existing in the network
 * If broadcast the message to all the clients, such that all clients would do the process individually 
 */
public class BullyServer extends JFrame {
	
	private static final long serialVersionUID = 1L;
	//Storing the client details in Dictionary
	Map<String,ClientData> clientDictionary = new HashMap<String, ClientData>();

	/**
	 * Creates Variable for Socket Environment
	 */
	private ServerSocket serverSocket = null;
	private Socket socket = null;
	private boolean ServerOn = true;
	static DataInputStream din;
	static DataOutputStream dout;
	private int port = 32000;
	ClientData clientData, coordinator = null;
	ObjectInputStream ois;

	/**
	 * Constructs the Server by laying out the GUI.  
	 */
	public BullyServer() {
		initComponents();
	}

	/** 
	 * Initiating Server  
	 */
	private void initiateConnection() {
		textArea1.append(" Server Initiated"
				+ "\n Waiting for the Clients to Connect.....\n");
		try {
			serverSocket = new ServerSocket(port);
			while (ServerOn) {
				socket = serverSocket.accept();
				/* Creating Thread for Multiple Client Architecture */
				ClientHandler clientThread = new ClientHandler(socket);
				clientThread.start();   
			}

			serverSocket.close();
			textArea1.append("Server Stopped..\n");
		} catch(Exception exec){
			exec.printStackTrace();
		}
	}
	/**
	 * Server Exit Button
	 */
	private void exitMouseClicked(MouseEvent e) {
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

	/**
	 * Runs the Server as an application in Jframe.  
	 */
	public static void main(String[] args) {
		BullyServer server = new BullyServer();
		server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		server.setVisible(true);
		/* Start the Server over a particular socket */
		server.initiateConnection();
	}
	
	/**
	 * Initialize SWING GUI 
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - AKSHAY SARKAR
		scrollPane1 = new JScrollPane();
		textArea1 = new JTextArea();
		button1 = new JButton();

		//======== this ========
		setTitle("Server");
		Container contentPane = getContentPane();
		contentPane.setLayout(null);

		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(textArea1);
		}
		contentPane.add(scrollPane1);
		scrollPane1.setBounds(5, 5, 385, 230);

		//---- button1 ----
		button1.setText("Exit");
		button1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				exitMouseClicked(e);
			}
		});
		contentPane.add(button1);
		button1.setBounds(new Rectangle(new Point(320, 240), button1.getPreferredSize()));

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
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - AKSHAY SARKAR
	private JScrollPane scrollPane1;
	private JTextArea textArea1;
	private JButton button1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	/**
	 * A handler thread class.  Handlers are spawned from the listening
	 * loop and are responsible for a dealing with a single client
	 * and broadcasting its messages.
	 */
	class ClientHandler extends Thread {
		private Socket socket;
		private ObjectOutputStream oos, writer;
		private ObjectInputStream ois;
		public ClientHandler() {
			super();
		}

		/**
		 * Constructs a handler thread, squirreling away the socket.
		 * All the interesting work is done in the run method.
		 */
		public ClientHandler(Socket s) {
			socket = s;
		}

		/**
		 * Services this thread's client by repeatedly requesting a
		 * screen name until a unique one has been submitted, then
		 * acknowledges the name and registers the output stream for
		 * the client in a global set, then repeatedly gets inputs and
		 * broadcasts them.
		 */
		@Override
		public void run() {
			try {
				ois = new ObjectInputStream(socket.getInputStream());
				oos = new ObjectOutputStream(socket.getOutputStream());

				// Request a name from this client.  Keep requesting until
				// a name is submitted that is not already used.  Note that
				// checking for the existence of a name and adding the name
				// must be done while locking the set of names.
				while (true) {

					oos.writeObject("SUBMITNAME");
					oos.flush();
					//reading partial object from client
					clientData = (ClientData) ois.readObject();

					//completing the object here as client send partial with just Name and Process ID
					// by adding input / output stream
					clientData.setReader(ois);
					clientData.setWriter(oos);

					textArea1.append("\n Connected with "+clientData.getName()+ " having ProcessId "+clientData.getProcess_id());
					if (clientData.getName() == null || clientData.getName().isEmpty()) {
						return;
					}else{
						clientDictionary.put(clientData.getName(), clientData);
						//Send Message that Client is accepted
						// Now that a successful name has been chosen, add the
						// socket's print writer to the set of all writers so
						// this client can receive broadcast messages.
						if(clientDictionary.size() <= 1)
							oos.writeObject("NAMEACCEPTED"+"#&#"+"ONE");
						else
							oos.writeObject("NAMEACCEPTED");
						break;
					}					
				}

				//Interaction with Client
				while(true){
					String str = (String) ois.readObject();
					System.out.println("CLIENT SAYS >"+str);

					// Every-time election request comes from client
					if(str.startsWith("START_ELECTION")){
						if(clientDictionary.size() > 1){
							System.out.println("----->START_ELECTION");
							// Client who is starting the election and get his details
							String clientName = str.split("#&#")[1];
							String processID = str.split("#&#")[2];
							clientData = clientDictionary.get(clientName);
							
							for (ClientData client : clientDictionary.values()) {
								// Not to send election request to requesting client itself
								if(!clientName.equalsIgnoreCase(client.getName())){
									//System.out.println(clientName+" - Election to - "+client.getName());
									writer = client.getWriter();
									writer.writeObject("ELECTION FROM"+"#&#"+clientName+"#&#"+processID);
									writer.flush();	
								}
							}
						}else{
							//Handling 1 client situation(Not mandatory do so)
							System.out.println("Just 1 Node, So stopping the timer from running..");
							oos.writeObject("OK, You ARE ONLY ONE !!");
						}
					} else if(str.startsWith("OK")){
						String sentByClientName = str.split("#&#")[1];// OK SENT BY A5
						String sentForClientName = str.split("#&#")[3];// OK SENT FOR A4
						System.out.println("Response from "+sentByClientName+ " for clienData "+ sentForClientName);
						if(!sentByClientName.equalsIgnoreCase(clientData.getName())){
							clientData = clientDictionary.get(sentForClientName);
							writer = clientData.getWriter();
							writer.writeObject("OK FROM "+sentByClientName);
						}
					} else if(str.startsWith("CLOSE_CLIENT")){
						String clientName = str.split("#&#")[1];
						clientDictionary.remove(clientName);
						textArea1.append("\n CLOSE_CLIENT ThreadID : "+Thread.currentThread().getId());
						//Checking Size
						//System.out.println("Number Clinet in Dictionary :"+ clientDictionary.size());
						
						//Check if same node was coordinator by this time; then set to null
						if(coordinator!=null && coordinator.getName().equalsIgnoreCase(clientName))
							coordinator = null;

						Thread.currentThread().interrupt();
						return;
					} else if(str.startsWith("SEND_ALIVE")){
						/* COORDINATOR ASKING FOR BROADCAST ALIVE MESSAGE TO EVERYONE */
						String clientName = str.split("#&#")[1];
						for (ClientData client : clientDictionary.values()) {
							writer = client.getWriter();
							writer.writeObject("SEND_ALIVE_TO_CLIENTS"+"#&#"+clientName);
							writer.flush();
						}
					} else if(str.startsWith("COORDINATOR_CHOOSEN")){
						/* COORDINATOR ASKING FOR BROADCAST MESSAGE TO EVERYONE */
						String clientName = str.split("#&#")[1];
						for (ClientData client : clientDictionary.values()) {
							writer = client.getWriter();
							writer.writeObject("CO-ORDINATOR"+"#&#"+clientName);
							writer.flush();
						}
					}
				}
			}catch (java.net.SocketException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
				System.out.println("Interupting this thread");
				return;
			}catch (IOException e) {
				System.out.println(e);
				e.printStackTrace();
			}catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			} 
			finally {
				try {
					socket.close();
				} catch (IOException e) {}
				try {
					ois.close(); 
					oos.close(); 
					this.socket.close(); 
				} catch(IOException ioe) { 
					ioe.printStackTrace(); 
				} 
			}
		}
	}
}
