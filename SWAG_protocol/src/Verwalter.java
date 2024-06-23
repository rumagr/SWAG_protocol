package src;

import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Verwalter {
    public static BlockingQueue<Task> Verwalter_Queue = new LinkedBlockingQueue<>();
    public static ConcurrentHashMap<UniqueIdentifier, SocketChannel> connections = new ConcurrentHashMap<>();

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

