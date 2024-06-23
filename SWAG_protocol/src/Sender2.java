package src;

import org.json.JSONObject;

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

//bracuhe SocketChannel
public class Sender2 implements Runnable {
    public static BlockingQueue<Task> Sender_Queue = new LinkedBlockingQueue<>();


    @Override
    public void run() {
        while (true)
        {
            try
            {
                Task t = Sender_Queue.take();
                main2.logger.info(STR."Task received in Sender\{t.toString()}");
                handleTask(t);
            }
            catch (Exception e)
            {
                main2.logger.error("Exeption in Sender",e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handleTask(Task t) {
    }


}
