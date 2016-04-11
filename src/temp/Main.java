package temp;

/**
 *
 * @author mgmalana
 */
import java.net.NetworkInterface;
import java.util.Enumeration;
/*  w  w  w. ja  va 2s . c o  m*/
public class Main {
  public static void main(String[] args) throws Exception {
    Enumeration<NetworkInterface> e = NetworkInterface
        .getNetworkInterfaces();
    while (e.hasMoreElements()) {
      NetworkInterface nif = e.nextElement();
      System.out.println("Name: " + nif.getName()
          + ",  Supports Multicast: " + nif.supportsMulticast()
          + ", isUp(): " + nif.isUp());
    }
  }
}
