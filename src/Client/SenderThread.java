package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;
import model.Sheep;

/**
 *
 * @author mgmalana
 */
class SenderThread extends Thread {
 
    private InetAddress serverIPAddress;
    private DatagramSocket udpClientSocket;
    private boolean stopped = false;
    private int serverport;
    private SheepClient sheepClient;
    
    public SenderThread(SheepClient sheepClient, InetAddress address, int serverport) throws SocketException {
        this.serverIPAddress = address;
        this.serverport = serverport;
        // Create client DatagramSocket
        this.udpClientSocket = new DatagramSocket();
        this.udpClientSocket.connect(serverIPAddress, serverport);
        this.sheepClient = sheepClient;
    }
    public void halt() {
        this.stopped = true;
    }
    public DatagramSocket getSocket() {
        return this.udpClientSocket;
    }
 
    public void run() {       
        try {    
            sendInitialPositionToServer();
            Random random = new Random();
                        
            while (true) 
            {
                if (stopped)
                    return;
 
                // Message to send
 
                char[] inputChoices = {'w', 's', 'a', 'd', 'j'};
                char inputChar = inputChoices[random.nextInt(inputChoices.length)];

                try {
                    Thread.sleep(500);                 //1000 milliseconds is one second.
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                
                /*
                if (clientMessage.equals(".")){
                    break;
                }*/
                /*String clientMessage = inFromUser.readLine();

                if (clientMessage.length() == 0){
                    continue;
                }
                char inputChar = clientMessage.charAt(0);
                */
                
                if(inputChar != 'a' || inputChar != 'w' 
                        || inputChar != 's' || inputChar != 'd'
                        || inputChar != 'j'){
                    //move sheep according to input                    
                    byte[] sendData = sheepClient.prepareSendToServer(inputChar);
                    
                    // Create a DatagramPacket with the data, IP address and port number
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIPAddress, serverport);

                    udpClientSocket.send(sendPacket);
                    System.out.println("Message sent: " + sendData);
                }
 
                Thread.yield();
            }
        }
        catch (IOException ex) {
            System.err.println(ex);
        }
    }

    private void sendInitialPositionToServer() throws IOException {
            //send out an empty one first
            DatagramPacket sendPacket = new DatagramPacket(new byte[]{(byte)'0'}, 1, serverIPAddress, serverport);

            // Send the UDP packet to server
            //System.out.println("I just sent: "+sendData);
            udpClientSocket.send(sendPacket);
            byte[] receiveData = new byte[4];
            
            sendPacket = new DatagramPacket(receiveData, receiveData.length);
            
            do{
                udpClientSocket.receive(sendPacket);
            } while(sendPacket.getLength() != 4);
            
            sheepClient.setIdAndStartReceiving(receiveData);
    }
}   
 
