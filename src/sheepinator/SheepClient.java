package sheepinator;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import model.Sheep;

public class SheepClient implements Runnable{
    private Socket socket = null;
    private BufferedReader streamIn =  null;
    private DataOutputStream streamOut = null;
    private SheepClientThread client    = null;
    private Thread thread = null;
    private static final int SERVERPORT = 1234;
    private static final String SERVERNAME = "0.0.0.0";
    private HashMap<Integer, Sheep> sheeps = new HashMap<>();
    private HashSet <Point> noGrass = new HashSet<>();
    private JFrame frame;
    private ImageCanvas canvas;

    public SheepClient() {  
        System.out.println("Establishing connection. Please wait ...");
        //initializeUI(); // uncomment this for no UI

        try {
            socket = new Socket(SERVERNAME, SERVERPORT);
            System.out.println("Connected: " + socket);
            start();
        }
        catch(UnknownHostException uhe) {
            System.out.println("Host unknown: " + uhe.getMessage());
            System.exit(0);
        } catch(IOException ioe){
            System.out.println("Unexpected exception: " + ioe.getMessage()); 
            System.exit(0);
        }

    }
    public void run() {  
        while (thread != null) {
            try {  
                Random random = new Random();

                //char inputString = streamIn.readLine().charAt(0);
                char[] inputChoices = {'w', 's', 'a', 'd', 'j'};
                char inputString = inputChoices[random.nextInt(inputChoices.length)];
                sleep(5);
                
            	sendToServer(inputString);
            } catch(IOException ioe) {
                System.out.println("Sending error: " + ioe.getMessage());
                stop();
            }
       }
    }
    public void handle(byte[] msg) {
        if (msg.equals(".bye")) {
            System.out.println("Good bye. Press RETURN to exit ...");
            stop();
        }
        else{
            updateScene(msg);  
        }
    }
    public void start() throws IOException {  
        streamIn = new BufferedReader(new InputStreamReader(System.in));
        streamOut = new DataOutputStream(socket.getOutputStream());
        
        
        if (thread == null){
            client = new SheepClientThread(this, socket);
            thread = new Thread(this);                   
            thread.start();
        }
    }
    public void stop() {
        if (thread != null) {
            thread.stop();  
            thread = null;
        }
        try {
            if (streamIn   != null)  
                streamIn.close();
            if (streamOut != null)
                streamOut.close();
            if (socket    != null)
                socket.close();
        } catch(IOException ioe) {
           System.out.println("Error closing ...");
        }
        
        client.close();  
        client.stop();
    }
    
    private void updateScene(byte[] msg){
        int key = toInt(msg);
        int x = msg[4] & 0xFF;
        int y = msg[5] & 0xFF;
        
        //System.out.println("sheep: " + key + " x: " + x + " y: " + y);
        
        if(key == -1){
            noGrass.add(new Point(x, y));
        } else {
            if(sheeps.containsKey(key)){
                Sheep sheep = sheeps.get(key);
                sheep.setXYPosition(x, y);
            } else {
                sheeps.put(key, new Sheep(x, y));
            }
        }
        //canvas.repaint(); //uncomment this for no UI
    }
    
    public static void main(String args[]) {  
        SheepClient client = new SheepClient();
    }
    
    /*
    *   UI PARTS
    *
    *
    */
    
    private void initializeUI() {
		frame = new JFrame();
		frame.setBounds(0, 0, Sheep.SIZE_CELL * Sheep.NUM_COLS, Sheep.SIZE_CELL * Sheep.NUM_ROWS + 50);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		canvas = new ImageCanvas();
		canvas.setBackground(Color.GREEN);
		frame.getContentPane().add(canvas, BorderLayout.CENTER);
		frame.setVisible(true);
    }
    
    public class ImageCanvas extends Canvas {

        private BufferedImage img;

        public ImageCanvas() {
            try {
                img = ImageIO.read(new File("Images/sheep.png"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
        }

        @Override
        public Dimension getPreferredSize() {
            return img == null ? new Dimension(200, 200) : new Dimension(img.getWidth(), img.getHeight());
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.setColor(Color.LIGHT_GRAY);
            
            //System.out.println("painting");

            if (img != null) {
                for(Point p: noGrass) {
                    g.fillRect(p.x * Sheep.SIZE_CELL, p.y * Sheep.SIZE_CELL, Sheep.SIZE_CELL, Sheep.SIZE_CELL);
                }
            	for(Map.Entry<Integer, Sheep> entry : sheeps.entrySet()) {
            	    //int key = entry.getKey();
            	    Sheep value = entry.getValue();
                    g.drawImage(img, value.getxPosition()*Sheep.SIZE_CELL, value.getyPosition()*Sheep.SIZE_CELL, this);
            	}
                
            }
            
            
        }

    }
    
    private int toInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }
    
    private void sendToServer(char inputString) throws IOException{
        if(inputString == 'W' || inputString == 'w' ||
                        inputString == 'S' || inputString == 's' ||
                        inputString == 'A' || inputString == 'a' ||
                        inputString == 'D' || inputString == 'd') {
                streamOut.writeChar(inputString);
        } else if(inputString == 'J' || inputString == 'j'){
            Sheep sheep = sheeps.get(socket.getLocalPort());                    
            Point sheepPosition = new Point(sheep.getxPosition(), sheep.getyPosition());

            if(!noGrass.contains(sheepPosition)){
                streamOut.writeChar(inputString);
            }
        }
        streamOut.flush();
    }

    private void sleep(int time){
        try {
            Thread.sleep(1000 * time);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
