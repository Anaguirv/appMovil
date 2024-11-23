package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class RegistroMedicion extends AppCompatActivity {

    private ListView listViewProyectos;
    private ArrayList<HashMap<String, String>> listaProyectos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_medicion);

        listViewProyectos = findViewById(R.id.listViewProyectos);
        listaProyectos = new ArrayList<>();

        // Llamar a la función para cargar los proyectos desde la API
        cargarProyectos();

        // Crear un adaptador personalizado para mostrar los proyectos en el ListView
        ArrayAdapter<HashMap<String, String>> adapter = new ArrayAdapter<HashMap<String, String>>(this, R.layout.item_proyecto, listaProyectos) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.item_proyecto, parent, false);
                }

                TextView nombreProyecto = convertView.findViewById(R.id.textViewNombreProyecto);
                TextView representanteLegal = convertView.findViewById(R.id.textViewRepresentanteLegal);
                TextView tipoAlumbrado = convertView.findViewById(R.id.textViewTipoAlumbrado);
                TextView tipoLampara = convertView.findViewById(R.id.textViewTipoLampara); // TextView para tipo de lámpara y cantidad

                // Obtener el proyecto actual y configurar los TextView
                HashMap<String, String> proyecto = listaProyectos.get(position);
                nombreProyecto.setText(proyecto.get("nombre"));
                representanteLegal.setText(proyecto.get("representante_legal"));
                tipoAlumbrado.setText(proyecto.get("tipo_alumbrado"));

                // Concatenar tipo_lampara y cantidad para mostrar en un solo TextView
                String tipoLamparaConCantidad = proyecto.get("tipo_lampara") + " " + proyecto.get("cantidad");
                tipoLampara.setText(tipoLamparaConCantidad); //

                return convertView;
            }
        };
        listViewProyectos.setAdapter(adapter);


        // Configurar el click listener para abrir Medicion con los datos del proyecto seleccionado
        listViewProyectos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(RegistroMedicion.this, Medicion.class);
                intent.putExtra("proyecto_id", listaProyectos.get(position).get("id"));
                intent.putExtra("proyecto_nombre", listaProyectos.get(position).get("nombre"));
                startActivity(intent);
            }
        });
    }

    // Función para cargar los proyectos desde la API
    private void cargarProyectos() {
        String url = "http://98.83.4.206:8080/ApiProyecto";

        Log.d("RegistroMedicion", "Iniciando solicitud a la URL: " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("RegistroMedicion", "Respuesta recibida del servidor");
                        listaProyectos.clear();
                        try {
                            JSONArray proyectosArray = response.getJSONArray("proyectos");
                            Log.d("RegistroMedicion", "Número de proyectos recibidos: " + proyectosArray.length());

                            for (int i = 0; i < proyectosArray.length(); i++) {
                                JSONObject proyecto = proyectosArray.getJSONObject(i);
                                HashMap<String, String> proyectoMap = new HashMap<>();
                                proyectoMap.put("id", proyecto.getString("id"));
                                proyectoMap.put("nombre", proyecto.getString("nombre"));
                                proyectoMap.put("representante_legal", proyecto.optString("representante_legal_id", "No asignado"));
                                proyectoMap.put("tipo_alumbrado", proyecto.optString("tipo_alumbrado", "No especificado"));
                                proyectoMap.put("tipo_lampara", proyecto.optString("detalle_luminarias_id", "N/A"));
                                proyectoMap.put("cantidad", "N/A"); // Valor predeterminado para cantidad

                                Log.d("RegistroMedicion", "Proyecto añadido: " + proyectoMap.toString());
                                listaProyectos.add(proyectoMap);
                            }

                            ((ArrayAdapter) listViewProyectos.getAdapter()).notifyDataSetChanged();
                            Log.d("RegistroMedicion", "Adaptador actualizado con los proyectos.");
                        } catch (JSONException e) {
                            Log.e("RegistroMedicion", "Error al procesar JSON: " + e.getMessage(), e);
                            Toast.makeText(RegistroMedicion.this, "Error al procesar datos JSON", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("RegistroMedicion", "Error de conexión: " + error.getMessage(), error);

                // Extraer más información si es posible
                if (error.networkResponse != null) {
                    Log.e("RegistroMedicion", "Código de respuesta HTTP: " + error.networkResponse.statusCode);
                    Log.e("RegistroMedicion", "Datos de respuesta: " + new String(error.networkResponse.data));
                }

                Toast.makeText(RegistroMedicion.this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
        Log.d("RegistroMedicion", "Solicitud añadida a la cola.");
    }

}
