package com.example.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;  // Asegúrate de que esta importación esté presente
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Medicion extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ImageView imageView1, imageView2;
    private Button btnDeletePhoto1, btnDeletePhoto2;
    private int imageCount = 0;

    private TextView textProyectoValor;
    private Spinner spinnerInstrumento;
    private RequestQueue requestQueue;
    private String url = "http://52.71.115.13/ConsutarInstrumentoMedicion.php";

    // Variables para almacenar los IDs
    private HashMap<Integer, String> instrumentoMap = new HashMap<>();
    private String proyectoId;  // Almacena el ID del proyecto recibido
    private String proyecto_nombre;  // Almacena el ID del proyecto recibido

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicion);

        // Obtener el nombre y el ID del proyecto desde el Intent
        Intent intent = getIntent();
        String proyecto_nombre = intent.getStringExtra("proyecto_nombre");
        proyectoId = intent.getStringExtra("proyecto_id");  // ID del proyecto

        // Mostrar el nombre del proyecto en el TextView
        textProyectoValor = findViewById(R.id.textProyectoValor);
        textProyectoValor.setText(proyecto_nombre);

        // Configurar el Spinner de Instrumento y cargar los datos
        spinnerInstrumento = findViewById(R.id.spinnerInstrumento);
        requestQueue = Volley.newRequestQueue(this);
        loadInstrumentData();

        Button btnTomarFoto = findViewById(R.id.btnTomarFoto);
        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);
        btnDeletePhoto1 = findViewById(R.id.btnDeletePhoto1);
        btnDeletePhoto2 = findViewById(R.id.btnDeletePhoto2);

        // Ocultar los botones de eliminar imagen al inicio
        btnDeletePhoto1.setVisibility(View.GONE);
        btnDeletePhoto2.setVisibility(View.GONE);

        // Inicializar el mapa
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapContainer);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Configurar el botón para tomar fotos
        btnTomarFoto.setOnClickListener(v -> dispatchTakePictureIntent());

        // Configurar los botones para eliminar fotos
        btnDeletePhoto1.setOnClickListener(v -> {
            imageView1.setImageBitmap(null);
            btnDeletePhoto1.setVisibility(View.GONE);
            imageCount = 0;
        });

        btnDeletePhoto2.setOnClickListener(v -> {
            imageView2.setImageBitmap(null);
            btnDeletePhoto2.setVisibility(View.GONE);
            imageCount = 1;
        });
    }

    // Método para capturar fotos
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");

            // Configuración de las imágenes en los ImageView correspondientes
            if (imageCount == 0) {
                imageView1.setImageBitmap(imageBitmap);
                btnDeletePhoto1.setVisibility(View.VISIBLE);
                imageCount = 1;
            } else if (imageCount == 1) {
                imageView2.setImageBitmap(imageBitmap);
                btnDeletePhoto2.setVisibility(View.VISIBLE);
                imageCount = 0;
            }
        }
    }

    // Método para configurar el mapa
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Configuración de controles del mapa
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Listener para cambiar el marcador en el mapa
        mMap.setOnMapClickListener(latLng -> setMarkerAtLocation(latLng));
        enableUserLocation();
    }

    private void setMarkerAtLocation(LatLng latLng) {
        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación actual"));
            mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(10)
                    .strokeColor(0x220000FF)
                    .fillColor(0x220000FF));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19f));
        }
    }

    // Habilitar la ubicación del usuario
    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    setMarkerAtLocation(currentLocation);
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            }
        }
    }

    private void loadInstrumentData() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        ArrayList<String> instrumentos = new ArrayList<>();
                        instrumentos.add("Seleccione un instrumento");
                        instrumentoMap.put(0, null);

                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject instrumento = response.getJSONObject(i);
                                String id = instrumento.getString("id");
                                String descripcion = instrumento.getString("nombre_instrumento");  // Usa "nombre_instrumento" en lugar de "marca" y "modelo"

                                instrumentos.add(descripcion);
                                instrumentoMap.put(i + 1, id);
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(Medicion.this, android.R.layout.simple_spinner_item, instrumentos);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerInstrumento.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(Medicion.this, "Error al procesar datos JSON", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Medicion.this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        requestQueue.add(jsonArrayRequest);
    }



    // Obtener el ID del instrumento seleccionado
    private String getSelectedInstrumentId() {
        int position = spinnerInstrumento.getSelectedItemPosition();
        return instrumentoMap.get(position);
    }

    // Método para guardar la medición con proyectoId e instrumentoId (pendiente de implementación)
    private void saveMedicion() {
        String instrumentoId = getSelectedInstrumentId();

        // Comentario: Utilizar proyectoId e instrumentoId para guardar la medición en la base de datos
        if (proyectoId != null && instrumentoId != null) {
            // TODO: Implementar guardado en la base de datos usando proyectoId e instrumentoId
            Toast.makeText(this, "Guardando con Proyecto ID: " + proyectoId + ", Instrumento ID: " + instrumentoId, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Seleccione un instrumento", Toast.LENGTH_SHORT).show();
        }
    }
}
