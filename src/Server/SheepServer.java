package Server;

import java.awt.Point;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket; // Imported because the Socket class is needed
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.ClientServerConnection;
import model.Message;
import model.Sheep;
 
public class SheepServer {	
 
    public static final int COORDINATOR_PORT = 1235;
    public static final String COORDINATOR_HOST = "localhost";
    
    public static final String HOST = "localhost";
    public static final int WAIT_SEND_COORDINATOR = 3000; //3 seconds to wait before sending
    public static final int SIZE_TO_SEND_TO_COORDINATOR = 6;
    
    public static final int NUM_THREADS_RECEIVER = 5; //this is actually for sending rin. forwarding from coordinator
    public static final int NUM_THREADS_SENDER = 10;

    
    private ServerReceiverThread receiver;
    private ServerSenderThread sender;
    public static final int PORT = 1234;
    private static ConcurrentHashMap <Integer, ClientServerConnection> clients = new ConcurrentHashMap<>();
    private static ConcurrentHashMap <Integer, Sheep> sheepsFromOtherServers = new ConcurrentHashMap<>();
    private DatagramSocket serverSocket;
    private ExecutorService executor;
    private Set <Point> noGrass = Collections.newSetFromMap(new ConcurrentHashMap<Point,Boolean>());
    private List<Message> messagesToSendCoordinator;
    private DatagramSocket udpClientSocket;
    
    public SheepServer(){
        try {
            serverSocket = new DatagramSocket(PORT);
        } catch (SocketException ex) {
            throw new RuntimeException("Error: " + ex.getMessage());
        }
        try {
            InetAddress ia = InetAddress.getByName(COORDINATOR_HOST);
            sender = new ServerSenderThread(this, ia, serverSocket);
            receiver = new ServerReceiverThread(this, serverSocket);
            udpClientSocket = sender.getSocket();
        } catch (UnknownHostException | SocketException ex) {
            throw new RuntimeException("Error: " + ex.getMessage());
        }
        
        executor = Executors.newFixedThreadPool(20);
        messagesToSendCoordinator = new LinkedList<>();
    }

    public void start() throws IOException{
        SheepServerThread.setStaticSheepServer(this); //sets the static server
        //ScheduledExecutorService scheduleExecutor = Executors.newScheduledThreadPool(10);
        
        
        sender.start();
        receiver.start();
        
        registerToCoordinator();
        //scheduleExecutor.scheduleWithFixedDelay(sender, 0, 1000, TimeUnit.MILLISECONDS);
        /*
        while(true) {
            // Create byte buffers to hold the messages to send and receive
            byte[] receiveData = new byte[SIZE_FROM_CLIENT];          
            // Create an empty DatagramPacket packet
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            //scheduleExecutor
            // Block until there is a packet to receive, then receive it  (into our empty packet)
            serverSocket.receive(receivePacket);
            //byte[] bytesToSendToCoordinator = new byte[SIZE_FROM_CLIENT];
            //System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), bytesToSendToCoordinator, 0, receivePacket.getLength());
            messagesToSendCoordinator.add(new Message(receivePacket.getAddress().getHostAddress(), receivePacket.getPort(), receiveData));            
        }*/
    }
    
    public synchronized void  addMessage(Message message){
        messagesToSendCoordinator.add(message);
    }
    
    public ClientServerConnection addToClients(String address, int port) throws UnknownHostException{
        InetAddress inetaddress = InetAddress.getByName(address);
        int key = ClientServerConnection.getNextID();
        ClientServerConnection c = new ClientServerConnection(inetaddress, port, key);

        c.setSheep(new Sheep());
        clients.put(key, c);
        byte[] sendData = intToByteArray(key);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, c.getAddress(), c.getPort());
        try {
            udpClientSocket.send(sendPacket);
        } catch (IOException ex) {
            Logger.getLogger(SheepServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        return c;
    }
    
    public void sendToClients(ClientServerConnection client, byte[] message){
        byte[] toSendToClients;
        /*
        if(client == null){
            toSendToClients = prepareToByteArray(-1, message);
        } else {
            toSendToClients = prepareToByteArray(client.getID(), message);        
        }
        for(ClientServerConnection c : clients.values()){
            
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
        }*/
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
    
    public ConcurrentHashMap <Integer, ClientServerConnection> getClients(){
        return clients;
    }

    public void addNoGrass(int x, int y){
        noGrass.add(new Point(x, y));
    }

    public synchronized Message[] emptyToSendToCoordinator(){
        Message[] temp = messagesToSendCoordinator.toArray(new Message[messagesToSendCoordinator.size()]);
        messagesToSendCoordinator.clear();
        return temp;
    }
    
    private void registerToCoordinator() throws IOException {
            byte[] data = {'a'};
            InetAddress address = InetAddress.getByName(HOST);

            DatagramPacket blankPacket = new DatagramPacket(data,data.length , address, COORDINATOR_PORT);
            udpClientSocket.send(blankPacket);
    }
    
    public byte[] handleMessages() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for(Message b : emptyToSendToCoordinator()){
            ClientServerConnection c = null;
            if(b.getMessage().length == 1){ //new one
                try {
                    c = addToClients(b.getAddress(), b.getPort());
                } catch (UnknownHostException ex) {
                    System.err.println("Error: " + ex.getMessage());
                }
            } else{
                c = clients.get(toInt(b.getMessage()));
                char input = (char)b.getMessage()[4];
                updateScene(c, input);
                
                if(input == 'j'){
                    outputStream.write(intToByteArray(-1));
                    outputStream.write(new byte[]{(byte)c.getSheep().getxPosition()});
                    outputStream.write(new byte[]{(byte)c.getSheep().getyPosition()});
                    continue;
                }
            }
            outputStream.write(intToByteArray(c.getId()));
            outputStream.write(new byte[]{(byte)c.getSheep().getxPosition()});
            outputStream.write(new byte[]{(byte)c.getSheep().getyPosition()});
        }
        return outputStream.toByteArray();
    }
    
    
    
    private void updateScene(ClientServerConnection c, char input){
        Sheep sheep = c.getSheep();
        switch(input){
                case 'W':
                case 'w':
                        sheep.goUp();
                        break;
                case 'S':
                case 's':
                        sheep.goDown();
                        break;
                case 'D':
                case 'd':
                        sheep.goRight();
                        break;
                case 'A':
                case 'a':
                        sheep.goLeft();
                        break;
                case 'J':
                case 'j':
                    noGrass.add(new Point(sheep.getxPosition(), sheep.getyPosition()));
                    break;
        }
    }
    
    private int toInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
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

    public void updateSceneAndForwardToClient(byte[] receiveData) {
        int currentIndex = 0;
        while(currentIndex + 6 <= receiveData.length){
            byte [] keyByteArray = Arrays.copyOfRange(receiveData, 0 + currentIndex, 4 + currentIndex);
            int key = toInt(keyByteArray);
            int x = receiveData[4 + currentIndex] & 0xFF;
            int y = receiveData[5 + currentIndex] & 0xFF;

            System.out.println("sheep: " + key + " x: " + x + " y: " + y);

            if(key == -1){
                noGrass.add(new Point(x, y));
            } else if(sheepsFromOtherServers.contains(key)){
                Sheep sheepThis = sheepsFromOtherServers.get(key);
                sheepThis.setXYPosition(x, y);
            } else{
                Sheep temp = new Sheep(x, y);
                sheepsFromOtherServers.put(key, temp);
            }

            currentIndex+=6;
        }
        
        try {
            sender.sendToClients(receiveData);
        } catch (IOException ex) {
            Logger.getLogger(SheepServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    

}
