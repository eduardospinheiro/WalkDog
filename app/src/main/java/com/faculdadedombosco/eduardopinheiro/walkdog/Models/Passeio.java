package com.faculdadedombosco.eduardopinheiro.walkdog.Models;

public class Passeio {

    private boolean buscouCachorro;
    private Coordenadas localizacao;

    public Passeio(){}

    public boolean getBuscouCachorro() {
        return buscouCachorro;
    }

    public void setBuscouCachorro(boolean buscouCachorro) {
        this.buscouCachorro = buscouCachorro;
    }

    public Coordenadas getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(Coordenadas localizacao) {
        this.localizacao = localizacao;
    }
}


