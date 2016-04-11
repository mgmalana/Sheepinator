package sheepinator;

import Client.SheepClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import model.Sheep;

public class MmoLoadTester {
	public static final int CLIENT_INSTANCES = Sheep.MAX_NUM_SHEEP;
	//public static final String SERVER_IP = "10.100.198.29";
	
	public static void main(String[] args) {
		ExecutorService executor = Executors.newFixedThreadPool(CLIENT_INSTANCES);
		
		// If we will do yung parang multi-machine, remove this na lang para
		// ibang machine si server.
		//new ChatServer().start();
		
		long startTime1 = System.currentTimeMillis();
		System.out.println("LOAD TESTER: Testing Start Time is (" + startTime1 + " ms.).");
		
		for (int i = 0; i < CLIENT_INSTANCES; i++) {
			System.out.println("LOAD TESTER: Running Client: SheepClient(" + i + ").");
			Runnable client = new SheepClient();
	        
			executor.execute(client);
		}
		
		executor.shutdown();
		
		// Wait until all threads are finish
		while (!executor.isTerminated()) {
 
		}
		
		long endTime1 = System.currentTimeMillis();
		System.out.println("LOAD TESTER: Testing Start Time is (" + endTime1 + " ms.).");
		
		System.out.println("LOAD TESTER: All (" + CLIENT_INSTANCES + ") Clients have successfuly transacted with the server.");
		System.out.println("LOAD TESTER: Time elapsed of Load Testing is (" + (endTime1 - startTime1) + " ms.).");
	}

}
