import java.util.ArrayList;
import java.util.List;

import station.Logger;
import station.Message;

public class AppLogger {
	public static void main(String[] args) throws InterruptedException {
		Logger log = new Logger("Log");
		List<Message> messages = new ArrayList<Message>();
		Message m = new Message('b');
		messages.add(m);
		while(true){
			log.printMessages(messages, 0, 0);
			Thread.sleep(1000);
		}
	}
}
