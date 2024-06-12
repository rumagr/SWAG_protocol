package SWAG_protocol.src; /**
    Importieren der notwendigen Pakete: Importieren Sie java.io.* und java.net.*, welche die grundlegenden Netzwerk- und Input/Output-Klassen in Java enthalten.

    ServerSocket erstellen: Erstellen Sie ein Objekt der Klasse ServerSocket. Dies repräsentiert den Server im Netzwerk. Sie müssen einen Port angeben, auf dem der Server auf eingehende Verbindungen hören soll.

    Auf Verbindungen warten: Der Server muss auf eingehende Verbindungen warten. Dies geschieht durch Aufrufen der accept()-Methode des ServerSocket-Objekts. Diese Methode blockiert, bis eine Verbindung hergestellt wird.

    Kommunikation mit dem Client: Nachdem eine Verbindung hergestellt wurde, können Sie Daten über Input- und Output-Streams austauschen. Jede akzeptierte Verbindung ist ein Socket-Objekt, das seine eigenen Input- und Output-Streams hat.

    Schließen der Verbindungen: Nach der Kommunikation sollten Sie sowohl den Client-Socket als auch den Server-Socket schließen, um Ressourcen freizugeben.
**/

import org.json.JSONObject;


import java.io.*;
import java.net.*;

public class SimpleTCPServer {
    public static void main(String[] args) throws IOException {
        int port = 6789; // Der Port, auf dem der Server hört
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server gestartet und hört auf Port " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("Verbunden mit Client");

                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    Writer f = new FileWriter("test.json");

                    String jsonstr = "";
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        jsonstr = jsonstr + inputLine;
                    }

                    JSONObject j = new JSONObject(jsonstr);

                    j.write(f);

                    System.out.println(j.get("message").toString());

                    f.flush();

                } catch (IOException e) {
                    System.out.println("Exception caught when trying to listen on port " + port + " or listening for a connection");
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
