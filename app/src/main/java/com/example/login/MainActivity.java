package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    // 1. Atributos
    // visibilidad - tipo/clase - nombre = valor(opcional)
    private TextView txtmsj, txtregistro, txtolvide;
    private Button btningresar;
    private EditText txtuser, txtclave;
    private String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    // Métodos
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 2. Crear relación entre parte gráfica (capa Vista) y parte lógica (capa Controlador)
        // Casteo -> (TextView) indica a la variable declarada como debe comportarse (debe ser del mismo tipo que el objeto en la vista)
        txtmsj=(TextView) findViewById(R.id.txtmensaje);
        btningresar=(Button) findViewById(R.id.btningresar);
        txtregistro=(TextView) findViewById(R.id.txtregistro);
        txtolvide=(TextView) findViewById(R.id.txtolvide);
        txtuser=(EditText) findViewById(R.id.txtuser);
        txtclave=(EditText) findViewById(R.id.txtclave);

        txtmsj.setText("Bienvenido a la Aplicación SFDS1");

        // 3. Setiar o configurar el escuchador del evento "clic" al botón "ingresar"
        btningresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 4. Capturar los textos de usuario y contraseña
                String usuario = txtuser.getText().toString().trim();
                String clave = txtclave.getText().toString().trim();

                // 5. Validar que el campo usuario (correo) no esté vacío
                if (usuario.isEmpty()) {
                    txtuser.setError("Este campo no puede estar vacío");
                    Toast.makeText(MainActivity.this, "Debe ingresar un usuario (email)", Toast.LENGTH_LONG).show();
                }
                // 6. Validar que el formato del correo sea correcto
                else if (!validarPatron(emailPattern, usuario)) {
                    txtuser.setError("Por favor, ingresa un email válido");
                    Toast.makeText(MainActivity.this, "El email ingresado no cumple con el formato", Toast.LENGTH_LONG).show();
                }
                // 7. Validar que el campo contraseña no esté vacío
                else if (clave.isEmpty()) {
                    txtclave.setError("Este campo no puede estar vacío");
                    Toast.makeText(MainActivity.this, "Debe ingresar una contraseña", Toast.LENGTH_LONG).show();
                }
                // 8. Si las validaciones son exitosas, iniciar la Activity "Dashboard"
                else {
                    Intent ventanaingresar = new Intent(MainActivity.this, Dashboard.class);
                    startActivity(ventanaingresar);
                }
            }
        });

        txtregistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ventanaRegistrar=new Intent(MainActivity.this, Registro.class);
                startActivities(new Intent[]{ventanaRegistrar});
            }
        });

        txtolvide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ventanaRecuperar=new Intent(MainActivity.this, RecuperarClave.class);
                startActivities(new Intent[]{ventanaRecuperar});
            }
        });
    }

    /*
    // Método para validar el email con una expresión regular
    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    */

    // Método para validaciones de inputs de usuario con las expresiones regulares
    private boolean validarPatron(String patron, String variable) {
        Pattern pattern = Pattern.compile(patron);
        Matcher matcher = pattern.matcher(variable);
        return matcher.matches();
    }
}