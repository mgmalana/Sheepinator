package Server;

import Server.Runnables.ServerForwarder;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import model.Message;

/**
 *
 * @author mgmalana
 */
public class ServerReceiverThread extends Thread {
    public static final int SIZE_FROM_CLIENT = 5;

    
    private DatagramSocket udpClientSocket;
    private boolean stopped = false;
    private SheepServer sheepServer;

    public ServerReceiverThread(SheepServer sheepServer, DatagramSocket ds) throws SocketException {
        this.udpClientSocket = ds;
        this.sheepServer = sheepServer;
    }
 
    public void halt() {
        this.stopped = true;
    }

    @Override
    public void run() {
        Executor executor = Executors.newFixedThreadPool(SheepServer.NUM_THREADS_RECEIVER);
        
        while (true) {            
            if (stopped)
                return;
            
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                udpClientSocket.receive(receivePacket);
                //System.out.println("Messaged received: " + receivePacket.getData());
                if(receivePacket.getLength() == 1){
                    receiveData = new byte[1];
                    System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), receiveData, 0, receivePacket.getLength());
                } else if(receivePacket.getLength() == 5){
                    receiveData = new byte[5];
                    System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), receiveData, 0, receivePacket.getLength());
                } else if(receivePacket.getLength() > 5){
                    receiveData = new byte[receivePacket.getLength()];
                    System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), receiveData, 0, receivePacket.getLength());
                    ServerForwarder forward = new ServerForwarder(sheepServer, receiveData);
                    executor.execute(forward);
                }
                sheepServer.addMessage(new Message(receivePacket.getAddress().getHostAddress(), receivePacket.getPort(), receiveData));             
            } catch (IOException ex) {
                System.err.println("Error: " + ex.getMessage());
            }
        }
    }    
}
