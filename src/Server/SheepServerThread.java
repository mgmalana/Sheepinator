package Server;

import java.net.DatagramPacket;
import java.net.InetAddress;
import model.Connection;
import model.Sheep;

/**
 *
 * @author mgmalana
 */
public class SheepServerThread implements Runnable{
    private DatagramPacket receivePacket;
    private static SheepServer sheepServer;
    
    public SheepServerThread(DatagramPacket receivePacket){
        this.receivePacket = receivePacket;
    }
    
    @Override
    public void run() {
            byte[] clientMessage = receivePacket.getData();
            int x = clientMessage[0] & 0xFF;
            int y = clientMessage[1] & 0xFF;
            boolean isClientNew = true;
            
            // Get the port number which the recieved connection came from
            // Get the IP address and the the port number which the received connection came from            
            Connection client = new Connection(receivePacket.getAddress().getHostAddress(), receivePacket.getPort());            
            for(Connection c : sheepServer.getClients()){
                if(c.equals(client)){
                    client = c;
                    isClientNew = false;
                    break;
                }
            }
            
            if(isClientNew){
                System.out.println("Adding "+ client.getAddress() + " with port " + client.getPort());
                sheepServer.addClient(client);
                client.setSheep(new Sheep(x, y));
                client.setID(Connection.getNextID());
            }
            
            // Message		
            System.out.println(clientMessage);
            //System.out.println("clientID+ " + client.getID()); 
            // Create an empty buffer/array of bytes to send back 
            sheepServer.sendToClients(client, clientMessage);
    }    
    
    public static void setStaticSheepServer(SheepServer server){
        sheepServer = server;
    }    
}
