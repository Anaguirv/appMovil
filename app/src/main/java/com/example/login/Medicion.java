package com.example.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.android.volley.toolbox.StringRequest;
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
import java.util.Map;

public class Medicion extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private TextView textProyectoValor;
    private EditText editTextTemperatura, editTextHumedad, editTextValorMedido, editTextObservacion;
    private Spinner spinnerInstrumento;
    private ImageView imageView1, imageView2;
    private Button btnDeletePhoto1, btnDeletePhoto2;

    private RequestQueue requestQueue;
    private String urlInstrumentos = "http://52.71.115.13/ConsutarInstrumentoMedicion.php";
    private String urlGuardarMedicion = "http://52.71.115.13/GuardarMedicion.php";

    private HashMap<Integer, String> instrumentoMap = new HashMap<>();
    private String proyectoId;
    private LatLng selectedLocation;

    private int imageCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicion);

        Intent intent = getIntent();
        proyectoId = intent.getStringExtra("proyecto_id");
        String proyectoNombre = intent.getStringExtra("proyecto_nombre");

        textProyectoValor = findViewById(R.id.textProyectoValor);
        textProyectoValor.setText(proyectoNombre);

        editTextTemperatura = findViewById(R.id.editTextTemperatura);
        editTextHumedad = findViewById(R.id.editTextHumedad);
        editTextValorMedido = findViewById(R.id.medicion_input);
        editTextObservacion = findViewById(R.id.editTextObservaciones);
        spinnerInstrumento = findViewById(R.id.spinnerInstrumento);
        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);
        btnDeletePhoto1 = findViewById(R.id.btnDeletePhoto1);
        btnDeletePhoto2 = findViewById(R.id.btnDeletePhoto2);

        requestQueue = Volley.newRequestQueue(this);
        loadInstrumentData();

        Button btnGuardarMedicion = findViewById(R.id.btnguardarmedicion);
        btnGuardarMedicion.setOnClickListener(v -> guardarMedicion());

        Button btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnTomarFoto.setOnClickListener(v -> dispatchTakePictureIntent());

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapContainer);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void loadInstrumentData() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, urlInstrumentos, null,
                response -> {
                    ArrayList<String> instrumentos = new ArrayList<>();
                    instrumentos.add("Seleccione un instrumento");
                    instrumentoMap.put(0, null);

                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject instrumento = response.getJSONObject(i);
                            String id = instrumento.getString("id");
                            String descripcion = instrumento.getString("nombre_instrumento");
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
                }, error -> Toast.makeText(Medicion.this, "Error de conexión", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonArrayRequest);
    }

    private void guardarMedicion() {
        String instrumentoId = getSelectedInstrumentId();
        String temperatura = editTextTemperatura.getText().toString();
        String humedad = editTextHumedad.getText().toString();
        String valorMedido = editTextValorMedido.getText().toString();
        String observacion = editTextObservacion.getText().toString();

        if (selectedLocation == null) {
            Toast.makeText(this, "Seleccione una ubicación en el mapa", Toast.LENGTH_SHORT).show();
            return;
        }

        String latitud = String.valueOf(selectedLocation.latitude);
        String longitud = String.valueOf(selectedLocation.longitude);

        if (instrumentoId == null || proyectoId == null) {
            Toast.makeText(this, "Complete todos los campos y seleccione ubicación", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlGuardarMedicion,
                response -> {
                    Toast.makeText(Medicion.this, "Medición guardada exitosamente", Toast.LENGTH_SHORT).show();
                    // Redirigir a RegistroMedicion después de guardar con éxito
                    Intent intent = new Intent(Medicion.this, RegistroMedicion.class);
                    startActivity(intent);
                    finish();  // Cierra la actividad actual para evitar volver atrás
                },
                error -> Toast.makeText(Medicion.this, "Error al guardar medición", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("latitud", latitud);
                params.put("longitud", longitud);
                params.put("temperatura", temperatura);
                params.put("humedad", humedad);
                params.put("valor_medido", valorMedido);
                params.put("observacion", observacion);
                params.put("inspector_id", "1");
                params.put("instrumento_medicion_id", instrumentoId);
                params.put("proyecto_id", proyectoId);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }


    private String getSelectedInstrumentId() {
        int position = spinnerInstrumento.getSelectedItemPosition();
        return instrumentoMap.get(position);
    }

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        mMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            setMarkerAtLocation(latLng);
        });

        enableUserLocation();
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    selectedLocation = currentLocation;
                    setMarkerAtLocation(currentLocation);
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void setMarkerAtLocation(LatLng latLng) {
        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación seleccionada"));
            mMap.addCircle(new CircleOptions().center(latLng).radius(10).strokeColor(0x220000FF).fillColor(0x220000FF));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19f));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            }
        }
    }
}
