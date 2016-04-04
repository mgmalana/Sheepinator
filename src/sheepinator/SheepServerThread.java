package sheepinator;

import java.net.*;
import java.io.*;

public class SheepServerThread extends Thread {  
    private Socket socket = null;
    private SheepServer server   = null;
    private int ID = -1;
    private BufferedReader streamIn =  null;
    private PrintStream streamOut = null;

    public SheepServerThread(SheepServer _server, Socket _socket) {  
        super();
        server = _server;  
        socket = _socket;  
        ID = socket.getPort();
    }
    
    public void run() {   
        System.out.println("Server Thread " + ID + " running.");
        while (true){
            try {
                server.handle(ID, streamIn.readLine());
            } catch(IOException ioe) { 
                System.out.println(ID + " ERROR reading: " + ioe.getMessage());
                server.remove(ID);
                stop();
            }
        }
    }
    
    public void open() throws IOException {
        streamIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        streamOut = new PrintStream(new BufferedOutputStream(socket.getOutputStream()));
    }
    public void close() throws IOException {
        if (socket != null)    
            socket.close();
        if (streamIn != null)  
            streamIn.close();
        if (streamOut != null)  
            streamOut.close();
    }
    
    public int getID()
    {
        return ID;
    }
    
    public void send(String msg) {
        streamOut.println(msg);
        streamOut.flush();
   }

}