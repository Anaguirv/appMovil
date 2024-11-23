package com.example.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
    private String urlInstrumentos = "http://98.83.4.206:8080/ApiInstrumentoMedicion";
    private String urlGuardarMedicion = "http://52.71.115.13/GuardarMedicion.php";
    private String urlUploadPhoto = "http://52.71.115.13/upload.php";

    private HashMap<Integer, String> instrumentoMap = new HashMap<>();
    private String proyectoId;
    private LatLng selectedLocation;
    private int inspectorId;

    private MapHandler mapHandler;
    private Bitmap capturedImage;
    private Uri imageUri;

    private String photoUrl; // Variable para almacenar la URL de la foto subida

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicion);

        Log.d(TAG, "onCreate: Iniciando componentes de la UI");

        SharedPreferences sharedPref = getSharedPreferences("InspectorPrefs", Context.MODE_PRIVATE);
        inspectorId = sharedPref.getInt("inspector_id", -1);
        Log.d(TAG, "onCreate: Inspector ID = " + inspectorId);

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
        btnTomarFoto = findViewById(R.id.btnTomarFoto);

        requestQueue = Volley.newRequestQueue(this);
        loadInstrumentData();

        Button btnGuardarMedicion = findViewById(R.id.btnguardarmedicion);
        btnGuardarMedicion.setOnClickListener(v -> guardarMedicion());

        // Configurar el botón para tomar foto
        btnTomarFoto.setOnClickListener(v -> captureImage());

        // Verificar permisos de cámara
        checkCameraPermission();

        // Configurar el manejo del mapa usando MapHandler
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapContainer);
        mapHandler = new MapHandler(this, mapFragment);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
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
                // Crear un archivo temporal para almacenar la imagen completa
                File photoFile = File.createTempFile("captured_image", ".jpg", getCacheDir());
                imageUri = FileProvider.getUriForFile(
                        this, getApplicationContext().getPackageName() + ".provider", photoFile);

                // Configurar el intent para que use el archivo como destino
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
                // Leer la imagen de alta calidad desde el archivo
                capturedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView1.setImageBitmap(capturedImage);  // Mostrar la imagen en el ImageView
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No se tomó ninguna foto", Toast.LENGTH_SHORT).show();
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

                        Log.d(TAG, "loadInstrumentData: Datos de instrumentos cargados correctamente");

                    } catch (JSONException e) {
                        Log.e(TAG, "loadInstrumentData: Error al procesar datos JSON", e);
                        Toast.makeText(Medicion.this, "Error al procesar datos JSON", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            Log.e(TAG, "loadInstrumentData: Error de conexión al cargar instrumentos", error);
            Toast.makeText(Medicion.this, "Error de conexión", Toast.LENGTH_SHORT).show();
        });

        requestQueue.add(jsonArrayRequest);
    }

    private void guardarMedicion() {
        String instrumentoId = getSelectedInstrumentId();
        String temperatura = editTextTemperatura.getText().toString();
        String humedad = editTextHumedad.getText().toString();
        String valorMedido = editTextValorMedido.getText().toString();
        String observacion = editTextObservacion.getText().toString();

        selectedLocation = mapHandler.getSelectedLocation();
        if (selectedLocation == null) {
            Toast.makeText(this, "Seleccione una ubicación en el mapa", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "guardarMedicion: Ubicación no seleccionada en el mapa");
            return;
        }

        String latitud = String.valueOf(selectedLocation.latitude);
        String longitud = String.valueOf(selectedLocation.longitude);

        if (instrumentoId == null || proyectoId == null || inspectorId == -1) {
            Toast.makeText(this, "Complete todos los campos y seleccione ubicación", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "guardarMedicion: Campos faltantes - instrumentoId: " + instrumentoId + ", proyectoId: " + proyectoId + ", inspectorId: " + inspectorId);
            return;
        }

        String fechaHoraActual = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        if (capturedImage != null) {
            // Subir la foto primero
            new UploadImageTask(latitud, longitud, temperatura, humedad, valorMedido, observacion, fechaHoraActual, instrumentoId, proyectoId).execute();
        } else {
            // No hay foto, enviar los datos de la medición sin URL de foto
            sendMeasurementData(latitud, longitud, temperatura, humedad, valorMedido, observacion, fechaHoraActual, instrumentoId, proyectoId, null);
        }
    }

    private void sendMeasurementData(String latitud, String longitud, String temperatura, String humedad,
                                     String valorMedido, String observacion, String fechaHoraActual,
                                     String instrumentoId, String proyectoId, @Nullable String photoUrl) {

        StringRequest request = new StringRequest(Request.Method.POST, urlGuardarMedicion,
                response -> {
                    Log.d(TAG, "guardarMedicion: Respuesta del servidor = " + response);
                    Toast.makeText(Medicion.this, "Medición guardada exitosamente", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Medicion.this, RegistroMedicion.class);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    String message = "Error al guardar medición";
                    if (error.networkResponse != null) {
                        message += ": " + new String(error.networkResponse.data);
                    }
                    Log.e(TAG, "guardarMedicion: " + message, error);
                    Toast.makeText(Medicion.this, message, Toast.LENGTH_LONG).show();
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
                params.put("creado", fechaHoraActual);
                params.put("inspector_id", String.valueOf(inspectorId));
                params.put("instrumento_medicion_id", instrumentoId);
                params.put("proyecto_id", proyectoId);
                if (photoUrl != null) {
                    params.put("photo_url", photoUrl);
                }
                Log.d(TAG, "Parámetros enviados (sendMeasurementData): " + params.toString());
                return params;
            }
        };

        requestQueue.add(request);
    }

    private String getSelectedInstrumentId() {
        int position = spinnerInstrumento.getSelectedItemPosition();
        return instrumentoMap.get(position);
    }

    // AsyncTask para subir la imagen en segundo plano
    private class UploadImageTask extends AsyncTask<Void, Void, String> {

        private String latitud, longitud, temperatura, humedad, valorMedido, observacion, fechaHoraActual, instrumentoId, proyectoId;

        public UploadImageTask(String latitud, String longitud, String temperatura, String humedad,
                               String valorMedido, String observacion, String fechaHoraActual,
                               String instrumentoId, String proyectoId) {
            this.latitud = latitud;
            this.longitud = longitud;
            this.temperatura = temperatura;
            this.humedad = humedad;
            this.valorMedido = valorMedido;
            this.observacion = observacion;
            this.fechaHoraActual = fechaHoraActual;
            this.instrumentoId = instrumentoId;
            this.proyectoId = proyectoId;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Convertir Bitmap a ByteArrayOutputStream
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                capturedImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] imageData = byteArrayOutputStream.toByteArray();

                // Crear la URL de conexión
                URL url = new URL(urlUploadPhoto);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/octet-stream");
                connection.setRequestProperty("Connection", "Keep-Alive");

                // Enviar datos de la imagen usando OutputStream
                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(imageData);
                    outputStream.flush();
                }

                int responseCode = connection.getResponseCode();

                // Leer la respuesta del servidor
                InputStream inputStream;
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    inputStream = connection.getInputStream();
                } else {
                    inputStream = connection.getErrorStream();
                }

                String serverResponse = convertStreamToString(inputStream);

                connection.disconnect();

                // Registrar la respuesta del servidor
                Log.d(TAG, "Response Code: " + responseCode);
                Log.d(TAG, "Server Response: " + serverResponse);

                // Parsear la respuesta JSON
                JSONObject jsonResponse = new JSONObject(serverResponse);
                if (jsonResponse.has("success") && jsonResponse.getBoolean("success")) {
                    photoUrl = jsonResponse.getString("file_url");
                    return photoUrl;
                } else {
                    String errorMessage = jsonResponse.optString("error", "Error desconocido");
                    Log.e(TAG, "Error al subir la foto: " + errorMessage);
                    return null;
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Error al subir la imagen: " + e.getMessage());
                return null;
            }
        }

        private String convertStreamToString(InputStream is) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            reader.close();
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String photoUrl) {
            if (photoUrl != null) {

                sendMeasurementData(latitud, longitud, temperatura, humedad, valorMedido, observacion, fechaHoraActual, instrumentoId, proyectoId, photoUrl);
            } else {
                Toast.makeText(Medicion.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
