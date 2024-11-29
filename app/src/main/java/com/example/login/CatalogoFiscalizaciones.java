package com.example.login;

import android.content.Intent;
import android.os.Bundle;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CatalogoFiscalizaciones extends AppCompatActivity {

    private static final String URL_FISCALIZACIONES = "http://98.83.4.206/Api_fiscalizaciones";
    private static final String URL_PROYECTOS = "http://98.83.4.206/Api_Proyectos";

    private ListView listViewFiscalizaciones;
    private ArrayAdapter<String> adapter;
    private ArrayList<HashMap<String, String>> listaFiscalizaciones;

    // Map para convertir tipo de alumbrado
    private final Map<String, String> tipoAlumbradoMap = new HashMap<String, String>() {{
        put("v", "Vehicular");
        put("p", "Peatonal");
        put("i", "Industrial");
        put("o", "Ornamental");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_catalogo_fiscalizaciones);

        // Vincular la ListView
        listViewFiscalizaciones = findViewById(R.id.listViewFiscalizaciones);
        listaFiscalizaciones = new ArrayList<>();

        // Ajustar el padding para el contenido según las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Cargar fiscalizaciones desde la API
        cargarFiscalizaciones();

        // Manejar clics en los elementos de la lista
        listViewFiscalizaciones.setOnItemClickListener((parent, view, position, id) -> {
            HashMap<String, String> fiscalizacion = listaFiscalizaciones.get(position);
            String fiscalizacionId = fiscalizacion.get("id");
            String proyectoId = fiscalizacion.get("proyecto_id");

            // Abrir la actividad de RegistroMedicion con los datos seleccionados
            Intent intent = new Intent(CatalogoFiscalizaciones.this, RegistroMedicion.class);
            intent.putExtra("fiscalizacion_id", fiscalizacionId);
            intent.putExtra("proyecto_id", proyectoId);
            startActivity(intent);
        });
    }

    private void cargarFiscalizaciones() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Solicitar fiscalizaciones desde la API
        JsonObjectRequest fiscalizacionesRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL_FISCALIZACIONES,
                null,
                response -> {
                    try {
                        JSONArray fiscalizacionesArray = response.getJSONArray("fiscalizaciones_completas");
                        for (int i = 0; i < fiscalizacionesArray.length(); i++) {
                            JSONObject fiscalizacion = fiscalizacionesArray.getJSONObject(i);
                            String fiscalizacionId = fiscalizacion.getString("id");
                            String proyectoId = fiscalizacion.getString("proyecto_id");

                            // Cargar detalles del proyecto asociado a cada fiscalización
                            cargarDetallesProyecto(fiscalizacionId, proyectoId);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(fiscalizacionesRequest);
    }

    private void cargarDetallesProyecto(String fiscalizacionId, String proyectoId) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Solicitar proyectos desde la API
        JsonObjectRequest proyectosRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL_PROYECTOS,
                null,
                response -> {
                    try {
                        JSONArray proyectosArray = response.getJSONArray("proyectos");
                        for (int i = 0; i < proyectosArray.length(); i++) {
                            JSONObject proyecto = proyectosArray.getJSONObject(i);

                            // Buscar el proyecto correspondiente por ID
                            if (proyecto.getString("id").equals(proyectoId)) {
                                String proyectoNombre = proyecto.getString("nombre");
                                String tipoAlumbrado = proyecto.getString("tipo_alumbrado");
                                String representanteNombre = proyecto.getJSONObject("representante_legal").getString("nombre");
                                String representanteApellido = proyecto.getJSONObject("representante_legal").getString("a_paterno");

                                // Convertir el tipo de alumbrado a su descripción
                                String tipoAlumbradoCompleto = tipoAlumbradoMap.getOrDefault(tipoAlumbrado, "Desconocido");

                                // Crear un mapa con los datos de la fiscalización
                                HashMap<String, String> fiscalizacionData = new HashMap<>();
                                fiscalizacionData.put("id", fiscalizacionId);
                                fiscalizacionData.put("proyecto_id", proyectoId);
                                fiscalizacionData.put("proyecto_nombre", proyectoNombre);
                                fiscalizacionData.put("tipo_alumbrado", tipoAlumbradoCompleto);
                                fiscalizacionData.put("representante_legal", representanteNombre + " " + representanteApellido);

                                listaFiscalizaciones.add(fiscalizacionData);
                                break;
                            }
                        }

                        // Actualizar la lista después de agregar datos
                        actualizarListView();
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar datos del proyecto", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error de conexión al cargar proyectos", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(proyectosRequest);
    }

    private void actualizarListView() {
        // Ordenar la lista por ID en orden ascendente (IDs más pequeños primero)
        Collections.sort(listaFiscalizaciones, (f1, f2) -> {
            int id1 = Integer.parseInt(f1.get("id"));
            int id2 = Integer.parseInt(f2.get("id"));
            return Integer.compare(id1, id2); // Orden ascendente
        });

        // Crear una lista para mostrar en la ListView
        ArrayList<String> displayList = new ArrayList<>();
        for (HashMap<String, String> fiscalizacion : listaFiscalizaciones) {
            String displayText = "ID: " + fiscalizacion.get("id") +
                    "\nProyecto: " + fiscalizacion.get("proyecto_nombre") +
                    "\nTipo Alumbrado: " + fiscalizacion.get("tipo_alumbrado") +
                    "\nRepresentante Legal: " + fiscalizacion.get("representante_legal");
            displayList.add(displayText);
        }

        // Configurar el adaptador con los datos
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listViewFiscalizaciones.setAdapter(adapter);
    }
}
