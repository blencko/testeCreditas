package com.example.testecreditas;

import com.example.testecreditas.application.dto.RequestSimulacao;
import com.example.testecreditas.application.dto.ResponseSimulacao;
import com.example.testecreditas.application.dto.TipoTaxa;
import com.example.testecreditas.application.service.EmailService;
import com.example.testecreditas.application.service.SimulacaoService;
import com.example.testecreditas.domain.RepositorioSimulacao;
import com.example.testecreditas.domain.Simulacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

class SimulacaoServiceTest {

    @Mock
    private RepositorioSimulacao repositorio;

    @Mock
    private EmailService emailService;

    private SimulacaoService simulacaoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        simulacaoService = new SimulacaoService(repositorio, emailService);
    }

    @Test
    void deveSimularComDadosValidos() {
        RequestSimulacao solicitacao = new RequestSimulacao(1000.0, LocalDate.of(1990, 1, 1), 12, TipoTaxa.VARIAVEL);
        Simulacao simulacao = new Simulacao(
                UUID.randomUUID().toString(),
                1000.0,
                LocalDate.of(1990, 1, 1),
                12,
                1030.0,
                85.83,
                30.0
        );

        when(repositorio.save(any(Simulacao.class))).thenReturn(Mono.just(simulacao));

        Mono<ResponseSimulacao> resultado = simulacaoService.simular(solicitacao, true);

        StepVerifier.create(resultado)
                .expectNextMatches(res -> res.valorEmprestimo() == 1000.0 && res.totalJuros() == 30.0)
                .verifyComplete();

        verify(repositorio, times(1)).save(any(Simulacao.class));
    }

    @Test
    void deveRetornarErroParaDadosInvalidos() {
        RequestSimulacao solicitacao = new RequestSimulacao(-500.0, LocalDate.of(1990, 1, 1), -12, TipoTaxa.FIXA);
        Mono<ResponseSimulacao> resultado = simulacaoService.simular(solicitacao, false);

        StepVerifier.create(resultado)
                .expectErrorMatches(ex -> ex instanceof IllegalArgumentException
                        && ex.getMessage().equals("Valor do empréstimo e prazo devem ser positivos."))
                .verify();
    }

    @Test
    void deveRetornarSimulacoesExistentes() {
        Simulacao simulacao1 = new Simulacao(UUID.randomUUID().toString(), 1000.0, LocalDate.of(1990, 1, 1), 12, 1030.0, 85.83, 30.0);
        Simulacao simulacao2 = new Simulacao(UUID.randomUUID().toString(), 2000.0, LocalDate.of(1985, 6, 15), 24, 2060.0, 85.83, 60.0);
        when(repositorio.findAll()).thenReturn(Flux.just(simulacao1, simulacao2));

        Flux<Simulacao> resultado = simulacaoService.buscarTodas();

        StepVerifier.create(resultado)
                .expectNext(simulacao1)
                .expectNext(simulacao2)
                .verifyComplete();

        verify(repositorio, times(1)).findAll();
    }

    @Test
    void deveRetornarErroQuandoSimulacaoNaoEncontrada() {
        String id = UUID.randomUUID().toString();
        when(repositorio.findById(id)).thenReturn(Mono.empty());

        RuntimeException exception = null;
        try {
            simulacaoService.buscarSimulacaoPorId(id);
        } catch (RuntimeException ex) {
            exception = ex;
        }

        assert exception != null;
        assert exception.getMessage().equals("Simulação não encontrada para o ID: " + id);

        verify(repositorio, times(1)).findById(id);
    }

    @Test
    void deveSimularEmLoteComSucesso() {
        RequestSimulacao solicitacao1 = new RequestSimulacao(1000.0, LocalDate.of(1990, 1, 1), 12, TipoTaxa.FIXA);
        RequestSimulacao solicitacao2 = new RequestSimulacao(2000.0, LocalDate.of(1985, 6, 15), 24, TipoTaxa.VARIAVEL);

        Simulacao simulacao1 = new Simulacao(UUID.randomUUID().toString(), 1000.0, LocalDate.of(1990, 1, 1), 12, 1030.0, 85.83, 30.0);
        Simulacao simulacao2 = new Simulacao(UUID.randomUUID().toString(), 2000.0, LocalDate.of(1985, 6, 15), 24, 2060.0, 85.83, 60.0);

        when(repositorio.save(any(Simulacao.class))).thenReturn(Mono.just(simulacao1), Mono.just(simulacao2));

        Mono<Void> resultado = simulacaoService.simularEmLote(List.of(solicitacao1, solicitacao2));

        StepVerifier.create(resultado)
                .verifyComplete();

        verify(repositorio, times(2)).save(any(Simulacao.class));
        verify(emailService, times(1)).montarCorpoEmailLote(anyList());

    }
}