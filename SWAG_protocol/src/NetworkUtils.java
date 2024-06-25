package src;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

public class NetworkUtils {

    public static String getFirstNonLoopbackAddress(boolean preferIpv4) throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface iface : Collections.list(interfaces)) {
            // Skip down or loopback interfaces.
            if (!iface.isUp() || iface.isLoopback()) continue;

            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            for (InetAddress addr : Collections.list(addresses)) {
                // Check for IPv4 address if preferred.
                if (preferIpv4 && addr instanceof Inet4Address) {
                    return addr.getHostAddress();
                }
                // If not preferring IPv4, or no IPv4 address found, check for any non-loopback address.
                if (!preferIpv4 && !addr.isLoopbackAddress()) {
                    return addr.getHostAddress();
                }
            }
        }
        return null;
    }
}