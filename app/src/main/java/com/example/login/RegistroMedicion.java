package com.example.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class RegistroMedicion extends AppCompatActivity implements OnMapReadyCallback {

    // Componentes de la interfaz
    private TextView textViewNombreProyecto, textViewTipoAlumbrado, textViewRepresentanteLegal, textViewTitular, textViewDescripcion, textViewDetallesLuminarias;
    private Button buttonTomarFoto, buttonEliminarFoto, buttonAgregarMedicion;
    private ImageView imageViewFoto;

    // Variables de lógica de negocio
    private String fiscalizacionId;
    private String proyectoId;
    private String fotoRuta;
    private double latitud, longitud;
    private GoogleMap googleMap;
    private Bitmap fotoCapturada;

    // Constantes
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String URL_BASE = "http://98.83.4.206/";
    private static final String URL_PROYECTO = URL_BASE + "Api_Proyectos";
    private static final String URL_GUARDAR_FOTO = URL_BASE + "Guardar_foto_proyecto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_medicion);

        // Vinculación de los componentes del XML con los atributos del código
        textViewNombreProyecto = findViewById(R.id.textViewNombreProyecto);
        textViewTipoAlumbrado = findViewById(R.id.textViewTipoAlumbrado);
        textViewRepresentanteLegal = findViewById(R.id.textViewRepresentanteLegal);
        textViewTitular = findViewById(R.id.textViewTitular);
        textViewDetallesLuminarias = findViewById(R.id.textViewDetallesLuminarias);
        textViewDescripcion = findViewById(R.id.textViewDescripcion);
        buttonTomarFoto = findViewById(R.id.buttonTomarFoto);
        buttonEliminarFoto = findViewById(R.id.buttonEliminarFoto);
        buttonAgregarMedicion = findViewById(R.id.buttonAgregarMedicion);
        imageViewFoto = findViewById(R.id.imageViewFoto);

        // Ocultar el botón "Eliminar Foto" inicialmente
        buttonEliminarFoto.setVisibility(View.GONE);

        // Obtener IDs necesarios desde el Intent
        fiscalizacionId = getIntent().getStringExtra("fiscalizacion_id");
        proyectoId = getIntent().getStringExtra("proyecto_id");

        if (fiscalizacionId == null || proyectoId == null) {
            Toast.makeText(this, "Faltan datos necesarios para continuar.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Inicializar el mapa
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapContainer);
        mapFragment.getMapAsync(this);

        // Configurar acciones de los botones
        configurarBotones();

        // Cargar detalles del proyecto desde la API
        cargarDetallesProyecto();
    }

    private void configurarBotones() {
        buttonTomarFoto.setOnClickListener(v -> {
            if ("Tomar Foto".equals(buttonTomarFoto.getText().toString())) {
                tomarFoto();
            } else if ("Guardar Foto".equals(buttonTomarFoto.getText().toString())) {
                guardarFoto();
            }
        });

        buttonEliminarFoto.setOnClickListener(v -> eliminarFoto());

        buttonAgregarMedicion.setOnClickListener(v -> abrirMedicion());
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
    }

    private void abrirMedicion() {
        Intent intent = new Intent(RegistroMedicion.this, Medicion.class);
        intent.putExtra("fiscalizacion_id", fiscalizacionId);
        intent.putExtra("proyecto_id", proyectoId);
        intent.putExtra("proyecto_nombre", textViewNombreProyecto.getText().toString()); // Enviar el nombre del proyecto
        startActivity(intent);
    }


    private void tomarFoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No se puede acceder a la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null && data.getExtras() != null) {
            fotoCapturada = (Bitmap) data.getExtras().get("data");
            imageViewFoto.setImageBitmap(fotoCapturada);
            buttonEliminarFoto.setVisibility(View.VISIBLE);
            buttonTomarFoto.setText("Guardar Foto");
        }
    }

    private void guardarFoto() {
        if (fotoCapturada == null) {
            Toast.makeText(this, "Debe tomar una foto antes de guardar.", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fotoCapturada.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] fotoBytes = baos.toByteArray();

        VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, URL_GUARDAR_FOTO,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(new String(response.data));
                        fotoRuta = jsonResponse.getString("foto_url");
                        cargarFotoDesdeRuta(fotoRuta);
                        Toast.makeText(this, "Foto guardada correctamente.", Toast.LENGTH_SHORT).show();
                        buttonTomarFoto.setText("Tomar Foto");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error al guardar la foto.", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("proyecto_id", proyectoId);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("foto", new DataPart("proyecto_" + proyectoId + ".jpg", fotoBytes, "image/jpeg"));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void eliminarFoto() {
        imageViewFoto.setImageResource(0);
        buttonEliminarFoto.setVisibility(View.GONE);
        buttonTomarFoto.setText("Tomar Foto");
        fotoCapturada = null;
    }

    private void cargarDetallesProyecto() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL_PROYECTO, null,
                response -> {
                    try {
                        JSONArray proyectosArray = response.getJSONArray("proyectos");
                        for (int i = 0; i < proyectosArray.length(); i++) {
                            JSONObject proyecto = proyectosArray.getJSONObject(i);
                            if (proyecto.getString("id").equals(proyectoId)) {
                                textViewNombreProyecto.setText("" + proyecto.getString("nombre"));

                                String tipoAlumbrado = convertirTipoAlumbrado(proyecto.getString("tipo_alumbrado"));
                                textViewTipoAlumbrado.setText("" + tipoAlumbrado);

                                JSONObject titular = proyecto.getJSONObject("titular");
                                textViewTitular.setText("" + titular.getString("nombre") + " " +
                                        titular.getString("a_paterno") + " " + titular.getString("a_materno"));

                                JSONObject representante = proyecto.getJSONObject("representante_legal");
                                textViewRepresentanteLegal.setText("" + representante.getString("nombre") + " " +
                                        representante.getString("a_paterno") + " " + representante.getString("a_materno"));

                                JSONObject detalleLuminarias = proyecto.getJSONObject("detalle_luminarias");
                                textViewDetallesLuminarias.setText("" +
                                        "CANTIDAD: " + detalleLuminarias.getInt("cantidad") + "\n" +
                                        "TIPO: " + detalleLuminarias.getString("tipo_lampara") + "\n" +
                                        "MARCA: " + detalleLuminarias.getString("marca") + "\n" +
                                        "MODELO: " + detalleLuminarias.getString("modelo"));

                                textViewDescripcion.setText("" + proyecto.getString("descripcion"));

                                fotoRuta = proyecto.optString("foto", null);
                                cargarFotoDesdeRuta(fotoRuta);

                                latitud = proyecto.getDouble("latitud");
                                longitud = proyecto.getDouble("longitud");
                                mostrarUbicacionEnMapa();
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error al cargar detalles del proyecto.", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(request);
    }

    private void cargarFotoDesdeRuta(String rutaRelativa) {
        if (rutaRelativa != null && !rutaRelativa.isEmpty()) {
            if (!rutaRelativa.startsWith("media/")) {
                rutaRelativa = "media/" + rutaRelativa;
            }
            String urlCompleta = URL_BASE + rutaRelativa.replaceFirst("^/", "");
            Glide.with(this).load(urlCompleta).error(R.drawable.ic_error).into(imageViewFoto);
            buttonEliminarFoto.setVisibility(View.VISIBLE);
        } else {
            eliminarFoto();
        }
    }

    private void mostrarUbicacionEnMapa() {
        if (googleMap != null) {
            LatLng ubicacionProyecto = new LatLng(latitud, longitud);
            googleMap.addMarker(new MarkerOptions().position(ubicacionProyecto).title("Ubicación del Proyecto"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionProyecto, 15));
        }
    }

    private String convertirTipoAlumbrado(String tipo) {
        switch (tipo) {
            case "v":
                return "Vehicular";
            case "p":
                return "Peatonal";
            case "i":
                return "Industrial";
            case "o":
                return "Ornamental";
            default:
                return "Desconocido";
        }
    }
}
