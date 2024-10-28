package com.example.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardUsuario extends AppCompatActivity {

    private LinearLayout containerListarUsuarios, containerRegistrarUsuarios;
    // navBar start
    private LinearLayout containerNavBarInicio, containerNavBarUser, containerNavBarSensor;
    private ImageView navBarGit;
    // navBar end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_usuario);

        // Inicializar los TextView de la interfaz
        containerListarUsuarios = findViewById(R.id.containerListarUsuarios);
        containerRegistrarUsuarios = findViewById(R.id.containerRegistrarUsuarios);
        //navBar
        navBarGit = findViewById(R.id.navBarGit);
        containerNavBarSensor = findViewById(R.id.containerNavBarSensor);
        containerNavBarInicio = findViewById(R.id.containerNavBarInicio);
        containerNavBarUser = findViewById(R.id.containerNavBarUser);
        //navBar end

        //navBar Funcionalidad
        containerNavBarInicio.setOnClickListener(view -> {
            Intent intent2 = new Intent(DashboardUsuario.this, Dashboard.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent2);
        });

        containerNavBarUser.setOnClickListener(view -> {
            Intent intent2 = new Intent(DashboardUsuario.this, DashboardUsuario.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent2);
        });

        containerNavBarSensor.setOnClickListener(view -> {
            Intent intent2 = new Intent(DashboardUsuario.this, MonitoreoSensores.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent2);
        });

        navBarGit.setOnClickListener(view -> {
            String url = "https://github.com/Anaguirv/appMovil";
            Intent intent2 = new Intent(Intent.ACTION_VIEW);
            intent2.setData(Uri.parse(url));
            startActivity(intent2);
        });
        // navBar END

        // Configurar el clic en "Listar Usuarios" para abrir la actividad ListadoUsuarios
        containerListarUsuarios.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardUsuario.this, ListadoUsuarios.class);
            startActivity(intent);
        });

        // Configurar el clic en "Registrar Usuarios" para abrir la actividad Registro
        containerRegistrarUsuarios.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardUsuario.this, RegistroUsuario.class);
            startActivity(intent);
        });

    }
}
