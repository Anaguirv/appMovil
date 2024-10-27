package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardUsuario extends AppCompatActivity {

    private TextView txtListarUsuarios, txtRegistrarUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_usuario);

        // Inicializar los TextView de la interfaz
        txtListarUsuarios = findViewById(R.id.txtlistar);
        txtRegistrarUsuarios = findViewById(R.id.txtRegistrarUsuarios);

        // Configurar el clic en "Listar Usuarios" para abrir la actividad ListadoUsuarios
        txtListarUsuarios.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardUsuario.this, ListadoUsuarios.class);
            startActivity(intent);
        });

        // Configurar el clic en "Registrar Usuarios" para abrir la actividad Registro
        txtRegistrarUsuarios.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardUsuario.this, Registro.class);
            startActivity(intent);
        });
    }
}
