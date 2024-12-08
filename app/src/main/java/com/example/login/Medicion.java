package com.example.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Medicion extends AppCompatActivity {

    private static final String TAG = "MedicionActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private TextView textProyectoValor;
    private EditText editTextTemperatura, editTextHumedad, editTextValorMedido, editTextObservacion;
    private Spinner spinnerInstrumento;
    private ImageView imageView1;
    private Button btnTomarFoto;

    private RequestQueue requestQueue;
    private String urlInstrumentos = "http://98.83.4.206/Api_instrumentos_medicion";
    private String urlGuardarMedicion = "http://98.83.4.206/Guardar_medicion";

    private HashMap<Integer, String> instrumentoMap = new HashMap<>();
    private String fiscalizacion_id;
    private LatLng selectedLocation;
    private int inspectorId;

    private MapHandler mapHandler;
    private Bitmap capturedImage;
    private Uri imageUri;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicion);

        Log.d(TAG, "onCreate: Iniciando componentes de la UI");

        SharedPreferences sharedPref = getSharedPreferences("InspectorPrefs", Context.MODE_PRIVATE);
        inspectorId = sharedPref.getInt("inspector_id", -1);
        Log.d(TAG, "onCreate: Inspector ID = " + inspectorId);

        Intent intent = getIntent();
        fiscalizacion_id = intent.getStringExtra("fiscalizacion_id");
        Log.d(TAG, "onCreate: Proyecto ID = " + fiscalizacion_id);

        String proyectoNombre = intent.getStringExtra("proyecto_nombre");

        textProyectoValor = findViewById(R.id.textProyectoValor);
        textProyectoValor.setText(proyectoNombre);

        editTextTemperatura = findViewById(R.id.editTextTemperatura);
        editTextHumedad = findViewById(R.id.editTextHumedad);
        editTextValorMedido = findViewById(R.id.medicion_input);
        editTextObservacion = findViewById(R.id.editTextObservaciones);
        spinnerInstrumento = findViewById(R.id.spinnerInstrumento);
        imageView1 = findViewById(R.id.imageView1);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);

        requestQueue = Volley.newRequestQueue(this);
        loadInstrumentData();

        Button btnGuardarMedicion = findViewById(R.id.btnguardarmedicion);
        btnGuardarMedicion.setOnClickListener(v -> guardarMedicion());

        btnTomarFoto.setOnClickListener(v -> captureImage());

        checkCameraPermission();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapContainer);
        mapHandler = new MapHandler(this, mapFragment);
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

    private void captureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = File.createTempFile("captured_image", ".jpg", getCacheDir());
                imageUri = FileProvider.getUriForFile(
                        this, getApplicationContext().getPackageName() + ".provider", photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al crear archivo para la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                capturedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView1.setImageBitmap(capturedImage);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No se tomó ninguna foto", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadInstrumentData() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlInstrumentos, null,
                response -> {
                    ArrayList<String> instrumentos = new ArrayList<>();
                    instrumentos.add("Seleccione un instrumento");
                    instrumentoMap.put(0, null);

                    try {
                        JSONArray instrumentosArray = response.getJSONArray("instrumentos");
                        for (int i = 0; i < instrumentosArray.length(); i++) {
                            JSONObject instrumento = instrumentosArray.getJSONObject(i);
                            int id = instrumento.getInt("id");
                            String marca = instrumento.getString("marca");
                            String modelo = instrumento.getString("modelo");
                            String numSerie = instrumento.getString("num_serie");

                            String descripcion = marca + " " + modelo + " (" + numSerie + ")";
                            instrumentos.add(descripcion);
                            instrumentoMap.put(i + 1, String.valueOf(id));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(Medicion.this, android.R.layout.simple_spinner_item, instrumentos);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerInstrumento.setAdapter(adapter);

                        Log.d(TAG, "loadInstrumentData: Datos de instrumentos cargados correctamente");

                    } catch (JSONException e) {
                        Log.e(TAG, "loadInstrumentData: Error al procesar datos JSON", e);
                        Toast.makeText(Medicion.this, "Error al procesar datos JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "loadInstrumentData: Error de conexión al cargar instrumentos", error);
                    Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(jsonObjectRequest);
    }

    private String getSelectedInstrumentId() {
        int position = spinnerInstrumento.getSelectedItemPosition();
        return instrumentoMap.get(position);
    }

    private void guardarMedicion() {
        Log.d(TAG, "Iniciando la función guardarMedicion");

        String instrumentoId = getSelectedInstrumentId();
        String temperatura = editTextTemperatura.getText().toString();
        String humedad = editTextHumedad.getText().toString();
        String valorMedido = editTextValorMedido.getText().toString();
        String observacion = editTextObservacion.getText().toString();

        selectedLocation = mapHandler.getSelectedLocation();
        if (selectedLocation == null) {
            Log.e(TAG, "Ubicación no seleccionada en el mapa");
            Toast.makeText(this, "Seleccione una ubicación en el mapa", Toast.LENGTH_SHORT).show();
            return;
        }


        String latitud = String.valueOf(selectedLocation.latitude);
        String longitud = String.valueOf(selectedLocation.longitude);

        if (instrumentoId == null || fiscalizacion_id == null || inspectorId == -1) {
            Log.e(TAG, "Faltan campos requeridos");
            Toast.makeText(this, "Complete todos los campos y tome una foto", Toast.LENGTH_SHORT).show();
            return;
        }

        if (capturedImage == null) {
            Log.e(TAG, "No se tomó ninguna foto");
            Toast.makeText(this, "Debe tomar una foto antes de guardar", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Preparando datos para enviar al servidor");
        uploadMedicionWithPhoto(latitud, longitud, temperatura, humedad, valorMedido, observacion, instrumentoId, fiscalizacion_id);
        // Muestra el ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Guardando medición...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void uploadMedicionWithPhoto(String latitud, String longitud, String temperatura, String humedad,
                                         String valorMedido, String observacion, String instrumentoId, String proyectoId) {

        Log.d(TAG, "Iniciando solicitud al servidor");
        VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, urlGuardarMedicion,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(new String(response.data));
                        Log.d(TAG, "Respuesta del servidor: " + jsonResponse.toString());

                        if (jsonResponse.optString("mensaje").equals("Medición guardada exitosamente")) {
                            Toast.makeText(Medicion.this, "Medición guardada exitosamente", Toast.LENGTH_SHORT).show();

                            // Crear el Intent para ir a RegistroMedicion
                            Intent intent = new Intent(Medicion.this, RegistroMedicion.class);

                            // Pasar fiscalizacion_id y proyecto_id
                            intent.putExtra("fiscalizacion_id", fiscalizacion_id);
                            intent.putExtra("proyecto_id", proyectoId);  // Asegúrate de tener este valor disponible

                            startActivity(intent);
                            finish();
                        } else {
                            Log.e(TAG, "Error en la respuesta del servidor: " + jsonResponse.optString("error"));
                            Toast.makeText(Medicion.this, "Error al guardar la medición: " + jsonResponse.optString("error"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error al procesar la respuesta del servidor", e);
                    }
                },
                error -> {
                    // Cierra el ProgressDialog en caso de error
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    Toast.makeText(Medicion.this, "Error al guardar la medición", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error en la solicitud", error);
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("latitud", latitud);
                params.put("longitud", longitud);
                params.put("temperatura", temperatura);
                params.put("humedad", humedad);
                params.put("valor_medido", valorMedido);
                params.put("observacion", observacion);
                params.put("instrumento_medicion_id", instrumentoId);
                params.put("fiscalizacion_id", proyectoId);
                params.put("tipo", "m"); // Tipo predeterminado
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                capturedImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                params.put("foto", new DataPart("medicion_" + proyectoId + ".jpg", byteArrayOutputStream.toByteArray(), "image/jpeg"));
                Log.d(TAG, "Foto añadida a la solicitud");
                return params;
            }
        };

        Log.d(TAG, "Enviando solicitud al servidor");
        requestQueue.add(request);
    }
}
