package src;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class Sender {
    public static Queue<Task> Sender_Queue = new LinkedBlockingQueue<>();
}
