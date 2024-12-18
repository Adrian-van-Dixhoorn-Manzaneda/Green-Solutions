package com.example.greensolutions;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText etCorreo, etContrasenya, etNombre;
    private ProgressDialog dialogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Referencia a los campos del formulario
        etCorreo = findViewById(R.id.correo_register);
        etContrasenya = findViewById(R.id.contrasenya_register);
        etNombre = findViewById(R.id.nombre_register);
        dialogo = new ProgressDialog(this);
        dialogo.setTitle("Registrando usuario");
        dialogo.setMessage("Por favor espere...");
    }

    public void registroFirestore(View v) {
        // Obtener los valores de los campos
        String correo = etCorreo.getText().toString();
        String contraseña = etContrasenya.getText().toString();
        String nombre = etNombre.getText().toString();

        // Verificar los campos antes de intentar registrar al usuario
        if (verificaCampos(correo, contraseña, nombre)) {
            dialogo.show();  // Mostrar diálogo de carga

            // Crear un mapa con los datos del usuario
            Map<String, Object> usuario = new HashMap<>();
            usuario.put("email", correo);
            usuario.put("nombre", nombre);
            usuario.put("password", contraseña);  // (Nota: Almacenar contraseñas en texto plano no es seguro)

            // Agregar usuario a la colección "usuarios" en Firestore
            db.collection("usuarios").add(usuario)
                    .addOnSuccessListener(documentReference -> {
                        dialogo.dismiss();  // Ocultar diálogo de carga
                        Toast.makeText(RegisterActivity.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();

                        // Opcionalmente redirigir a otra actividad después del registro
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();  // Cerrar la actividad de registro
                    })
                    .addOnFailureListener(e -> {
                        dialogo.dismiss();  // Ocultar diálogo en caso de error
                        Toast.makeText(RegisterActivity.this, "Error al registrar usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private boolean verificaCampos(String correo, String contraseña, String nombre) {
        // Validar los campos de correo, contraseña y nombre
        if (nombre.isEmpty()) {
            etNombre.setError("Introduce un nombre");
            return false;
        }

        if (correo.isEmpty() || !correo.matches(".+@.+[.].+")) {
            etCorreo.setError("Correo no válido");
            return false;
        }

        if (contraseña.isEmpty() || contraseña.length() < 6) {
            etContrasenya.setError("La contraseña debe tener al menos 6 caracteres");
            return false;
        }

        return true;
    }
}


