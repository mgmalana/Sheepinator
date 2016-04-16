package Coordinator;

import java.net.DatagramPacket;
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
    }    
}
