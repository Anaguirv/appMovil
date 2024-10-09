package com.example.login;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class ListadoUsuarios extends AppCompatActivity {

    ListView listado;
    ArrayList<String> listaUsuario;

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

        listado = (ListView) findViewById(R.id.lista);
        CargarLista();
    }

    private ArrayList<String> ListaUsuario(){
        ArrayList<String> datos=new ArrayList<String>();
        ConexionDbHelper helper = new ConexionDbHelper(this, "APPSQLITE", null, 1);
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql="SELECT * FROM USUARIOS";
        Cursor c=db.rawQuery(sql,null);
        if(c.moveToFirst())
        {
            do{
                String linea=c.getInt(0)+" "+c.getString(1)+" "+c.getString(2)+" "+c.getString(3);
                datos.add(linea);
            }while(c.moveToNext());
        }
        db.close();
        return datos;
    }

    private void CargarLista()
    {
        listaUsuario=ListaUsuario();
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,listaUsuario);
        listado.setAdapter(adapter);
    }
}