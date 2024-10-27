package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class ListadoUsuarios extends AppCompatActivity {

    private ListView listado;
    private SearchView searchView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> listaUsuario;
    private ArrayList<String> listaUsuarioFiltrada;
    private ArrayList<JSONObject> listaUsuariosDatos; // Almacena todos los datos para EditarUsuario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_usuarios);

        listado = findViewById(R.id.lista);
        searchView = findViewById(R.id.searchView);

        // Cargar la lista de usuarios desde MySQL a través de PHP
        CargarLista();

        // Configurar el filtro para la barra de búsqueda
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarLista(newText);
                return false;
            }
        });

        // Configurar el clic en cada item de la lista para abrir EditarUsuario
        listado.setOnItemClickListener((adapterView, view, position, id) -> {
            try {
                JSONObject usuarioSeleccionado = listaUsuariosDatos.get(position); // Obtener el objeto JSON seleccionado

                // Crear un Intent para abrir la actividad EditarUsuario y pasar los datos completos
                Intent intent = new Intent(ListadoUsuarios.this, EditarUsuario.class);
                intent.putExtra("id_usuario", usuarioSeleccionado.getString("id_usuario"));
                intent.putExtra("nombre", usuarioSeleccionado.getString("nombre"));
                intent.putExtra("apellidoPaterno", usuarioSeleccionado.getString("apellidoPaterno"));
                intent.putExtra("apellidoMaterno", usuarioSeleccionado.getString("apellidoMaterno"));
                intent.putExtra("correo", usuarioSeleccionado.getString("correo")); // Pasar el correo
                intent.putExtra("fechaNacimiento", usuarioSeleccionado.getString("fechaNacimiento")); // Pasar la fecha de nacimiento
                startActivityForResult(intent, 1); // Iniciar EditarUsuario esperando resultado
            } catch (JSONException e) {
                Log.e("ListadoUsuarios", "Error al seleccionar el usuario: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Método para cargar la lista de usuarios desde MySQL usando PHP
    private void CargarLista() {
        String url = "http://52.71.115.13/consultarUsuarios.php"; // URL del archivo PHP
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        listaUsuario = new ArrayList<>();
                        listaUsuarioFiltrada = new ArrayList<>();
                        listaUsuariosDatos = new ArrayList<>();

                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject usuario = response.getJSONObject(i);
                                listaUsuariosDatos.add(usuario); // Agregar el objeto JSON completo a la lista
                                String nombreCompleto = usuario.getString("nombre") + " " +
                                        usuario.getString("apellidoPaterno") + " " +
                                        usuario.getString("apellidoMaterno");
                                listaUsuario.add(nombreCompleto); // Mostrar solo el nombre completo
                            }
                            listaUsuarioFiltrada.addAll(listaUsuario);
                            adapter = new ArrayAdapter<>(ListadoUsuarios.this, android.R.layout.simple_list_item_1, listaUsuarioFiltrada);
                            listado.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ListadoUsuarios.this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ListadoUsuarios.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        queue.add(jsonArrayRequest);
    }

    // Método para filtrar la lista
    private void filtrarLista(String texto) {
        String filtro = texto.toLowerCase(Locale.ROOT);
        listaUsuarioFiltrada.clear();

        if (filtro.isEmpty()) {
            listaUsuarioFiltrada.addAll(listaUsuario);
        } else {
            for (String usuario : listaUsuario) {
                if (usuario.toLowerCase(Locale.ROOT).contains(filtro)) {
                    listaUsuarioFiltrada.add(usuario);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // Método para recargar la lista al volver de EditarUsuario
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            CargarLista(); // Recargar la lista
        }
    }
}
