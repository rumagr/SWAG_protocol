package src.marterial;

import org.json.JSONObject;

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

    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("target_ip", targetIp);
        obj.put("target_port", targetPort);
        obj.put("next_ip", nextIp);
        obj.put("next_port", nextPort);
        obj.put("hop_count", hopCount);
        return obj;
    }

    @Override
    public String toString() {
        return "RoutingEntry{" +
                "target_ip='" + targetIp + '\'' +
                ", target_port=" + targetPort +
                ", next_ip='" + nextIp + '\'' +
                ", next_port=" + nextPort +
                ", hop_count=" + hopCount +
                '}';

    }

    public void setNextIp(String nextIp) {
        this.nextIp = nextIp;
    }

    public void setNextPort(int nextPort) {
        this.nextPort = nextPort;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RoutingEntry)) {
            return false;
        }
        RoutingEntry entry = (RoutingEntry) obj;
        return targetIp.equals(entry.targetIp) && targetPort == entry.targetPort;
    }

    public int hashCode() {
        return 5;
    }

}