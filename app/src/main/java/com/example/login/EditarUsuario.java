package com.example.login;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditarUsuario extends AppCompatActivity {

    private EditText nombreEditar, apellidoPaternoEditar, apellidoMaternoEditar, correoEditar, nacimientoEditar;
    private Button btnGuardarCambios, btnEliminarUsuario;
    private Calendar calendario;
    private boolean fechaModificada = false;
    private static final String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_usuario);

        nombreEditar = findViewById(R.id.nombreEditar);
        apellidoPaternoEditar = findViewById(R.id.apellidoPaternoEditar);
        apellidoMaternoEditar = findViewById(R.id.apellidoMaternoEditar);
        correoEditar = findViewById(R.id.correoEditar);
        nacimientoEditar = findViewById(R.id.nacimientoEditar);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);
        btnEliminarUsuario = findViewById(R.id.btnEliminarUsuario);

        calendario = Calendar.getInstance();

        nacimientoEditar.setOnClickListener(v -> mostrarDatePickerDialog());

        String idUsuario = getIntent().getStringExtra("id_usuario");
        String nombre = getIntent().getStringExtra("nombre");
        String apellidoPaterno = getIntent().getStringExtra("apellidoPaterno");
        String apellidoMaterno = getIntent().getStringExtra("apellidoMaterno");
        String correo = getIntent().getStringExtra("correo");
        String fechaNacimiento = getIntent().getStringExtra("fechaNacimiento");

        nombreEditar.setText(nombre != null ? nombre : "");
        apellidoPaternoEditar.setText(apellidoPaterno != null ? apellidoPaterno : "");
        apellidoMaternoEditar.setText(apellidoMaterno != null ? apellidoMaterno : "");
        correoEditar.setText(correo != null ? correo : "");
        nacimientoEditar.setText(fechaNacimiento != null ? fechaNacimiento : "");

        if (fechaNacimiento != null) {
            try {
                Date fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaNacimiento);
                calendario.setTime(fecha);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        btnGuardarCambios.setOnClickListener(v -> validarYGuardarCambios(idUsuario));
        btnEliminarUsuario.setOnClickListener(v -> eliminarUsuario(idUsuario));
    }

    private void mostrarDatePickerDialog() {
        int year, month, day;

        if (!TextUtils.isEmpty(nacimientoEditar.getText().toString())) {
            try {
                Date fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(nacimientoEditar.getText().toString());
                calendario.setTime(fecha);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        year = calendario.get(Calendar.YEAR);
        month = calendario.get(Calendar.MONTH);
        day = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendario.set(Calendar.YEAR, selectedYear);
                    calendario.set(Calendar.MONTH, selectedMonth);
                    calendario.set(Calendar.DAY_OF_MONTH, selectedDay);
                    actualizarCampoFecha();
                    fechaModificada = true;
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void actualizarCampoFecha() {
        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        nacimientoEditar.setText(formatoFecha.format(calendario.getTime()));
    }


    private void validarYGuardarCambios(String idUsuario) {
        String nombre = nombreEditar.getText().toString().trim();
        String apellidoPaterno = apellidoPaternoEditar.getText().toString().trim();
        String apellidoMaterno = apellidoMaternoEditar.getText().toString().trim();
        String correo = correoEditar.getText().toString().trim();
        String fechaNacimiento = nacimientoEditar.getText().toString().trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(apellidoPaterno) || TextUtils.isEmpty(apellidoMaterno) ||
                TextUtils.isEmpty(correo) || TextUtils.isEmpty(fechaNacimiento)) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validarPatron(emailPattern, correo)) {
            Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fechaModificada && !validarFechaNacimiento(fechaNacimiento)) {
            Toast.makeText(this, "Fecha de nacimiento inválida. Debes ser mayor de edad y no puedes haber nacido en el futuro.", Toast.LENGTH_LONG).show();
            return;
        }

        guardarCambios(idUsuario, nombre, apellidoPaterno, apellidoMaterno, correo, fechaNacimiento);
    }

    private boolean validarPatron(String patron, String variable) {
        Pattern pattern = Pattern.compile(patron);
        Matcher matcher = pattern.matcher(variable);
        return matcher.matches();
    }

    private boolean validarFechaNacimiento(String fechaNacimiento) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFormat.setLenient(false);
        try {
            Date fechaNac = dateFormat.parse(fechaNacimiento);
            Calendar fechaNacimientoCal = Calendar.getInstance();
            fechaNacimientoCal.setTime(fechaNac);

            Calendar fechaActual = Calendar.getInstance();

            // Validar que la fecha no sea en el futuro
            if (fechaNacimientoCal.after(fechaActual)) {
                return false;
            }

            // Comprobar si el usuario tiene al menos 18 años
            fechaNacimientoCal.add(Calendar.YEAR, 18);
            return !fechaNacimientoCal.after(fechaActual);
        } catch (ParseException e) {
            return false;
        }
    }


    private void guardarCambios(String idUsuario, String nombre, String apellidoPaterno, String apellidoMaterno, String correo, String fechaNacimiento) {
        String url = "http://52.71.115.13/editarUsuario.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(EditarUsuario.this, response, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                },
                error -> Toast.makeText(EditarUsuario.this, "Error al guardar cambios", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_usuario", idUsuario);
                params.put("nombre", nombre);
                params.put("apellidoPaterno", apellidoPaterno);
                params.put("apellidoMaterno", apellidoMaterno);
                params.put("correo", correo);
                params.put("fechaNacimiento", fechaNacimiento);
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void eliminarUsuario(String idUsuario) {
        String url = "http://52.71.115.13/eliminarUsuario.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(EditarUsuario.this, response, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                },
                error -> Toast.makeText(EditarUsuario.this, "Error al eliminar usuario", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_usuario", idUsuario);
                return params;
            }
        };

        queue.add(stringRequest);
    }
}
