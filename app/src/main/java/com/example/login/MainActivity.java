package com.example.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

    // Método para verificar usuario en la base de datos
    private void verificarUsuario(String usuario, String clave) {
        String url = "http://52.71.115.13/consultarUsuario.php?correo=" + usuario + "&clave=" + clave;
        new VerificarUsuarioTask(usuario).execute(url);
    }

    // Clase interna para manejar la solicitud en segundo plano
    private class VerificarUsuarioTask extends AsyncTask<String, Void, String> {
        private final String emailIngresado;

        // Constructor para inicializar el email
        public VerificarUsuarioTask(String emailIngresado) {
            this.emailIngresado = emailIngresado;
        }

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                // Intenta convertir el resultado a un número para verificar que es un ID válido
                int id = Integer.parseInt(result);

                // Guardar el ID en SharedPreferences
                SharedPreferences sharedPref = getSharedPreferences("InspectorPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("inspector_id", id);
                editor.apply();

                // Navegar a Dashboard
                Intent ventanaDashboard = new Intent(MainActivity.this, Dashboard.class);
                ventanaDashboard.putExtra("correo", emailIngresado); // Agrega el correo al intent
                startActivity(ventanaDashboard);

            } catch (NumberFormatException e) {
                // Si no es un ID válido, muestra un mensaje de error
                Toast.makeText(MainActivity.this, "Usuario no registrado o datos incorrectos.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
