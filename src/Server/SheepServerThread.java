package Server;

import java.net.DatagramPacket;
import java.net.InetAddress;
import model.ClientServerConnection;
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
    public void run() {/*
            byte[] clientMessage = receivePacket.getData();
            int isSheep = clientMessage[0] & 0xFF;
            System.out.println("[SheepServer] Message Received: " + clientMessage);
            clientMessage = new byte[]{clientMessage[1], clientMessage[2]};
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
            
            // Message		
            //System.out.println(clientMessage);
            //System.out.println("clientID+ " + client.getID()); 
            // Create an empty buffer/array of bytes to send back 
    }    
    
    public static void setStaticSheepServer(SheepServer server){
        sheepServer = server;
    }    
}
