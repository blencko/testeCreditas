package com.example.testecreditas.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document("simulacoes")
public record Simulacao(
        @Id
        String id,
        double valorEmprestimo,
        LocalDate dataNascimento,
        int prazoMeses,
        double valorTotal,
        double valorParcelaMensal,
        double totalJuros
) {


}