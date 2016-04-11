package Server;

import java.awt.Point;
import java.io.IOException;
import java.net.*; // Imported because the Socket class is needed
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import model.Connection;
 
public class SheepServer {	
 
    public static final int PORT = 1234;
    private static Set <Connection> clients = Collections.newSetFromMap(new ConcurrentHashMap<Connection,Boolean>());
    private DatagramSocket serverSocket;
    private ExecutorService executor;
    private Set <Point> noGrass = Collections.newSetFromMap(new ConcurrentHashMap<Point,Boolean>());

    
    public SheepServer(){
        try {
            serverSocket = new DatagramSocket(PORT);
        } catch (SocketException ex) {
            throw new RuntimeException("Error: " + ex.getMessage());
        }
        
        executor = Executors.newFixedThreadPool(20);
    }

    public void start() throws IOException{
        SheepServerThread.setStaticSheepServer(this); //sets the static server
        
        while(true) {
            // Create byte buffers to hold the messages to send and receive
            byte[] receiveData = new byte[1024];          
            // Create an empty DatagramPacket packet
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            // Block until there is a packet to receive, then receive it  (into our empty packet)
            serverSocket.receive(receivePacket);
            // Extract the message from the packet and make it into a string, then trim off any end characters
            executor.execute(new SheepServerThread(receivePacket));
        }
    }
    
    public void sendToClients(Connection client, byte[] message){
        byte[] toSendToClients;
        
        if(client == null){
            toSendToClients = prepareToByteArray(-1, message);
        } else {
            toSendToClients = prepareToByteArray(client.getID(), message);        
        }
        for(Connection c : clients){
            
            if(!c.equals(client)){
                try {
                    // Create a DatagramPacket to send, using the buffer, the clients IP address, and the clients port
                    InetAddress address = InetAddress.getByName(c.getAddress());
                    DatagramPacket sendPacket = new DatagramPacket(toSendToClients, toSendToClients.length, address, c.getPort()); 
                        // Send the echoed message
                    serverSocket.send(sendPacket);
                } catch (IOException ex){
                    System.out.println("Error: " + ex.getMessage());
                }
            }
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

    
    public void addClient(Connection client){
        clients.add(client);
    }
    
    public Set <Connection> getClients(){
        return clients;
    }

    public void addNoGrass(int x, int y){
        noGrass.add(new Point(x, y));
    }

    
    public static void main(String args[]){ 
        System.out.println("Server started...");
        SheepServer server = new SheepServer();
        try {
            server.start();
        } catch (IOException ex) {
            throw new RuntimeException("Error: " + ex.getMessage());
        }

    }
}
