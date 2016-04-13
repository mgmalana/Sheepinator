package Coordinator;

import java.net.DatagramPacket;
import java.net.InetAddress;
import model.ClientServerConnection;
import model.ServerCoordinatorConnection;
import model.Sheep;

/**
 *
 * @author mgmalana
 */
public class SheepCoordinatorThread implements Runnable{
    private DatagramPacket receivePacket;
    private SheepCoordinator sheepCoordinator;
    
    public SheepCoordinatorThread(SheepCoordinator sheepCoordinator, DatagramPacket receivePacket){
        this.receivePacket = receivePacket;
        this.sheepCoordinator = sheepCoordinator;
    }
    
    @Override
    public void run() {
            sheepCoordinator.handle(receivePacket);
           

            // Message		
            //System.out.println(serverMessage);
            //System.out.println("clientID+ " + server.getID()); 
            // Create an empty buffer/array of bytes to send back 
    }    
}
