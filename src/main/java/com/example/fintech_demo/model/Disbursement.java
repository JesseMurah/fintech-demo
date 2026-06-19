package com.example.fintech_demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "disbursements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Disbursement {

    @Id
    @Column(nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    private Status status;

    private BigDecimal amount;

    private String loanId;
}
