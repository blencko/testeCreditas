package com.example.testecreditas;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.CoreMatchers.equalTo;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestecreditasApplication.class)
class SimulacaoControllerPerformanceTest {


    private static final int LOTE_SIZE = 20000;
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:latest").withEnv("MONGO_INITDB_DATABASE", "testdb");

    static {
        mongodb.start();
    }

    @Autowired
    private TestRestTemplate testRestTemplate;
    @LocalServerPort
    private Integer port;

    @DynamicPropertySource
    static void config(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> mongodb.getReplicaSetUrl("testdb"));
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
    }

    public String simulacaoInputRequestMock(Float value, String date, Integer prazo) {
        return """
                     {
                         "valorEmprestimo": %s,
                         "dataNascimento": "%s",
                         "prazoMeses": %d
                     }
                """.formatted(value, date, prazo);
    }

    @Test
    void testa_simulacao_simples() {
        var body = simulacaoInputRequestMock(58263f, "1987-08-11", 52);
        RestAssured.given()
                .body(body)
                .contentType("application/json")
                .post("/api/simulacoes")
                .then().assertThat().statusCode(200)
                .assertThat().body("valorEmprestimo", equalTo(58263f));
    }

    @Test
    void simularEmLote_AltaVolumetria() {
        var sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < LOTE_SIZE; i++) {
            sb.append(simulacaoInputRequestMock(1000.0f + i, "1987-08-11", 12));
            if (i != LOTE_SIZE - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        RestAssured.given()
                .contentType("application/json")
                .body(sb.toString())
                .post("/api/simulacoes/lote")
                .then().assertThat().statusCode(200)
                .assertThat().time(Matchers.lessThan(4500L));
    }

}
