package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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
    private Sheep sheep;
    
    public SenderThread(Sheep sheep, InetAddress address, int serverport) throws SocketException {
        this.serverIPAddress = address;
        this.serverport = serverport;
        // Create client DatagramSocket
        this.udpClientSocket = new DatagramSocket();
        this.udpClientSocket.connect(serverIPAddress, serverport);
        this.sheep = sheep;
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
            
            // Create input stream
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            while (true) 
            {
                if (stopped)
                    return;
 
                // Message to send
                String clientMessage = inFromUser.readLine();
 
                if (clientMessage.equals(".") || clientMessage.length() == 0)
                    break;
                char inputChar = clientMessage.charAt(0);
                
                if(inputChar != 'a' || inputChar != 'w' 
                        || inputChar != 's' || inputChar != 'd'
                        || inputChar != 'j'){
                    //move sheep according to input
                    moveSheep(inputChar);
                    byte[] sendData = getSheepPositionInBytes();
                    
                    // Create a DatagramPacket with the data, IP address and port number
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIPAddress, serverport);

                    // Send the UDP packet to server
                    //System.out.println("I just sent: "+sendData);
                    udpClientSocket.send(sendPacket);
                }
 
                Thread.yield();
            }
        }
        catch (IOException ex) {
            System.err.println(ex);
        }
    }

    private byte[] getSheepPositionInBytes() {
        return new byte[]{(byte)sheep.getxPosition(), (byte)sheep.getyPosition()};
    }

    private void sendInitialPositionToServer() throws IOException {
            byte[] data = getSheepPositionInBytes();
            DatagramPacket blankPacket = new DatagramPacket(data,data.length , serverIPAddress, serverport);
            udpClientSocket.send(blankPacket);
    }

    private void moveSheep(char input) {
        switch(input){
            case 'w':
                    sheep.goUp();
                    break;
            case 's':
                    sheep.goDown();
                    break;
            case 'd':
                    sheep.goRight();
                    break;
            case 'a':
                    sheep.goLeft();
                    break;
            /*case 'J':
            case 'j':
                    noGrass.add(new Point(sheep.getxPosition(), sheep.getyPosition()));
                    ID = -1; //tells that the update is for the grass
                    break;
*/
        }

    }
}   
 
