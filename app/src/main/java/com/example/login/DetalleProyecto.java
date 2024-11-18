package com.example.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log; // Importar para usar Log.d
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetalleProyecto extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imageViewFoto;
    private Bitmap capturedImage;
    private Uri imageUri;

    private static final String TAG = "DetalleProyecto"; // Etiqueta para Logcat

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_proyecto);

        imageViewFoto = findViewById(R.id.imageViewFoto);
        Button btnTomarFoto = findViewById(R.id.btnTomarFoto);
        Button btnEliminarFoto = findViewById(R.id.btnEliminarFoto);
        Button btnGuardarFoto = findViewById(R.id.btnGuardarFoto);

        btnTomarFoto.setOnClickListener(v -> captureImage());

        btnEliminarFoto.setOnClickListener(v -> {
            capturedImage = null;
            imageViewFoto.setImageResource(0);  // Limpia la imagen
            Toast.makeText(this, "Foto eliminada", Toast.LENGTH_SHORT).show();
        });

        btnGuardarFoto.setOnClickListener(v -> {
            if (capturedImage != null) {
                new UploadImageTask().execute();  // Llama al AsyncTask para subir la imagen
            } else {
                Toast.makeText(this, "Capture una imagen primero", Toast.LENGTH_SHORT).show();
            }
        });
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
                imageViewFoto.setImageBitmap(capturedImage);  // Mostrar la imagen en el ImageView
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No se tomó ninguna foto", Toast.LENGTH_SHORT).show();
        }
    }

    // AsyncTask para subir la imagen en segundo plano
    private class UploadImageTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Convertir Bitmap a ByteArrayOutputStream
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                capturedImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] imageData = byteArrayOutputStream.toByteArray();

                // Crear la URL de conexión
                URL url = new URL("http://52.71.115.13/upload.php");
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

                // Devolver la respuesta del servidor
                return serverResponse;

            } catch (IOException e) {
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
        protected void onPostExecute(String serverResponse) {
            if (serverResponse != null) {
                Toast.makeText(DetalleProyecto.this, "Imagen subida exitosamente", Toast.LENGTH_SHORT).show();
                // Mostrar la respuesta del servidor en el Logcat
                Log.d(TAG, "Respuesta del servidor: " + serverResponse);
            } else {
                Toast.makeText(DetalleProyecto.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
