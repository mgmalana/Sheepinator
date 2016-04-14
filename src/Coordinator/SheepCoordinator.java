package Coordinator;

import java.awt.Point;
import java.io.IOException;
import java.net.*; // Imported because the Socket class is needed
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import model.ServerCoordinatorConnection;
 
public class SheepCoordinator {	
 
    public static final int PORT = 1235;
    private DatagramSocket serverSocket;
    private ExecutorService executor;
    private static Set <ServerCoordinatorConnection> servers = Collections.newSetFromMap(new ConcurrentHashMap<ServerCoordinatorConnection,Boolean>());

    
    public SheepCoordinator(){
        try {
            serverSocket = new DatagramSocket(PORT);
        } catch (SocketException ex) {
            throw new RuntimeException("Error: " + ex.getMessage());
        }
        
        executor = Executors.newFixedThreadPool(20);
    }

    public void start() throws IOException{        
        while(true) {
            // Create byte buffers to hold the messages to send and receive
            byte[] receiveData = new byte[1024];          
            // Create an empty DatagramPacket packet
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            // Block until there is a packet to receive, then receive it  (into our empty packet)
            serverSocket.receive(receivePacket);
            //System.out.println("Packet size received: " + receivePacket.getLength());
            // Extract the message from the packet and make it into a string, then trim off any end characters
            executor.execute(new SheepCoordinatorThread(this, receivePacket));
        }
    }
    
    private byte[] prepareToByteArray(int ID, byte[] message){
        byte[] finalArray = new byte[6];
        byte[] a1 = intToByteArray(ID);
        
        System.arraycopy(a1, 0, finalArray, 0, 4);        
       
        finalArray[4] = message[0];
        finalArray[5] = message[1];
        return finalArray;

    }
    
    private byte[] intToByteArray(int value) {
    return new byte[] {
            (byte)(value >> 24),
            (byte)(value >> 16),
            (byte)(value >> 8),
            (byte)value};
    }
    
    public static void main(String args[]){ 
        System.out.println("[SheepCoordinator] started...");
        SheepCoordinator server = new SheepCoordinator();
        try {
            server.start();
        } catch (IOException ex) {
            throw new RuntimeException("[SheepCoordinator] Error: " + ex.getMessage());
        }

    }

    public void handle(DatagramPacket receivePacket) {
            byte[] serverMessage = receivePacket.getData();
            ServerCoordinatorConnection server = new ServerCoordinatorConnection(receivePacket.getAddress().getHostAddress(), receivePacket.getPort());                 
            
            if(receivePacket.getLength()==1 && !servers.contains(server)){
                addServerCoordinatorConnection(server);
            } else {
                sendToServers(server, serverMessage);
            }
    }
    
    public void addServerCoordinatorConnection(ServerCoordinatorConnection c){
        System.out.println("[SheepCoordinator] Adding "+ c.getAddress() + " with port " + c.getPort());
        if(servers.add(c)){
            c.setID(ServerCoordinatorConnection.getNextID());
        }
    }
    
    public void sendToServers(ServerCoordinatorConnection client, byte[] message){
        byte[] toSendToClients = prepareToByteArray(client.getID(), message);        
        
        for(ServerCoordinatorConnection c : servers){
            
            if(!c.equals(client)){
                try {
                    // Create a DatagramPacket to send, using the buffer, the clients IP address, and the clients port
                    InetAddress address = InetAddress.getByName(c.getAddress());
                    DatagramPacket sendPacket = new DatagramPacket(toSendToClients, toSendToClients.length, address, c.getPort()); 
                        // Send the echoed message
                    serverSocket.send(sendPacket);
                } catch (IOException ex){
                    System.out.println("[SheepCoordinator] Error: " + ex.getMessage());
                }
            }
        }
    }
    
}
