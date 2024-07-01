package src.service;

import src.*;
import src.fachwert.TaskArt;
import src.marterial.Task;
import src.marterial.UniqueIdentifier;
import src.werkzeug.Verwalter;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class TimeoutHandler extends TimerTask
{
    private UniqueIdentifier id;

    public TimeoutHandler(UniqueIdentifier id)
    {
        this.id = id;
    }

    public void run() {
        try {
            main2.logger.info(String.format("Timeout for UniqueIdentifier{%s}", id.toString()));
            ProtokollTimer.Timer_Queue.put(new Task(TaskArt.TIMER_EXPIRED, id));
            Verwalter.Verwalter_Queue.put(new Task(TaskArt.TIMER_EXPIRED, id));

        } catch (InterruptedException e) {
            main2.logger.error("Exception in TimeoutHandler", e);
            Thread.currentThread().interrupt();
        }
    }
}

class TableUpdate extends TimerTask
{
    public void run() {
        try {
            main2.logger.info("Table update timer expired");
            Verwalter.Verwalter_Queue.put(new Task(TaskArt.TIMER_TABLE_UPDATE_EXPIRED));

        } catch (InterruptedException e) {
            main2.logger.error("Exception in TableUpdate", e);
            Thread.currentThread().interrupt();
        }
    }
}

public class ProtokollTimer implements Runnable
{
    public static BlockingQueue<Task> Timer_Queue = new LinkedBlockingQueue<>();
    private Map<UniqueIdentifier, Timer> Timer_Map= new HashMap<>();

    @Override
    public void run() {
        while (true)
        {
            try
            {
                Task t = Timer_Queue.take();
                main2.logger.info(String.format("Task received in ProtokollTimer{%s}", t.toString()));
                handleTask(t);
            }
            catch (Exception e)
            {
                main2.logger.error("Exception in ProtokollTimer",e);
                //Thread.currentThread().interrupt();
            }
        }
    }

    private void handleTask(Task t) {
        main2.logger.info(String.format("Starting to handle task with ID{%s} and TaskArt{%s}", t.getId().toString(), t.getArt().toString()));

        if(TaskArt.TIMER_START == t.getArt()) {
            Timer timer = new Timer();
            Timer_Map.put(t.getId(), timer);
            timer.schedule(new TimeoutHandler(t.getId()), t.getTime());
            main2.logger.info(String.format("Timer started for ID{%s} with duration{%d}ms", t.getId().toString(), t.getTime()));
        }
        else if(TaskArt.TIMER_STOP == t.getArt()) {
            Timer timer = Timer_Map.get(t.getId());
            if (timer != null) {
                timer.cancel();
                Timer_Map.remove(t.getId());
                main2.logger.info(String.format("Timer stopped for ID{%s}", t.getId().toString()));
            } else {
                main2.logger.warn(String.format("Attempted to stop a non-existent timer for ID{%s}", t.getId().toString()));
            }
        }
        else if(TaskArt.TIMER_EXPIRED == t.getArt()) {
            Timer_Map.remove(t.getId());
            main2.logger.info(String.format("Timer expired and removed for ID{%s}", t.getId().toString()));
        }
        else if(TaskArt.TIMER_TABLE_UPDATE_START == t.getArt())
        {
            Timer tableUpdateTimer = new Timer();
            tableUpdateTimer.schedule(new TableUpdate(), t.getTime(), t.getTime());
            main2.logger.info(String.format("Table update timer started with duration{%d}ms", t.getTime()));
        }
        else {
            main2.logger.error(String.format("Task{%s} not supported in ProtokollTimer", t.getArt().toString()));
        }

        main2.logger.info(String.format("Finished handling task with ID{%s}", t.getId().toString()));
    }
}
