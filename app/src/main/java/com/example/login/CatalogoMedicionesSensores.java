package com.example.login;

import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;
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
    private ArrayList<Integer> listaIdsMediciones;
    private ArrayAdapter<String> adaptador;

    private String formatearFechaHora(String fechaHora) {
        try {
            // Formato de entrada desde el servidor
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

            // Convertir a formato de fecha y hora legible
            Date fecha = formatoEntrada.parse(fechaHora);

            // Formatos de salida
            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

            String fechaFormateada = formatoFecha.format(fecha);
            String horaFormateada = formatoHora.format(fecha);

            return "Fecha: " + fechaFormateada + "\nHora: " + horaFormateada;

        } catch (ParseException e) {
            Log.e("CatalogoMediciones", "Error al formatear fecha y hora", e);
            return "Fecha inválida";
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogo_mediciones_sensores);

        listViewMedicionesSensor = findViewById(R.id.listViewMedicionesSensor);
        listaMediciones = new ArrayList<>();
        listaIdsMediciones = new ArrayList<>();
        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaMediciones);
        listViewMedicionesSensor.setAdapter(adaptador);

        cargarMedicionesSensor();

        listViewMedicionesSensor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Obtener el ID de la medición seleccionada
                int idMedicion = listaIdsMediciones.get(position);

                // Mostrar el ID en Logcat antes de enviar el Intent
                Log.d("CatalogoMediciones", "ID de medición seleccionado: " + idMedicion);

                // Enviar el ID a MedicionSensor.java
                Intent intent = new Intent(CatalogoMedicionesSensores.this, MedicionSensor.class);
                intent.putExtra("idMedicion", idMedicion);
                startActivity(intent);
            }
        });

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
                            listaIdsMediciones.clear();

                            // Crear lista temporal para ordenar
                            ArrayList<JSONObject> listaTemporal = new ArrayList<>();

                            for (int i = 0; i < medicionesArray.length(); i++) {
                                JSONObject medicion = medicionesArray.getJSONObject(i);
                                listaTemporal.add(medicion);  // Agregar medición a la lista temporal
                            }

                            // Ordenar por fecha de creación
                            listaTemporal.sort((med1, med2) -> {
                                try {
                                    String fecha1 = med1.getString("creado");
                                    String fecha2 = med2.getString("creado");

                                    SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                                    Date date1 = formatoEntrada.parse(fecha1);
                                    Date date2 = formatoEntrada.parse(fecha2);

                                    // Orden descendente: más reciente primero
                                    return date2.compareTo(date1);

                                } catch (ParseException | JSONException e) {
                                    e.printStackTrace();
                                }
                                return 0;
                            });

                            // Cargar datos en la lista en orden
                            for (JSONObject medicion : listaTemporal) {
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
                                listaIdsMediciones.add(id);
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

}
