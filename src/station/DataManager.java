package station;

public class DataManager {

	private char[] dataBuffer;
	private boolean hasNextData = false;
 
	
	public DataManager() {
	}
	
	public boolean hasNextData() {
		return this.hasNextData ;
	}

	public char[] getData() {
		this.hasNextData = false;
		return dataBuffer;
	}

	public void setDataBuffer(char[] dataBuffer) {
		this.hasNextData = true;
		this.dataBuffer = dataBuffer;
	}
	
}
