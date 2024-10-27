package com.example.login;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSender {

    private final String remitente = "galindez2020@gmail.com"; // Tu correo de Gmail
    private final String contrasena = "psnkwlotabsorirm"; // Sin espacios

    public void enviarCorreo(String destinatario, String asunto, String cuerpo) {
        new EnviarCorreoAsync(destinatario, asunto, cuerpo).execute();
    }

    private class EnviarCorreoAsync extends AsyncTask<Void, Void, Boolean> {

        private final String destinatario;
        private final String asunto;
        private final String cuerpo;

        public EnviarCorreoAsync(String destinatario, String asunto, String cuerpo) {
            this.destinatario = destinatario;
            this.asunto = asunto;
            this.cuerpo = cuerpo;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Properties propiedades = new Properties();
            propiedades.put("mail.smtp.auth", "true");
            propiedades.put("mail.smtp.starttls.enable", "true");
            propiedades.put("mail.smtp.host", "smtp.gmail.com");
            propiedades.put("mail.smtp.port", "587");

            Session sesion = Session.getInstance(propiedades, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(remitente, contrasena);
                }
            });

            try {
                Log.d("MailSender", "Preparando mensaje...");
                Message mensaje = new MimeMessage(sesion);
                mensaje.setFrom(new InternetAddress(remitente));
                mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
                mensaje.setSubject(asunto);
                mensaje.setText(cuerpo);

                Log.d("MailSender", "Intentando enviar el mensaje...");
                Transport.send(mensaje);
                Log.d("MailSender", "Correo enviado con éxito.");
                return true;

            } catch (Exception e) {
                Log.e("MailSender", "Error al enviar el correo", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Log.d("MailSender", "Correo enviado exitosamente.");
            } else {
                Log.d("MailSender", "Error al enviar el correo.");
            }
        }
    }
}
