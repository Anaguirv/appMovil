package com.example.login;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CatalogoMedicionesSensores extends AppCompatActivity {

    private ListView listViewMedicionesSensor;
    private ArrayList<String> listaMediciones;
    private ArrayAdapter<String> adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogo_mediciones_sensores);

        listViewMedicionesSensor = findViewById(R.id.listViewMedicionesSensor);
        listaMediciones = new ArrayList<>();
        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaMediciones);
        listViewMedicionesSensor.setAdapter(adaptador);

        cargarMedicionesSensor();
    }

    private void cargarMedicionesSensor() {
        String url = "http://98.83.4.206/Cargar_mediciones_sensor";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray medicionesArray = response.getJSONArray("mediciones_sensor");

                            listaMediciones.clear();
                            for (int i = 0; i < medicionesArray.length(); i++) {
                                JSONObject medicion = medicionesArray.getJSONObject(i);

                                int id = medicion.getInt("id");
                                double temperatura = medicion.optDouble("temperatura", 0.0);
                                double humedad = medicion.optDouble("humedad", 0.0);
                                double luminancia = medicion.getDouble("luminancia");
                                double iluminancia = medicion.getDouble("iluminancia");
                                String creado = medicion.getString("creado");
                                int sensorId = medicion.getInt("sensor_id");

                                String fechaHoraFormateada = formatearFechaHora(creado);

                                String infoMedicion = String.format(
                                        "ID: %d\nTemp: %.1f°C\nHum: %.1f%%\nLuminancia: %.1f\nIluminancia: %.1f\n%s\nSensor ID: %d",
                                        id, temperatura, humedad, luminancia, iluminancia, fechaHoraFormateada, sensorId
                                );

                                listaMediciones.add(infoMedicion);
                            }
                            adaptador.notifyDataSetChanged();

                        } catch (JSONException e) {
                            Toast.makeText(CatalogoMedicionesSensores.this, "Error de formato JSON", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CatalogoMedicionesSensores.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private String formatearFechaHora(String fechaHora) {
        try {
            // Formato original de la fecha
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

            // Fecha convertida
            Date fecha = formatoEntrada.parse(fechaHora);

            // Formato de salida para la fecha
            SimpleDateFormat formatoFecha = new SimpleDateFormat("MMMM yyyy dd", Locale.getDefault());

            // Formato de salida para la hora
            SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

            String fechaFormateada = formatoFecha.format(fecha);
            String horaFormateada = formatoHora.format(fecha);

            return "Fecha: " + fechaFormateada + "\nHora: " + horaFormateada;

        } catch (ParseException e) {
            e.printStackTrace();
            return "Fecha inválida";
        }
    }
}
