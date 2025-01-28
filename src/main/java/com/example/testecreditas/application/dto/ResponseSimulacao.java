package com.example.testecreditas.application.dto;

import java.time.LocalDate;

public record ResponseSimulacao(
        String id,
        double valorTotal,
        double valorParcelaMensal,
        double totalJuros,
        double valorEmprestimo,
        LocalDate dataNascimento,
        int prazoMeses
) {
}