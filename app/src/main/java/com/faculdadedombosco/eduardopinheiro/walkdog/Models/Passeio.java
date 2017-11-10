package com.faculdadedombosco.eduardopinheiro.walkdog.Models;

/**
 * Created by Eduardo Lion on 10/11/2017.
 */

public class Passeio {

    private String UId;
    private Coordenadas Localizacao;

    public String getUId() {
        return UId;
    }

    public void setUId(String UId) {
        this.UId = UId;
    }

    public Coordenadas getLocalizacao() {
        return Localizacao;
    }

    public void setLocalizacao(Coordenadas localizacao) {
        Localizacao = localizacao;
    }
}

class Coordenadas
{
    public String Latitude;
    public String Longitude;
}
