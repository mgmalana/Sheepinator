package NewServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
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

    public void run() {
 
        // Create a byte buffer/array for the receive Datagram packet
 
        while (true) {            
            if (stopped)
                return;
            
            byte[] receiveData = new byte[5];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                udpClientSocket.receive(receivePacket);
                //System.out.println("Messaged received: " + receivePacket.getData());
                if(receivePacket.getLength() == 1){
                    receiveData = new byte[1];
                    System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), receiveData, 0, receivePacket.getLength());
                }
                sheepServer.addMessage(new Message(receivePacket.getAddress().getHostAddress(), receivePacket.getPort(), receiveData));             
            } catch (IOException ex) {
                System.err.println("Error: " + ex.getMessage());
            }
        }
    }
}
