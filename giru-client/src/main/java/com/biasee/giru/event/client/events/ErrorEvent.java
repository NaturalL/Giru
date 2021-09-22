package com.biasee.giru.event.client.events;


public class ErrorEvent extends Event {
    private String error;

    public ErrorEvent() {
        this(null, null);
    }

    public ErrorEvent(String name, String error) {
        super(name, EventType.ERROR.type);
        this.error = error;
    }
    public void putExceptionClass(String stackTrace) {
        label("exception", stackTrace);
    }

    public String takeExceptionClass() {
        return label("exception");
    }

    public void putStackTrace(String stackTrace) {
        label("stackTrace", stackTrace);
    }

    public String takeStackTrace() {
        return label("stackTrace");
    }

    public void putFirstStack(String firstStack) {
        label("firstStack", firstStack);
    }

    public String takeFirstStack() {
        return label("firstStack");
    }

    public void putRootStack(String rootStack) {
        label("rootStack", rootStack);
    }

    public String takeRootStack() {
        return label("rootStack");
    }


    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}