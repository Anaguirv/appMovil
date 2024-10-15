package com.example.login;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Locale;

public class ListadoUsuarios extends AppCompatActivity {

    private ListView listado;
    private SearchView searchView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> listaUsuario;
    private ArrayList<String> listaUsuarioFiltrada;

    @Override
    protected void onPostResume() {
        super.onPostResume();
        CargarLista();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_listado_usuarios);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listado = findViewById(R.id.lista);
        searchView = findViewById(R.id.searchView);

        // Cargar la lista de usuarios desde la base de datos
        CargarLista();

        // Configuración del filtro para la barra de búsqueda
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try {
                    filtrarLista(newText);
                } catch (Exception e) {
                    Log.e("ListadoUsuarios", "Error al filtrar la lista: " + e.getMessage());
                    e.printStackTrace();
                }
                return false;
            }
        });

        // Configurar el comportamiento del clic en cada item de la lista
        listado.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    String[] datosUsuario = listaUsuarioFiltrada.get(i).split(" ");
                    int idusu = Integer.parseInt(datosUsuario[0]);
                    String nombre = datosUsuario[1];
                    String apellido = datosUsuario[2];
                    Intent intent = new Intent(ListadoUsuarios.this, ModificarRegistro.class);
                    intent.putExtra("Id", idusu);
                    intent.putExtra("Nombre", nombre);
                    intent.putExtra("Apellido", apellido);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("ListadoUsuarios", "Error al seleccionar el item: " + e.getMessage());
                }
            }
        });
    }

    // Método para obtener la lista de usuarios desde SQLite
    private ArrayList<String> ListaUsuario() {
        ArrayList<String> datos = new ArrayList<>();
        ConexionDbHelper helper = new ConexionDbHelper(this, "APPSQLITE", null, 1);
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT * FROM USUARIOS";
        Cursor c = db.rawQuery(sql, null);
        if (c.moveToFirst()) {
            do {
                String linea = c.getInt(0) + " " + c.getString(1) + " " + c.getString(2) + " " + c.getString(3);
                datos.add(linea);
            } while (c.moveToNext());
        }
        c.close(); // Asegurarse de cerrar el cursor
        db.close(); // Asegurarse de cerrar la base de datos
        return datos;
    }

    // Método para cargar la lista en el ListView
    private void CargarLista() {
        try {
            listaUsuario = ListaUsuario();
            listaUsuarioFiltrada = new ArrayList<>(listaUsuario); // Inicialmente, lista filtrada = lista completa
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaUsuarioFiltrada);
            listado.setAdapter(adapter);
        } catch (Exception e) {
            Log.e("ListadoUsuarios", "Error al cargar la lista: " + e.getMessage());
        }
    }

    // Método para filtrar la lista
    private void filtrarLista(String texto) {
        String filtro = texto.toLowerCase(Locale.ROOT);
        listaUsuarioFiltrada.clear();

        if (filtro.isEmpty()) {
            listaUsuarioFiltrada.addAll(listaUsuario); // Mostrar lista completa si no hay filtro
        } else {
            for (String usuario : listaUsuario) {
                if (usuario.toLowerCase(Locale.ROOT).contains(filtro)) {
                    listaUsuarioFiltrada.add(usuario); // Agregar usuarios que coincidan con el filtro
                }
            }
        }

        adapter.notifyDataSetChanged(); // Notificar cambios al adaptador
    }
}
