package com.example.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardUsuario extends AppCompatActivity {

    private TextView txtListarUsuarios, txtRegistrarUsuarios;
    private ImageView navBarInicio, navBarUser, navBarSensor, navBarGit;
    private TextView txtNavBarInicio, txtNavBarUser, txtNavBarSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_usuario);

        // Inicializar los TextView de la interfaz
        txtListarUsuarios = findViewById(R.id.txtlistar);
        txtRegistrarUsuarios = findViewById(R.id.txtRegistrarUsuarios);
        //navBar
        navBarGit = findViewById(R.id.navBarGit);
        navBarSensor = findViewById(R.id.navBarSensor);
        navBarUser = findViewById(R.id.navBarUser);
        navBarInicio = findViewById(R.id.navBarInicio);
        txtNavBarSensor = findViewById(R.id.txtNavBarSensor);
        txtNavBarUser = findViewById(R.id.txtNavBarUser);
        txtNavBarInicio = findViewById(R.id.txtNavBarInicio);
        //navBar end


        // Configurar el clic en "Listar Usuarios" para abrir la actividad ListadoUsuarios
        txtListarUsuarios.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardUsuario.this, ListadoUsuarios.class);
            startActivity(intent);
        });

        // Configurar el clic en "Registrar Usuarios" para abrir la actividad Registro
        txtRegistrarUsuarios.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardUsuario.this, RegistroUsuario.class);
            startActivity(intent);
        });


        // Funcionalidad navBar
        navBarInicio.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardUsuario.this, Dashboard.class);
            startActivity(intent);
        });
        txtNavBarInicio.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardUsuario.this, Dashboard.class);
            startActivity(intent);
        });

        navBarUser.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardUsuario.this, DashboardUsuario.class);
            startActivity(intent);
        });
        txtNavBarUser.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardUsuario.this, DashboardUsuario.class);
            startActivity(intent);
        });

        navBarSensor.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardUsuario.this, MonitoreoSensores.class);
            startActivity(intent);
        });
        txtNavBarSensor.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardUsuario.this, MonitoreoSensores.class);
            startActivity(intent);
        });

        navBarGit.setOnClickListener(view -> {
            String url = "https://github.com/Anaguirv/appMovil";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
        // navBar END

    }
}
