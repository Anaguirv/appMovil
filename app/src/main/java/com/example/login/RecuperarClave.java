package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RecuperarClave extends AppCompatActivity {

    private EditText txtUserRegistro;
    private Button btnRegistro;
    private TextView textViewTimer;
    private String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"; // Patrón de correo electrónico
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long tiempoInicial = 300000; // 5 minutos en milisegundos (300,000 ms)

    private EditText editText3, editText4, editText5, editText6, editText7;
    private String codigoValido = "66666";  // Código correcto


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recuperar_clave);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Vincular la vista con el controlador
        txtUserRegistro = findViewById(R.id.txtuserregistro);
        btnRegistro = findViewById(R.id.btnregistro);
        textViewTimer = findViewById(R.id.textView3); // TextView para la cuenta regresiva
        // Vincular los EditTexts de los inputs de números
        editText3 = findViewById(R.id.editTextNumber3);
        editText4 = findViewById(R.id.editTextNumber4);
        editText5 = findViewById(R.id.editTextNumber5);
        editText6 = findViewById(R.id.editTextNumber6);
        editText7 = findViewById(R.id.editTextNumber7);

        // Configurar el salto automático al siguiente input
        configurarSaltoAutomatico(editText3, editText4);
        configurarSaltoAutomatico(editText4, editText5);
        configurarSaltoAutomatico(editText5, editText6);
        configurarSaltoAutomatico(editText6, editText7);

        // Validar el código después de ingresar el último número
        validarCodigoAlFinal(editText7);

        // Mostrar inicialmente 5 minutos en formato "05:00"
        textViewTimer.setText("05:00");

        // Configurar la acción al hacer clic en el botón "Enviar"
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String correo = txtUserRegistro.getText().toString().trim();

                // Validar si el campo de correo no está vacío y si cumple con el formato
                if (correo.isEmpty()) {
                    txtUserRegistro.setError("Este campo no puede estar vacío");
                    Toast.makeText(RecuperarClave.this, "Por favor, ingrese un correo", Toast.LENGTH_LONG).show();
                } else if (!correo.matches(emailPattern)) {
                    txtUserRegistro.setError("Ingrese un correo válido");
                    Toast.makeText(RecuperarClave.this, "Formato de correo incorrecto", Toast.LENGTH_LONG).show();
                } else {
                    // Iniciar la cuenta regresiva si el correo es válido y no se está ejecutando ya
                    if (!isTimerRunning) {
                        iniciarCuentaRegresiva(tiempoInicial);
                        Toast.makeText(RecuperarClave.this, "Código enviado al correo", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        // Ajustes para EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Método para configurar el salto automático entre inputs
    private void configurarSaltoAutomatico(EditText actual, EditText siguiente) {
        actual.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // Comprobar si el usuario ingresó un número y no es un evento de "soltar tecla"
                if (actual.getText().toString().length() == 1 && event.getAction() == KeyEvent.ACTION_UP) {
                    siguiente.requestFocus(); // Cambiar el foco al siguiente campo
                }
                return false;
            }
        });
    }

    // Método para validar el código al final del último input
    private void validarCodigoAlFinal(EditText ultimoInput) {
        ultimoInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // Comprobar si se ingresó el último número y se soltó la tecla
                if (ultimoInput.getText().toString().length() == 1 && event.getAction() == KeyEvent.ACTION_UP) {
                    // Concatenar el código ingresado de todos los inputs
                    String codigoIngresado = editText3.getText().toString()
                            + editText4.getText().toString()
                            + editText5.getText().toString()
                            + editText6.getText().toString()
                            + editText7.getText().toString();

                    // Validar si el código es correcto
                    if (codigoIngresado.equals(codigoValido)) {
                        // Mostrar un mensaje de éxito y redirigir al dashboard
                        Toast.makeText(RecuperarClave.this, "Código válido", Toast.LENGTH_LONG).show();
                        cargarDashboard();
                    } else {
                        // Mostrar un mensaje de error si el código no es válido
                        Toast.makeText(RecuperarClave.this, "Código incorrecto", Toast.LENGTH_LONG).show();
                    }
                }
                return false;
            }
        });
    }

    // Método para cargar el Activity para crear nueva clave
    private void cargarDashboard() {
        Intent intent = new Intent(RecuperarClave.this, CrearClave.class);
        startActivity(intent);
        finish(); // Opcional: Finaliza el Activity actual para que no se pueda volver atrás
    }


    // Método para iniciar la cuenta regresiva de 5 minutos (300000 milisegundos)
    private void iniciarCuentaRegresiva(long tiempo) {
        countDownTimer = new CountDownTimer(tiempo, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                isTimerRunning = true;
                // Calcular los minutos y segundos restantes
                int minutos = (int) (millisUntilFinished / 1000) / 60;
                int segundos = (int) (millisUntilFinished / 1000) % 60;

                // Formatear el tiempo en "MM:SS" y mostrarlo en el TextView
                String tiempoRestante = String.format("%02d:%02d", minutos, segundos);
                textViewTimer.setText(tiempoRestante);
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                textViewTimer.setText("00:00");
                Toast.makeText(RecuperarClave.this, "Tiempo terminado. El código expiro.", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

}