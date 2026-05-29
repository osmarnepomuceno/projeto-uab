package com.sga.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class UsuarioModel extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(nullable = false)
    public String nome;

    @Column(unique = true, nullable = false)
    public String email;

    @Column(name = "senha_hash", nullable = false)
    public String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Perfil perfil;

    public Boolean ativo = true;

    public enum Perfil {
        ADMINISTRADOR, ATENDENTE
    }
}
