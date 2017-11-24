package com.faculdadedombosco.eduardopinheiro.walkdog.models;

import java.sql.Timestamp;
import java.util.List;

public class Passeio {

    private String id;
    private boolean buscouCachorro;
    private Coordenadas localizacao;
    private Usuario usuario;
    private Usuario solicitante;
    private Long dataCriacao;
    private String passeioAceito;

    public Passeio(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Long dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Usuario solicitante) {
        this.solicitante = solicitante;
    }

    public String getPasseioAceito() {
        return passeioAceito;
    }

    public void setPasseioAceito(String passeioAceito) {
        this.passeioAceito = passeioAceito;
    }
}


