package com.example.greensolutions;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FrameLayout frameLayout;
    Toolbar toolbar;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar Firebase
        /**
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();

        // Verificar si el usuario ha iniciado sesión
        checkUserSession();
         **/
        setContentView(R.layout.activity_main);
        frameLayout = findViewById(R.id.framelayout);
        toolbar = findViewById(R.id.toolbar);

        // Configura la Toolbar como ActionBar
        setSupportActionBar(toolbar);

        // Configura la navegación inferior
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                if (item.getItemId() == R.id.nav_home) {
                    fragment = new HomeFragment(); // Fragmento de inicio
                } else if (item.getItemId() == R.id.nav_profile) {
                    fragment = new PerfilFragment(); // Fragmento de perfil
                }
                else if (item.getItemId() == R.id.nav_publish)
                {
                    fragment = new PublicarFragment();
                }
                return loadFragment(fragment);
            }
        });

        // Carga el fragmento por defecto (HomeFragment)
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home); // Seleccionar "Home"
            loadFragment(new HomeFragment());
        }
    }

    // Método para verificar si el usuario está autenticado
    private void checkUserSession() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // Usuario no autenticado, redirigir a la pantalla de inicio de sesión
            Log.d("MainActivity", "No hay usuario autenticado. Redirigiendo al Login...");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpiar la pila de actividades
            startActivity(intent);
        } else {
            // Usuario autenticado
            Log.d("MainActivity", "Usuario autenticado: " + currentUser.getEmail());
        }
    }

    // Método para cargar un fragmento en el FrameLayout
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.framelayout, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    // Configuración del menú de la Toolbar
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu); // Usa el menú de la Toolbar
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.acercaDe) {
            lanzarAcercaDe(); // Abre la actividad de "Acerca de"
            return true;
        }
        /**if (item.getItemId() == R.id.action_close_session) {
            closeSession(); // Cierra la sesión del usuario
            return true;
        }**/
        return super.onOptionsItemSelected(item);
    }

    // Método para lanzar la actividad "Acerca de"
    public void lanzarAcercaDe() {
        Intent intent = new Intent(this, AcercaDeActivity.class);
        startActivity(intent);
    }

    // Método para cerrar sesión
    private void closeSession() {
        // Limpiar cualquier estado de inicio de sesión guardado (opcional)
        getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                .edit()
                .remove("userEmail")
                .apply();

        // Cerrar sesión en Firebase Auth
        firebaseAuth.signOut();

        // Redirigir a la pantalla de inicio de sesión
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpiar la pila de actividades
        startActivity(intent);

        // Mostrar un mensaje de confirmación
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
    }
}
