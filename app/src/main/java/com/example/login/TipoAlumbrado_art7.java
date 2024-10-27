package com.example.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;

public class TipoAlumbrado_art7 extends AppCompatActivity {

    private EditText vlrMed1, vlrReflexion, vlrTemperatura, vlrObservacion;
    private Spinner spinnerMed1, spinnerTemperatura, spinnerHorario, spinner_valor_horario, Spinner_ValorEmisionConjunta;
    private Button btnRegistrarMedicion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tipo_alumbrado_art7);

        // Manejar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar campos
        spinnerMed1 = findViewById(R.id.spinner_med1);
        spinnerTemperatura = findViewById(R.id.spinner_temperatura);
        spinnerHorario = findViewById(R.id.spinner_horario);
        spinner_valor_horario = findViewById(R.id.spinner_valor_horario);
        Spinner_ValorEmisionConjunta = findViewById(R.id.spinner_valor_emision_conjunta);
        vlrMed1 = findViewById(R.id.vlr_med1);
        vlrReflexion = findViewById(R.id.vlr_reflexion);
        vlrTemperatura = findViewById(R.id.vlr_temperatura);
        vlrObservacion = findViewById(R.id.vlr_observacion);
        btnRegistrarMedicion = findViewById(R.id.btn_regsitrarMedicion);

        // Configurar todos los Spinners usando el método setupSpinner
        setupSpinner(spinnerMed1, R.array.limite_emision_intensidad_luminosa);
        setupSpinner(spinnerTemperatura, R.array.limite_temperatura_color_recinto);
        setupSpinner(spinnerHorario, R.array.limite_horario_alumbrado_deportivo);
        setupSpinner(spinner_valor_horario, R.array.opciones_cumple);
        setupSpinner(Spinner_ValorEmisionConjunta, R.array.opciones_cumple);

        // Acción al presionar el botón de registro
        btnRegistrarMedicion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarCampos()) {
                    // Proceder con la acción de guardado
                    Toast.makeText(TipoAlumbrado_art7.this, "Datos registrados correctamente", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Método auxiliar para configurar un Spinner.
     *
     * @param spinner El Spinner a configurar.
     * @param arrayResourceId El ID del recurso array que contiene las opciones.
     */
    private void setupSpinner(Spinner spinner, int arrayResourceId) {
        // Obtener las opciones del recurso array y convertirlas en una lista
        String[] opcionesArray = getResources().getStringArray(arrayResourceId);
        ArrayList<String> opcionesList = new ArrayList<>(Arrays.asList(opcionesArray));

        // Añadir la opción vacía al inicio de la lista
        opcionesList.add(0, "Seleccione una opción");

        // Crear un adaptador usando la lista de opciones y el diseño predeterminado de spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opcionesList);

        // Especificar el diseño que aparece cuando se despliega el spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Aplicar el adaptador al spinner
        spinner.setAdapter(adapter);
        spinner.setSelection(0, false);
    }

    /**
     * Método para validar los campos del formulario.
     *
     * @return true si todos los campos son válidos, false si hay errores.
     */
    private boolean validarCampos() {
        boolean valid = true;

        // Validar campos de texto
        if (TextUtils.isEmpty(vlrMed1.getText().toString())) {
            vlrMed1.setError("Este campo es obligatorio");
            valid = false;
        }

        if (TextUtils.isEmpty(vlrReflexion.getText().toString())) {
            vlrReflexion.setError("Este campo es obligatorio");
            valid = false;
        }

        if (TextUtils.isEmpty(vlrTemperatura.getText().toString())) {
            vlrTemperatura.setError("Este campo es obligatorio");
            valid = false;
        }
        if (spinner_valor_horario.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Debe seleccionar una opción en Cumple para Límite Horario", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        // No se valida vlrObservacion, ya que puede estar vacío

        // Validar spinners
        if (spinnerMed1.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Debe seleccionar una opción en Límite de Emisión de Intensidad Luminosa", Toast.LENGTH_SHORT).show();
            valid = false;
        }


        if (spinnerTemperatura.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Debe seleccionar una opción en Límites de Temperatura de Color Correlacionada", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (spinnerHorario.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Debe seleccionar una opción en Límite Horario", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }
}
