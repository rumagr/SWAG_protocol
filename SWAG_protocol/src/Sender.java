package src;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


//von Verwaltung an Sender
//public Task(TaskArt art, JSONObject jsonData, UniqueIdentifier id) {
//    this.art = art;
//    this.jsonData = jsonData;
//    this.user = null;
//    this.message = null;
//    this.time = 0;
//    this.id = id;
//}

//bracuhe SocketChannel -> liegt im Verwalter als static
public class Sender implements Runnable {
    public static BlockingQueue<Task> Sender_Queue = new LinkedBlockingQueue<>();


    @Override
    public void run() {
        while (true) {
            try {
                Task t = Sender_Queue.take();
                main2.logger.info(String.format("Task received in Sender{%s}", t.toString()));
                handleTask(t);
            } catch (Exception e) {
                main2.logger.error("Exeption in Sender", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handleTask(Task t) {
        JSONArray jsonPacket = t.getJsonPacket();
        UniqueIdentifier id = new UniqueIdentifier(t.getId().getIP(), Empfaenger.SERVER_PORT);

        SocketChannel socketChannel = Verwalter.connections.get(id);

        //handle not yet connected
        if (socketChannel == null) {
            main2.logger.info("SocketChannel not found");

            try {
                //TODO weiß auch nicht
                main2.logger.info("Creating new SocketChannel with IP{" + id.getIP() + "} and Port{" + id.getPort() + "}");
                socketChannel = SocketChannel.open(new InetSocketAddress(id.getIP(), id.getPort()));

                socketChannel.configureBlocking(false);
                socketChannel.register(Empfaenger.empfaengerSelector, SelectionKey.OP_READ);
                Empfaenger.empfaengerSelector.wakeup();
                Verwalter.connections.put(id,socketChannel);
            }
            catch (IOException e) {
                main2.logger.error("Exception while creating SocketChannel", e);
            }
        }
        ByteBuffer buffer = ByteBuffer.wrap(jsonPacket.toString().getBytes());
        try {
            socketChannel.write(buffer);
            main2.logger.info("Message sent to IP{" + id.getIP() + "} and Port{" + id.getPort() + "}");
        } catch (IOException e) {
            main2.logger.error("Exception while writing to SocketChannel", e);
        }
    }
}
