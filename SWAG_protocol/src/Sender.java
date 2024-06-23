package src;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
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
                main2.logger.info(STR."Task received in Sender\{t.toString()}");
                handleTask(t);
            } catch (Exception e) {
                main2.logger.error("Exeption in Sender", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handleTask(Task t) {
        JSONObject jsonData = t.getJsonData();
        UniqueIdentifier id = t.getId();

        SocketChannel s = Verwalter.connections.get(id);
        if (s == null) {
            main2.logger.error("SocketChannel not found");
            throw new NullPointerException("SocketChannel not found");
        }
        ByteBuffer buffer = ByteBuffer.wrap(jsonData.toString().getBytes());
        try {
            s.write(buffer);
        } catch (IOException e) {
            main2.logger.error("Exception while writing to SocketChannel", e);
        }


    }
}
