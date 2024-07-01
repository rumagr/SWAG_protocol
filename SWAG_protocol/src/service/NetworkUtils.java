package src.service;

import src.main2;

import java.net.*;
import java.util.Collections;
import java.util.Enumeration;

public class NetworkUtils {

    public static final boolean LINUX = System.getProperty("os.name").toLowerCase().contains("linux");

    public static String getFirstNonLoopbackAddress(boolean preferIpv4) throws SocketException {
        if (LINUX) {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface iface : Collections.list(interfaces)) {
                // Skip down or loopback interfaces.
                if (!iface.isUp() || iface.isLoopback()) continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();

                int i = 0;

                for (InetAddress addr : Collections.list(addresses)) {
                    // Check for IPv4 address if preferred.

                    //main2.logger.info("IP: " + addr.getHostAddress());

                    if (preferIpv4 && addr instanceof Inet4Address && (i == 1)) {
                        return addr.getHostAddress();
                    }
                    // If not preferring IPv4, or no IPv4 address found, check for any non-loopback address.
                    if (!preferIpv4 && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }

                    i++;
                }
            }
            return null;
        } else {
            try {
                return Inet4Address.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                main2.logger.info("Error while getting IP address", e);
                return null;
            }
        }
    }

    public static NetworkInterface getNetworkInterface() {
        try {
            return NetworkInterface.getByName("ztnfai6ge2");
        } catch (SocketException e) {
            return null;
        }
    }

    public static void print()
    {
        try
        {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            for(NetworkInterface ni : Collections.list(networkInterfaces))
            {
                System.out.println("Display name: " + ni.getDisplayName());
                System.out.println("Name: " + ni.getName());
            }
        } catch(Exception e)
        {
            main2.logger.error("Error while getting network interfaces", e);
        }
    }
}