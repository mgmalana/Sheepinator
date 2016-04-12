package temp;

/**
 *
 * @author mgmalana
 */
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
//from w ww.j  av a2  s .c o m
public class Client {
  public static final String MULTICAST_IP = "239.1.1.1";
  public static final int MULTICAST_PORT = 8989;

  public static final String MULTICAST_INTERFACE_NAME = "en0";

  public static void main(String[] args) throws Exception {
    MembershipKey key = null;
    DatagramChannel client = DatagramChannel.open(StandardProtocolFamily.INET);

    NetworkInterface interf = NetworkInterface.getByName(MULTICAST_INTERFACE_NAME);
    client.setOption(StandardSocketOptions.SO_REUSEADDR, true);
    client.bind(new InetSocketAddress(MULTICAST_PORT));
    client.setOption(StandardSocketOptions.IP_MULTICAST_IF, interf);

    InetAddress group = InetAddress.getByName(MULTICAST_IP);
    key = client.join(group, interf);

    System.out.println("Joined the   multicast  group:" + key);
    System.out.println("Waiting for a  message  from  the"
        + "  multicast group....");

    ByteBuffer buffer = ByteBuffer.allocate(1048);
    client.receive(buffer);
    buffer.flip();
    int limits = buffer.limit();
    byte bytes[] = new byte[limits];
    buffer.get(bytes, 0, limits);
    String msg = new String(bytes);

    System.out.format("Multicast Message:%s%n", msg);
    key.drop();
  }
}