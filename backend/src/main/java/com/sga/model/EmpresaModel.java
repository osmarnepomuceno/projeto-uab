package com.sga.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "empresa")
public class EmpresaModel extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(unique = true, nullable = false, length = 14)
    public String cnpj;

    @Column(name = "razao_social", nullable = false)
    public String razaoSocial;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String endereco;
}
