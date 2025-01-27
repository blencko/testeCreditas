package com.example.testecreditas.application.dto;

import java.time.LocalDate;

public record RequestSimulacao(
        double valorEmprestimo,
        LocalDate dataNascimento,
        int prazoMeses,
        TipoTaxa tipoTaxa
) {}