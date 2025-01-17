package src.werkzeug;

import org.json.JSONObject;
import src.fachwert.TaskArt;
import src.main2;
import src.marterial.Task;
import src.marterial.UniqueIdentifier;
import src.service.CRC32Check;
import src.service.NetworkUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Empfaenger implements Runnable{
    public static int SERVER_PORT = 6789;
    private static final int BUFFER_SIZE = 4096;
    private static final int EXPECTED_DATA_LENGTH = 53;
    private static final int THREAD_POOL_SIZE = 10;
    public static Selector empfaengerSelector;
    public static final int commonHeaderLength = 53;
    public static final boolean ZEROTIER = false;


    @Override
    public void run() {
        // Initialize the executor service with a fixed thread pool size
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        while(!portIsAvailable(SERVER_PORT))
        {
            SERVER_PORT++;
        }

        main2.logger.info("Port: " + SERVER_PORT);

        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            empfaengerSelector = selector;

            //NetworkUtils.print();

            InetSocketAddress serverAdress = null;

            if (ZEROTIER) {
                serverAdress = new InetSocketAddress(NetworkUtils.getNetworkInterface().getInetAddresses().nextElement(), SERVER_PORT);
            }
            else
            {
                serverAdress = new InetSocketAddress(NetworkUtils.getFirstNonLoopbackAddress(true), SERVER_PORT);
            }

            // Bind the server socket channel to the specified port
            serverSocketChannel.bind(serverAdress);
            // Configure the server socket to non-blocking mode
            serverSocketChannel.configureBlocking(false);
            // Register the server socket channel with the selector for accept operations
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            // Log the start of the server
            main2.logger.info("Server gestartet auf Port " + serverAdress.getPort() + " IP: " + serverAdress.getAddress().getHostAddress());

            // Continuously handle incoming connections and data
            while (true) {
                // Block until at least one channel is ready for the operations you're interested in
                selector.select();

                // Get a set of the ready keys
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();

                    // Check if the key's channel is ready to accept a new socket connection
                    if (key.isAcceptable()) {
                        // Register the new connection
                        register(selector, serverSocketChannel);
                    }

                    // Check if the key's channel is ready for reading
                    if (key.isReadable()) {
                        // Read the data and forward the packet
                        forwardPacket(executorService, key);
                    }

                    // Remove the key from the selected set; it's been handled
                    iter.remove();
                }
            }
        } catch (IOException e) {
            // Log any exceptions that occur during the server's operation
            main2.logger.info("Exception in Empfaenger", e);
        } finally {
            // Shutdown the executor service to stop all running tasks
            executorService.shutdown();
        }
    }

    private static void register(Selector selector, ServerSocketChannel serverSocketChannel) throws IOException {
        // Accept a new client connection
        SocketChannel client = serverSocketChannel.accept();
        // Configure the client's channel to non-blocking mode
        client.configureBlocking(false);
        // Register the client's channel with the selector for read operations
        client.register(selector, SelectionKey.OP_READ);
        // Log socket information for the new connection
        logSocketInfo(client);
    }

    private static void forwardPacket(ExecutorService executorService, SelectionKey key) throws IOException {

        main2.logger.info("in forwardPacket");

        // Retrieve the client channel from the selection key
        SocketChannel client = (SocketChannel) key.channel();
        // Allocate a buffer to read data from the client
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        // Read data into the buffer from the client channel
        int read = client.read(buffer);

        // Determine if the connection with the client should be closed
        InetSocketAddress remoteAddress = (InetSocketAddress) client.getRemoteAddress();
        String ip = remoteAddress.getAddress().getHostAddress();
        int port = remoteAddress.getPort();
        UniqueIdentifier uniqueId = new UniqueIdentifier(ip, port);

        main2.logger.info("forwardPacket from IP: " + ip + " Port: " + port);
        // If no data is read, close the client channel and exit the method
        if ((read == -1)) {
            client.close();
            Verwalter.connections.remove(uniqueId);
            // Log the closure of the connection
            main2.logger.info(String.format("Connection from %s:%d closed", ip, port));
            return;
        }
        // Submit a task to the executor service to process the received data
        executorService.submit(() -> {
            try {
                // Convert the buffer's data to a string representation
                String jsonstr = new String(buffer.array(), StandardCharsets.UTF_8);

                //main2.logger.info("Received message: {}", jsonstr);

                String commonHeader = jsonstr.substring(0, commonHeaderLength + 1);

                main2.logger.info("Received common header: {}", commonHeader);

                // Parse the string into a JSON object
                JSONObject header = new JSONObject(commonHeader);

                String paketData = jsonstr.substring(commonHeaderLength, commonHeaderLength + header.getInt("length"));
                //main2.logger.info("Received common data: {}", paketData);


                JSONObject data = new JSONObject(paketData);

                // Log the received message
                main2.logger.info("Received data: {}", data.toString());

                // Perform an integrity check on the received JSON object
                if(!checkIntegrity(header, paketData))
                {
                    // Log an error if the integrity check fails and exit the method
                    main2.logger.error("Integrity check failed");
                    return;
                }
                // Extract the header, data, and shared header from the JSON object
                JSONObject sharedHeader = data.getJSONObject("header"); // get shared header

                main2.logger.info("Received shared header: {}", sharedHeader.toString());
                // Determine the task type from the header
                TaskArt ta = TaskArt.intToTaskArt(Integer.parseInt((String) header.get("type_id")));

                // Check if the message is intended for the current server
                if(TaskArt.MESSAGE == ta)
                {
                    try {
                        // Get the local IP address
                        String ipAddress = NetworkUtils.getFirstNonLoopbackAddress(true);

                        main2.logger.info("Local IP: {}", ipAddress);

                        // Compare the destination IP in the shared header with the local IP
                        if((sharedHeader.getString("dest_ip").equals(ipAddress) ||
                                sharedHeader.getString("dest_ip").equals("127.0.0.1"))
                                && sharedHeader.getInt("dest_port") == SERVER_PORT)
                        {
                            // If the IPs match, set the task type to MESSAGE_SELF
                            ta = TaskArt.MESSAGE_SELF;
                        }
                        else
                        {
                            // Otherwise, set the task type to MESSAGE_OTHERS
                            ta = TaskArt.MESSAGE_OTHERS;
                        }
                    } catch (Exception e) {
                        // Log any exceptions encountered during IP comparison
                        main2.logger.info("Exception in Empfaenger forwardPacket", e);
                    }
                }

                // Create a new task with the determined task type and JSON object
                Task t = new Task(ta, data, new UniqueIdentifier(sharedHeader.getString("dest_ip"), sharedHeader.getInt("dest_port")),client, sharedHeader.getString("source_ip"), sharedHeader.getInt("source_port"));

                // Add the task to the appropriate queue based on the task type
                if(TaskArt.MESSAGE_SELF == ta)
                {
                    try {
                        // Add to UI queue if the task type is MESSAGE_SELF
                        UI.UI_Queue.put(t);
                        main2.logger.info("Task added to UI_Queue{%s}", t.toString());
                    }
                    catch (InterruptedException e)
                    {
                        // Log any exceptions encountered while adding to the UI queue
                        main2.logger.info("Exception in Empfaenger forwardPacket nach UI", e);
                    }
                }
                else
                {
                    try {
                        // Add to Verwalter queue if the task type is MESSAGE_OTHERS
                        Verwalter.Verwalter_Queue.put(t);
                        main2.logger.info("Task added to Verwalter_Queue{%s}", t.toString());
                    }
                    catch (InterruptedException e)
                    {
                        // Log any exceptions encountered while adding to the Verwalter queue
                        main2.logger.info("Exception in Empfaenger forwardPacket nach Verwalter", e);
                    }
                }
            }
            catch (Exception e) {
                // Log any exceptions encountered during task processing
                main2.logger.info("Exception in Empfaenger forwardPacket", e);
            }
            finally {
                // Clear the buffer for future use
                buffer.clear();
            }
        });
    }

    private static boolean checkIntegrity(JSONObject header, String data) {

        // Retrieve the expected CRC32 checksum value from the header
        long expectedCRC32 = Long.parseLong(header.getString("crc32"));

        // Return true if both the CRC32 checksum and the length of the data are valid
        return CRC32Check.isChecksumValid(data, expectedCRC32);
    }


    private static void logSocketInfo(SocketChannel socketChannel) throws IOException {
        InetSocketAddress localAddress = (InetSocketAddress) socketChannel.getLocalAddress();
        InetSocketAddress remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();

        //main2.logger.info("Connection Info: ");
        //main2.logger.info("Local Address: {}:{}", localAddress.getAddress().getHostAddress(), localAddress.getPort());
        //main2.logger.info("Remote Address: {}:{}", remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort());
        //main2.logger.info("Protocol: TCP");

        // Hier können Sie zusätzliche Socket-Optionen loggen
        //for (SocketOption<?> option : socketChannel.supportedOptions()) {
            //main2.logger.info("{}: {}", option.name(), socketChannel.getOption(option));
        //}
    }

    private boolean portIsAvailable(int port) {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(port));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
