package com.faculdadedombosco.eduardopinheiro.walkdog.models;

import java.util.List;

/**
 * Created by Eduardo Lion on 10/11/2017.
 */

public class Usuario {
    private String id;
    private String nome;
    private String telefone;
    private String cpf;
    private String email;
    private Coordenadas coordenadas;
    private List<String> notas;
    private String oneSignalPlayerId;

    public Usuario(){}

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Coordenadas getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(Coordenadas coordenadas) {
        this.coordenadas = coordenadas;
    }

    public List<String> getNotas() {
        return notas;
    }

    public void setNotas(List<String> notas) {
        this.notas = notas;
    }

    public String getOneSignalPlayerId() {
        return oneSignalPlayerId;
    }

    public void setOneSignalPlayerId(String oneSignalPlayerId) {
        this.oneSignalPlayerId = oneSignalPlayerId;
    }
}
