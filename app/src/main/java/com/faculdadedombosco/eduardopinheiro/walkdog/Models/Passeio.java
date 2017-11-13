package com.faculdadedombosco.eduardopinheiro.walkdog.Models;

public class Passeio {

    private boolean BuscouCachorro;
    private Coordenadas Localizacao;

    public boolean getBuscouCachorro() {
        return BuscouCachorro;
    }

    public void setBuscouCachorro(boolean buscouCachorro) {
        this.BuscouCachorro = buscouCachorro;
    }

    public Coordenadas getLocalizacao() {
        return Localizacao;
    }

    public void setLocalizacao(Coordenadas localizacao) {
        Localizacao = localizacao;
    }
}


