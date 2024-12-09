package com.example.login;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.ProgressBar;
import android.app.ProgressDialog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.net.Uri;

import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
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
    private Button buttonTomarFoto, buttonEliminarFoto, buttonAgregarMedicion,buttonVerMediciones;
    private ImageView imageViewFoto;

    // Variables de lógica de negocio
    private String fiscalizacionId;
    private String proyectoId;
    private String fotoRuta;
    private double latitud, longitud;
    private GoogleMap googleMap;
    private Bitmap fotoCapturada;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri imageUri;
    private Bitmap capturedImage;
    private ProgressDialog progressDialog;


    // Constantes
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
        buttonVerMediciones = findViewById(R.id.buttonVerMediciones);

        imageViewFoto = findViewById(R.id.imageViewFoto);

        // Ocultar el botón "Eliminar Foto" inicialmente
        buttonEliminarFoto.setVisibility(View.GONE);

        // Obtener IDs necesarios desde el Intent
        fiscalizacionId = getIntent().getStringExtra("fiscalizacion_id");
        proyectoId = getIntent().getStringExtra("proyecto_id");

        // Registrar el valor recibido en Logcat
        Log.d("RegistroMedicion", "proyectoId recibido: " + proyectoId);

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

        // Configurar botón Ver Mediciones
        buttonVerMediciones.setOnClickListener(v -> abrirMedicionesProyecto());
    }


    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
    }
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 100);
        }
    }


    private void abrirMedicion() {
        Intent intent = new Intent(RegistroMedicion.this, Medicion.class);
        intent.putExtra("fiscalizacion_id", fiscalizacionId);
        intent.putExtra("proyecto_id", proyectoId);
        intent.putExtra("proyecto_nombre", textViewNombreProyecto.getText().toString());
        startActivity(intent);
    }


    private void abrirMedicionesProyecto() {
        if (proyectoId != null && !proyectoId.isEmpty()) {
            try {
                int idProyecto = Integer.parseInt(proyectoId);
                String nombreProyecto = textViewNombreProyecto.getText().toString();

                // Verifica si los datos se están configurando correctamente
                Log.d("RegistroMedicion", "Enviando ID del Proyecto: " + idProyecto);
                Log.d("RegistroMedicion", "Enviando Nombre del Proyecto: " + nombreProyecto);

                Intent intent = new Intent(RegistroMedicion.this, CatalogoMedicionesProyecto.class);
                intent.putExtra("proyectoId", String.valueOf(idProyecto));  // Envía como String
                intent.putExtra("proyecto_nombre", nombreProyecto);  // Envía el nombre del proyecto
                startActivity(intent);

            } catch (NumberFormatException e) {
                Log.e("RegistroMedicion", "Error: No se pudo convertir proyectoId a entero.");
                Toast.makeText(this, "ID de proyecto inválido.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("RegistroMedicion", "Error: proyectoId es nulo o vacío.");
            Toast.makeText(this, "ID de proyecto no disponible.", Toast.LENGTH_SHORT).show();
        }
    }





    private void tomarFoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = File.createTempFile(
                        "captured_image",  // Nombre del archivo
                        ".jpg",            // Extensión
                        getCacheDir()      // Directorio temporal
                );

                // Obtener URI del archivo temporal
                imageUri = FileProvider.getUriForFile(
                        this, getApplicationContext().getPackageName() + ".provider", photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                fotoCapturada = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageViewFoto.setImageBitmap(fotoCapturada);
                buttonEliminarFoto.setVisibility(View.VISIBLE);
                buttonTomarFoto.setText("Guardar Foto");
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No se tomó ninguna foto", Toast.LENGTH_SHORT).show();
        }
    }




    private void guardarFoto() {
        if (fotoCapturada == null) {
            Toast.makeText(this, "Debe tomar una foto antes de guardar.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Muestra el ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Guardando foto...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fotoCapturada.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] fotoBytes = baos.toByteArray();

        VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, URL_GUARDAR_FOTO,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(new String(response.data));
                        fotoRuta = jsonResponse.getString("foto_url");

                        // Ocultar el ProgressDialog
                        if (progressDialog.isShowing()) progressDialog.dismiss();

                        Toast.makeText(this, "Foto guardada correctamente.", Toast.LENGTH_SHORT).show();
                        buttonTomarFoto.setText("Tomar Foto");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                    }
                },
                error -> {
                    if (progressDialog.isShowing()) progressDialog.dismiss();
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

        VolleySingleton.getInstance(this).addToRequestQueue(request);
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
                                textViewNombreProyecto.setText(proyecto.getString("nombre"));

                                String tipoAlumbrado = convertirTipoAlumbrado(proyecto.getString("tipo_alumbrado"));
                                textViewTipoAlumbrado.setText(tipoAlumbrado);

                                JSONObject titular = proyecto.getJSONObject("titular");
                                textViewTitular.setText(titular.getString("nombre") + " " +
                                        titular.getString("a_paterno") + " " + titular.getString("a_materno"));

                                JSONObject representante = proyecto.getJSONObject("representante_legal");
                                textViewRepresentanteLegal.setText(representante.getString("nombre") + " " +
                                        representante.getString("a_paterno") + " " + representante.getString("a_materno"));

                                JSONObject detalleLuminarias = proyecto.getJSONObject("detalle_luminarias");
                                textViewDetallesLuminarias.setText("CANTIDAD: " + detalleLuminarias.getInt("cantidad") +
                                        "\nTIPO: " + detalleLuminarias.getString("tipo_lampara") +
                                        "\nMARCA: " + detalleLuminarias.getString("marca") +
                                        "\nMODELO: " + detalleLuminarias.getString("modelo"));

                                textViewDescripcion.setText(proyecto.getString("descripcion"));

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

        VolleySingleton.getInstance(this).addToRequestQueue(request);
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
