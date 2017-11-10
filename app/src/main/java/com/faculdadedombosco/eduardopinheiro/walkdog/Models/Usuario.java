package com.faculdadedombosco.eduardopinheiro.walkdog.Models;

/**
 * Created by Eduardo Lion on 10/11/2017.
 */

public class Usuario {
    public Usuario(){}

    private String Nome;

    public String getNome() {
        return Nome;
    }

    public void setNome(String nome) {
        this.Nome = nome;
    }

    private String Telefone;

    private String Cpf;

    public String getTelefone() {
        return Telefone;
    }

    public void setTelefone(String telefone) {
        Telefone = telefone;
    }

    public String getCpf() {
        return Cpf;
    }

    public void setCpf(String cpf) {
        Cpf = cpf;
    }

    private String Email;

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }
}
