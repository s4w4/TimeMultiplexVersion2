package station;


import java.io.IOException;
import java.io.InputStreamReader;

public class DataSourceListener extends Thread{
	 

	private final int DATA_SIZE = 24;
	private DataManager dataManager;


	public DataSourceListener(DataManager dataManager ){ 
		this.dataManager = dataManager;
	}
	
	public void run() {		
		InputStreamReader input = new InputStreamReader(System.in);
		while(true){
			char[] dataBuffer = new char[DATA_SIZE];
			try {
				
				input.read(dataBuffer);
				dataManager.setDataBuffer(dataBuffer);
//				printCharArray(dataBuffer);	//ausgabe
				 
			} catch (IOException e) { 
				e.printStackTrace();
			} 
		}
	}
	
	
	/**
	 * gibt ein CharArray in Console aus
	 * @param charArray
	 */
	private void printCharArray(char[] charArray){
		for (int i = 0; i < 24; i++)
			System.out.print(charArray[i]);			
		System.out.println();
	}
}
