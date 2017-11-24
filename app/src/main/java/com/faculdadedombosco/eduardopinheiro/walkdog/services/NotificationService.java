package com.faculdadedombosco.eduardopinheiro.walkdog.services;

import com.faculdadedombosco.eduardopinheiro.walkdog.models.Notification;
import com.faculdadedombosco.eduardopinheiro.walkdog.models.NotificationResponse;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificationService {
    @Headers("Authorization: Basic NzFhYmMxMDQtNjUzZC00ZmJjLWJjMDMtMjg0MTZmZWVlMGE5")
    @POST("notifications")
    Call<NotificationResponse> create(@Body Notification notification);
}