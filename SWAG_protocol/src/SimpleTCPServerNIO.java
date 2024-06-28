package src;
/**
  
  Nicht SO https://stackoverflow.com/questions/13111308/simple-java-threaded-tcp-server-thread sondern:
  Non-Blocking I/O (NIO) und Blocking I/O (BIO) sind zwei grundlegende Ansätze zur Datenübertragung in Netzwerkapplikationen, und beide haben ihre spezifischen Vorteile und Einsatzgebiete. Die Wahl zwischen NIO und BIO hängt von den Anforderungen und der Natur der Anwendung ab. Hier sind die Hauptvorteile von Non-Blocking I/O gegenüber Blocking I/O:

    Skalierbarkeit: NIO kann eine größere Anzahl von Netzwerkverbindungen mit weniger Threads effizienter verwalten. Da Threads in einem NIO-basierten Server nicht blockiert werden, während sie auf Daten warten, können diese Threads für andere Aufgaben verwendet werden. Im Vergleich dazu benötigt ein BIO-basierter Server für jede offene Verbindung einen eigenen Thread, was die Skalierbarkeit begrenzt, insbesondere bei einer großen Anzahl gleichzeitiger Verbindungen.

    Ressourcennutzung: NIO reduziert den Bedarf an Thread-Verwaltung und -Synchronisation, was zu einer geringeren Overhead und einer effizienteren Nutzung der Systemressourcen führt. In BIO-Modellen kann der hohe Ressourcenbedarf für Thread-Kontextwechsel und die Verwaltung vieler offener Threads problematisch sein.

    Leistung bei hoher Last: NIO behält seine Leistungsfähigkeit bei, auch wenn die Anzahl der Nutzer und Anfragen steigt. Dies ist besonders wichtig in Anwendungen, die eine große Anzahl von gleichzeitig verbundenen Clients erwarten, wie Webserver oder Chat-Server.

    Nicht-blockierende Natur: Non-Blocking I/O erlaubt es, dass ein Thread gleichzeitig mehrere Kanäle bedienen kann. Das bedeutet, dass der Thread nicht warten muss, bis eine Operation abgeschlossen ist, bevor er mit der nächsten fortfahren kann. Dieses Modell ermöglicht eine reaktive und ereignisgesteuerte Programmierung, was in modernen Anwendungen oft wünschenswert ist.

    Flexibilität: NIO bietet einen flexibleren Ansatz zur Datenübertragung, da Sie die Kontrolle darüber haben, wann gelesen oder geschrieben wird. Dies kann besonders nützlich in Situationen sein, in denen Sie auf mehrere Ereignisse gleichzeitig reagieren müssen.

    Verbesserte Funktionen: NIO bietet zusätzliche Funktionen wie Kanäle und Buffer, was eine feinere Kontrolle über die I/O-Operationen ermöglicht. Diese Funktionen erlauben es, fortgeschrittenere Netzwerkkonstrukte wie Multiplexing, Non-Blocking Reads/Writes und FileChannel-Operationen zu implementieren.
**/
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.*;


public class SimpleTCPServerNIO implements Runnable{

    public void run() {
        int port = 6789;
        ExecutorService executorService = Executors.newFixedThreadPool(10); // Max Thead Anzahl

        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server gestartet auf Port " + port);

            while (true) {
                selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();

                    if (key.isAcceptable()) {
                        register(selector, serverSocketChannel);
                    }

                    if (key.isReadable()) {
                        answerWithEcho(executorService, key);
                    }

                    iter.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }

    private static void register(Selector selector, ServerSocketChannel serverSocketChannel) throws IOException {


        SocketChannel client = serverSocketChannel.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        logSocketInfo(client);
    }

    private static void answerWithEcho(ExecutorService executorService, SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = client.read(buffer);
        if (read == -1) {
            client.close();
            return;
        }
        executorService.submit(() -> {

            String jsonstr = new String(buffer.array(), StandardCharsets.UTF_8);
            //System.out.println(jsonstr);
            JSONObject j = new JSONObject(jsonstr);


            System.out.println(j.get("message").toString());
            buffer.clear();

        });
    }

    private static void logSocketInfo(SocketChannel socketChannel) throws IOException {
        InetSocketAddress localAddress = (InetSocketAddress) socketChannel.getLocalAddress();
        InetSocketAddress remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();

        System.out.println("Connection Info: ");
        System.out.println("Local Address: " + localAddress.getAddress().getHostAddress() + ":" + localAddress.getPort());
        System.out.println("Remote Address: " + remoteAddress.getAddress().getHostAddress() + ":" + remoteAddress.getPort());
        System.out.println("Protocol: TCP");

        // Hier können Sie zusätzliche Socket-Optionen loggen
        for (SocketOption<?> option : socketChannel.supportedOptions()) {
            System.out.println(option.name() + ": " + socketChannel.getOption(option));
        }
    }
}
