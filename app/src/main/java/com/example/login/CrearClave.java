package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;


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

        ingresoClave = findViewById(R.id.ingresoClave);
        repetirClave = findViewById(R.id.repetirClave);
        btnRegistrarClaveNueva = findViewById(R.id.btnRegistrarClaveNueva);

        btnRegistrarClaveNueva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarYRegistrarClave();
            }
        });
    }

    private void validarYRegistrarClave() {
        String clave = ingresoClave.getText().toString();
        String repetirClaveTexto = repetirClave.getText().toString();

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

        if (!clave.equals(repetirClaveTexto)) {
            ingresoClave.setError("Las claves no coinciden");
            Toast.makeText(this, "Las claves no coinciden", Toast.LENGTH_LONG).show();
            return;
        }

        if (!esClaveValida(clave)) {
            Toast.makeText(this, "La clave debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial.", Toast.LENGTH_LONG).show();
            return;
        }

        guardarNuevaClave(clave);
    }

    private boolean esClaveValida(String clave) {
        String passwordPattern = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!_-])(?=\\S+$).{8,}$";
        Pattern pattern = Pattern.compile(passwordPattern);
        Matcher matcher = pattern.matcher(clave);
        return matcher.matches();
    }

    private void guardarNuevaClave(String clave) {
        String correo = getIntent().getStringExtra("correo");
        Log.d("CrearClave", "Correo recibido: " + correo);  // Log para verificar que el correo se recibe correctamente
        String url = "http://52.71.115.13/guardarNuevaClave.php?correo=" + correo + "&nuevaClave=" + clave;
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("CrearClave", "Respuesta del servidor: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean success = jsonResponse.getBoolean("success");

                        if (success) {
                            Toast.makeText(CrearClave.this, "Clave registrada con éxito", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CrearClave.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(CrearClave.this, "Error al guardar la clave: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(CrearClave.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(CrearClave.this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        queue.add(stringRequest);
    }


}
