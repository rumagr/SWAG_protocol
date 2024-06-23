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
public class Sender implements Runnable {
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

    private void handleTask(Task task)
    {
        switch(task.getArt())
        {
            case SEND_MESSAGE_TO:
                sendTo(task);
                break;
            case CR:
                cr(task);
                break;
            case CRR:
                crr(task);
                break;
            case CONNECT_TO:
                connectTo(task);
                break;
            case GET_CONNECTED_USERS:
                getConnectedUsers(task);
                break;
            case TIMER_EXPIRED:
                timer_expired(task);
                break;
            case TIMER_START:
                timer_start(task);
                break;
            case TIMER_STOP:
                timer_stop(task);
                break;
            case MESSAGE_OTHERS:
                message_others(task);
                break;
            case MESSAGE_SELF:
                message_self(task);
                break;
            case SCC:
                scc(task);
                break;
            case SCCR:
                sccr(task);
                break;
            case STU:
                stu(task);
                break;
            case CONNECTED_USERS:
                connected_users(task);
                break;

            default:
                main2.logger.error("Invalid commmand at switch statement");
                throw new IllegalArgumentException("Invalid command");
        }
    }

    private void crr(Task task) {
    }

    private void cr(Task task) {
    }

    private void connectTo(Task task) {
    }

    private void getConnectedUsers(Task task) {
    }

    private void timer_expired(Task task) {
    }

    private void timer_start(Task task) {
    }

    private void timer_stop(Task task) {
    }

    private void message_others(Task task) {
    }

    private void message_self(Task task) {
    }

    private void scc(Task task) {
    }

    private void sccr(Task task) {
    }

    private void stu(Task task) {
    }

    private void connected_users(Task task) {
    }

    private void sendTo(Task task) {
    }

}
