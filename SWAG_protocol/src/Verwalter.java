package src;

import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Verwalter {
    public static BlockingQueue<Task> Verwalter_Queue = new LinkedBlockingQueue<>();
    public static ConcurrentHashMap<UniqueIdentifier, SocketChannel> map = new ConcurrentHashMap<>();
}
