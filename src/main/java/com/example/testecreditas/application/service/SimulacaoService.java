package com.example.testecreditas.application.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

import com.example.testecreditas.application.dto.RequestSimulacao;
import com.example.testecreditas.application.dto.ResponseSimulacao;
import com.example.testecreditas.domain.RepositorioSimulacao;
import com.example.testecreditas.domain.Simulacao;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Tag(name = "Simulações", description = "Endpoints para operações de Simulação de Crédito")
@Service
public class SimulacaoService {

    private static final Logger log = LoggerFactory.getLogger(SimulacaoService.class);
    private final RepositorioSimulacao repositorio;

    public SimulacaoService(RepositorioSimulacao repositorio) {
        this.repositorio = repositorio;
    }



    public Mono<ResponseSimulacao> simular(RequestSimulacao solicitacao) {

        if (solicitacao.valorEmprestimo() <= 0 || solicitacao.prazoMeses() <= 0) {
            return Mono.error(new IllegalArgumentException("Valor do empréstimo e prazo devem ser positivos."));
        }

        int idade = Period.between(solicitacao.dataNascimento(), LocalDate.now()).getYears();

        if (idade <= 0) {
            return Mono.error(new IllegalArgumentException("Data de nascimento inválida."));
        }

        double taxaAnual = calcularTaxaAnual(idade);
        double taxaMensal = taxaAnual / 12 / 100;

        double n = solicitacao.prazoMeses();
        double pmt;

        if (taxaMensal == 0) {
            pmt = solicitacao.valorEmprestimo() / n;
        } else {
            pmt = (solicitacao.valorEmprestimo() * taxaMensal)
                    / (1 - Math.pow(1 + taxaMensal, -n));
        }

        double valorTotal = pmt * n;
        double totalJuros = valorTotal - solicitacao.valorEmprestimo();
        Simulacao simulacao = new Simulacao(
                UUID.randomUUID().toString(),
                solicitacao.valorEmprestimo(),
                solicitacao.dataNascimento(),
                solicitacao.prazoMeses(),
                valorTotal,
                pmt,
                totalJuros
        );

        var salvo = repositorio.save(simulacao)
                .map(simSalvo -> new ResponseSimulacao(
                        simSalvo.id(),
                        simSalvo.valorTotal(),
                        simSalvo.valorParcelaMensal(),
                        simSalvo.totalJuros(),
                        simSalvo.valorEmprestimo(),
                        simSalvo.dataNascimento(),
                        simSalvo.prazoMeses()
                ));

        log.info("Salvo com sucesso");

        return salvo;

    }

    private double calcularTaxaAnual(int idade) {
        if (idade <= 25) return 5.0;
        if (idade <= 40) return 3.0;
        if (idade <= 60) return 2.0;
        return 4.0;
    }

    public Simulacao buscarSimulacaoPorId(String id) {
        Simulacao simulacao = repositorio.findById(id).block();
        if (simulacao == null) {
            throw new RuntimeException("Simulação não encontrada para o ID: " + id);
        }
        return simulacao;
    }

    public Flux<Simulacao> buscarTodas() {
        return repositorio.findAll();
    }


}
