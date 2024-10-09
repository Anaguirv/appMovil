package com.example.login;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Registro extends AppCompatActivity {

    // 1.- Atributos
    //Declaración de variables
    private EditText nombreRegistro, apellidoRegistro, usuarioRegistro, nacimientoRegistro,
            claveRegistro, claveRepetirRegistro;
    private Button btnRegistrar;
    private Calendar calendario; // Para manejar la fecha seleccionada


    // Expresiones regulares
    private String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private String passwordPattern = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!_-])(?=\\S+$).{8,}$";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 2. Crear relación entre capa vista y controlador
        nombreRegistro=(EditText) findViewById(R.id.nombreRegistro);
        apellidoRegistro=(EditText)findViewById(R.id.apellidoRegistro);
        usuarioRegistro=(EditText) findViewById(R.id.usuarioRegistro);
        nacimientoRegistro=(EditText) findViewById(R.id.nacimientoRegistro);
        claveRegistro=(EditText) findViewById(R.id.claveRegistro);
        claveRepetirRegistro=(EditText) findViewById(R.id.claveRepetirRegistro);
        btnRegistrar=(Button) findViewById(R.id.btnRegistrar);

        // Inicializar el calendario
        calendario = Calendar.getInstance();

        // Configurar DatePickerDialog
        nacimientoRegistro.setOnClickListener(v -> mostrarDatePickerDialog());

        // Acción del botón Registrar
        btnRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    // Método para mostrar el DatePickerDialog
    private void mostrarDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    // Actualizar el campo de texto con la fecha seleccionada
                    calendario.set(Calendar.YEAR, year);
                    calendario.set(Calendar.MONTH, monthOfYear);
                    calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    actualizarCampoFecha();
                },
                // Valores iniciales del DatePicker (año, mes, día)
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
        );

        // Mostrar el DatePickerDialog
        datePickerDialog.show();
    }

    // Método para actualizar el EditText con la fecha seleccionada
    private void actualizarCampoFecha() {
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        nacimientoRegistro.setText(formatoFecha.format(calendario.getTime()));
    }

    // 3. Método para registrar usuario
    private void registrarUsuario() {
        // 4.Tomar datos ingresados en la vista con método get()
        String nombre = nombreRegistro.getText().toString();
        String apellido = apellidoRegistro.getText().toString();
        String usuario = usuarioRegistro.getText().toString();
        String nacimiento = nacimientoRegistro.getText().toString();
        String clave = claveRegistro.getText().toString();
        String claveRepetir = claveRepetirRegistro.getText().toString();

        //5. Validaciones usando patrones
        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(apellido) || TextUtils.isEmpty(usuario) ||
                TextUtils.isEmpty(nacimiento) || TextUtils.isEmpty(clave) ||
                TextUtils.isEmpty(claveRepetir)) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validarPatron(emailPattern, usuario)) {
            Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validarPatron(passwordPattern, clave)) {
            Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial", Toast.LENGTH_LONG).show();
            return;
        }

        if (!clave.equals(claveRepetir)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validarFechaNacimiento(nacimiento)) {
            Toast.makeText(this, "Fecha de nacimiento inválida. Debes ser mayor de edad y no puedes haber nacido en el futuro.", Toast.LENGTH_LONG).show();
            return;
        }

        // 6. Si pasa todas las validaciones se guardaran los datos en la base de datos
        guardar(nombre, apellido, usuario, clave);
        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_LONG).show();

        // 7. Redirigir a MainActivity
        Intent intent = new Intent(Registro.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Método genérico para validar patrones
    private boolean validarPatron(String patron, String variable) {
        Pattern pattern = Pattern.compile(patron);
        Matcher matcher = pattern.matcher(variable);
        return matcher.matches();
    }

    // Método para validar la fecha de nacimiento
    private boolean validarFechaNacimiento(String fechaNacimiento) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false); // No permitir fechas inválidas
        try {
            // Parsear la fecha ingresada
            Date fechaNac = dateFormat.parse(fechaNacimiento);
            Calendar fechaNacimientoCal = Calendar.getInstance();
            fechaNacimientoCal.setTime(fechaNac);

            // Obtener la fecha actual
            Calendar fechaActual = Calendar.getInstance();

            // Verificar si la fecha ingresada es en el futuro
            if (fechaNacimientoCal.after(fechaActual)) {
                return false;
            }

            // Verificar si el usuario tiene al menos 18 años
            fechaNacimientoCal.add(Calendar.YEAR, 18); // Sumar 18 años a la fecha de nacimiento
            return !fechaNacimientoCal.after(fechaActual); // Comprobar si después de sumar 18 años está antes o en la fecha actual
        } catch (ParseException e) {
            // Si la fecha ingresada no es válida (formato incorrecto)
            return false;
        }
    }

    //Función para guardar los datos ingresados por el usuario
    public void guardar(String nom, String ape, String mai, String cla)
    {
        ConexionDbHelper helper = new ConexionDbHelper(this, "APPSQLITE", null, 1);
        SQLiteDatabase db = helper.getReadableDatabase();
        try{
            ContentValues datos=new ContentValues();
            datos.put("Nombre",nom);
            datos.put("Apellido",ape);
            datos.put("Email",mai);
            datos.put("Clave",cla);
            db.insert("USUARIOS",null,datos);
            Toast.makeText(this,"Datos Ingresados Sin Problemas",
                    Toast.LENGTH_LONG).show();
        }catch (Exception e){
            Toast.makeText(this,"Error"+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}