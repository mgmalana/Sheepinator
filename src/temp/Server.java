package temp;

/**
 *
 * @author mgmalana
 */
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
//  w  ww .j a  v  a2 s.co m
public class Server {
  public static final String MULTICAST_IP = "239.1.1.1";
  public static final int MULTICAST_PORT = 8989;
  public static final String MULTICAST_INTERFACE_NAME = "awdl0";

  public static void main(String[] args) throws Exception {
    DatagramChannel server = DatagramChannel.open();
    server.bind(null);
    NetworkInterface interf = NetworkInterface
        .getByName(MULTICAST_INTERFACE_NAME);
    server.setOption(StandardSocketOptions.IP_MULTICAST_IF, interf);

    String msg = "Hello!";
    ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
    InetSocketAddress group = new InetSocketAddress(MULTICAST_IP,
        MULTICAST_PORT);

    server.send(buffer, group);
    System.out.println("Sent the   multicast  message: " + msg);
  }
}
