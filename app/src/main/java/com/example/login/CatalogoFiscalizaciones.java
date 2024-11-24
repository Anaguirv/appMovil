package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class CatalogoFiscalizaciones extends AppCompatActivity {

    private ListView listViewFiscalizaciones;
    private ArrayAdapter<String> adapter;
    private ArrayList<HashMap<String, String>> listaFiscalizaciones;
    private static final String URL_FISCALIZACIONES = "http://98.83.4.206:8080/api_fiscalizacion";
    private static final String URL_PROYECTOS = "http://98.83.4.206:8080/api_proyecto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_catalogo_fiscalizaciones);

        listViewFiscalizaciones = findViewById(R.id.listViewFiscalizaciones);
        listaFiscalizaciones = new ArrayList<>();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cargarFiscalizaciones(); // Cargar datos de las fiscalizaciones

        listViewFiscalizaciones.setOnItemClickListener((parent, view, position, id) -> {
            HashMap<String, String> fiscalizacion = listaFiscalizaciones.get(position);
            String fiscalizacionId = fiscalizacion.get("id");
            String proyectoId = fiscalizacion.get("proyecto_id"); // Extraemos el proyecto_id

            // Navegar a RegistroMedicion con el ID de la fiscalización y proyecto_id
            Intent intent = new Intent(CatalogoFiscalizaciones.this, RegistroMedicion.class);
            intent.putExtra("fiscalizacion_id", fiscalizacionId);
            intent.putExtra("proyecto_id", proyectoId); // Pasamos proyecto_id
            startActivity(intent);
        });
    }

    private void cargarFiscalizaciones() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Obtener fiscalizaciones
        JsonObjectRequest fiscalizacionesRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL_FISCALIZACIONES,
                null,
                response -> {
                    try {
                        JSONArray fiscalizacionesArray = response.getJSONArray("fiscalizaciones");
                        for (int i = 0; i < fiscalizacionesArray.length(); i++) {
                            JSONObject fiscalizacion = fiscalizacionesArray.getJSONObject(i);
                            String fiscalizacionId = fiscalizacion.getString("id");
                            String proyectoId = fiscalizacion.getString("proyecto_id");
                            String proyectoNombre = fiscalizacion.getString("proyecto_nombre");

                            // Obtener detalles del proyecto desde la segunda URL
                            cargarDetallesProyecto(fiscalizacionId, proyectoId, proyectoNombre);
                        }
                    } catch (JSONException e) {
                        Log.e("CatalogoFiscalizaciones", "Error al procesar fiscalizaciones: " + e.getMessage());
                        Toast.makeText(CatalogoFiscalizaciones.this, "Error al procesar datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("CatalogoFiscalizaciones", "Error de red: " + error.getMessage());
                    Toast.makeText(CatalogoFiscalizaciones.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(fiscalizacionesRequest);
    }

    private void cargarDetallesProyecto(String fiscalizacionId, String proyectoId, String proyectoNombre) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest proyectosRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL_PROYECTOS,
                null,
                response -> {
                    try {
                        JSONArray proyectosArray = response.getJSONArray("proyectos");
                        for (int i = 0; i < proyectosArray.length(); i++) {
                            JSONObject proyecto = proyectosArray.getJSONObject(i);
                            if (proyecto.getString("nombre").equals(proyectoNombre)) {
                                String tipoAlumbrado = proyecto.getString("tipo_alumbrado");
                                String representanteLegal = "Representante Legal"; // Modifica si hay más datos

                                HashMap<String, String> fiscalizacionData = new HashMap<>();
                                fiscalizacionData.put("id", fiscalizacionId);
                                fiscalizacionData.put("proyecto_id", proyectoId); // Guardamos el proyecto_id
                                fiscalizacionData.put("proyecto_nombre", proyectoNombre);
                                fiscalizacionData.put("tipo_alumbrado", tipoAlumbrado);
                                fiscalizacionData.put("representante_legal", representanteLegal);

                                listaFiscalizaciones.add(fiscalizacionData);
                                break;
                            }
                        }
                        actualizarListView();
                    } catch (JSONException e) {
                        Log.e("CatalogoFiscalizaciones", "Error al procesar proyectos: " + e.getMessage());
                    }
                },
                error -> Log.e("CatalogoFiscalizaciones", "Error de red al cargar proyectos: " + error.getMessage())
        );

        requestQueue.add(proyectosRequest);
    }

    private void actualizarListView() {
        ArrayList<String> displayList = new ArrayList<>();
        for (HashMap<String, String> fiscalizacion : listaFiscalizaciones) {
            String displayText = "Proyecto: " + fiscalizacion.get("proyecto_nombre") +
                    "\nTipo Alumbrado: " + fiscalizacion.get("tipo_alumbrado") +
                    "\nRepresentante Legal: " + fiscalizacion.get("representante_legal");
            displayList.add(displayText);
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listViewFiscalizaciones.setAdapter(adapter);
    }
}
