package SWAG_protocol.src;

import org.json.JSONObject;

import java.util.List;

public class Task {
    private TaskArt art;
    //von Empfaenger an Verwalter/UI und von Verwalter an Sender
    private JSONObject jsonData;
    //an UI von Verwalter
    private List<UniqueIdentifier> user;
    //an Verwalter von UI
    private String message;
    //an Timer von Verwaltung
    private int time;
    //an Timer/Verwalter von Timer/UI
    private UniqueIdentifier id;


    //von Empfaenger an Verwalter/UI
    public Task(TaskArt art, JSONObject jsonData) {
        this.art = art;
        this.jsonData = jsonData;
        this.user = null;
        this.message = null;
        this.time = 0;
        this.id = null;
    }

    //von UI an Verwaltung (sende an)
    public Task(TaskArt art, String message, UniqueIdentifier id) {
        this.art = art;
        this.jsonData = null;
        this.user = null;
        this.message = message;
        this.time = 0;
        this.id = id;
    }

    //von UI an Verwaltung (gib verbunbdene user)
    public Task(TaskArt art) {
        this.art = art;
        this.jsonData = null;
        this.user = null;
        this.message = null;
        this.time = 0;
        this.id = null;
    }


    //von UI an Verwaltung (verbinde mit user)
    //von Verwalter an Timer (stoppe Timer)
    //von Timer an Verwalter (Timer abgelaufen)
    public Task(TaskArt art, UniqueIdentifier id) {
        this.art = art;
        this.jsonData = null;
        this.user = null;
        this.message = null;
        this.time = 0;
        this.id = id;
    }

    //von Verwalter an Timer (starte Timer)
    public Task(TaskArt art, int time, UniqueIdentifier id) {
        this.art = art;
        this.jsonData = null;
        this.user = null;
        this.message = null;
        this.time = time;
        this.id = id;
    }

    //von Verwaltung an UI
    public Task(TaskArt art, List<UniqueIdentifier> user) {
        this.art = art;
        this.jsonData = null;
        this.user = user;
        this.message = null;
        this.time = 0;
        this.id = null;
    }

    //von Verwaltung an Sender
    public Task(TaskArt art, JSONObject jsonData, UniqueIdentifier id) {
        this.art = art;
        this.jsonData = jsonData;
        this.user = null;
        this.message = null;
        this.time = 0;
        this.id = id;
    }

}
