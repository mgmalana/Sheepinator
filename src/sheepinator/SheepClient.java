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
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import model.Sheep;

public class SheepClient implements Runnable{
    private Socket socket = null;
    private BufferedReader streamIn =  null;
    private PrintStream streamOut = null;
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
        //initializeUI(); //uncomment this for no UI

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
                String inputString = streamIn.readLine();
            	if(inputString.equals("W") || inputString.equals("w") ||
            			inputString.equals("S") || inputString.equals("s") ||
            			inputString.equals("A") || inputString.equals("a") ||
            			inputString.equals("D") || inputString.equals("d")) {
	                streamOut.println(inputString.charAt(0));
                } else if(inputString.equals("J") || inputString.equals("j")){
                    Sheep sheep = sheeps.get(socket.getLocalPort());                    
                    Point sheepPosition = new Point(sheep.getxPosition(), sheep.getyPosition());
                    
                    if(!noGrass.contains(sheepPosition)){
                        streamOut.println(inputString.charAt(0));
                    }
                }
                streamOut.flush();
            } catch(IOException ioe) {
                System.out.println("Sending error: " + ioe.getMessage());
                stop();
            }
       }
    }
    public void handle(String msg) {
        if (msg.equals(".bye")) {
            System.out.println("Good bye. Press RETURN to exit ...");
            stop();
        }
        else{
            System.out.println(msg);
            updateScene(msg);  
        }
    }
    public void start() throws IOException {  
        streamIn = new BufferedReader(new InputStreamReader(System.in));
        streamOut = new PrintStream(new BufferedOutputStream(socket.getOutputStream()));
        
        
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
    
    private void updateScene(String msg){
        String [] parser = msg.split(",");
        int key = Integer.parseInt(parser[0]);
        int x = Integer.parseInt(parser[1]);
        int y = Integer.parseInt(parser[2]);
        
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
        canvas.repaint();
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
		frame.setBounds(0, 0, 700, 750);
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
            
            System.out.println("painting");

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

}
