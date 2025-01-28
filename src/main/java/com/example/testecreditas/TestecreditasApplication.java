package com.example.testecreditas;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@OpenAPIDefinition(
        info = @Info(
                title = "API de Simulação de Crédito",
                version = "1.0",
                description = "Endpoints para simular e gerenciar empréstimos"
        )
)
@SpringBootApplication
@EnableAsync
public class TestecreditasApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(TestecreditasApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Rodando");
    }
}
