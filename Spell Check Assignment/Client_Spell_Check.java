/*
 * Created by JFormDesigner on Thu Jun 22 23:39:14 CDT 2017
 */
/**
 * @author Akshay Sarkar - 1001506793
 */
/* Required Packages */
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.*;
import javax.swing.border.*;

/* Application implemented using JFrame for Java UI */
public class Client_Spell_Check extends JFrame {
	
	private static final long serialVersionUID = 1L;
	/**
	 * Creates Variable for Socket Environment
	 * Port number using 32000
	 */
	Socket requestSocket;
	PrintWriter out;
	BufferedReader in, input, serverInput;
	String message, serverMessage;
	int port = 32000;

	/* 
	 * Constructor for JSwing Setup
	 * */
	public Client_Spell_Check() {
		initComponents();
	}
	/*
	 * Will receive request from event listener for sending data to server
	 */
	private void sendingRequest(){
		/* Request Connection Intialization */
		initiateConnection();
		/* Sending Request to Server */
		try {
			//Reading from text field
			message = textField1.getText();
			if (message.isEmpty()) {
				textArea1.append("Error: Enter text before sending to server. \n");
				return;
			}
			textField1.setText("");
			//Sending to server
			out.println(message);
			textArea1.append("Client Request > " + message + "\n");
			//Reading response from server
			serverMessage = serverInput.readLine();
			textArea1.append("Server Response > " + serverMessage + "\n");
			textArea1.append("Terminating Connection \n");
			textArea1.append("--------------------------------------\n");
		} catch (UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		/* Closing connection - Closing Socket */
		try {
			out.close();
			serverInput.close();
			requestSocket.close();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
	/* Start the Client on a Particular Socket */
	private void initiateConnection() {
		// creating a socket to connect to the server
		try {
			requestSocket = new Socket("127.0.0.1", port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		textArea1.append("--------------------------------------\n");
		textArea1.append("Connected to Server\n");
		// 2. get Input and Output streams
		try {
			out = new PrintWriter(requestSocket.getOutputStream(), true);
			serverInput = new BufferedReader(new InputStreamReader(requestSocket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	* Starting point of the application
	**/
	public static void main(String[] args) {
		Client_Spell_Check client = new Client_Spell_Check();
		client.setVisible(true);
	}
	/* 
     * Swing Event Listener 
     * */
	//Exit Button
	private void exitButtonMouseClicked(MouseEvent e) {
		System.exit(0);
	}
	//Send Button 
	private void sendButtonMouseClicked(MouseEvent e) {
		sendingRequest();
	}
	//On Key Press (Enter Key)
	private void textField1KeyPressed(KeyEvent e) {
		/* Enter Key -> Send Request */
		if(e.getKeyCode()==10)
			sendingRequest();
	}

	/*
	* Swing UI setup
	* */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Akshay Sarkar
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		scrollPane1 = new JScrollPane();
		textArea1 = new JTextArea("");
		buttonBar = new JPanel();
		textField1 = new JTextField();
		okButton = new JButton();
		cancelButton = new JButton();

		textArea1.setEditable(false);
		textArea1.setLineWrap(true);

		//======== this ========
		setTitle("Client");
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));

			// JFormDesigner evaluation mark
			dialogPane.setBorder(new javax.swing.border.CompoundBorder(
				new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
					"JFormDesigner Evaluation", javax.swing.border.TitledBorder.CENTER,
					javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
					java.awt.Color.red), dialogPane.getBorder())); dialogPane.addPropertyChangeListener(new java.beans.PropertyChangeListener(){public void propertyChange(java.beans.PropertyChangeEvent e){if("border".equals(e.getPropertyName()))throw new RuntimeException();}});

			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(null);

				//======== scrollPane1 ========
				{
					scrollPane1.setViewportView(textArea1);
				}
				contentPanel.add(scrollPane1);
				scrollPane1.setBounds(5, 5, 365, 170);

				{ // compute preferred size
					Dimension preferredSize = new Dimension();
					for(int i = 0; i < contentPanel.getComponentCount(); i++) {
						Rectangle bounds = contentPanel.getComponent(i).getBounds();
						preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
						preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
					}
					Insets insets = contentPanel.getInsets();
					preferredSize.width += insets.right;
					preferredSize.height += insets.bottom;
					contentPanel.setMinimumSize(preferredSize);
					contentPanel.setPreferredSize(preferredSize);
				}
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
				buttonBar.setLayout(new GridBagLayout());
				((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
				((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

				//---- textField1 ----
				textField1.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						textField1KeyPressed(e);
					}
				});
				buttonBar.add(textField1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- okButton ----
				okButton.setText("Send");
				okButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						sendButtonMouseClicked(e);
					}
				});
				buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- cancelButton ----
				cancelButton.setText("Exit");
				cancelButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						exitButtonMouseClicked(e);
					}
				});
				buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Akshay Sarkar
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JScrollPane scrollPane1;
	private JTextArea textArea1;
	private JPanel buttonBar;
	private JTextField textField1;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
