package net.mrbt0907.weather2remastered.event;

import net.minecraftforge.eventbus.api.Event;

public class EventEZGUIData extends Event {
    
    private final String id;
    private final int oldValue;
    private final int newValue;

    public EventEZGUIData(String id, int oldValue, int newValue) {
        this.id = id;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getId() {
        return id;
    }

    public int getOldValue() {
        return oldValue;
    }

    public int getNewValue() {
        return newValue;
    }
}