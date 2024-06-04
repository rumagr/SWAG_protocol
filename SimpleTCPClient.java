/**
    Importieren der notwendigen Pakete: Importieren Sie java.io.* und java.net.* für Netzwerk- und I/O-Funktionalitäten.

    Socket erstellen: Erstellen Sie ein Socket-Objekt, um eine Verbindung zum Server herzustellen. Geben Sie die Adresse des Servers (zum Beispiel localhost, wenn der Server auf demselben Rechner läuft) und den Port an, auf dem der Server hört.

    Streams initialisieren: Initialisieren Sie Input- und Output-Streams, um mit dem Server zu kommunizieren.

    Daten senden und empfangen: Verwenden Sie die Streams, um Daten zum Server zu senden und Antworten zu empfangen.

    Verbindung schließen: Schließen Sie den Socket und die Streams, wenn die Kommunikation beendet ist.

**/    

import java.io.*;
import java.net.*;

public class SimpleTCPClient implements Runnable{
    public void run() {
        String serverAddress = "localhost"; // Die Adresse des Servers
        int port = 6789; // Der Port, auf dem der Server hört

        try (Socket socket = new Socket(serverAddress, port)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                System.out.println("Echo: " + in.readLine());
            }
        } catch (UnknownHostException e) {
            System.err.println("Host unbekannt: " + serverAddress);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("I/O-Fehler beim Verbinden zum Server: " + e.getMessage());
            System.exit(1);
        }
    }
}
