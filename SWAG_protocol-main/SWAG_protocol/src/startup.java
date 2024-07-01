package src;

public class startup {
    public static void main(String[] args){
        System.out.println("Ausgabe aus der main()-Methode");

        //Thread server = new Thread(new SimpleTCPServerNIO());
        Thread client = new Thread(new SimpleTCPClient());
        //server.start();

        client.start();
    }
}
