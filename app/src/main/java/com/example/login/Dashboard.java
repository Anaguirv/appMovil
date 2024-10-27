package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Dashboard extends AppCompatActivity {
    // Atributos
    TextView txtFecha, txtUsuario;
    private TextView txtSensores, txtUsuarios, txtRegistroMedicion;
    private final Handler mHandler = new Handler();

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

        // Obtener el correo desde el Intent
        Intent intent = getIntent();
        String correo = intent.getStringExtra("correo");

        // Crear relación entre parte gráfica y lógica
        txtFecha = findViewById(R.id.txt_fecha);
        txtUsuario = findViewById(R.id.txt_usuario);

        // Configurar el texto para mostrar la fecha y el correo por separado
        txtUsuario.setText(correo);
        mHandler.post(actualizarFechaRunnable);

        txtSensores = findViewById(R.id.txtSensores);
        txtUsuarios = findViewById(R.id.txtUsuarios);
        txtRegistroMedicion = findViewById(R.id.txt_registro_medicion);

        // Configurar escuchador para ir a Activity Sensores
        txtSensores.setOnClickListener(view -> {
            Intent ventanaSensores = new Intent(Dashboard.this, MonitoreoSensores.class);
            startActivity(ventanaSensores);
        });

        // Configurar escuchador para ir a Activity Usuarios
        txtUsuarios.setOnClickListener(view -> {
            Intent ventanaUsuarios = new Intent(Dashboard.this, DashboardUsuario.class);
            startActivity(ventanaUsuarios);
        });

        // Configurar escuchador para ir a Activity RegistroMedicion
        txtRegistroMedicion.setOnClickListener(view -> {
            Intent ventanaRegistroMedicion = new Intent(Dashboard.this, RegistroMedicion.class);
            startActivity(ventanaRegistroMedicion);
        });
    }

    // Runnable para actualizar la fecha cada segundo
    private final Runnable actualizarFechaRunnable = new Runnable() {
        @Override
        public void run() {
            txtFecha.setText(obtenerFechaActual());
            mHandler.postDelayed(this, 1000); // Actualiza cada segundo
        }
    };

    // Método para obtener la fecha y hora actual formateada
    public String obtenerFechaActual() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, hh:mm:ss a", new Locale("es", "ES"));
        return sdf.format(c.getTime());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(actualizarFechaRunnable); // Detener actualizaciones al salir
    }
}
