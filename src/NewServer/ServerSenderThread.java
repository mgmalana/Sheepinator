package NewServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.ClientServerConnection;
import model.Message;

/**
 *
 * @author mgmalana
 */
class ServerSenderThread extends Thread {
 
    private InetAddress serverIPAddress;
    private DatagramSocket udpClientSocket;
    private boolean stopped = false;
    private DatagramSocket serverport;
    private SheepServer server;
    
    public ServerSenderThread(SheepServer server, InetAddress address, DatagramSocket serverSocket) throws SocketException {
        this.serverIPAddress = address;
        this.serverport = serverSocket;
        this.server = server;
        // Create client DatagramSocket
        this.udpClientSocket = serverport;
    }
    public void halt() {
        this.stopped = true;
    }
    public DatagramSocket getSocket() {
        return this.udpClientSocket;
    }
 
    public void run() {                     
            
            while(true){

                
                try {
                    byte[] sendData = server.handleMessages();
                    
                    if(sendData.length > 0){
                        for(ClientServerConnection c : server.getClients().values()){
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, c.getAddress(), c.getPort());
                            udpClientSocket.send(sendPacket);
                        }
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIPAddress , SheepServer.COORDINATOR_PORT);
                        udpClientSocket.send(sendPacket);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ServerSenderThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
       
    }
}   
 
