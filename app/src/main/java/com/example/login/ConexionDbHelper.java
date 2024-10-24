package com.example.login;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class ConexionDbHelper extends SQLiteOpenHelper {

    // SQL para crear la tabla USUARIOS
    String sqlUsuarios = "CREATE TABLE USUARIOS (ID INTEGER PRIMARY KEY, NOMBRE TEXT, APELLIDO TEXT, EMAIL TEXT, CLAVE TEXT)";

    // SQL para crear la tabla de mediciones
    String sqlMediciones_art6 = "CREATE TABLE MEDICIONES_ART6 (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "flujo_luminoso TEXT," +
            "valor_flujo_luminoso REAL," +
            "emision_reflexion TEXT," +
            "valor_reflexion REAL," +
            "temperatura_color TEXT," +
            "valor_temperatura REAL," +
            "limite_horario TEXT," +
            "valor_horario REAL," +
            "observaciones TEXT" +
            ")";

    public ConexionDbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Crear las tablas USUARIOS y mediciones
        sqLiteDatabase.execSQL(sqlUsuarios);
        sqLiteDatabase.execSQL(sqlMediciones_art6);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Actualizar las tablas si hay cambios en la estructura
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS USUARIOS");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS MEDICIONES_ART6");
        onCreate(sqLiteDatabase);
    }
}
