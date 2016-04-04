package sheepinator;

/**
 *
 * @author mgmalana
 */
import sheepinator.SheepClient;
import java.net.*;
import java.io.*;

public class SheepClientThread extends Thread {  
    private Socket socket = null;
    private SheepClient client = null;
    private BufferedReader streamIn = null;

    public SheepClientThread(SheepClient _client, Socket _socket)  {  client = _client;
        socket = _socket;
        open();  
        start();
    }
    public void open() {
        try {
            //streamIn = new DataInputStream(socket.getInputStream());
            streamIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch(IOException ioe) {
            System.out.println("Error getting input stream: " + ioe);
            client.stop();
        }
    }
    public void close()
    {  
        try {
            if (streamIn != null) streamIn.close();
        } catch(IOException ioe) {
            System.out.println("Error closing input stream: " + ioe);
        }
    }
    public void run() {
        while (true) {
            try
            {
                client.handle(streamIn.readLine());
            }
            catch(IOException ioe)
            {  System.out.println("Listening error: " + ioe.getMessage());
               client.stop();
            }
       }
    }
}