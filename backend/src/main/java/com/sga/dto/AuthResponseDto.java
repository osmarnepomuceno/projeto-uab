package com.sga.dto;

public class AuthResponseDto {
    public String token;
    public String nome;
    public String perfil;

    public AuthResponseDto(String token, String nome, String perfil) {
        this.token = token;
        this.nome = nome;
        this.perfil = perfil;
    }
}
