package com.faculdadedombosco.eduardopinheiro.walkdog.models;

/**
 * Created by Eduardo Lion on 10/11/2017.
 */

public class Coordenadas
{
    private Double latitude;
    private Double longitude;

    public Coordenadas (){}

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
