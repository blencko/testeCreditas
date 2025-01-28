package com.example.testecreditas;

import com.example.testecreditas.application.dto.ResponseSimulacao;
import com.example.testecreditas.application.service.EmailService;
import com.example.testecreditas.domain.Simulacao;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;


public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "remetentePadrao", "test@test.com");
        ReflectionTestUtils.setField(emailService, "destinatario", "test@test.com");
    }

    @Test
    void deveEnviarEmailSimples() throws Exception {
        Simulacao simulacao = new Simulacao(
                UUID.randomUUID().toString(),
                1000.0,
                LocalDate.of(1990, 1, 1),
                12,
                1030.0,
                85.83,
                30.0
        );

        MimeMessage mockMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMessage);

        emailService.configurarMailSender(simulacao);

        verify(mailSender, times(1)).send(mockMessage);
    }

    @Test
    void deveEnviarEmailLote() throws Exception {
        List<ResponseSimulacao> simulacoes = List.of(
                new ResponseSimulacao(
                        UUID.randomUUID().toString(),
                        1030.0,
                        85.83,
                        30.0,
                        1000.0,
                        LocalDate.of(1990, 1, 1),
                        12
                ),
                new ResponseSimulacao(
                        UUID.randomUUID().toString(),
                        2060.0,
                        171.66,
                        60.0,
                        2000.0,
                        LocalDate.of(1985, 6, 15),
                        24
                )
        );

        MimeMessage mockMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMessage);

        String corpoEmail = emailService.montarCorpoEmailLote(simulacoes);
        emailService.enviarEmailLote(corpoEmail);

        verify(mailSender, times(1)).send(mockMessage);
    }


}
