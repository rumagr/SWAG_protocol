package src.marterial;

public class UniqueIdentifier {
    private String ip;
    private int port;

    public UniqueIdentifier(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIP() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public boolean equals(Object obj) {
        if(obj instanceof UniqueIdentifier) {
            UniqueIdentifier other = (UniqueIdentifier) obj;
            return ip.equals(other.getIP()) && port == other.getPort();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return ip.hashCode();
    }

    public String toString() {
        return ip + ":" + port;
    }
}
