package com.example.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private TextView txtmsj, txtregistro, txtolvide;
    private Button btningresar;
    private EditText txtuser, txtclave;
    private String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtmsj = findViewById(R.id.txtmensaje);
        btningresar = findViewById(R.id.btningresar);
        txtregistro = findViewById(R.id.txtregistro);
        txtolvide = findViewById(R.id.txtolvide);
        txtuser = findViewById(R.id.txtuser);
        txtclave = findViewById(R.id.txtclave);

        txtmsj.setText("Bienvenido a la Aplicación SFDS1");

        btningresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usuario = txtuser.getText().toString().trim();
                String clave = txtclave.getText().toString().trim();

                if (usuario.isEmpty()) {
                    txtuser.setError("Este campo no puede estar vacío");
                    Toast.makeText(MainActivity.this, "Debe ingresar un usuario (email)", Toast.LENGTH_LONG).show();
                } else if (!validarPatron(emailPattern, usuario)) {
                    txtuser.setError("Por favor, ingresa un email válido");
                    Toast.makeText(MainActivity.this, "El email ingresado no cumple con el formato", Toast.LENGTH_LONG).show();
                } else if (clave.isEmpty()) {
                    txtclave.setError("Este campo no puede estar vacío");
                    Toast.makeText(MainActivity.this, "Debe ingresar una contraseña", Toast.LENGTH_LONG).show();
                } else {
                    // Llamar al método para verificar usuario en la base de datos
                    verificarUsuario(usuario, clave);
                }
            }
        });

        txtregistro.setOnClickListener(view -> {
            Intent ventanaRegistrar = new Intent(MainActivity.this, Registro.class);
            startActivity(ventanaRegistrar);
        });

        txtolvide.setOnClickListener(view -> {
            Intent ventanaRecuperar = new Intent(MainActivity.this, RecuperarClave.class);
            startActivity(ventanaRecuperar);
        });
    }

    private boolean validarPatron(String patron, String variable) {
        Pattern pattern = Pattern.compile(patron);
        Matcher matcher = pattern.matcher(variable);
        return matcher.matches();
    }

    // Método para verificar usuario en la base de datos utilizando Volley con POST
    private void verificarUsuario(String usuario, String clave) {
        String url = "http://98.83.4.206:8080/ApiLogin";

        // Crear el cuerpo de la solicitud como JSON
        JSONObject params = new JSONObject();
        try {
            params.put("correo", usuario);
            params.put("clave", clave);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Error al construir los parámetros.", Toast.LENGTH_LONG).show();
            return;
        }

        // Crear la solicitud POST usando Volley
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Procesar la respuesta JSON
                            int id = response.getInt("id");
                            String correo = response.getString("correo");

                            // Guardar el ID en SharedPreferences
                            SharedPreferences sharedPref = getSharedPreferences("InspectorPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("inspector_id", id);
                            editor.putString("correo", correo);
                            editor.apply();

                            // Navegar a Dashboard
                            Intent ventanaDashboard = new Intent(MainActivity.this, Dashboard.class);
                            ventanaDashboard.putExtra("correo", correo);
                            startActivity(ventanaDashboard);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error al procesar la respuesta del servidor.", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Contraseña o Correo Incorrecto: " , Toast.LENGTH_LONG).show();
                    }
                });

        // Agregar la solicitud a la cola de Volley
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }
}
