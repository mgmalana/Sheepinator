package model;

/**
 *
 * @author mgmalana
 */
public class Message {
    private String address;
    private int port;
    private byte[] message;

    public Message(String address, int port, byte[] message) {
        this.address = address;
        this.port = port;
        this.message = message;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public byte[] getMessage() {
        return message;
    }

    
    
    
}
