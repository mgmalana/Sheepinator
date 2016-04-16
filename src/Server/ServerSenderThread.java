package Server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
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
    private ServerSenderRunnable serverSenderRun;
    
    public ServerSenderThread(SheepServer server, InetAddress address, DatagramSocket serverSocket) throws SocketException {
        this.serverIPAddress = address;
        this.serverport = serverSocket;
        this.server = server;
        // Create client DatagramSocket
        this.udpClientSocket = serverport;
        serverSenderRun = new ServerSenderRunnable();
    }
    public void halt() {
        this.stopped = true;
    }
    public DatagramSocket getSocket() {
        return this.udpClientSocket;
    }
 
    @Override
    public void run() {                     
        Executor executor = Executors.newFixedThreadPool(SheepServer.NUM_THREADS_SENDER);
        
        while(true){
            executor.execute(serverSenderRun);
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerSenderThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
       
    }
    
    public void sendToClients(byte[] sendData) throws IOException{
        for(ClientServerConnection c : server.getClients().values()){
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, c.getAddress(), c.getPort());
            udpClientSocket.send(sendPacket);
        }
    }
    
    public class ServerSenderRunnable implements Runnable{
    
        @Override
        public void run() {
            byte[] sendData;
            try {
                sendData = server.handleMessages();
                if(sendData.length > 0){
                    sendToClients(sendData);
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIPAddress , SheepServer.COORDINATOR_PORT);
                    udpClientSocket.send(sendPacket);
                }
            } catch (IOException ex) {
                Logger.getLogger(ServerSenderRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    
    }
}   
 
