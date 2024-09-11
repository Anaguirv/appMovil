package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

//  Declaración de variables
    TextView txtmsj, txtregistro;
    Button btningresar;

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

//      Casteo de variables
        txtmsj=(TextView) findViewById(R.id.txtmensaje);
        btningresar=(Button) findViewById(R.id.btningresar);
        txtregistro=(TextView) findViewById(R.id.txtregistro);

        txtmsj.setText("Bienvenido a la Aplicación SFDS1");

        btningresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this, "PRESIONO EL BOTÓN!!", Toast.LENGTH_LONG).show();
                Intent ventanaingresar=new Intent(MainActivity.this, Dashboard.class);
                startActivities(new Intent[]{ventanaingresar});
            }
        });

        txtregistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ventanaregistrar=new Intent(MainActivity.this, Registro.class);
                startActivities(new Intent[]{ventanaregistrar});
            }
        });
    }
}