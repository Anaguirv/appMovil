package com.example.login;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class EditarMedicion extends AppCompatActivity implements OnMapReadyCallback {

    private static final String URL_BASE = "http://98.83.4.206/";
    private static final String URL_CARGAR_MEDICION = URL_BASE + "Cargar_medicion_por_id/";

    // Componentes de la interfaz
    private TextView textProyectoValor;
    private Spinner spinnerTipo, spinnerInstrumento;
    private EditText editTextTemperatura, editTextHumedad, medicion_input, editTextObservaciones;
    private Button btnGuardarMedicion, btnEliminarMedicion, btnCambiarFoto;
    private ImageView imageViewFoto;

    private GoogleMap googleMap;
    private int medicionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_medicion);

        configurarVentana();
        inicializarVistas();

        // Obtener el ID de la medición desde el Intent
        medicionId = getIntent().getIntExtra("medicionId", -1);
        if (medicionId != -1) {
            cargarMedicion(medicionId);
        } else {
            mostrarError("ID de medición no válido");
            finish();
        }

        // Inicializar el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapContainer);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void configurarVentana() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.editarMedicion), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void inicializarVistas() {
        spinnerTipo = findViewById(R.id.spinnerTipo);
        spinnerInstrumento = findViewById(R.id.spinnerInstrumento);
        editTextTemperatura = findViewById(R.id.editTextTemperatura);
        editTextHumedad = findViewById(R.id.editTextHumedad);
        medicion_input = findViewById(R.id.medicion_input);
        editTextObservaciones = findViewById(R.id.editTextObservaciones);
        btnGuardarMedicion = findViewById(R.id.btnguardarmedicion);
        btnEliminarMedicion = findViewById(R.id.btnEliminarMedicion);
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto);
        imageViewFoto = findViewById(R.id.imageViewFoto);
    }

    private void cargarMedicion(int medicionId) {
        String url = URL_CARGAR_MEDICION + medicionId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject medicion = response.getJSONObject("medicion");

                        // Asignar valores a las vistas
                        editTextTemperatura.setText(String.valueOf(medicion.optDouble("temperatura", 0.0)));
                        editTextHumedad.setText(String.valueOf(medicion.optDouble("humedad", 0.0)));
                        medicion_input.setText(String.valueOf(medicion.optDouble("valor_medido", 0.0)));
                        editTextObservaciones.setText(medicion.optString("observacion", "Sin observación"));

                        String fotoRuta = medicion.optString("foto", "");
                        if (!fotoRuta.isEmpty()) {
                            String fotoUrl = URL_BASE + fotoRuta.replaceFirst("^/", "");
                            Glide.with(this)
                                    .load(fotoUrl)
                                    .error(R.drawable.ic_error)
                                    .into(imageViewFoto);
                        } else {
                            mostrarError("Imagen no disponible.");
                        }

                        mostrarUbicacionEnMapa(
                                medicion.optDouble("latitud", 0.0),
                                medicion.optDouble("longitud", 0.0)
                        );

                    } catch (JSONException e) {
                        mostrarError("Error al procesar los datos de la medición");
                        Log.e("EditarMedicion", "Error al procesar datos: ", e);
                    }
                },
                error -> {
                    mostrarError("Error al cargar la medición");
                    Log.e("EditarMedicion", "Error en la solicitud: " + error.getMessage());
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void mostrarUbicacionEnMapa(double latitud, double longitud) {
        if (googleMap != null && latitud != 0.0 && longitud != 0.0) {
            LatLng ubicacion = new LatLng(latitud, longitud);
            googleMap.addMarker(new MarkerOptions()
                    .position(ubicacion)
                    .title("Ubicación de la Medición"));
            googleMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(ubicacion, 19));
        } else {
            mostrarError("Ubicación no disponible.");
        }
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
    }
}
