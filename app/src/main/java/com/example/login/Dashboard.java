package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Dashboard extends AppCompatActivity {
    // Atributos
    private TextView txtSensores, txtUsuarios;

    //Métodos
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Crear relación entre parte grafica y logica
        txtSensores = (TextView) findViewById(R.id.txtSensores);
        txtUsuarios = (TextView) findViewById(R.id.txtUsuarios);

        //Confirurar escuchador para ir a Activity sensores
        txtSensores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ventana_sensores = new Intent(Dashboard.this, MonitoreoSensores.class);
                startActivity(ventana_sensores);
            }
        });

        //Configurar escuchador para ir a Activity Usuarios
        txtUsuarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ventana_usuarios = new Intent(Dashboard.this, ListadoUsuarios.class);
                startActivity(ventana_usuarios);
            }
        });
    }
}