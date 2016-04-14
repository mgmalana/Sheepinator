package NewServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import model.Message;

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
                     
            for(Message b : server.emptyToSendToCoordinator()){
                byte[] clientMessage = b.getMessage();
                int isSheep = clientMessage[0] & 0xFF;
                clientMessage = new byte[]{clientMessage[1], clientMessage[2]};
                
                if(isSheep == 0) {
                    
                } else {
                
                }
                
            }        
            if(toSendList.size()!=0){
                byte[] sendData = toSendList.toByteArray();

                // Create a DatagramPacket with the data, IP address and port number
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIPAddress, serverport);

                udpClientSocket.send(sendPacket);
                System.out.println("sent to coordinator: " + sendData.length);
            }
            /*
            
            if(isSheep == 0){ //if grass yung input
                sheepServer.sendToClients(null, clientMessage);

                //sheepServer.addNoGrass(x, y); //TODO: idk pabagal lang sya eh
            } else { //if sheep movement
                // Get the port number which the recieved connection came from
                // Get the IP address and the the port number which the received connection came from            
                ClientServerConnection client = new ClientServerConnection(receivePacket.getAddress().getHostAddress(), receivePacket.getPort());                 
                boolean isClientNew = true;            
                int x = clientMessage[0] & 0xFF;
                int y = clientMessage[1] & 0xFF;
                for(ClientServerConnection c : sheepServer.getClients()){
                    if(c.equals(client)){
                        client = c;
                        isClientNew = false;
                        break;
                    }
                }       
                
                if(isClientNew){
                    System.out.println("[SheepServer] Adding "+ client.getAddress() + " with port " + client.getPort());
                    sheepServer.addClient(client);
                    client.setSheep(new Sheep(x, y));
                    client.setID(ClientServerConnection.getNextID());
                }
                
                sheepServer.sendToClients(client, clientMessage);

            }
            */
//            Thread.yield();
        }
        catch (IOException ex) {
            System.err.println(ex);
        }
    }
}   
 
