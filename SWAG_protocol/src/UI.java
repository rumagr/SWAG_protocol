package src;

import org.json.JSONObject;

import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


//CONNECT_TO, SEND_MESSAGE_TO, GET_CONNECTED_USERS

//cto@ip:port
//smto@ip:port:message
//gcu


public class UI implements Runnable {
    public static BlockingQueue<Task> UI_Queue = new LinkedBlockingQueue<>();

    prtivate static final int NUM_THREADS = 1;
    private static final String regex_cto = "cto@((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\:(6553[0-5]|655[0-2]\\d|65[0-4]\\d{2}|6[0-4]\\d{3}|[1-5]\\d{4}|[1-9]\\d{0,3})";
    private static final String regex_smto = "smto@((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\:(6553[0-5]|655[0-2]\\d|65[0-4]\\d{2}|6[0-4]\\d{3}|[1-5]\\d{4}|[1-9]\\d{0,3}):.*";

    private static final String regex_gcu = "gcu";

    private static final String isMessage = "((smto@|cto@|gcu).*)";
    private String nickname;
    public UI()
    {
        for(int i = 0; i< NUM_THREADS; i++)
        {
            Thread t = new Thread(new UI_Queue_Worker());
            t.start();
        }
    }
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Bitte einen Nickname eingeben: ");

        this.nickname = scanner.nextLine();

        while (true) {
            System.out.println("Bitte einen Command eingeben: ");
            System.out.println("Die Commands sind: cto@ip:port, smto@ip:port:message, gcu");
            String input = scanner.nextLine();

            if (input.matches(isMessage)) {
                String[] fullcommand = input.split("@");

                String command = fullcommand[0];

                main2.logger.info(STR."Command:\{fullcommand[0]}");

                switch(command)
                {
                    case "cto":
                        connectToManagment(fullcommand[1]);
                        break;
                    case "smto":
                        sendToManagement(fullcommand[1]);
                        break;
                    case "gcu":
                        getConnectedUsers();
                        break;
                    default:
                        main2.logger.error("Invalid commmand at switch statement");
                        throw new IllegalArgumentException("Invalid command");
                }
            }
            else
            {
                main2.logger.error("Invalid commmand");
                throw new IllegalArgumentException("Invalid command");
            }

        }

    }

    private void sendToManagement(String information) {
      //split information and make a new Task
        String[] split = information.split(":");

        if(split.length < 3)
        {
            main2.logger.error("Invalid Information at sendToManagement");
            throw new IllegalArgumentException("Invalid command");
        }

        String ip = split[0];
        main2.logger.info(STR."IP @cto is: \{ip}");
        String port = split[1];
        main2.logger.info(STR."Port @ cto is: \{port}");
        String message = split[2];
        main2.logger.info(STR."Message is: \{message}");

        if(!checkIPandPort(ip, port))
        {
            main2.logger.error("Invalid IP or Port at sendToManagement");
            throw new IllegalArgumentException("Invalid command");
        }
        int iport = Integer.parseInt(port);

        Task t = new Task(TaskArt.SEND_MESSAGE_TO, message, new UniqueIdentifier(ip, iport), this.nickname);

        main2.logger.info("SEND_MESSAGE_TO Task to UI_Queue");
        UI_Queue.add(t);

        System.out.println("SEND_MESSAGE_TO added to UI_Queue");
    }

    private void connectToManagment(String information) {
      //split information and make a new Task
        String[] split = information.split(":");

        if(split.length < 2)
        {
            main2.logger.error("Invalid Information at connectToManagment");
            throw new IllegalArgumentException("Invalid command");
        }

        String ip = split[0];
        main2.logger.info(STR."IP @cto is: \{ip}");
        String port = split[1];
        main2.logger.error(STR."Port @cto is: \{port}");

        if(!checkIPandPort(ip, port))
        {
            main2.logger.error("Invalid IP or Port at sendToManagement");
            throw new IllegalArgumentException("Invalid command");
        }
        int iport = Integer.parseInt(port);

        Task t = new Task(TaskArt.SEND_MESSAGE_TO, new UniqueIdentifier(ip, iport));

        main2.logger.info("SEND_MESSAGE_TO Task to Verwalter_Queue");
        Verwalter.Verwalter_Queue.add(t);

        System.out.println("SEND_MESSAGE_TO added to UI_Queue");

    }

    private void getConnectedUsers() {
      //make a new Task
        Task t = new Task(TaskArt.GET_CONNECTED_USERS);

        main2.logger.info("GET_CONNECTED_USERS Task to UI_Queue");
        UI_Queue.add(t);

        System.out.println("GET_CONNECTED_USERS added to UI_Queue");
    }

    private boolean checkIPandPort(String ip, String port) {
        return IPv4Address.isValid(ip) && Ports.isValid(port);
    }

    public String getNickname() {
        return this.nickname;
    }

    private class UI_Queue_Worker implements Runnable
    {
        @Override
        public void run() {
            while (true)
            {
                try
                {
                    Task t = UI_Queue.take();
                    main2.logger.info(STR."Task received in UI_Queue_Worker\{t.toString()}");
                    handleUItask(t);
                }
                catch (Exception e)
                {
                    main2.logger.error("Exeption in UI_Queue_Worker",e);
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void handleUItask(Task t) {
            switch (t.getArt())
            {
                case CONNECTED_USERS:
                    connectedUsers(t);
                    break;
                case MESSAGE_SELF:
                    messageSelf(t);
                    break;
                default:
                    main2.logger.error("Invalid Task at switch statement");
                    throw new IllegalArgumentException("Invalid Task");
            }
        }


        //Message ausgeben (von wem anders an mich)
        private void messageSelf(Task t) {
            JSONObject body = t.getJsonData();
            System.out.println(STR."Message from: \{body.getString("nickname")}\{body.getString("message")}");
            main2.logger.info(STR."Message from: \{body.getString("nickname")}\{body.getString("message")}");

        }
        //Paket wird analsiert und ich kriege es, quasi zurück
        private void connectedUsers(Task t) {
            //hier Routing Tabelle ausgeben
            List<UniqueIdentifier> users = t.getUser();

            System.out.println("Connected Users: ");
            for (UniqueIdentifier user : users) {
                System.out.println(user.toString());
            }
        }
    }
}

