package src;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Verwalter implements Runnable
{
    public static BlockingQueue<Task> Verwalter_Queue = new LinkedBlockingQueue<>();
    public static ConcurrentHashMap<UniqueIdentifier, SocketChannel> connections = new ConcurrentHashMap<>();
    private static RoutingTable routingTabelle = new RoutingTable();
    private int refreshCounter = 0;

    @Override
    public void run() {

        erstelleRoutingTable();
        long delay = 10000L;
        try {
            ProtokollTimer.Timer_Queue.put(new Task(TaskArt.TIMER_TABLE_UPDATE_EXPIRED, delay, null));
        } catch (InterruptedException e) {
            main2.logger.error("Exception in Verwalter", e);
        }

        while (true)
        {
            try
            {
                Task t = Verwalter_Queue.take();
                main2.logger.info(String.format("Task received in Verwalter{%s}", t.toString()));
                handleTask(t);
            }
            catch (Exception e)
            {
                main2.logger.error("Exception in Verwalter", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handleTask(Task t) {
        switch (t.getArt()) {
            case CR:
                handleCR(t);
                break;
            case CRR:
                handleCRR(t);
                break;
            case SCC:
                handleSCC(t);
                break;
            case SCCR:
                handleSCCR(t);
                break;
            case STU:
                handleSTU(t);
                break;
            case MESSAGE:
                handleMessage(t);
                break;
            case TIMER_TABLE_UPDATE_EXPIRED:
                handleTableUpdateExpired(t);
                break;
            case TIMER_EXPIRED:
                handleTimerExpired(t);
                break;
            case CONNECT_TO:
                handleCONNECT_TO(t);
                break;
            case SEND_MESSAGE_TO:
                handleSEND_MESSAGE_TO(t);
                break;
            case GET_CONNECTED_USERS:
                handleGET_CONNECTED_USERS(t);
                break;
            default:
                main2.logger.error("Unknown TaskArt");
                throw new IllegalArgumentException("Unknown TaskArt");
        }
    }


    private void erstelleRoutingTable() {
        // erstelle RoutingTable mit diesem Knoten als entry
        try {
            InetAddress ip = InetAddress.getLocalHost();
            routingTabelle.addEntry(ip.getHostAddress(), Empfaenger.SERVER_PORT, ip.getHostAddress(), Empfaenger.SERVER_PORT, 0);
            main2.logger.info("RoutingTable created");
        } catch (Exception e) {
            main2.logger.error("Exception in erstelleRoutingTable", e);
        }

    }

    private void handleCR(Task t) {
        //Verarbeite RouringTable und sende CCR

        JSONObject data = t.getJsonData();

        JSONArray table = (JSONArray) data.get("table");

        routingTabelle.updateRoutingTable(table);

        //TODO sende CCR
    }

    private void handleCRR(Task t) {
        //update RoutingTable

        JSONObject data = t.getJsonData();

        JSONArray table = (JSONArray) data.get("table");

        routingTabelle.updateRoutingTable(table);
    }

    private void handleSCC(Task t) {
        //TODO antworte mit SCCR
    }

    private void handleSCCR(Task t) {

        //stoppe den Timer fuer den Knoten
        try {
            ProtokollTimer.Timer_Queue.put(new Task(TaskArt.TIMER_STOP, t.getId()));
        } catch (InterruptedException e) {
            main2.logger.error("Exception in handleSCCR", e);
        }

        //keep counter up to date
        refreshCounter--;

        if(!(refreshCounter > 0))
        {
            //TODO sende STU an alle next in RoutingTable
        }
    }

    private void handleSTU(Task t) {
        //update RoutingTable

        JSONObject data = t.getJsonData();

        JSONArray table = (JSONArray) data.get("table");

        routingTabelle.updateRoutingTable(table);
    }

    private void handleMessage(Task t) {
        // Extract the unique identifier and JSON data from the task
        UniqueIdentifier id = t.getId();
        JSONObject paket = t.getJsonData();
        // Attempt to find the next hop in the routing table using the task's ID
        RoutingEntry entry = routingTabelle.findNextHop(id.getIP(), id.getPort());

        // Extract the data and headers from the packet
        JSONObject data = paket.getJSONObject("data");
        JSONObject header = paket.getJSONObject("header");
        JSONObject sharedHeader = data.getJSONObject("header");

        // Check and decrement the TTL value in the shared header
        int TLL = (int) sharedHeader.get("ttl");
        if (TLL > 0) {
            TLL--;
            sharedHeader.put("ttl", TLL);
        } else {
            // Log if the TTL has expired
            main2.logger.info("TTL expired");
        }

        // If no routing entry is found, log an error and throw an exception
        if (entry == null) {
            main2.logger.error("No entry found in RoutingTable");
            throw new NullPointerException("No entry found in RoutingTable");
        }

        // Update the data with the modified header
        data.put("header", sharedHeader);

        // Find the next hop for the destination IP and port in the shared header
        RoutingEntry nextHop = routingTabelle.findNextHop(sharedHeader.getString("dest_ip"),
                sharedHeader.getInt("dest_port"));

        // Create a unique identifier for the next hop
        UniqueIdentifier next = new UniqueIdentifier(nextHop.getNextIp(), nextHop.getNextPort());

        // Calculate the CRC32 checksum for the data and add it to the header
        long checkSum = CRC32Check.getCRC32Checksum(data.toString());
        header.put("checksum", checkSum);

        // Update the packet with the new header and data
        paket.put("header", header);
        paket.put("data", data);

        // Forward the updated packet to the Sender queue
        try {
            Sender.Sender_Queue.put(new Task(TaskArt.MESSAGE, paket, next));
        }
        catch (InterruptedException e) {
            main2.logger.error("Exception in handleMessage", e);
        }
    }

    private void handleTableUpdateExpired(Task t) {
        // starte Timer fuer alle Eintraege in der RoutingTable

        //counter for keeping track of the missing number of responses
        refreshCounter = routingTabelle.getAllNextUniqueIds().size();

        for (UniqueIdentifier id : routingTabelle.getAllNextUniqueIds()) {
            try {
                ProtokollTimer.Timer_Queue.put(new Task(TaskArt.TIMER_START, 1000L, id));
            } catch (InterruptedException e) {
                main2.logger.error("Exception in handleTableUpdateExpired", e);
            }
        }
    }

    private void handleTimerExpired(Task t) {
        //poison reverse
        routingTabelle.setHopCountForPoisonReverse(t.getId().getIP(), t.getId().getPort(), 32);
        CloseConnection(t.getId());
        refreshCounter--;

        if(!(refreshCounter > 0))
        {
            //TODO sende STU an alle next in RoutingTable
        }
    }

    private void handleCONNECT_TO(Task t) {
        //TODO sende CR
    }

    private void handleSEND_MESSAGE_TO(Task t) {
        //TODO Nachricht senden
    }

    private void handleGET_CONNECTED_USERS(Task t) {
        //sende Liste der verbundenen User
        try {
            UI.UI_Queue.put(new Task(TaskArt.CONNECTED_USERS, routingTabelle.getAllConectedUniqueIds()));
        }
        catch (InterruptedException e) {
            main2.logger.error("Exception in handleGET_CONNECTED_USERS", e);
        }
    }

    private void CloseConnection(UniqueIdentifier uniqueId) {

        SocketChannel client = Verwalter.connections.get(uniqueId);
        if(client != null)
        {
            try {
                client.close();
                main2.logger.info(String.format("Connection from %s:%d closed", uniqueId.getIP(), uniqueId.getPort()));
            }
            catch (Exception e) {
                main2.logger.error("Exception in CloseConnection", e);
            }
        }
        Verwalter.connections.remove(uniqueId);
    }
}

