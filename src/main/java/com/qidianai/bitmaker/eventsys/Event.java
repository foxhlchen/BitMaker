package com.qidianai.bitmaker.eventsys;

/**
 * Created by fox on 2017/7/8.
 */
public class Event<T> {
    protected T data = null;
    protected Object sender = null;

    protected String name = "";
    protected String tag;

    public Event() {

    }

    public Event(Object s, T d) {
        sender = s;
        data = d;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Object getSender() {
        return sender;
    }

    public void setSender(Object sender) {
        this.sender = sender;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Class getType() {
        return getClass();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
