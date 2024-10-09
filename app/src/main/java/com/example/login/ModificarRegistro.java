package com.example.login;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.database.sqlite.SQLiteDatabase;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ModificarRegistro extends AppCompatActivity {
    // 1.- Declarar variables
    private EditText id,nombre,apellido;
    private Button btn_mod,btn_eli;
    // Variables que traen los datos a modificar
    private int idusu_capturado;
    private String nombre_capturado,apellio_capturado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_modificar_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 2.- Casteamos para crear la relación entre UI y Lógica
        id = (EditText) findViewById(R.id.txt_id);
        nombre = (EditText) findViewById(R.id.txt_nombre_mod);
        apellido = (EditText) findViewById(R.id.txt_apellido_mod);
        btn_mod = (Button) findViewById(R.id.btn_modificar);
        btn_eli = (Button) findViewById(R.id.btn_eliminar);

        // 3.- Declarar bundle que captura los datos que vienen viajando de otra Activity
        Bundle datos_capturados=getIntent().getExtras();

        if(datos_capturados!=null)
        {
            idusu_capturado=datos_capturados.getInt("Id");
            nombre_capturado=datos_capturados.getString("Nombre");
            apellio_capturado=datos_capturados.getString("Apellido");
        }

        id.setText(Integer.toString(idusu_capturado));
        nombre.setText(nombre_capturado);
        apellido.setText(apellio_capturado);

        // Agregar funcionalidad a los botones
        btn_mod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modificar(idusu_capturado,nombre.getText().toString(),apellido.getText().toString());
                onBackPressed();
            }
        });

        btn_eli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eliminar(idusu_capturado);
                onBackPressed();
            }
        });
    }

    // Querys para modificar o eliminar registro de usuario
    private void modificar(int Id, String Nombre, String Apellido)
    {
        ConexionDbHelper helper=new ConexionDbHelper(this,"APPSQLITE", null,1);
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql="UPDATE USUARIOS SET NOMBRE='"+Nombre+"', APELLIDO='"+Apellido+"'WHERE ID="+Id;
        db.execSQL(sql);
        db.close();
    }
    private void eliminar(int Id)
    {
        ConexionDbHelper helper=new ConexionDbHelper(this,"APPSQLITE", null,1);
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql="DELETE FROM USUARIOS WHERE ID="+Id;
        db.execSQL(sql);
        db.close();
    }
}