package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrearClave extends AppCompatActivity {

    private EditText ingresoClave, repetirClave;
    private Button btnRegistrarClaveNueva;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crear_clave);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Vincular los EditTexts y el botón a la capa de diseño
        ingresoClave = findViewById(R.id.ingresoClave);
        repetirClave = findViewById(R.id.repetirClave);
        btnRegistrarClaveNueva = findViewById(R.id.btnRegistrarClaveNueva);

        // Configurar el evento de clic del botón "Registrar"
        btnRegistrarClaveNueva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarYRegistrarClave();
            }
        });

    }
    private void validarYRegistrarClave() {
        // Obtener las entradas de texto
        String clave = ingresoClave.getText().toString();
        String repetirClaveTexto = repetirClave.getText().toString();

        // Validar que los campos no estén vacíos
        if (TextUtils.isEmpty(clave)) {
            ingresoClave.setError("Por favor, ingresa una contraseña");
            Toast.makeText(this, "El campo de clave no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(repetirClaveTexto)) {
            repetirClave.setError("Por favor, repita contraseña ingresada");
            Toast.makeText(this, "El campo de repetir clave no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que ambas claves coincidan
        if (!clave.equals(repetirClaveTexto)) {
            ingresoClave.setError("Por favor, ingresa una contraseña");
            Toast.makeText(this, "Las claves no coinciden", Toast.LENGTH_LONG).show();
            return;
        }

        // Validar robustez de la clave
        if (!esClaveValida(clave)) {
            Toast.makeText(this, "La clave debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial.", Toast.LENGTH_LONG).show();
            return;
        }

        // Si todo es correcto, mostramos un mensaje de éxito
        Toast.makeText(this, "Clave registrada con éxito", Toast.LENGTH_SHORT).show();

        // Redirigir a otra actividad si es necesario, o realizar la operación deseada
        Intent intent = new Intent(CrearClave.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Método para validar que la clave cumpla con los requisitos
    private boolean esClaveValida(String clave) {
        // Expresión regular para una clave robusta
        String passwordPattern = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!_-])(?=\\S+$).{8,}$";
        Pattern pattern = Pattern.compile(passwordPattern);
        Matcher matcher = pattern.matcher(clave);
        return matcher.matches();
    }

}