package sheepinator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import model.Sheep;

public class SheepServer implements Runnable{  
    
    private ServerSocket server = null;
    private Thread thread = null;
    private static final int SERVERPORT = 1234;
    private ConcurrentHashMap<Integer, SheepServerThread> clients = new ConcurrentHashMap<>();
    private Set <Point> noGrass = Collections.newSetFromMap(new ConcurrentHashMap<Point,Boolean>());
    
    private JFrame frame;
    private ImageCanvas canvas;
    
    public SheepServer() {  
        try
        {
            System.out.println("Binding to port " + SERVERPORT + ", please wait  ...");
            server = new ServerSocket(SERVERPORT);  
            System.out.println("Server started: " + server);
            start();
        } catch(IOException ioe) {
            System.out.println(ioe); 
            System.exit(0);
        }
        initializeUI();

    }
    
    @Override
    public void run() {  
        while (thread != null)
        {
            try {
                System.out.println("Waiting for a client ..."); 
                addThread(server.accept());
            }
            catch(IOException ie) {
                System.out.println("Acceptance Error: " + ie); 
            }
       }
    }
    
    public final void start()
    {  
        if (thread == null) {
            thread = new Thread((Runnable) this); 
            thread.start();
        }
    }
    public void stop() {  
        if (thread != null) {
            thread.stop(); 
            thread = null;
        }
    }
        
    public synchronized void handle(int ID, char input) {
        try{
            /*if (input.equals(".bye")) {
                clients[findClient(ID)].send(".bye".toCharArray());
                remove(ID); 
            }
            else{*/
                sendToClients(ID, input);
                canvas.repaint();
                //for (int i = 0; i < clientCount; i++)
                  //  clients[i].send(ID + ": " + input);
            //}
        } catch(NullPointerException e){ //if client exits unexpectedly
            remove(ID);
        }
        //System.out.println(ID + ": " + input);
    }
    public synchronized void remove(int ID)
    {  

        if (clients.contains(ID)) {
            SheepServerThread toTerminate = clients.get(ID);
            System.out.println("Removing client thread " + ID);
            clients.remove(ID);
            canvas.repaint();
            
            byte[] toSend = prepareToByteArray(ID, Sheep.VALUE_FOR_REMOVE , Sheep.VALUE_FOR_REMOVE);
           
            for(Map.Entry<Integer, SheepServerThread> entry : clients.entrySet()) {
                //int key = entry.getKey();
                SheepServerThread value = entry.getValue();
                value.send(toSend);
            }

            try {  
                toTerminate.close();
            }
            catch(IOException ioe){
                System.out.println("Error closing thread: " + ioe);
            }
            toTerminate.stop(); 
        }
    }
    private void addThread(Socket socket) {  
        if (clients.size() < Sheep.MAX_NUM_SHEEP) {
            System.out.println("Client accepted: " + socket);
            SheepServerThread sheepThread = new SheepServerThread(this, socket);
            clients.put(socket.getPort(), sheepThread);
            try {  
                sheepThread.open(); 
                sheepThread.start(); 
                                                
                for(Map.Entry<Integer, SheepServerThread> entry : clients.entrySet()) {
            	    int key = entry.getKey();
            	    Sheep value = entry.getValue().getSheep();
                    byte[] toSend = prepareToByteArray(key, value.getxPosition(),value.getyPosition());
                    sheepThread.send(toSend);
            	}

                
                for(Point p: noGrass){ //for the nograss positions
                    byte[] toSend = prepareToByteArray(-1, p.x,p.y);
                    sheepThread.send(toSend);
                }

                
                sendToClients(socket.getPort(), 'n');
                canvas.repaint();                
            } catch(IOException ioe) {
                System.out.println("Error opening thread: " + ioe);
            } 
        } else
            System.out.println("Client refused: maximum " + Sheep.MAX_NUM_SHEEP + " reached.");
    }
    
    private void sendToClients(int ID, char input) {
    	Sheep sheep = clients.get(ID).getSheep();
        //System.out.println("input: " + input);
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
                        ID = -1; //tells that the update is for the grass
                        break;
        }
    	byte[] toSend = prepareToByteArray(ID, sheep.getxPosition(),sheep.getyPosition());
        //System.out.println("sheep: " + ID + " x: " + sheep.getxPosition() + " y: " + sheep.getyPosition());
                
        for(Map.Entry<Integer, SheepServerThread> entry : clients.entrySet()) {
            //int key = entry.getKey();
            SheepServerThread value = entry.getValue();
            value.send(toSend);
        }

    }
    
    public static void main(String args[]) {  
        SheepServer server = new SheepServer();
    }
    
    /*
     * 
     * 
     * 
     * UI
     * 
     * 
     * 
     * 
     * */
    public final void initializeUI() {
		frame = new JFrame();
		frame.setBounds(0, 0, Sheep.SIZE_CELL * Sheep.NUM_COLS, Sheep.SIZE_CELL * Sheep.NUM_ROWS + 50); //750 because may sumosobra
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		canvas = new ImageCanvas();
		canvas.setBackground(Color.GREEN);
                canvas.setDoubleBuffered(true);
		frame.getContentPane().add(canvas, BorderLayout.CENTER);
		frame.setVisible(true);
    }
    
    public class ImageCanvas extends JPanel {

        private BufferedImage img;
  
        public ImageCanvas() {
            try {
                img = ImageIO.read(new File("Images/sheep.png"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
        }

        @Override
        public void update( Graphics g )
        {
            paint( g );
        }
        
        @Override
        public Dimension getPreferredSize() {
            return img == null ? new Dimension(200, 200) : new Dimension(img.getWidth(), img.getHeight());
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
                        
            g.setColor(Color.LIGHT_GRAY);
            
            if (img != null) {
                for(Point p: noGrass) {
                    g.fillRect(p.x * Sheep.SIZE_CELL, p.y * Sheep.SIZE_CELL, Sheep.SIZE_CELL, Sheep.SIZE_CELL);
                }
            	for(Map.Entry<Integer, SheepServerThread> entry : clients.entrySet()) {
            	    //int key = entry.getKey();
            	    Sheep value = entry.getValue().getSheep();
                    g.drawImage(img, value.getxPosition()*Sheep.SIZE_CELL, value.getyPosition()*Sheep.SIZE_CELL, this);
            	}
                
            }
        }

    }
    
    private byte[] intToByteArray(int value) {
    return new byte[] {
            (byte)(value >> 24),
            (byte)(value >> 16),
            (byte)(value >> 8),
            (byte)value};
    }
    
    private byte[] prepareToByteArray(int ID, int posX, int posY){
        byte[] finalArray = new byte[6];
        byte[] a1 = intToByteArray(ID);
        
        System.arraycopy(a1, 0, finalArray, 0, 4);
       
        finalArray[4] = (byte) posX;
        finalArray[5] = (byte) posY;
        return finalArray;
    }
}