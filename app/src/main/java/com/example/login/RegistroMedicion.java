package com.example.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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

    private TextView textViewNombreProyecto, textViewTipoAlumbrado, textViewRepresentanteLegal, textViewDescripcion;
    private Button buttonTomarFoto, buttonEliminarFoto, buttonAgregarMedicion;
    private ImageView imageViewFoto;

    private String fiscalizacionId; // ID de la fiscalización
    private String proyectoId; // ID del proyecto asociado
    private String fotoRuta; // Ruta de la foto almacenada
    private double latitud, longitud; // Coordenadas del proyecto
    private GoogleMap googleMap; // Referencia al mapa
    private Bitmap fotoCapturada; // Foto tomada

    private static final int REQUEST_IMAGE_CAPTURE = 1; // Código para la cámara
    private static final String URL_BASE = "http://98.83.4.206:8080/";
    private static final String URL_PROYECTO = URL_BASE + "api_modelo_proyecto";
    private static final String URL_GUARDAR_FOTO = URL_BASE + "guardar_foto_proyecto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_medicion);

        // Vincular elementos del XML
        textViewNombreProyecto = findViewById(R.id.textViewNombreProyecto);
        textViewTipoAlumbrado = findViewById(R.id.textViewTipoAlumbrado);
        textViewRepresentanteLegal = findViewById(R.id.textViewRepresentanteLegal);
        textViewDescripcion = findViewById(R.id.textViewDescripcion);
        buttonTomarFoto = findViewById(R.id.buttonTomarFoto);
        buttonEliminarFoto = findViewById(R.id.buttonEliminarFoto);
        buttonAgregarMedicion = findViewById(R.id.buttonAgregarMedicion);
        imageViewFoto = findViewById(R.id.imageViewFoto);

        // Botón "Eliminar Foto" oculto al inicio
        buttonEliminarFoto.setVisibility(View.GONE);

        // Obtener el ID de fiscalización y proyecto desde el Intent
        fiscalizacionId = getIntent().getStringExtra("fiscalizacion_id");
        proyectoId = getIntent().getStringExtra("proyecto_id");

        if (fiscalizacionId == null || proyectoId == null) {
            Toast.makeText(this, "Faltan datos necesarios para continuar.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Inicializar el mapa
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapContainer);
        mapFragment.getMapAsync(this);

        // Configurar acciones para los botones
        configurarBotones();

        // Cargar detalles del proyecto
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
        // La carga de la ubicación se hace después de obtener los detalles del proyecto
    }

    private void abrirMedicion() {
        Intent intent = new Intent(RegistroMedicion.this, Medicion.class);
        intent.putExtra("fiscalizacion_id", fiscalizacionId);
        intent.putExtra("proyecto_id", proyectoId);
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

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                fotoCapturada = (Bitmap) data.getExtras().get("data");
                imageViewFoto.setImageBitmap(fotoCapturada);
                buttonEliminarFoto.setVisibility(View.VISIBLE); // Mostrar botón "Eliminar Foto"
                buttonTomarFoto.setText("Guardar Foto"); // Cambiar texto del botón
                Log.d("RegistroMedicion", "Foto capturada y mostrada correctamente.");
            }
        }
    }

    private void guardarFoto() {
        if (fotoCapturada == null) {
            Toast.makeText(this, "Debe tomar una foto antes de guardar.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar el ID del proyecto
        if (proyectoId == null || proyectoId.isEmpty()) {
            Log.e("RegistroMedicion", "Error: proyectoId está vacío o nulo.");
            Toast.makeText(this, "Error: No se ha recibido el ID del proyecto.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convertir la foto capturada en bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fotoCapturada.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] fotoBytes = baos.toByteArray();

        // Crear la solicitud multipart para enviar la foto como archivo
        VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, URL_GUARDAR_FOTO,
                response -> {
                    try {
                        String responseString = new String(response.data);
                        Log.d("RegistroMedicion", "Respuesta del servidor: " + responseString);
                        JSONObject jsonResponse = new JSONObject(responseString);
                        fotoRuta = jsonResponse.getString("foto_url");
                        cargarFotoDesdeRuta(fotoRuta); // Cargar la foto guardada
                        Toast.makeText(this, "Foto guardada correctamente.", Toast.LENGTH_SHORT).show();
                        buttonTomarFoto.setText("Tomar Foto");
                    } catch (JSONException e) {
                        Log.e("RegistroMedicion", "Error procesando respuesta: " + e.getMessage());
                        Toast.makeText(this, "Error al procesar respuesta del servidor.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("RegistroMedicion", "Error al guardar la foto: " + (error.networkResponse != null
                            ? new String(error.networkResponse.data)
                            : "Error desconocido"));
                    Toast.makeText(this, "Error al guardar la foto.", Toast.LENGTH_SHORT).show();
                }) {
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

        // Agregar la solicitud a la cola de Volley
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void eliminarFoto() {
        imageViewFoto.setImageResource(0);
        buttonEliminarFoto.setVisibility(View.GONE);
        buttonTomarFoto.setText("Tomar Foto");
        fotoCapturada = null;
    }

    private void cargarDetallesProyecto() {
        String urlProyectoConId = URL_PROYECTO; // No es necesario agregar el ID en la URL
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlProyectoConId, null,
                response -> {
                    try {
                        // Obtener el arreglo de proyectos
                        JSONArray proyectosArray = response.getJSONArray("proyectos");

                        // Recorrer los proyectos para encontrar el correspondiente al proyectoId
                        for (int i = 0; i < proyectosArray.length(); i++) {
                            JSONObject proyecto = proyectosArray.getJSONObject(i);
                            if (proyecto.getString("id").equals(proyectoId)) {
                                // Cargar detalles del proyecto
                                textViewNombreProyecto.setText("Nombre: " + proyecto.getString("nombre"));
                                textViewTipoAlumbrado.setText("Tipo Alumbrado: " + proyecto.getString("tipo_alumbrado"));
                                textViewRepresentanteLegal.setText("Representante: " + proyecto.getJSONObject("representante_legal").getString("nombre"));
                                textViewDescripcion.setText("Descripción: " + proyecto.getString("descripcion"));

                                // Cargar la foto si existe
                                fotoRuta = proyecto.optString("foto", null);
                                cargarFotoDesdeRuta(fotoRuta);

                                // Coordenadas del proyecto
                                latitud = proyecto.getDouble("latitud");
                                longitud = proyecto.getDouble("longitud");
                                mostrarUbicacionEnMapa();

                                break; // Salir del bucle, ya que encontramos el proyecto
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("RegistroMedicion", "Error al procesar respuesta del proyecto: " + e.getMessage());
                    }
                },
                error -> Log.e("RegistroMedicion", "Error al cargar detalles del proyecto: " + error.getMessage()));

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }


    private void cargarFotoDesdeRuta(String rutaRelativa) {
        if (rutaRelativa != null && !rutaRelativa.isEmpty()) {
            String urlCompleta = URL_BASE + rutaRelativa.replaceFirst("^/", ""); // Asegurar que no haya doble "/"
            Log.d("RegistroMedicion", "Cargando foto desde URL: " + urlCompleta);
            Glide.with(this)
                    .load(urlCompleta)
                    .error(R.drawable.ic_error) // Imagen de error en caso de fallo
                    .into(imageViewFoto);
            buttonEliminarFoto.setVisibility(View.VISIBLE); // Mostrar botón "Eliminar Foto"
        } else {
            Log.d("RegistroMedicion", "No se proporcionó una ruta válida para la foto.");
            eliminarFoto(); // Limpiar la vista si no hay foto
        }
    }


    private void mostrarUbicacionEnMapa() {
        if (googleMap != null) {
            LatLng ubicacionProyecto = new LatLng(latitud, longitud);
            googleMap.addMarker(new MarkerOptions().position(ubicacionProyecto).title("Ubicación del Proyecto"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionProyecto, 15));
        }
    }
}
