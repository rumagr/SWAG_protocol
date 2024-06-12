import SWAG_protocol.src.TCPClientSimpleOld;

public class startup {
    public static void main(String[] args){
        System.out.println("Ausgabe aus der main()-Methode");

        Thread server = new Thread(new SimpleTCPServerNIO()); 
        Thread client = new Thread(new TCPClientSimpleOld());
        server.start();
        
        try {
            Thread.sleep(1000);
        } catch (Exception e)
        {

        }

        client.start(); 
    }
}
