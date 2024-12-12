package com.example.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Dashboard extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    TextView txtFecha, txtUsuario;
    private LinearLayout containerUsuarios, containerSensores, containerRegistroMedicion, containerSensores2;
    private final Handler mHandler = new Handler();
    private LinearLayout containerNavBarInicio, containerNavBarUser, containerNavBarSensor;
    private ImageView navBarGit, imglogo;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener el ID y username desde el Intent
        Intent intent = getIntent();
        int id = intent.getIntExtra("id", -1); // Valor predeterminado si no se recibe
        String username = intent.getStringExtra("username");

        // Registrar en Logcat para verificar los datos recibidos
        Log.d(TAG, "ID recibido: " + id);
        Log.d(TAG, "Username recibido: " + username);

        // Asignar los componentes gráficos
        txtFecha = findViewById(R.id.txt_fecha);
        txtUsuario = findViewById(R.id.txt_usuario);
        imglogo = findViewById(R.id.imglogo);

        navBarGit = findViewById(R.id.navBarGit);
        containerNavBarSensor = findViewById(R.id.containerNavBarSensor);
        containerNavBarInicio = findViewById(R.id.containerNavBarInicio);
        containerNavBarUser = findViewById(R.id.containerNavBarUser);
        containerSensores2 = findViewById(R.id.containerSensores2);

        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        imglogo.startAnimation(rotateAnimation);

        // Funcionalidad del NavBar
        containerNavBarInicio.setOnClickListener(view -> {
            Intent intent2 = new Intent(Dashboard.this, Dashboard.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent2);
        });

        containerNavBarUser.setOnClickListener(view -> {
            Intent intent2 = new Intent(Dashboard.this, DashboardUsuario.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent2);
        });

        containerNavBarSensor.setOnClickListener(view -> {
            Intent intent2 = new Intent(Dashboard.this, MonitoreoSensores.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent2);
        });
        containerSensores2.setOnClickListener(view -> {
            Intent ventanaSensores2 = new Intent(Dashboard.this, CatalogoMedicionesSensores.class);
            startActivity(ventanaSensores2);
        });


        navBarGit.setOnClickListener(view -> {
            String url = "https://github.com/Anaguirv/appMovil";
            Intent intent2 = new Intent(Intent.ACTION_VIEW);
            intent2.setData(Uri.parse(url));
            startActivity(intent2);
        });

        // Configurar el texto para mostrar el username recibido
        if (username != null) {
            txtUsuario.setText(username);
        } else {
            txtUsuario.setText("Usuario desconocido");
        }

        mHandler.post(actualizarFechaRunnable);

        containerSensores = findViewById(R.id.containerSensores);
        containerSensores2 = findViewById(R.id.containerSensores2);
        containerUsuarios = findViewById(R.id.containerUsuarios);
        containerRegistroMedicion = findViewById(R.id.containerRegistroMedicion);

        // Configurar escuchadores para los contenedores
        containerSensores.setOnClickListener(view -> {
            Intent ventanaSensores = new Intent(Dashboard.this, MonitoreoSensores.class);
            startActivity(ventanaSensores);
        });
        containerSensores2.setOnClickListener(view -> {
            Intent ventanaSensores = new Intent(Dashboard.this, CatalogoMedicionesSensores.class);
            startActivity(ventanaSensores);
        });

        containerUsuarios.setOnClickListener(view -> {
            Intent ventanaUsuarios = new Intent(Dashboard.this, DashboardUsuario.class);
            startActivity(ventanaUsuarios);
        });

        containerRegistroMedicion.setOnClickListener(view -> {
            Intent ventanaCatalogoFiscalizaciones = new Intent(Dashboard.this, CatalogoFiscalizaciones.class);
            startActivity(ventanaCatalogoFiscalizaciones);
        });
    }

    // Runnable para actualizar la fecha cada segundo
    private final Runnable actualizarFechaRunnable = new Runnable() {
        @Override
        public void run() {
            txtFecha.setText(obtenerFechaActual());
            mHandler.postDelayed(this, 1000);
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
        mHandler.removeCallbacks(actualizarFechaRunnable);
    }
}
