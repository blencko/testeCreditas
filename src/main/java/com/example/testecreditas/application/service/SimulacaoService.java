package com.example.testecreditas.application.service;

import com.example.testecreditas.application.dto.RequestSimulacao;
import com.example.testecreditas.application.dto.ResponseSimulacao;
import com.example.testecreditas.application.dto.TipoTaxa;
import com.example.testecreditas.domain.RepositorioSimulacao;
import com.example.testecreditas.domain.Simulacao;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;


@Tag(name = "Simulações", description = "Endpoints para operações de Simulação de Crédito")
@Service
public class SimulacaoService {

    private static final Logger log = LoggerFactory.getLogger(SimulacaoService.class);
    private final RepositorioSimulacao repositorio;

    @Autowired
    private final EmailService emailService;

    public SimulacaoService(RepositorioSimulacao repositorio, EmailService emailService) {
        this.repositorio = repositorio;
        this.emailService = emailService;
    }


    public Mono<ResponseSimulacao> simular(RequestSimulacao solicitacao, boolean enviarEmail) {

        if (solicitacao.valorEmprestimo() <= 0 || solicitacao.prazoMeses() <= 0) {
            return Mono.error(new IllegalArgumentException("Valor do empréstimo e prazo devem ser positivos."));
        }

        int idade = Period.between(solicitacao.dataNascimento(), LocalDate.now()).getYears();

        if (idade <= 0) {
            return Mono.error(new IllegalArgumentException("Data de nascimento inválida."));
        }

        double prazoMeses = solicitacao.prazoMeses();
        double pmt = calculaPmt(solicitacao, idade, prazoMeses);

        double valorTotal = pmt * prazoMeses;
        double totalJuros = valorTotal - solicitacao.valorEmprestimo();

        Simulacao simulacao = contruirSimulacao(solicitacao, valorTotal, pmt, totalJuros);

        var salvo = repositorio.save(simulacao)
                .map(SimulacaoService::getResponseSimulacao);

        log.info("Simulação salva com sucesso");

        enviarEmailSeNecessario(enviarEmail, simulacao);

        return salvo;
    }

    private void enviarEmailSeNecessario(boolean enviarEmail, Simulacao simulacao) {
        if (enviarEmail) {
            log.info("Enviando Email");
            emailService.configurarMailSender(simulacao);
            log.info("Email enviado");
        }
    }

    private static ResponseSimulacao getResponseSimulacao(Simulacao sim) {
        return new ResponseSimulacao(
                sim.id(),
                sim.valorTotal(),
                sim.valorParcelaMensal(),
                sim.totalJuros(),
                sim.valorEmprestimo(),
                sim.dataNascimento(),
                sim.prazoMeses()
        );
    }

    private static Simulacao contruirSimulacao(RequestSimulacao solicitacao, double valorTotal, double pmt, double totalJuros) {
        return new Simulacao(
                UUID.randomUUID().toString(),
                solicitacao.valorEmprestimo(),
                solicitacao.dataNascimento(),
                solicitacao.prazoMeses(),
                valorTotal,
                pmt,
                totalJuros
        );
    }

    private double calculaPmt(RequestSimulacao solicitacao, int idade, double prazoMeses) {
        double taxaAnualBase = calcularTaxaAnual(idade);
        var taxaAnual = validaTipoTaxa(solicitacao, taxaAnualBase);

        double taxaMensal = taxaAnual / 12 / 100;

        double pmt = (taxaMensal == 0)
                ? solicitacao.valorEmprestimo() / prazoMeses
                : (solicitacao.valorEmprestimo() * taxaMensal)
                / (1 - Math.pow(1 + taxaMensal, -prazoMeses));
        return pmt;
    }

    private static double validaTipoTaxa(RequestSimulacao solicitacao, double taxaAnualBase) {
        double taxaAnual;
        if (solicitacao.tipoTaxa() == TipoTaxa.FIXA) {
            taxaAnual = taxaAnualBase;
        } else {
            taxaAnual = taxaAnualBase + 1.0;
        }
        return taxaAnual;
    }


    /**
     * TODO Executar metodo Async
     * **/
    public Mono<Void> simularEmLote(List<RequestSimulacao> solicitacoes) {
        return Flux.fromIterable(solicitacoes)
                .flatMap(solicitacao -> this.simular(solicitacao, false))
                .collectList()
                .flatMap(resultados -> {
                    String corpoEmail = emailService.montarCorpoEmailLote(resultados);
                    emailService.enviarEmailLote(corpoEmail);
                    return Mono.empty();
                });
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
