package com.example.testecreditas.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RepositorioSimulacao extends ReactiveCrudRepository<Simulacao, String> {
    Flux<Simulacao> findAll();
}