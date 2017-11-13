package com.faculdadedombosco.eduardopinheiro.walkdog.Models;

/**
 * Created by Eduardo Lion on 10/11/2017.
 */

public class Coordenadas
{
    private Double Latitude;
    private Double Longitude;

    public Coordenadas (){}

    public Double getLatitude() {
        return Latitude;
    }

    public void setLatitude(Double latitude) {
        Latitude = latitude;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public void setLongitude(Double longitude) {
        Longitude = longitude;
    }
}
