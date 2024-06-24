package src;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RoutingTable {
    private List<RoutingEntry> entries = new CopyOnWriteArrayList<>();

    public void addEntry(String targetIp, int targetPort, String nextIp, int nextPort, int hopCount) {
        entries.add(new RoutingEntry(targetIp, targetPort, nextIp, nextPort, hopCount));
    }

    public void removeEntry(String targetIp, int targetPort) {
        entries.removeIf(entry -> entry.getTargetIp().equals(targetIp) && entry.getTargetPort() == targetPort);
    }

    public RoutingEntry findNextHop(String targetIp, int targetPort) {
        return entries.stream()
                .filter(entry -> entry.getTargetIp().equals(targetIp) && entry.getTargetPort() == targetPort)
                .findFirst()
                .orElse(null);
    }

    public void setHopCountForPoisonReverse(String targetIp, int targetPort, int poisonHopCount) {
        entries.forEach(entry -> {
            if (entry.getTargetIp().equals(targetIp) && entry.getTargetPort() == targetPort) {
                entry.setHopCount(poisonHopCount);
            }
        });
    }

    public List<UniqueIdentifier> getAllUniqueIds() {
        List<UniqueIdentifier> uniqueIds = new ArrayList<>();
        for (RoutingEntry entry : entries) {
            UniqueIdentifier newId = new UniqueIdentifier(entry.getTargetIp(), entry.getTargetPort());
            if (!uniqueIds.contains(newId)) {
                uniqueIds.add(newId);
            }
        }
        return uniqueIds;
    }

    // Additional methods can be implemented as needed, such as updating an entry, listing all entries, etc.
}
