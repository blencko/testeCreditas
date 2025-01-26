
package com.example.testecreditas;

import com.example.testecreditas.application.dto.RequestSimulacao;
import com.example.testecreditas.application.dto.ResponseSimulacao;
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
import java.util.UUID;

import static org.mockito.Mockito.*;


class SimulacaoServiceTest {

    @Mock
    private RepositorioSimulacao repositorio;

    private SimulacaoService simulacaoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        simulacaoService = new SimulacaoService(repositorio);
    }

    @Test
    void deveSimularComDadosValidos() {
        RequestSimulacao solicitacao = new RequestSimulacao(1000.0, LocalDate.of(1990, 1, 1), 12);
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
        Mono<ResponseSimulacao> resultado = simulacaoService.simular(solicitacao);
        StepVerifier.create(resultado)
                .expectNextMatches(res -> res.valorEmprestimo() == 1000.0 && res.totalJuros() == 30.0)
                .verifyComplete();
        verify(repositorio, times(1)).save(any(Simulacao.class));
    }

    @Test
    void deveRetornarErroParaDadosInvalidos() {
        RequestSimulacao solicitacao = new RequestSimulacao(-500.0, LocalDate.of(1990, 1, 1), -12);
        Mono<ResponseSimulacao> resultado = simulacaoService.simular(solicitacao);

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

        try {
            simulacaoService.buscarSimulacaoPorId(id);
        } catch (RuntimeException ex) {
            assert ex.getMessage().equals("Simulação não encontrada para o ID: " + id);
        }

        verify(repositorio, times(1)).findById(id);
    }
}
