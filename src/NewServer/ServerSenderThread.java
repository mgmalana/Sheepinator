package NewServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 *
 * @author mgmalana
 */
class ServerSenderThread implements Runnable {
 
    private InetAddress serverIPAddress;
    private DatagramSocket udpClientSocket;
    private boolean stopped = false;
    private int serverport;
    private SheepServer server;
    
    public ServerSenderThread(SheepServer server, InetAddress address, int serverport) throws SocketException {
        this.serverIPAddress = address;
        this.serverport = serverport;
        this.server = server;
        // Create client DatagramSocket
        this.udpClientSocket = new DatagramSocket();
        this.udpClientSocket.connect(serverIPAddress, serverport);
    }
    public void halt() {
        this.stopped = true;
    }
    public DatagramSocket getSocket() {
        return this.udpClientSocket;
    }
 
    public void run() {       
        try {
            ByteArrayOutputStream toSendList = new ByteArrayOutputStream();            
                     
            for(byte[] b : server.emptyToSendToCoordinator()){
                toSendList.write(b);
            }        
            if(toSendList.size()!=0){
                byte[] sendData = toSendList.toByteArray();

                // Create a DatagramPacket with the data, IP address and port number
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIPAddress, serverport);

                udpClientSocket.send(sendPacket);
            }
//            Thread.yield();
        }
        catch (IOException ex) {
            System.err.println(ex);
        }
    }
}   
 
