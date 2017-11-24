package com.faculdadedombosco.eduardopinheiro.walkdog.models;

import java.lang.reflect.Array;

public class NotificationResponse {
    private String id;
    private int recipients;
    private Array errors;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRecipients() {
        return recipients;
    }

    public void setRecipients(int recipients) {
        this.recipients = recipients;
    }

    public Array getErrors() {
        return errors;
    }

    public void setErrors(Array errors) {
        this.errors = errors;
    }
}