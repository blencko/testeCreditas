package com.example.testecreditas.application.service;

import com.example.testecreditas.application.dto.ResponseSimulacao;
import com.example.testecreditas.domain.Simulacao;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Value("${app.mail.destinatario}")
    private String destinatario;

    @Value("${app.mail.destinatario}")
    private String remetentePadrao;


    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void configurarMailSender(Simulacao simulacao) {
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");

            helper.setFrom(remetentePadrao);
            helper.setTo(destinatario);
            helper.setSubject("Sua simulação foi realizada");

            helper.setText(montarCorpoEmail(simulacao), true);

            mailSender.send(mensagem);
            log.info("E-mail enviado para: " + destinatario);
        } catch (MessagingException e) {
            log.error("Erro ao enviar e-mail: " + e.getMessage(), e);
        }
    }

    public void enviarEmailLote(String corpoEmail) {
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");

            helper.setFrom(remetentePadrao);
            helper.setTo(destinatario);
            helper.setSubject("Resumo das Simulações - Lote");
            helper.setText(corpoEmail, true);

            mailSender.send(mensagem);
            log.info("E-mail enviado com o resumo das simulações em lote para: " + destinatario);
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail em lote: " + e.getMessage(), e);
        }
    }


    private String montarCorpoEmail(Simulacao simulacao) {
        return String.format(
                """
                        <h1>Resultado da Simulação</h1>
                        <table border="1" style="border-collapse: collapse; width: 100%%;">
                            <tr><th>ID</th><td>%s</td></tr>
                            <tr><th>Valor Total</th><td>%.2f</td></tr>
                            <tr><th>Parcela Mensal</th><td>%.2f</td></tr>
                            <tr><th>Total Juros</th><td>%.2f</td></tr>
                        </table>
                        <p>Atenciosamente,</p>
                        <p>Equipe de Simulação</p>
                        """,
                simulacao.id(),
                simulacao.valorTotal(),
                simulacao.valorParcelaMensal(),
                simulacao.totalJuros()
        );
    }

    public String montarCorpoEmailLote(List<ResponseSimulacao> resultados) {
        DecimalFormat df = new DecimalFormat("#.##");

        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Resultado das Simulações</h1>")
                .append("<p>Segue abaixo o detalhamento das simulações realizadas:</p>")
                .append("<table style='border-collapse: collapse; width: 100%;'>")
                .append("<thead>")
                .append("<tr>")
                .append("<th style='border: 1px solid #ddd; padding: 8px;'>ID</th>")
                .append("<th style='border: 1px solid #ddd; padding: 8px;'>Valor Total</th>")
                .append("<th style='border: 1px solid #ddd; padding: 8px;'>Parcela Mensal</th>")
                .append("<th style='border: 1px solid #ddd; padding: 8px;'>Total Juros</th>")
                .append("</tr>")
                .append("</thead>")
                .append("<tbody>");
        for (ResponseSimulacao simulacao : resultados) {
            sb.append("<tr>")
                    .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(simulacao.id()).append("</td>")
                    .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(df.format(simulacao.valorTotal())).append("</td>")
                    .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(df.format(simulacao.valorParcelaMensal())).append("</td>")
                    .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(df.format(simulacao.totalJuros())).append("</td>")
                    .append("</tr>");
        }
        sb.append("</tbody>")
                .append("</table>")
                .append("<p>Atenciosamente,</p>")
                .append("<p>Equipe de Simulação</p>");

        return sb.toString();
    }


}
