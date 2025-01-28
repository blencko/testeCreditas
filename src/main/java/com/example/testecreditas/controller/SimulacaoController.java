package com.example.testecreditas.controller;


import com.example.testecreditas.application.dto.RequestSimulacao;
import com.example.testecreditas.application.dto.ResponseSimulacao;
import com.example.testecreditas.application.service.SimulacaoService;
import com.example.testecreditas.domain.RepositorioSimulacao;
import com.example.testecreditas.domain.Simulacao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/simulacoes")
public class SimulacaoController {


    private final SimulacaoService service;
    private final RepositorioSimulacao repositorio;

    public SimulacaoController(SimulacaoService service, RepositorioSimulacao repositorio) {
        this.service = service;
        this.repositorio = repositorio;
    }

    @PostMapping
    public Mono<ResponseSimulacao> criarSimulacao(@RequestBody RequestSimulacao solicitacao) {
        return service.simular(solicitacao, true);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ResponseSimulacao> buscarPorId(@PathVariable String id) {
        Simulacao simulacao = service.buscarSimulacaoPorId(id);
        ResponseSimulacao resposta = new ResponseSimulacao(
                simulacao.id(),
                simulacao.valorTotal(),
                simulacao.valorParcelaMensal(),
                simulacao.totalJuros(),
                simulacao.valorEmprestimo(),
                simulacao.dataNascimento(),
                simulacao.prazoMeses()
        );
        return ResponseEntity.ok(resposta);
    }

    @GetMapping
    public Flux<Simulacao> listarTodas() {
        return service.buscarTodas();
    }

    @PostMapping("/lote")
    public Mono<ResponseEntity<String>> simularEmLote(@RequestBody List<RequestSimulacao> solicitacoes) {
        service.simularEmLote(solicitacoes).subscribe();
        return Mono.just(ResponseEntity.accepted()
                .body("Sua solicitação está em processamento. Você receberá um e-mail com os resultados em breve."));
    }

}
