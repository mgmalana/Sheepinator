package NewServer;

import java.awt.Point;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket; // Imported because the Socket class is needed
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import model.ClientServerConnection;
 
public class SheepServer {	
 
    public static final int COORDINATOR_PORT = 1235;
    public static final String HOST = "localhost";
    public static final int SIZE_FROM_CLIENT =3;
    
    private ServerReceiverThread receiver;
    private ServerSenderThread sender;
    public static final int PORT = 1234;
    private static Set <ClientServerConnection> clients = Collections.newSetFromMap(new ConcurrentHashMap<ClientServerConnection,Boolean>());
    private DatagramSocket serverSocket;
    private ExecutorService executor;
    private Set <Point> noGrass = Collections.newSetFromMap(new ConcurrentHashMap<Point,Boolean>());
    private List<byte[]> toSendToCoordinator;
    private DatagramSocket udpClientSocket;
    
    public SheepServer(){
        try {
            serverSocket = new DatagramSocket(PORT);
        } catch (SocketException ex) {
            throw new RuntimeException("Error: " + ex.getMessage());
        }
        try {
            InetAddress ia = InetAddress.getByName(HOST);
            sender = new ServerSenderThread(this, ia, COORDINATOR_PORT);
            receiver = new ServerReceiverThread(this, sender.getSocket());
            udpClientSocket = sender.getSocket();
        } catch (UnknownHostException | SocketException ex) {
            throw new RuntimeException("Error: " + ex.getMessage());
        }
        
        executor = Executors.newFixedThreadPool(20);
        toSendToCoordinator = new LinkedList<>();
    }

    public void start() throws IOException{
        SheepServerThread.setStaticSheepServer(this); //sets the static server
        ScheduledExecutorService scheduleExecutor = Executors.newSingleThreadScheduledExecutor();

        
        //sender.start();
        receiver.start();
        
        registerToCoordinator();
        scheduleExecutor.scheduleWithFixedDelay(sender, 0, 400, TimeUnit.MILLISECONDS);
        
        while(true) {
            // Create byte buffers to hold the messages to send and receive
            byte[] receiveData = new byte[1024];          
            // Create an empty DatagramPacket packet
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            
            //scheduleExecutor
            // Block until there is a packet to receive, then receive it  (into our empty packet)
            serverSocket.receive(receivePacket);
            
            byte[] bytesToSendToCoordinator = new byte[receivePacket.getLength()];
            System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), bytesToSendToCoordinator, 0, receivePacket.getLength());
            
            this.addToSendToCoordinatorQueue(bytesToSendToCoordinator);
            executor.execute(new SheepServerThread(receivePacket));
        }
    }
    
    
    
    public List<byte[]> getToSendToCoordinator(){
        return toSendToCoordinator;
    }
    
    public void sendToClients(ClientServerConnection client, byte[] message){
        byte[] toSendToClients;
        
        if(client == null){
            toSendToClients = prepareToByteArray(-1, message);
        } else {
            toSendToClients = prepareToByteArray(client.getID(), message);        
        }
        for(ClientServerConnection c : clients){
            
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

    
    public void addClient(ClientServerConnection client){
        clients.add(client);
    }
    
    public Set <ClientServerConnection> getClients(){
        return clients;
    }

    public void addNoGrass(int x, int y){
        noGrass.add(new Point(x, y));
    }

    public synchronized byte[][] emptyToSendToCoordinator(){
        byte[][] temp = toSendToCoordinator.toArray(new byte[toSendToCoordinator.size()][SIZE_FROM_CLIENT]);
        toSendToCoordinator.clear();
        return temp;
    }
    
    public synchronized void addToSendToCoordinatorQueue(byte[] toSend){
        toSendToCoordinator.add(toSend);
    }
    
    private void registerToCoordinator() throws IOException {
            byte[] data = {'a'};
            InetAddress address = InetAddress.getByName(HOST);

            
            DatagramPacket blankPacket = new DatagramPacket(data,data.length , address, COORDINATOR_PORT);
            udpClientSocket.send(blankPacket);
    }
    
    public static void main(String args[]){ 
        System.out.println("[SheepServer] started...");
        SheepServer server = new SheepServer();
        try {
            server.start();
        } catch (IOException ex) {
            throw new RuntimeException("[SheepServer] Error: " + ex.getMessage());
        }

    }

}
