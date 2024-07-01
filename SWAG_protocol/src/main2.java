package src;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import src.service.ProtokollTimer;
import src.werkzeug.Empfaenger;
import src.werkzeug.Sender;
import src.werkzeug.UI;
import src.werkzeug.Verwalter;

public class main2 {
    public final static String s = "test.json";

    public static final Logger logger = LogManager.getLogger(main2.class);

    public static void main(String[] args)
{
        //Auskommentieren um Logausgaben zu deaktivieren
        //Configurator.setLevel(main2.class.getName(), Level.OFF);

        UI ui = new UI();
        Verwalter verwalter = new Verwalter();
        Empfaenger empfaenger = new Empfaenger();
        Sender sender = new Sender();
        ProtokollTimer protokollTimer = new ProtokollTimer();

        Thread t1 = new Thread(verwalter);
        Thread t2 = new Thread(empfaenger);
        Thread t3 = new Thread(sender);
        Thread t4 = new Thread(protokollTimer);
        Thread t = new Thread(ui);

        t.start();
        t1.start();
        t2.start();
        t3.start();
        t4.start();
    }

}