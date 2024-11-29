package com.example.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
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

    private void verificarUsuario(String usuario, String clave) {
        String url = "http://98.83.4.206/Login";

        JSONObject params = new JSONObject();
        try {
            params.put("username", usuario);
            params.put("password", clave);
        } catch (JSONException e) {
            Log.e(TAG, "Error al construir los parámetros JSON", e);
            Toast.makeText(MainActivity.this, "Error al construir los parámetros.", Toast.LENGTH_LONG).show();
            return;
        }

        // Registro del JSON y la URL en Logcat
        Log.d(TAG, "URL: " + url);
        Log.d(TAG, "JSON Enviado: " + params.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                response -> {
                    Log.d(TAG, "Respuesta del servidor: " + response.toString());
                    try {
                        if (response.has("id")) {
                            int id = response.getInt("id");
                            String username = response.getString("username");

                            // Registrar en Logcat
                            Log.d(TAG, "ID del usuario obtenido: " + id);
                            Log.d(TAG, "Username del usuario obtenido: " + username);

                            // Guardar el ID y el username en SharedPreferences
                            SharedPreferences sharedPref = getSharedPreferences("InspectorPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("inspector_id", id);
                            editor.putString("username", username);
                            editor.apply();

                            // Pasar el ID y el username al siguiente Activity
                            Intent ventanaDashboard = new Intent(MainActivity.this, Dashboard.class);
                            ventanaDashboard.putExtra("username", username);
                            ventanaDashboard.putExtra("id", id); // Pasar el ID
                            startActivity(ventanaDashboard);
                        } else {
                            Toast.makeText(MainActivity.this, response.getString("error"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error procesando la respuesta JSON", e);
                        Toast.makeText(MainActivity.this, "Error al procesar la respuesta del servidor.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Error en la solicitud Volley", error);
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Código de estado: " + error.networkResponse.statusCode);
                    }
                    Toast.makeText(MainActivity.this, "Error de autenticación o comunicación.", Toast.LENGTH_LONG).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        jsonObjectRequest.setShouldCache(false);

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }
}
