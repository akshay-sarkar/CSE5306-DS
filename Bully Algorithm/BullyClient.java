import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;

/**
 * @author AKSHAY SARKAR - axs6793 - 1001506793
 */

/**
 *  * A simple Swing-based client for the Bully Algorithm Simulation.  *  * The
 * client follows the Broadcast Protocol which is as follows.  * When the server
 * sends "SUBMITNAME" the client replies with the  * desired screen name.  The
 * server will keep sending "SUBMITNAME"  * requests as long as the client
 * submits screen names that are  * already in use.  When the server sends a
 * line beginning  * with "NAMEACCEPTED" the client is now allowed to start  *
 * start election("START_ELECTION") and broadcast that to all  * clients
 * connected to the server. Only the client with higher process id would respond
 * with "OK" in the "ELECTION" part.  
 */
public class BullyClient extends JFrame {

	/**
	 * Variable Declarations for the code Consist of timer and flags.
	 */
	private static final long serialVersionUID = 1L;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private static BullyClient client;
	// Timers
	private Timer aliveTimerFromCoordinator, checkCoordinatorTimer = null, okRecieveTimer = null;
	private int port = 32000, globalProcessId;
	private String serverAddress = "localhost", line, globalClientName;
	private Socket socket;
	boolean coordinatorFlag = false;

	/**
	 * Prompt for Name and ProcessId; Return it to the server.      
	 */
	private String getClientName() {
		return JOptionPane.showInputDialog(this, "Enter Clinet Name :", "Welcome Client", JOptionPane.QUESTION_MESSAGE);
	}

	private String getClientProcessID() {
		return JOptionPane.showInputDialog(this, "Enter Process Id :", "Welcome Client", JOptionPane.QUESTION_MESSAGE);
	}

	/**
	 * Constructs the client by laying out the GUI.  
	 */
	public BullyClient() {
		initComponents();
	}

	/**
	 * Connects to the server then enters the processing loop for tha
	 * algorithm.  
	 */
	private void run(String clientName, int processID) {
		// Storing ClientName and ProcessId globally
		globalClientName = clientName;
		globalProcessId = processID;

		// Make connection and initialize i/o streams
		try {
			socket = new Socket(serverAddress, port);
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
			ClientData clientData = new ClientData(globalClientName, processID);

			// Process all messages from server in the loop
			while (true) {
				try {
					line = (String) ois.readObject();
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				System.out.println("Server Request>" + line);
				if (line.startsWith("SUBMITNAME")) {
					// Sending Client Data Object to Server
					oos.writeObject(clientData);
					oos.flush();
				} else if (line.startsWith("NAMEACCEPTED")) {

					messageArea.setText(" Client Name - " + globalClientName + " and ProcessId - " + globalProcessId);
					if(line.split("#&#").length == 1){
						messageArea.append("\n STARTING ELECTION by " + globalClientName);
					}
					// Lets start election once the name is accepted
					oos.writeObject("START_ELECTION" + "#&#" + globalClientName + "#&#" + processID);
					oos.flush();
					starOKTimer();

				} else if (line.startsWith("MESSAGE")) {
					messageArea.append("\n " + line.substring(8));
				} else if (line.startsWith("ELECTION")) {
					String[] str_arr = line.split("#&#");
					if (!globalClientName.equalsIgnoreCase(str_arr[1])) {
						messageArea.append("\n ELECTION REQUEST COMES FROM " + str_arr[1] + " FOR " + globalClientName);
						/* SENDING OK ONLY IF ProcessID is greater */
						if (processID > Integer.parseInt(str_arr[2])) {
							messageArea.append("\n OK GOING BACK FROM " + globalClientName);
							oos.writeObject("OK" + "#&#" + globalClientName + "#&#" + processID + "#&#" + str_arr[1]);
							oos.flush();
							// Once OK SENT; CLIENT SHOULD START ELECTION NOW
							TimeUnit.SECONDS.sleep(1);
							messageArea.append("\n STARTING ELECTION - " + globalClientName);
							oos.writeObject("START_ELECTION" + "#&#" + globalClientName + "#&#" + processID);
							oos.flush();
							// Once Election is initiated startOK timer, if nobody responds with OK
							// then this client would become co-ordinaotor  itself
							if (!okRecieveTimer.isRunning())
								starOKTimer();
						} else {
							messageArea.append("\n NOT OK");
							stopOKTimer();
						}
					}
				} else if (line.startsWith("CO-ORDINATOR")) {
					messageArea.append("\n SELECTED CO-ORDINATOR IS " + line.split("#&#")[1]);
					// NEW CO-ORDINATOR MIGHT HAVE SELECTED. RESET/STOP Timer.
					if (checkCoordinatorTimer != null && !line.split("#&#")[1].equalsIgnoreCase(globalClientName))
						checkCoordinatorTimer.stop();
				} else if (line.startsWith("OK")) {
					/* OK RECIEVED HERE - STOP THE TIMER */
					if(!line.contains("OK, You ARE ONLY ONE !!")){
						messageArea.append("\n " + line);	
					}
					stopOKTimer();
				} else if (line.startsWith("START_BEING_COORDINATOR")) {
					/* NOW BRAODACTSING MESSAGE AT REGULAR INTERVALS */
					startBeingCoordinator();
				} else if (line.startsWith("STOP_BEING_COORDINATOR")) {
					System.out.println("STOP BEING COORDINATOR");
					aliveTimerFromCoordinator.stop();
					aliveTimerFromCoordinator.setRepeats(false);
					coordinatorFlag = false;
				} else if (line.startsWith("SEND_ALIVE_TO_CLIENTS")) {
					if (!globalClientName.equalsIgnoreCase(line.split("#&#")[1])) {
						messageArea.append("\n ALIVE RECIEVED FROM CO-ORDINATOR - " + line.split("#&#")[1]);
						// ONCE ALIVE IS RECIVED
						Random r = new Random();
						int Low = 9, High = 20;
						int initialDelay = (r.nextInt(High - Low) + Low) * 1000;

						if (checkCoordinatorTimer != null) {
							System.out.println("Should Restart Timer. Set at " + initialDelay);
							checkCoordinatorTimer.setInitialDelay(initialDelay);
							checkCoordinatorTimer.restart();
						} else {
							ActionListener taskPerformer = new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									// ALIVE MESSAGE NOT RECIVED FROM CO_ORDINATOR ; Lets start election
									messageArea.append("\n STARTING RE-ELECTION - " + globalClientName + " with timer "
											+ initialDelay);
									try {
										// STOP CHECKING FOR CORDINATOR ALIVE MSG AND START ELECTION
										checkCoordinatorTimer.stop();;
										oos.writeObject("START_ELECTION" + "#&#" + globalClientName + "#&#" + processID);
										oos.flush();

									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
							};
							checkCoordinatorTimer = new Timer(initialDelay, taskPerformer);
							checkCoordinatorTimer.start();
						}
					}
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Start Being Co-ordinator; Need to send ALIVE message to others at regular interval
	 */
	private void startBeingCoordinator() {
		if ((aliveTimerFromCoordinator != null && !aliveTimerFromCoordinator.isRunning())
				|| aliveTimerFromCoordinator == null) {
			coordinatorFlag = true;
			try {
				oos.writeObject("COORDINATOR_CHOOSEN" + "#&#" + globalClientName);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			aliveTimerFromCoordinator = new Timer(7000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					messageArea.append("\n BROADACTSING ALIVE MESSAGE AFTER 7 sec");
					try {
						oos.writeObject("SEND_ALIVE" + "#&#" + globalClientName);
						oos.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
			aliveTimerFromCoordinator.setRepeats(true);
			aliveTimerFromCoordinator.start();
		} else {
			System.out.println("Already Running..");
		}
	}
	/** 
	 * START OK TIMER ; If not received OK then no greater ProcessID and Client becomes Client 
	 * */
	private void starOKTimer() {
		okRecieveTimer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startBeingCoordinator();
			}
		});
		okRecieveTimer.start();
		okRecieveTimer.setRepeats(false);
	}
	/**
	 * STOP OK TIMER; there are other clients who are all having process id greater than this client 
	 * */
	private void stopOKTimer() {
		System.out.println("Stopping OK TIMERS..");
		if (okRecieveTimer != null && okRecieveTimer.isRunning())
			okRecieveTimer.stop();
		// If node was currently a Co-ordinator that needs to stop too
		if (aliveTimerFromCoordinator != null && aliveTimerFromCoordinator.isRunning()) {
			aliveTimerFromCoordinator.stop();
			coordinatorFlag = false;
		}
	}

	/**
	 *  * Runs the client as an application in Jframe.  
	 */
	public static void main(String[] args) throws Exception {
		client = new BullyClient();
		client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.setVisible(true);
		String client_name = client.getClientName();
		String client_processId = client.getClientProcessID();
		client.setTitle("Client Name - " + client_name + " ProcessId - " + client_processId);
		client.run(client_name, Integer.parseInt(client_processId));
	}

	/**
	 * Exiting client should inform server about it 
	 */
	private void exitButtonClicked(MouseEvent e) {
		try {
			oos.writeObject("CLOSE_CLIENT" + "#&#" + globalClientName);
			oos.flush();
			System.exit(0);
		} catch (IOException e1) {
			e1.printStackTrace();
			try {
				oos.close();
				ois.close();
				socket.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}

		}
	}
	/**
	 * Clear Button for clearing the textarea
	 */
	private void clearClicked(MouseEvent e) {
		messageArea.setText(" Client Name - " + globalClientName + " ProcessId - " + globalProcessId);
	}

	/**
	 * Initialize SWING GUI 
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - AKSHAY SARKAR
		scrollPane1 = new JScrollPane();
		messageArea = new JTextArea();
		button1 = new JButton();
		button2 = new JButton();

		// ======== this ========
		setTitle("Client");
		Container contentPane = getContentPane();
		contentPane.setLayout(null);

		// ======== scrollPane1 ========
		{
			scrollPane1.setViewportView(messageArea);
		}
		contentPane.add(scrollPane1);
		scrollPane1.setBounds(5, 10, 385, 220);

		// ---- button1 ----
		button1.setText("Exit");
		button1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				exitButtonClicked(e);
			}
		});
		contentPane.add(button1);
		button1.setBounds(new Rectangle(new Point(300, 240), button1.getPreferredSize()));

		// ---- button2 ----
		button2.setText("Clear");
		button2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				clearClicked(e);
			}
		});
		contentPane.add(button2);
		button2.setBounds(new Rectangle(new Point(235, 240), button2.getPreferredSize()));

		{ // compute preferred size
			Dimension preferredSize = new Dimension();
			for (int i = 0; i < contentPane.getComponentCount(); i++) {
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
		// JFormDesigner - End of component initialization
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - AKSHAY SARKAR
	private JScrollPane scrollPane1;
	private JTextArea messageArea;
	private JButton button1;
	private JButton button2;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}