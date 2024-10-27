package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class RecuperarClave extends AppCompatActivity {

    private EditText txtUserRegistro;
    private Button btnRegistro;
    private TextView textViewTimer, textViewMensajeTiempo;
    private final String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long tiempoInicial = 300000; // 5 minutos en milisegundos
    private EditText editText3, editText4, editText5, editText6, editText7;
    private String codigoVerificacion;
    private String correo;  // Definimos `correo` como una variable global

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_clave);

        txtUserRegistro = findViewById(R.id.usuarioRegistro);
        btnRegistro = findViewById(R.id.btnRegistrar);
        textViewTimer = findViewById(R.id.textView3);
        textViewMensajeTiempo = findViewById(R.id.textView2);
        editText3 = findViewById(R.id.editTextNumber3);
        editText4 = findViewById(R.id.editTextNumber4);
        editText5 = findViewById(R.id.editTextNumber5);
        editText6 = findViewById(R.id.editTextNumber6);
        editText7 = findViewById(R.id.editTextNumber7);

        configurarSaltoAutomatico(editText3, editText4);
        configurarSaltoAutomatico(editText4, editText5);
        configurarSaltoAutomatico(editText5, editText6);
        configurarSaltoAutomatico(editText6, editText7);
        validarCodigoAlFinal(editText7);

        btnRegistro.setOnClickListener(view -> {
            correo = txtUserRegistro.getText().toString().trim();
            if (correo.isEmpty()) {
                txtUserRegistro.setError("Este campo no puede estar vacío");
                Toast.makeText(RecuperarClave.this, "Por favor, ingrese un correo", Toast.LENGTH_LONG).show();
            } else if (!correo.matches(emailPattern)) {
                txtUserRegistro.setError("Ingrese un correo válido");
                Toast.makeText(RecuperarClave.this, "Formato de correo incorrecto", Toast.LENGTH_LONG).show();
            } else {
                if (!isTimerRunning) {
                    verificarCorreoYEnviarCodigo(correo);
                }
            }
        });
    }

    private void verificarCorreoYEnviarCodigo(String correo) {
        String url = "http://52.71.115.13/guardarCodigoVerificacion.php?correo=" + correo;
        RequestQueue queue = Volley.newRequestQueue(this);

        Log.d("RecuperarClave", "URL de solicitud: " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean success = jsonResponse.getBoolean("success");
                        if (success) {
                            // Obtener el código de verificación desde la respuesta del servidor
                            codigoVerificacion = jsonResponse.getString("codigoVerificacion");
                            Log.d("RecuperarClave", "Código de verificación obtenido: " + codigoVerificacion);


                            // Iniciar el envío del correo con el código de verificación
                            MailSender mailSender = new MailSender();
                            String asunto = "Código de Verificación";
                            String cuerpoMensaje = "Tu código de verificación es: " + codigoVerificacion;
                            mailSender.enviarCorreo(correo, asunto, cuerpoMensaje);
                            Log.d("RecuperarClave", "Correo enviado a " + correo + " con el código " + codigoVerificacion);


                            iniciarCuentaRegresiva(tiempoInicial);
                            Toast.makeText(RecuperarClave.this, "Código enviado al correo", Toast.LENGTH_LONG).show();
                            mostrarCamposCodigo();
} else {
                            Toast.makeText(RecuperarClave.this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(RecuperarClave.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(RecuperarClave.this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        queue.add(stringRequest);
    }

    private void mostrarCamposCodigo() {
        editText3.setVisibility(View.VISIBLE);
        editText4.setVisibility(View.VISIBLE);
        editText5.setVisibility(View.VISIBLE);
        editText6.setVisibility(View.VISIBLE);
        editText7.setVisibility(View.VISIBLE);
        textViewMensajeTiempo.setVisibility(View.VISIBLE);
        textViewTimer.setVisibility(View.VISIBLE);
    }

    private void validarCodigoAlFinal(EditText ultimoInput) {
        ultimoInput.setOnKeyListener((v, keyCode, event) -> {
            if (ultimoInput.getText().toString().length() == 1 && event.getAction() == KeyEvent.ACTION_UP) {
                String codigoIngresado = editText3.getText().toString()
                        + editText4.getText().toString()
                        + editText5.getText().toString()
                        + editText6.getText().toString()
                        + editText7.getText().toString();

                if (codigoIngresado.equals(codigoVerificacion)) {
                    Toast.makeText(RecuperarClave.this, "Código válido", Toast.LENGTH_LONG).show();
                    cargarCrearClave();
                } else {
                    Toast.makeText(RecuperarClave.this, "Código incorrecto", Toast.LENGTH_LONG).show();
                    limpiarCampos();
                }
            }
            return false;
        });
    }

    private void limpiarCampos() {
        editText3.setText("");
        editText4.setText("");
        editText5.setText("");
        editText6.setText("");
        editText7.setText("");
        editText3.requestFocus();
    }

    private void cargarCrearClave() {
        Intent intent = new Intent(RecuperarClave.this, CrearClave.class);
        intent.putExtra("correo", correo);  // Ahora, `correo` es una variable global
        startActivity(intent);
        finish();
    }

    private void iniciarCuentaRegresiva(long tiempo) {
        countDownTimer = new CountDownTimer(tiempo, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                isTimerRunning = true;
                int minutos = (int) (millisUntilFinished / 1000) / 60;
                int segundos = (int) (millisUntilFinished / 1000) % 60;
                String tiempoRestante = String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);
                textViewTimer.setText(tiempoRestante);
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                textViewTimer.setText("00:00");
                Toast.makeText(RecuperarClave.this, "Tiempo terminado. El código expiró.", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private void configurarSaltoAutomatico(EditText actual, EditText siguiente) {
        actual.setOnKeyListener((v, keyCode, event) -> {
            if (actual.getText().toString().length() == 1 && event.getAction() == KeyEvent.ACTION_UP) {
                siguiente.requestFocus();
            }
            return false;
        });
    }
}
