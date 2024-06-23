package src;

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
    //an ProtokollTimer von Verwaltung
    private int time;
    //an ProtokollTimer/Verwalter von ProtokollTimer/UI
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
    //von Verwalter an ProtokollTimer (stoppe ProtokollTimer)
    //von ProtokollTimer an Verwalter (ProtokollTimer abgelaufen)
    public Task(TaskArt art, UniqueIdentifier id) {
        this.art = art;
        this.jsonData = null;
        this.user = null;
        this.message = null;
        this.time = 0;
        this.id = id;
    }

    //von Verwalter an ProtokollTimer (starte ProtokollTimer)
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

    public TaskArt getArt() {
        return art;
    }

    public JSONObject getJsonData() {
        return jsonData;
    }

    public List<UniqueIdentifier> getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    public int getTime() {
        return time;
    }

    public UniqueIdentifier getId() {
        return id;
    }
}
