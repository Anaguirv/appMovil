package com.example.login;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.text.ParseException;
import java.text.ParseException;



public class MedicionSensor extends AppCompatActivity {

    private TextView txtTemp, txtHumedad, txtLuminancia, txtIluminancia, txtFecha;
    private static final String TAG = "MedicionSensor";  // Definición del TAG de Logcat
    private String formatearFechaHora(String fechaHora) {
        try {
            // Formato de entrada desde el servidor
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

            // Conversión a formato de fecha y hora legible
            Date fecha = formatoEntrada.parse(fechaHora);

            // Formatos de salida
            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

            String fechaFormateada = formatoFecha.format(fecha);
            String horaFormateada = formatoHora.format(fecha);

            return "Fecha: " + fechaFormateada + "\nHora: " + horaFormateada;

        } catch (ParseException e) {
            Log.e(TAG, "Error al formatear fecha y hora", e);  // Registro de error de formato
            return "Fecha inválida";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicion_sensor);

        // Referencias a los TextViews del XML
        txtTemp = findViewById(R.id.txt_temp);
        txtHumedad = findViewById(R.id.txt_humedad);
        txtLuminancia = findViewById(R.id.txt_luminancia);
        txtIluminancia = findViewById(R.id.txt_iluminancia);
        txtFecha = findViewById(R.id.txt_fecha);

        // Obtener el idMedicion recibido
        int idMedicion = getIntent().getIntExtra("idMedicion", -1);
        Log.d(TAG, "ID de medición recibido: " + idMedicion);  // Log de ID recibido


        if (idMedicion != -1) {
            cargarMedicion(idMedicion);
        } else {
            Toast.makeText(this, "ID de medición no recibido", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Método para cargar la medición desde la API usando el id recibido
     */
    private void cargarMedicion(int idMedicion) {
        String url = "http://98.83.4.206/Cargar_mediciones_sensor";
        Log.d(TAG, "Solicitando datos desde: " + url);  // Registro de la solicitud

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Respuesta recibida: " + response.toString());  // Registro de respuesta

                        boolean encontrado = false;

                        try {
                            // Extraer el arreglo "mediciones_sensor"
                            JSONArray medicionesArray = response.getJSONArray("mediciones_sensor");

                            for (int i = 0; i < medicionesArray.length(); i++) {
                                JSONObject medicion = medicionesArray.getJSONObject(i);

                                int id = medicion.getInt("id");
                                Log.d(TAG, "ID de medición analizado: " + id);  // Verificación del ID recibido

                                if (id == idMedicion) {
                                    encontrado = true;
                                    Log.d(TAG, "Medición encontrada: " + medicion.toString());  // Detalles de la medición

                                    // Extraer datos de la medición
                                    double temperatura = medicion.optDouble("temperatura", 0.0);
                                    double humedad = medicion.optDouble("humedad", 0.0);
                                    double luminancia = medicion.optDouble("luminancia", 0.0);
                                    double iluminancia = medicion.optDouble("iluminancia", 0.0);
                                    String creado = medicion.getString("creado");

                                    // Mostrar datos en los TextViews
                                    txtTemp.setText(String.format("%.1f°C", temperatura));
                                    txtHumedad.setText(String.format("%.1f%%", humedad));
                                    txtLuminancia.setText(String.format("%.1f Luminancia", luminancia));
                                    txtIluminancia.setText(String.format("%.1f Iluminancia", iluminancia));
                                    txtFecha.setText(formatearFechaHora(creado));
                                    break;
                                }
                            }

                            if (!encontrado) {
                                Log.w(TAG, "Medición no encontrada");
                                Toast.makeText(MedicionSensor.this, "Medición no encontrada", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "Error al procesar datos JSON", e);  // Registro de error de JSON
                            Toast.makeText(MedicionSensor.this, "Error al procesar datos", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error de red: " + error.toString());  // Registro de error de red
                        Toast.makeText(MedicionSensor.this, "Error de red", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Agregar la solicitud a la cola de Volley
        VolleySingleton.getInstance(this).addToRequestQueue(request);
        Log.d(TAG, "Solicitud agregada a la cola de Volley");
    }

}
