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

public class RegistroMedicion extends AppCompatActivity {

    // atributos
    private TextView tipo_alumbrado_art5, tipo_alumbrado_art6,tipo_alumbrado_art7, tipo_alumbrado_art8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_medicion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.monitoreoSensores), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Relacionar capas
        tipo_alumbrado_art5 = (TextView) findViewById(R.id.tipo_alumbrado_art5);
        tipo_alumbrado_art6 = (TextView) findViewById(R.id.tipo_alumbrado_art6);
        tipo_alumbrado_art7 = (TextView) findViewById(R.id.tipo_alumbrado_art7);
        tipo_alumbrado_art8 = (TextView) findViewById(R.id.tipo_alumbrado_art8);




        // Configurar escuchador para ir a Activity tipo alumbrado art5
        tipo_alumbrado_art5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ventana_art5 = new Intent(RegistroMedicion.this, TipoAlumbrado_art5.class);
                startActivity(ventana_art5);
            }
        });

        // Configurar escuchador para ir a Activity tipo alumbrado art6
        tipo_alumbrado_art6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ventana_art6 = new Intent(RegistroMedicion.this, TipoAlumbrado_art6.class);
                startActivity(ventana_art6);
            }

        });

        // Configurar escuchador para ir a Activity tipo alumbrado art7
        tipo_alumbrado_art7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ventana_art7 = new Intent(RegistroMedicion.this, TipoAlumbrado_art7.class);
                startActivity(ventana_art7);
            }

        });
        // Configurar escuchador para ir a Activity tipo alumbrado art7
        tipo_alumbrado_art8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ventana_art8 = new Intent(RegistroMedicion.this, TipoAlumbrado_art8.class);
                startActivity(ventana_art8);
            }

        });
    }
}