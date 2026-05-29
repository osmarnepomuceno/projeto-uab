package com.sga.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "associado")
public class AssociadoModel extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(nullable = false)
    public String nome;

    @Column(unique = true, nullable = false, length = 11)
    public String cpf;

    @Column(unique = true, nullable = false)
    public String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Status status = Status.ATIVO;

    public enum Status {
        ATIVO, INADIMPLENTE, INATIVO
    }
}
