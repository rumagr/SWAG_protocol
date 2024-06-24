package src;

public class RoutingEntry {
    private String targetIp;
    private int targetPort;
    private String nextIp;
    private int nextPort;
    private int hopCount;

    public RoutingEntry(String targetIp, int targetPort, String nextIp, int nextPort, int hopCount) {
        this.targetIp = targetIp;
        this.targetPort = targetPort;
        this.nextIp = nextIp;
        this.nextPort = nextPort;
        this.hopCount = hopCount;
    }

    // Getters and Setters
    public String getTargetIp() {
        return targetIp;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public String getNextIp() {
        return nextIp;
    }

    public int getNextPort() {
        return nextPort;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }
}