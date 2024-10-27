package com.example.login;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
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

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistroUsuario extends AppCompatActivity {

    // Atributos de vista
    private EditText nombreRegistro, apellidoRegistro, apellidoMaternoRegistro, usuarioRegistro, nacimientoRegistro,
            claveRegistro, claveRepetirRegistro;
    private Button btnRegistrar;
    private Calendar calendario;

    // Patrones de validación para correo y contraseña
    private String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private String passwordPattern = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!_-])(?=\\S+$).{8,}$";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Inicialización de los elementos de la interfaz
        nombreRegistro = findViewById(R.id.nombreRegistro);
        apellidoRegistro = findViewById(R.id.apellidoRegistro);
        apellidoMaternoRegistro = findViewById(R.id.apellidoMaternoRegistro);  // Nuevo campo para el apellido materno
        usuarioRegistro = findViewById(R.id.usuarioRegistro);
        nacimientoRegistro = findViewById(R.id.nacimientoRegistro);
        claveRegistro = findViewById(R.id.claveRegistro);
        claveRepetirRegistro = findViewById(R.id.claveRepetirRegistro);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        // Configuración de la fecha actual para el DatePicker
        calendario = Calendar.getInstance();

        // Configurar el campo de fecha de nacimiento para mostrar un DatePicker al hacer clic
        nacimientoRegistro.setOnClickListener(v -> mostrarDatePickerDialog());

        // Acción del botón Registrar
        btnRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    // Muestra un DatePickerDialog para seleccionar la fecha de nacimiento
    private void mostrarDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    calendario.set(Calendar.YEAR, year);
                    calendario.set(Calendar.MONTH, monthOfYear);
                    calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    actualizarCampoFecha();
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // Actualiza el campo de texto con la fecha seleccionada
    private void actualizarCampoFecha() {
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        nacimientoRegistro.setText(formatoFecha.format(calendario.getTime()));
    }

    // Realiza las validaciones de los datos ingresados y llama a enviarDatosServidor si todo es válido
    private void registrarUsuario() {
        String nombre = nombreRegistro.getText().toString();
        String apellido = apellidoRegistro.getText().toString();
        String apellidoMaterno = apellidoMaternoRegistro.getText().toString(); // Obtener apellido materno
        String usuario = usuarioRegistro.getText().toString();
        String nacimiento = nacimientoRegistro.getText().toString();
        String clave = claveRegistro.getText().toString();
        String claveRepetir = claveRepetirRegistro.getText().toString();

        // Validación de campos obligatorios
        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(apellido) || TextUtils.isEmpty(apellidoMaterno) ||
                TextUtils.isEmpty(usuario) || TextUtils.isEmpty(nacimiento) || TextUtils.isEmpty(clave) || TextUtils.isEmpty(claveRepetir)) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de correo electrónico
        if (!validarPatron(emailPattern, usuario)) {
            Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de contraseña
        if (!validarPatron(passwordPattern, clave)) {
            Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial", Toast.LENGTH_LONG).show();
            return;
        }

        // Comprobación de coincidencia de contraseñas
        if (!clave.equals(claveRepetir)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de fecha de nacimiento
        if (!validarFechaNacimiento(nacimiento)) {
            Toast.makeText(this, "Fecha de nacimiento inválida. Debes ser mayor de edad y no puedes haber nacido en el futuro.", Toast.LENGTH_LONG).show();
            return;
        }

        // Convierte la fecha a formato yyyy-MM-dd y envía los datos al servidor
        String fechaConvertida = convertirFecha(nacimiento);
        enviarDatosServidor(nombre, apellido, apellidoMaterno, usuario, fechaConvertida, clave);

        // Muestra un mensaje de éxito y redirige a la actividad principal
        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(RegistroUsuario.this, DashboardUsuario.class);
        startActivity(intent);
        finish();
    }

    // Valida si el texto coincide con el patrón de expresión regular
    private boolean validarPatron(String patron, String variable) {
        Pattern pattern = Pattern.compile(patron);
        Matcher matcher = pattern.matcher(variable);
        return matcher.matches();
    }

    // Valida que la fecha de nacimiento sea en el pasado y que el usuario sea mayor de edad
    private boolean validarFechaNacimiento(String fechaNacimiento) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        try {
            Date fechaNac = dateFormat.parse(fechaNacimiento);
            Calendar fechaNacimientoCal = Calendar.getInstance();
            fechaNacimientoCal.setTime(fechaNac);

            Calendar fechaActual = Calendar.getInstance();
            if (fechaNacimientoCal.after(fechaActual)) {
                return false;
            }

            fechaNacimientoCal.add(Calendar.YEAR, 18);
            return !fechaNacimientoCal.after(fechaActual);
        } catch (ParseException e) {
            return false;
        }
    }

    // Convierte la fecha de dd/MM/yyyy a yyyy-MM-dd
    private String convertirFecha(String fechaOriginal) {
        try {
            SimpleDateFormat formatoOriginal = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date fecha = formatoOriginal.parse(fechaOriginal);
            SimpleDateFormat formatoDestino = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return formatoDestino.format(fecha);
        } catch (ParseException e) {
            return "";  // Devuelve una cadena vacía en caso de error
        }
    }

    // Envía los datos al servidor mediante GET
    public void enviarDatosServidor(String nom, String ape, String ape2, String mai, String fech, String cla) {
        new Thread(() -> {
            try {
                // Construcción de la URL codificada
                String urlString = "http://52.71.115.13/inserUsuarios.php?n=" + URLEncoder.encode(nom, "UTF-8") +
                        "&aP=" + URLEncoder.encode(ape, "UTF-8") +
                        "&aM=" + URLEncoder.encode(ape2, "UTF-8") + // Usar ape2 para apellido materno dinámico
                        "&c=" + URLEncoder.encode(mai, "UTF-8") +
                        "&cla=" + URLEncoder.encode(cla, "UTF-8") +
                        "&fech=" + URLEncoder.encode(fech, "UTF-8");

                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "Datos enviados al servidor correctamente", Toast.LENGTH_LONG).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "Error en la conexión: " + responseCode, Toast.LENGTH_LONG).show();
                    });
                }
                urlConnection.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Error al enviar los datos", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}