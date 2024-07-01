package src.marterial;

import org.json.JSONArray;
import src.main2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RoutingTable {
    private List<RoutingEntry> entries = new CopyOnWriteArrayList<>();

    public void addEntry(String targetIp, int targetPort, String nextIp, int nextPort, int hopCount) {
        entries.add(new RoutingEntry(targetIp, targetPort, nextIp, nextPort, hopCount));
    }

    public void addEntry(RoutingEntry entry) {

        int index = entries.indexOf(entry);

        if(index != -1)
        {
            if(entries.get(index).getHopCount() >= entry.getHopCount())
            {
                entries.remove(entry);
                entries.add(entry);
            }
        }
        else
        {
            entries.add(entry);
        }

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

    public List<UniqueIdentifier> getAllConectedUniqueIds() {
        List<UniqueIdentifier> uniqueIds = new ArrayList<>();
        for (RoutingEntry entry : entries) {
            UniqueIdentifier newId = new UniqueIdentifier(entry.getTargetIp(), entry.getTargetPort());
            if ((!uniqueIds.contains(newId)) && (entry.getHopCount() < 32)) {
                uniqueIds.add(newId);
            }
        }
        return uniqueIds;
    }

    public List<UniqueIdentifier> getAllNextUniqueIds() {
        List<UniqueIdentifier> uniqueIds = new ArrayList<>();
        for (RoutingEntry entry : entries) {
            UniqueIdentifier newId = new UniqueIdentifier(entry.getNextIp(), entry.getNextPort());
            if ((!uniqueIds.contains(newId)) && (entry.getHopCount() != 32))
            {
                uniqueIds.add(newId);
            }
        }
        return uniqueIds;
    }

    public JSONArray toJSONArray(UniqueIdentifier next) {
        JSONArray array = new JSONArray();
        for (RoutingEntry entry : entries) {
            if(!(entry.getNextIp().equals(next.getIP())) && !(entry.getTargetIp().equals(next.getIP()))) {
                array.put(entry.toJSONObject());
            }
        }
        return array;
    }

    public RoutingEntry getEntry(String targetIp, int targetPort,String nextIP, int nextPort) {
        return entries.stream()
                .filter(entry -> entry.getTargetIp().equals(targetIp) && entry.getTargetPort() == targetPort && entry.getNextIp().equals(nextIP) && entry.getNextPort() == nextPort)
                .findFirst()
                .orElse(null);
    }

    public void updateRoutingTable(JSONArray otherTable, String sourceIp, int sourcePort) {
        for (int i = 0; i < otherTable.length(); i++) {
            //get entries from other table
            String targetIp = otherTable.getJSONObject(i).getString("target_ip");
            int targetPort = otherTable.getJSONObject(i).getInt("target_port");
            String nextIp = sourceIp;
            int nextPort = sourcePort;
            int hopCount = otherTable.getJSONObject(i).getInt("hop_count") + 1;

            //check if there was a poison reverse or if there is a lower hop count
            RoutingEntry entry = getEntry(targetIp, targetPort, nextIp, nextPort);
            main2.logger.info(String.format("Entry: %s", entry));
            if (entry == null) {
                addEntry(targetIp, targetPort, nextIp, nextPort, hopCount);
            } else if (hopCount >= 32) {
                entry.setHopCount(32);
            } else {
                entry.setHopCount(hopCount);
                entry.setNextIp(nextIp);
                entry.setNextPort(nextPort);
            }
        }
    }


    public void markALlAsDead() {
        entries.forEach(entry -> entry.setHopCount(32));
    }
}
