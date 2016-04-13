package model;

import static model.ClientServerConnection.ID_COUNT;

/**
 *
 * @author mgmalana
 */
public class ServerCoordinatorConnection {
    private String address;
    private int port;
    private int id;
    public static int ID_COUNT = 0;

    public ServerCoordinatorConnection(String address, int port) {
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
    
    public void setID(int id){
        this.id = id;
    }


    
    @Override
    public boolean equals(Object o){
        if(o == null){             
            return false;
        }
        if(!(o instanceof ServerCoordinatorConnection)){
            return false;
        }

        ServerCoordinatorConnection other = (ServerCoordinatorConnection) o;

        if(!this.address.equals(other.address))
            return false;
        if(this.port != other.port)
            return false;
        return true;
    }
    public static synchronized int getNextID(){
        return ID_COUNT++;
    }
    

}
