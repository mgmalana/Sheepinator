package Server;

/**
 *
 * @author mgmalana
 */
public class ServerForwarder implements Runnable{
    private SheepServer sheepServer;
    private byte[] receiveData;

    public ServerForwarder(SheepServer sheepServer, byte[] receiveData) {
        this.sheepServer = sheepServer;
        this.receiveData = receiveData;
    }
    
    @Override
    public void run() {
        sheepServer.updateSceneAndForwardToClient(receiveData);
    }
    
}
