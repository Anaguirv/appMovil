package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CatalogoMedicionesProyecto extends AppCompatActivity {

    private ListView listViewMedicionesProyecto;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> listaMediciones;
    private ArrayList<Integer> listaIdsMediciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogo_mediciones_proyecto);

        // Configuración de ventana completa
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listViewMedicionesProyecto = findViewById(R.id.listViewMedicionesProyecto);
        listaMediciones = new ArrayList<>();
        listaIdsMediciones = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaMediciones);
        listViewMedicionesProyecto.setAdapter(adapter);

        // Recibir el proyectoId desde el Intent
        String proyectoIdStr = getIntent().getStringExtra("proyectoId");

        if (proyectoIdStr != null && !proyectoIdStr.isEmpty()) {
            try {
                int proyectoId = Integer.parseInt(proyectoIdStr);
                Log.d("CatalogoMediciones", "ID del Proyecto Recibido: " + proyectoId);
                cargarMediciones(proyectoId);

                // Configurar clic en ListView
                configurarClickEnMedicion();

            } catch (NumberFormatException e) {
                Log.e("CatalogoMediciones", "Error: No se puede convertir proyectoId a entero.");
                Toast.makeText(this, "Error al convertir el ID del proyecto", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("CatalogoMediciones", "Error: proyectoId no válido.");
            Toast.makeText(this, "ID de proyecto no válido", Toast.LENGTH_SHORT).show();
        }
    }

    private void configurarClickEnMedicion() {
        listViewMedicionesProyecto.setOnItemClickListener((parent, view, position, id) -> {
            int medicionId = listaIdsMediciones.get(position);

            // Crear Intent para abrir EditarMedicion
            Intent intent = new Intent(CatalogoMedicionesProyecto.this, EditarMedicion.class);
            intent.putExtra("medicionId", medicionId);  // Pasar el ID de la medición
            startActivity(intent);

            Log.d("CatalogoMediciones", "Medición seleccionada: ID " + medicionId);
        });
    }

    private String formatearFecha(String fechaOriginal) {
        try {
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat formatoSalida = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            Date fecha = formatoEntrada.parse(fechaOriginal);
            return formatoSalida.format(fecha);

        } catch (ParseException e) {
            Log.e("CatalogoMediciones", "Error al formatear la fecha", e);
            return "Fecha inválida";
        }
    }

    private void cargarMediciones(int proyectoId) {
        String url = "http://98.83.4.206/Cargar_mediciones/" + proyectoId + "/";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray mediciones = response.getJSONArray("mediciones");
                        listaMediciones.clear();
                        listaIdsMediciones.clear();

                        for (int i = 0; i < mediciones.length(); i++) {
                            JSONObject medicion = mediciones.getJSONObject(i);

                            int idMedicion = medicion.getInt("id");
                            int idFiscalizacion = medicion.getInt("fiscalizacion_id");
                            String fechaCreada = medicion.getString("creado");

                            String fechaFormateada = formatearFecha(fechaCreada);

                            String item = "ID Medición: " + idMedicion +
                                    "\nID Fiscalización: " + idFiscalizacion +
                                    "\nFecha y Hora: " + fechaFormateada;

                            listaMediciones.add(item);
                            listaIdsMediciones.add(idMedicion);
                        }

                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        Log.e("CatalogoMediciones", "Error procesando datos", e);
                        Toast.makeText(this, "Error procesando datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("CatalogoMediciones", "Error al cargar datos: " + error.getMessage());
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
