import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author AKSHAY SARKAR - axs6793 - 1001506793
 */

public class ClientData implements Serializable{
	/**
	 * Object to store Client/Node Data Together
	 */	

	private static final long serialVersionUID = 1L;
	
	String name;
	int Process_id;
	ObjectOutputStream writer;
	ObjectInputStream reader;
	
	public ClientData(String clientName, int processID){
		super();
		this.name = clientName;
		Process_id = processID;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getProcess_id() {
		return Process_id;
	}
	public void setProcess_id(int process_id) {
		Process_id = process_id;
	}

	public ObjectOutputStream getWriter() {
		return writer;
	}

	public void setWriter(ObjectOutputStream writer) {
		this.writer = writer;
	}

	public ObjectInputStream getReader() {
		return reader;
	}

	public void setReader(ObjectInputStream reader) {
		this.reader = reader;
	}

}
