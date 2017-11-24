package com.faculdadedombosco.eduardopinheiro.walkdog.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class Notification {
    private String app_id;
    private List<String> include_player_ids;
    private NotificationData data;
    private NotificationContents contents;
    private List<Button> buttons;

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public List<String> getInclude_player_ids() {
        return include_player_ids;
    }

    public void setInclude_player_ids(List<String> include_player_ids) {
        this.include_player_ids = include_player_ids;
    }

    public NotificationData getData() {
        return data;
    }

    public void setData(NotificationData data) {
        this.data = data;
    }

    public NotificationContents getContents() {
        return contents;
    }

    public void setContents(NotificationContents contents) {
        this.contents = contents;
    }

    public List<Button> getButtons() {
        return buttons;
    }

    public void setButtons(List<Button> buttons) {
        this.buttons = buttons;
    }
}