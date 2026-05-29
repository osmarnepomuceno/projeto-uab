package com.sga.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "boleto")
public class BoletoModel extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @ManyToOne
    @JoinColumn(name = "associado_id", nullable = false)
    public AssociadoModel associado;

    @Column(nullable = false)
    public BigDecimal valor;

    @Column(name = "data_vencimento", nullable = false)
    public LocalDate dataVencimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Status status = Status.PENDENTE;

    public enum Status {
        PENDENTE, PAGO, CANCELADO
    }
}
