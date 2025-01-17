package src.werkzeug;

import org.json.JSONArray;
import org.json.JSONObject;
import src.fachwert.TaskArt;
import src.main2;
import src.marterial.RoutingEntry;
import src.marterial.RoutingTable;
import src.marterial.Task;
import src.marterial.UniqueIdentifier;
import src.service.CRC32Check;
import src.service.NetworkUtils;
import src.service.ProtokollTimer;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Verwalter implements Runnable
{
    public static final int STATUS = 0;
    public static BlockingQueue<Task> Verwalter_Queue = new LinkedBlockingQueue<>();
    public static ConcurrentHashMap<UniqueIdentifier, SocketChannel> connections = new ConcurrentHashMap<>();
    private static RoutingTable routingTabelle = new RoutingTable();
    private int refreshCounter = 0;

    @Override
    public void run() {

       // erstelleRoutingTable();
        long delay = 10000L;
        try {
            ProtokollTimer.Timer_Queue.put(new Task(TaskArt.TIMER_TABLE_UPDATE_START, delay, new UniqueIdentifier("", 0)));
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
            case MESSAGE_OTHERS:
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
            case EXIT:
                handleEXIT();
                break;
            default:
                main2.logger.error("Unknown TaskArt");
                throw new IllegalArgumentException("Unknown TaskArt");
        }
    }


    private void erstelleRoutingTable() {
        // erstelle RoutingTable mit diesem Knoten als entry
        //try {
            //InetAddress ip = InetAddress.getLocalHost();
            //routingTabelle.addEntry(ip.getHostAddress(), Empfaenger.SERVER_PORT, ip.getHostAddress(), Empfaenger.SERVER_PORT, 0);
          //  main2.logger.info("RoutingTable created");
        //} catch (Exception e) {
          //  main2.logger.error("Exception in erstelleRoutingTable", e);
        //}

    }

    private void handleCR(Task t) {
        //Verarbeite RouringTable und sende CCR
        main2.logger.info("Handling CR");

        connections.put(new UniqueIdentifier(t.getSourceIp(), t.getSourcePort()), t.getSocketChannel());

        RoutingEntry newEntry = new RoutingEntry(t.getSourceIp(), t.getSourcePort(), t.getSourceIp(), t.getSourcePort(), 1);

        routingTabelle.addEntry(newEntry);

        main2.logger.info("Entry added to RoutingTable " + newEntry.toString());

        JSONObject data = t.getJsonData();

        JSONArray table = (JSONArray) data.get("table");

        routingTabelle.updateRoutingTable(table, t.getSourceIp(), t.getSourcePort());

        //sende CRR
        sendCRR(new UniqueIdentifier(t.getSourceIp(), t.getSourcePort()));
    }

    private void handleCRR(Task t) {
        //update RoutingTable

        RoutingEntry newEntry = new RoutingEntry(t.getSourceIp(), t.getSourcePort(), t.getSourceIp(), t.getSourcePort(), 1);

        routingTabelle.addEntry(newEntry);

        JSONObject data = t.getJsonData();

        JSONArray table = (JSONArray) data.get("table");

        routingTabelle.updateRoutingTable(table, t.getSourceIp(), t.getSourcePort());
    }

    private void handleSCC(Task t) {
        //antworte mit SCCR
        sendSCCR(new UniqueIdentifier(t.getSourceIp(), t.getSourcePort()));
    }

    private void handleSCCR(Task t) {

        //stoppe den Timer fuer den Knoten
        try {
            ProtokollTimer.Timer_Queue.put(new Task(TaskArt.TIMER_STOP, new UniqueIdentifier(t.getSourceIp(), t.getSourcePort())));
        } catch (InterruptedException e) {
            main2.logger.error("Exception in handleSCCR", e);
        }

        //keep counter up to date
        refreshCounter--;

        if(!(refreshCounter > 0))
        {
            //sende STU an alle next in RoutingTable
            for(UniqueIdentifier id : routingTabelle.getAllNextUniqueIds())
            {
                sendSTU(id);
            }
        }
    }

    private void handleSTU(Task t) {
        //update RoutingTable

        JSONObject data = t.getJsonData();

        JSONArray table = (JSONArray) data.get("table");

        main2.logger.info("Table received in STU" + table.toString());
        routingTabelle.updateRoutingTable(table, t.getSourceIp(), t.getSourcePort());
    }

    private void handleMessage(Task t) {
        // Extract the unique identifier and JSON data from the task
        JSONObject data = t.getJsonData();

        // Extract the data and headers from the packet
        JSONObject sharedHeader = data.getJSONObject("header");

        // Check and decrement the TTL value in the shared header
        int TLL = (int) sharedHeader.get("ttl");
        if (TLL > 1) {
            TLL--;
            sharedHeader.put("ttl", TLL);
        } else {
            // Log if the TTL has expired
            main2.logger.info("TTL expired");
            return;
        }

        // Update the data with the modified header
        data.put("header", sharedHeader);

        // Find the next hop for the destination IP and port in the shared header
        RoutingEntry nextHop = routingTabelle.findNextHop(sharedHeader.getString("dest_ip"),
                sharedHeader.getInt("dest_port"));

        // If no routing entry is found, log an error and throw an exception
        if (nextHop == null) {
            main2.logger.error("No entry found in RoutingTable");
            //throw new NullPointerException("No entry found in RoutingTable");
            return;
        }

        // Create a unique identifier for the next hop
        UniqueIdentifier next = new UniqueIdentifier(nextHop.getNextIp(), nextHop.getNextPort());

        JSONArray paket = new JSONArray();
        JSONObject header = buildCommonHeader(data, 1);

        // Update the packet with the new header and data
        paket.put(header);
        paket.put(data);

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

        main2.logger.info("Table update timer handling started");
        //counter for keeping track of the missing number of responses
        refreshCounter = routingTabelle.getAllNextUniqueIds().size();

        for (UniqueIdentifier id : routingTabelle.getAllNextUniqueIds()) {
            try {
                ProtokollTimer.Timer_Queue.put(new Task(TaskArt.TIMER_START, 1000L, id));
                sendSCC(id);
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
            //sende STU an alle next in RoutingTable
            for(UniqueIdentifier id : routingTabelle.getAllNextUniqueIds())
            {
                sendSTU(id);
            }
        }
    }

    private void handleCONNECT_TO(Task t) {
        //sende CR
        sendCR(t.getId());
    }

    private void handleSEND_MESSAGE_TO(Task t) {
        //Nachricht senden
        JSONArray message = buildMessage(t.getMessage(), t.getNickname(), t.getId());

        RoutingEntry next = routingTabelle.findNextHop(t.getId().getIP(), t.getId().getPort());

        try {
            if(next == null)
            {
                main2.logger.error("[handleSEND_MESSAGE_TO] No entry found in RoutingTable");
                Sender.Sender_Queue.put(new Task(TaskArt.MESSAGE, message, t.getId()));
            }
            else {
                Sender.Sender_Queue.put(new Task(TaskArt.MESSAGE, message, new UniqueIdentifier(next.getNextIp(), next.getNextPort())));
            }
        }
        catch (InterruptedException e) {
            main2.logger.error("Exception in handleSEND_MESSAGE_TO", e);
        }
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

    private JSONObject buildCommonHeader(JSONObject data,int type_id)
    {
        JSONObject header = new JSONObject();

        String length = data.toString().length()  + "";

        String crc32 = CRC32Check.getCRC32Checksum(data.toString()) + "";

        for(int i = 0; i < (6 - length.length()); i++)
        {
            length = "0" + length;
        }

        for(int i = crc32.length(); i < 10 ; i++)
        {
            crc32 = "0" + crc32;
        }

        header.put("length", length);
        header.put("type_id","" + type_id);
        header.put("crc32", crc32);

        return header;
    }

    private JSONObject buildSharedHeader(UniqueIdentifier id)
    {

        String src_ip = null;


        try {
            src_ip = NetworkUtils.getFirstNonLoopbackAddress(true);

            main2.logger.info("Fetched own ID");
        } catch (Exception e) {
            main2.logger.error("Exception in buildSharedHeader", e);
            return null;
        }

        //eigene IP und port
        int src_port = Empfaenger.SERVER_PORT;
        String dest_ip = id.getIP();
        int dest_port = id.getPort();
        int ttl = 16;

        JSONObject header = new JSONObject();

        header.put("source_ip", src_ip);
        header.put("source_port", src_port);
        header.put("dest_ip", dest_ip);
        header.put("dest_port", dest_port);
        header.put("ttl", ttl);
        main2.logger.info("Shared Header created {src_ip: " + src_ip + ", src_port: " + src_port + ", dest_ip: " + dest_ip + ", dest_port: " + dest_port + ", ttl: " + ttl + "}");
        return header;
    }

    private JSONArray buildMessage(String message, String nickname,UniqueIdentifier id)
    {
        JSONArray paket = new JSONArray();
        JSONObject data;
        JSONObject header;
        JSONObject sharedHeader;

        sharedHeader = buildSharedHeader(id);

        if(sharedHeader == null)
        {
            main2.logger.error("Error in buildMessage");
            return null;
        }

        data = new JSONObject();

        data.put("message", message);
        data.put("nickname", nickname);
        data.put("header", sharedHeader);

        //TODO typen als Define in einer Klasse
        header = buildCommonHeader(data, 1);

        paket.put(header);
        paket.put(data);

        main2.logger.info("Paket Message created");
        return paket;
    }

    private JSONObject buildTableData(UniqueIdentifier id)
    {
        JSONObject data = new JSONObject();
        JSONArray table = routingTabelle.toJSONArray(id);
        JSONObject header = buildSharedHeader(id);

        data.put("header", header);
        data.put("table", table);

        return data;
    }

    private void sendCR(UniqueIdentifier id)
    {
        JSONObject data = buildTableData(id);
        JSONObject header = buildCommonHeader(data, 2);

        JSONArray paket = new JSONArray();
        paket.put(header);
        paket.put(data);

        try {
            Sender.Sender_Queue.put(new Task(TaskArt.CR, paket, id));
        }
        catch (InterruptedException e) {
            main2.logger.error("Exception in sendCR", e);
        }
    }

    private void sendCRR(UniqueIdentifier id)
    {
        JSONObject data = buildTableData(id);
        JSONObject header = buildCommonHeader(data, 3);

        JSONArray paket = new JSONArray();
        paket.put(header);
        paket.put(data);

        try {
            Sender.Sender_Queue.put(new Task(TaskArt.CRR, paket, id));
        }
        catch (InterruptedException e) {
            main2.logger.error("Exception in sendCRR", e);
        }
    }

    private void sendSCC(UniqueIdentifier id)
    {
        JSONObject data = new JSONObject();

        JSONObject sharedHeader = buildSharedHeader(id);

        data.put("header", sharedHeader);

        JSONObject header = buildCommonHeader(data, 4);

        JSONArray paket = new JSONArray();
        paket.put(header);
        paket.put(data);

        try {
            Sender.Sender_Queue.put(new Task(TaskArt.SCC, paket, id));
            main2.logger.info("SCC sent with data " + data.toString());
        }
        catch (InterruptedException e) {
            main2.logger.error("Exception in sendSCC", e);
        }
    }

    private void sendSCCR(UniqueIdentifier id)
    {
        JSONObject data = new JSONObject();

        data.put("header", buildSharedHeader(id));

        JSONObject header = buildCommonHeader(data, 5);

        JSONArray paket = new JSONArray();
        paket.put(header);
        paket.put(data);

        if(routingTabelle.getEntry(id.getIP(), id.getPort(), id.getIP(), id.getPort()).getHopCount() == 32)
        {
            return;
        }

        try {
            Sender.Sender_Queue.put(new Task(TaskArt.SCCR, paket, id));
        }
        catch (InterruptedException e) {
            main2.logger.error("Exception in sendSCCR", e);
        }
    }

    private void sendSTU(UniqueIdentifier id)
    {
        JSONObject data = buildTableData(id);
        JSONObject header = buildCommonHeader(data, 6);

        JSONArray paket = new JSONArray();
        paket.put(header);
        paket.put(data);

        try {
            Sender.Sender_Queue.put(new Task(TaskArt.STU, paket, id));
        }
        catch (InterruptedException e) {
            main2.logger.error("Exception in sendSTU", e);
        }
    }

    public void handleEXIT()
    {
        //sende EXIT an alle next in RoutingTable
        List<UniqueIdentifier> uniqueIds = routingTabelle.getAllNextUniqueIds();

        routingTabelle.markALlAsDead();

        for(UniqueIdentifier id : uniqueIds)
        {
            sendSTU(id);
        }

        while(!Sender.Sender_Queue.isEmpty())
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                main2.logger.error("Exception in handleEXIT", e);
            }
        }

        exitProgram();
    }

    private void exitProgram() {
        System.exit(STATUS);
    }

}

