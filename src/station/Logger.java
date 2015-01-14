package station;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class Logger {

	private java.util.logging.Logger logger;
	private FileHandler fh;

	public Logger(String name) {
		int index = 0;
		File file;
		do {
			index++;
			file = new File(name+index+".log");
		} while (file.exists());

		logger = java.util.logging.Logger.getLogger(name);
		try {
			fh = new FileHandler(name+index+".log");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printMessages(List<Message> allReceivedMessage) {
		for (Message message : new ArrayList<Message>(allReceivedMessage)) {
			if (!message.isKollision()) 
				logger.info(message.toString());
			else
				logger.info("<<<<< Kollision >>>>> Slot ("+message.getSendingSlot()+")");
		}
	}
	
	public void printMessages(Message message) {
		if (!message.isKollision()) 
			logger.info(message.toString());
		else
			logger.info("<<<<< Kollision >>>>> Slot ("+message.getSendingSlot()+")");		
	}
		

}
