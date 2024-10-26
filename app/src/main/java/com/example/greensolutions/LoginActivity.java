package com.example.greensolutions;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;


    // Firestore and Firebase Auth
    public static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        /*login();*/


        TextView botonregistrar = findViewById(R.id.botonregistrar);

        // Redirigir al registro cuando se haga clic en el enlace
        botonregistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Initialize UI components
        EditText editTextEmail = findViewById(R.id.correologin);
        EditText editTextPassword = findViewById(R.id.contrasenalogin);
        Button buttonLogin = findViewById(R.id.botonlogin);
        Button buttonGoogleSignIn = findViewById(R.id.botonlogingoogle); // Ensure this button exists in your layout

        // Set up Login Button Click Listener
        buttonLogin.setOnClickListener(v -> handleEmailPasswordLogin(editTextEmail, editTextPassword));

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();


        // Configurar el botón de inicio de sesión de Google
        /*findViewById(R.id.botonlogingoogle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });*/

        // Verificar si hay un usuario autenticado en SharedPreferences
        String userEmail = getSharedPreferences("LoginPrefs", MODE_PRIVATE).getString("userEmail", null);
        if (userEmail != null) {
            // Redirigir a MainActivity si el usuario ya ha iniciado sesión
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

    }

    // Method to handle Email/Password Login
    private void handleEmailPasswordLogin(EditText editTextEmail, EditText editTextPassword) {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email requerido");
            editTextEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Contraseña requerida");
            editTextPassword.requestFocus();
            return;
        }

        // Consulta Firestore para encontrar el usuario por email
        db.collection("usuarios")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (!documents.isEmpty()) {
                            DocumentSnapshot document = documents.getDocuments().get(0);
                            String storedPassword = document.getString("password");

                            if (password.equals(storedPassword)) {
                                // Guardar el email en SharedPreferences para mantener la sesión
                                getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                                        .edit()
                                        .putString("userEmail", email)
                                        .apply();

                                // Redirigir a MainActivity
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Error al verificar el usuario", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    /*private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Resultado del intento de iniciar sesión
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Inicio de sesión exitoso, autenticar con Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Error al iniciar sesión
                Log.w("GoogleSignIn", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Inicio de sesión exitoso
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("FirebaseAuth", "signInWithCredential:success");
                    } else {
                        // Error en el inicio de sesión
                        Log.w("FirebaseAuth", "signInWithCredential:failure", task.getException());
                    }
                });
    }*/

}


