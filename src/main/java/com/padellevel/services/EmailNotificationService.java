package com.padellevel.services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * Servicio para envío de emails utilizando SendGrid.
 *
 * Características:
 * - Envío de emails transaccionales
 * - Soporte para plantillas HTML
 * - Variables dinámicas en plantillas
 * - Modo mock para desarrollo (cuando SendGrid está deshabilitado)
 */
@Service
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name}")
    private String fromName;

    @Value("${sendgrid.enabled:false}")
    private boolean sendGridEnabled;

    /**
     * Envía un email simple con texto plano.
     *
     * @param toEmail Email del destinatario
     * @param subject Asunto del email
     * @param body Cuerpo del mensaje (texto plano)
     */
    public void enviarEmail(String toEmail, String subject, String body) {
        enviarEmail(toEmail, subject, body, null);
    }

    /**
     * Envía un email con variables para personalización.
     *
     * @param toEmail Email del destinatario
     * @param subject Asunto del email
     * @param body Cuerpo del mensaje
     * @param variables Variables para reemplazar en el mensaje
     */
    public void enviarEmail(String toEmail, String subject, String body, Map<String, String> variables) {
        logger.info("Enviando email a {} - Asunto: {}", toEmail, subject);

        // Si SendGrid está deshabilitado, solo loggear (modo desarrollo)
        if (!sendGridEnabled) {
            logger.info("SendGrid deshabilitado - Email simulado enviado a {}", toEmail);
            logger.debug("Contenido del email:\nAsunto: {}\nCuerpo:\n{}", subject, body);
            return;
        }

        try {
            // Reemplazar variables en el cuerpo si existen
            String bodyWithVariables = body;
            if (variables != null && !variables.isEmpty()) {
                bodyWithVariables = reemplazarVariables(body, variables);
            }

            // Crear email con SendGrid
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(toEmail);
            Content content = new Content("text/plain", bodyWithVariables);
            Mail mail = new Mail(from, subject, to, content);

            // Enviar email
            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("Email enviado exitosamente a {} - Status: {}",
                           toEmail, response.getStatusCode());
            } else {
                logger.error("Error al enviar email - Status: {} - Body: {}",
                           response.getStatusCode(), response.getBody());
            }

        } catch (IOException e) {
            logger.error("Error al enviar email a {}", toEmail, e);
            throw new RuntimeException("Error al enviar email", e);
        }
    }

    /**
     * Envía un email HTML con formato enriquecido.
     *
     * @param toEmail Email del destinatario
     * @param subject Asunto del email
     * @param htmlBody Cuerpo del mensaje en HTML
     * @param variables Variables para reemplazar en el HTML
     */
    public void enviarEmailHtml(String toEmail, String subject, String htmlBody,
                                Map<String, String> variables) {
        logger.info("Enviando email HTML a {} - Asunto: {}", toEmail, subject);

        if (!sendGridEnabled) {
            logger.info("SendGrid deshabilitado - Email HTML simulado enviado a {}", toEmail);
            return;
        }

        try {
            // Reemplazar variables
            String htmlWithVariables = htmlBody;
            if (variables != null && !variables.isEmpty()) {
                htmlWithVariables = reemplazarVariables(htmlBody, variables);
            }

            Email from = new Email(fromEmail, fromName);
            Email to = new Email(toEmail);
            Content content = new Content("text/html", htmlWithVariables);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("Email HTML enviado exitosamente a {}", toEmail);
            } else {
                logger.error("Error al enviar email HTML - Status: {}",
                           response.getStatusCode());
            }

        } catch (IOException e) {
            logger.error("Error al enviar email HTML a {}", toEmail, e);
            throw new RuntimeException("Error al enviar email HTML", e);
        }
    }

    /**
     * Envía un email usando una plantilla de SendGrid.
     *
     * @param toEmail Email del destinatario
     * @param templateId ID de la plantilla en SendGrid
     * @param dynamicData Datos dinámicos para la plantilla
     */
    public void enviarEmailConPlantilla(String toEmail, String templateId,
                                       Map<String, Object> dynamicData) {
        logger.info("Enviando email con plantilla {} a {}", templateId, toEmail);

        if (!sendGridEnabled) {
            logger.info("SendGrid deshabilitado - Email con plantilla simulado");
            return;
        }

        try {
            Mail mail = new Mail();

            Email from = new Email(fromEmail, fromName);
            mail.setFrom(from);
            mail.setTemplateId(templateId);

            Personalization personalization = new Personalization();
            personalization.addTo(new Email(toEmail));

            // Agregar datos dinámicos
            if (dynamicData != null) {
                dynamicData.forEach(personalization::addDynamicTemplateData);
            }

            mail.addPersonalization(personalization);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("Email con plantilla enviado exitosamente");
            } else {
                logger.error("Error al enviar email con plantilla - Status: {}",
                           response.getStatusCode());
            }

        } catch (IOException e) {
            logger.error("Error al enviar email con plantilla", e);
            throw new RuntimeException("Error al enviar email con plantilla", e);
        }
    }

    /**
     * Envía emails en lote a múltiples destinatarios.
     *
     * @param toEmails Lista de emails destinatarios
     * @param subject Asunto del email
     * @param body Cuerpo del mensaje
     */
    public void enviarEmailsEnLote(java.util.List<String> toEmails, String subject, String body) {
        logger.info("Enviando emails en lote a {} destinatarios", toEmails.size());

        for (String email : toEmails) {
            try {
                enviarEmail(email, subject, body);
            } catch (Exception e) {
                logger.error("Error al enviar email a {} en lote", email, e);
                // Continuar con los siguientes emails
            }
        }

        logger.info("Lote de emails procesado");
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Reemplaza variables en el texto usando sintaxis {{variable}}.
     */
    private String reemplazarVariables(String texto, Map<String, String> variables) {
        String resultado = texto;

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            resultado = resultado.replace(placeholder, entry.getValue());
        }

        return resultado;
    }
}
