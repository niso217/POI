package com.benezra.nir.poi;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nirb on 01/10/2017.
 */

public class EventModel {

    private Map<String,Event> events;

    public EventModel() {
        this.events = new HashMap<>();
    }

    public void addEvent(String key, Event value){
        events.put(key,value);
    }

    public void removeEvent(String key){
        events.remove(key);
    }

    public Event getEvent(String key){
        return events.get(key);
    }

    public Map<String, Event> getEvents() {
        return events;
    }

    public void setEvents(Map<String, Event> events) {
        this.events = events;
    }
}
