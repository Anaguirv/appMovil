package com.example.login;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MonitoreoSensores extends AppCompatActivity {

    TextView fechahora, temp, hum;
    ImageView imagentemp, imagenluz;
    RequestQueue datos;
    Handler mHandler = new Handler();
    private int estadoLuz = 0; // Estado inicial: 0 (apagado)

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_monitoreo_sensores);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fechahora = findViewById(R.id.txt_fecha);
        temp = findViewById(R.id.txt_temp);
        hum = findViewById(R.id.txt_humedad);
        imagentemp = findViewById(R.id.imagen_temp);
        imagenluz = findViewById(R.id.imagen_luz);

        fechahora.setText(fechahora());
        datos = Volley.newRequestQueue(this);

        // Iniciar la actualización periódica de datos
        mHandler.post(Refrescar);

        // Configurar el cambio de imagen con un toque en `imagenluz`
        imagenluz.setOnClickListener(view -> {
            estadoLuz = (estadoLuz == 0) ? 1 : 0;  // Alternar estado
            CambiarImagenLuz(estadoLuz); // Actualizar la imagen según el nuevo estado
        });
    }

    public String fechahora() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, hh:mm:ss a", new Locale("es", "ES"));
        return sdf.format(c.getTime());
    }

    public void ObtenerDatos() {
        String url = "https://www.pnk.cl/muestra_datos.php";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                null, response -> {
            try {
                // Obtener temperatura y humedad
                temp.setText(response.getString("temperatura") + " C");
                hum.setText(response.getString("humedad") + " %");

                // Cambiar imagen de temperatura
                Float val = Float.parseFloat(response.getString("temperatura"));
                CambiarImagenTemp(val);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Throwable::printStackTrace);

        datos.add(request);
    }

    // Método para cambiar la imagen de temperatura
    public void CambiarImagenTemp(Float valor) {
        if (valor >= 25) {
            imagentemp.setImageResource(R.drawable.tempalta);
        } else {
            imagentemp.setImageResource(R.drawable.tempbaja);
        }
    }

    // Método para cambiar la imagen de luz según el estado
    public void CambiarImagenLuz(int estado) {
        if (estado == 0) {
            imagenluz.setImageResource(R.drawable.luzoff);  // Imagen para luz apagada
        } else {
            imagenluz.setImageResource(R.drawable.luzon);   // Imagen para luz encendida
        }
    }

    public Runnable Refrescar = new Runnable() {
        @Override
        public void run() {
            fechahora.setText(fechahora());
            ObtenerDatos();
            mHandler.postDelayed(this, 1000);  // Refrescar cada segundo
        }
    };
}
