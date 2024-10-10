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
    private TextView txtSensores, txtUsuarios, txt_registro_medicion;

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
        txt_registro_medicion = (TextView) findViewById(R.id.txt_registro_medicion);

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

        //Configurar escuchador para ir a Activity RegitroMedicion
        txt_registro_medicion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ventana_regitro_medicion = new Intent(Dashboard.this, RegistroMedicion.class);
                startActivity(ventana_regitro_medicion);
            }
        });
    }
}