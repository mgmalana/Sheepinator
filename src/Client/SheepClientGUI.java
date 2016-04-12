package Client;

import Server.SheepServerThread;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import model.Sheep;

public class SheepClientGUI  extends SheepClient implements Runnable{
    
    public static final int PORT = 1234;
    public static final String HOST = "localhost";
    private ReceiverThread receiver;
    private SenderThread sender;
    private Sheep sheep;
    private ConcurrentHashMap<Integer, Sheep> sheeps = new ConcurrentHashMap<>();
    private JFrame frame;
    private ImageCanvas canvas;
    private Set <Point> noGrass = Collections.newSetFromMap(new ConcurrentHashMap<Point,Boolean>());

    
    public SheepClientGUI(){   
        this.sheep = new Sheep();
        try {
            InetAddress ia = InetAddress.getByName(HOST);
            sender = new SenderThread(this, sheep, ia, PORT);
            receiver = new ReceiverThread(this, sender.getSocket());
        } catch (UnknownHostException | SocketException ex) {
            throw new RuntimeException("Error: " + ex.getMessage());
        }
        initializeUI(); // uncomment this for no UI

    }
    
    public void updateScene(byte[] msg){
        int key = toInt(msg);
        int x = msg[4] & 0xFF;
        int y = msg[5] & 0xFF;
        
        /*
        if(key == -1){
            noGrass.add(new Point(x, y));
        } else if(x == Sheep.VALUE_FOR_REMOVE && y == Sheep.VALUE_FOR_REMOVE) {
            //System.out.println("remove: " + key);
            sheeps.remove(key);
        } else if(key == socket.getLocalPort()) {
            sheepClient.setXYPosition(x, y);
        }else {
            if(sheeps.containsKey(key)){
                Sheep sheep = sheeps.get(key);
                sheep.setXYPosition(x, y);
            } else {
                sheeps.put(key, new Sheep(x, y));
            }
        }*/
        System.out.println("sheep: " + key + " x: " + x + " y: " + y);
 
        if(key == -1){
            noGrass.add(new Point(x, y));
        } else if(sheeps.contains(key)){
            Sheep sheepThis = sheeps.get(key);
            sheepThis.setXYPosition(x, y);
        } else{
            sheeps.put(key, new Sheep(x, y));
        }
        
        repaintCanvas();
    }
    
    public void start(){
        sender.start();
        receiver.start();
    }
    
    private int toInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }
    
    public void addNoGrass(int x, int y){
        noGrass.add(new Point(x, y));
    }
    
    public boolean isThereGrass(int x, int y){
        return noGrass.contains(new Point(x, y));
    }
    
    public static void main(String args[]) { 
        SheepClientGUI sheepClient = new SheepClientGUI();
        sheepClient.start();
    }
    
    public void repaintCanvas(){
        canvas.repaint();
    }
    
    private void initializeUI() {
        frame = new JFrame();
        frame.setBounds(0, 0, Sheep.SIZE_CELL * Sheep.NUM_COLS, Sheep.SIZE_CELL * Sheep.NUM_ROWS + 50);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        canvas = new ImageCanvas();
        canvas.setBackground(Color.GREEN);
        frame.getContentPane().add(canvas, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    @Override
    public void run() {
        start();
    }
    
    
    public class ImageCanvas extends Canvas {

        private BufferedImage img;
        private BufferedImage imgSelf;
  
        public ImageCanvas() {
            try {
                img = ImageIO.read(new File("Images/sheep.png"));
                imgSelf = ImageIO.read(new File("Images/sheepblack.png"));
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
            	for(Map.Entry<Integer, Sheep> entry : sheeps.entrySet()) {
            	    //int key = entry.getKey();
            	    Sheep value = entry.getValue();
                    g.drawImage(img, value.getxPosition()*Sheep.SIZE_CELL, value.getyPosition()*Sheep.SIZE_CELL, this);
            	}
                g.drawImage(imgSelf, sheep.getxPosition()*Sheep.SIZE_CELL, sheep.getyPosition()*Sheep.SIZE_CELL, this);

            }
        }

    }
}