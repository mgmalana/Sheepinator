package model;

import java.net.InetAddress;
import java.util.Objects;

/**
 *
 * @author mgmalana
 */
public class ClientServerConnection {
    private String address;
    private int port;
    private Sheep sheep;
    private int id;
    public static int ID_COUNT = 0;
    
    public ClientServerConnection(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
    
    public int getID(){
        return id;
    }
    
    public Sheep getSheep(){
        return sheep;
    }
    
    public void setSheep(Sheep sheep) {
        this.sheep = sheep;
    }
    
    public void setID(int id){
        this.id = id;
    }
    
    public static synchronized int getNextID(){
        return ID_COUNT++;
    }
    
    @Override
    public boolean equals(Object o){
        if(o == null){             
            return false;
        }
        if(!(o instanceof ClientServerConnection)){
            return false;
        }

        ClientServerConnection other = (ClientServerConnection) o;

        if(!this.address.equals(other.address))
            return false;
        if(this.port != other.port)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.address);
        hash = 41 * hash + this.port;
        return hash;
    }
    
}
