package com.example.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private LinearLayout containerUsuarios, containerSensores, containerRegistroMedicion;
    private final Handler mHandler = new Handler();
    // navBar start
    private LinearLayout containerNavBarInicio, containerNavBarUser, containerNavBarSensor;
    private ImageView navBarGit, imglogo;
    // navBar end



    @SuppressLint("MissingInflatedId")
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
        imglogo = findViewById(R.id.imglogo);

        //navBar
        navBarGit = findViewById(R.id.navBarGit);
        containerNavBarSensor = findViewById(R.id.containerNavBarSensor);
        containerNavBarInicio = findViewById(R.id.containerNavBarInicio);
        containerNavBarUser = findViewById(R.id.containerNavBarUser);
        //navBar end

        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        imglogo.startAnimation(rotateAnimation);

        //navBar Funcionalidad
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

        navBarGit.setOnClickListener(view -> {
            String url = "https://github.com/Anaguirv/appMovil";
            Intent intent2 = new Intent(Intent.ACTION_VIEW);
            intent2.setData(Uri.parse(url));
            startActivity(intent2);
        });
        // navBar END

        // Configurar el texto para mostrar la fecha y el correo por separado
        txtUsuario.setText(correo);
        mHandler.post(actualizarFechaRunnable);

        containerSensores = findViewById(R.id.containerSensores);
        containerUsuarios = findViewById(R.id.containerUsuarios);
        containerRegistroMedicion = findViewById(R.id.containerRegistroMedicion);


        // Configurar escuchador para ir a Activity Sensores
        containerSensores.setOnClickListener(view -> {
            Intent ventanaSensores = new Intent(Dashboard.this, MonitoreoSensores.class);
            startActivity(ventanaSensores);
        });

        // Configurar escuchador para ir a Activity Usuarios
        containerUsuarios.setOnClickListener(view -> {
            Intent ventanaUsuarios = new Intent(Dashboard.this, DashboardUsuario.class);
            startActivity(ventanaUsuarios);
        });

        // Configurar escuchador para ir a Activity RegistroMedicion
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
