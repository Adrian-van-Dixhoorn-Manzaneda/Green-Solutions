package com.example.greensolutions;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {


    FrameLayout frameLayout;
    Toolbar toolbar;
    FirebaseAuth firebaseAuth;

    private FirebaseFirestore firestore;

    private static final String TAG = "MainActivity";

    private static final String CHANNEL_ID = "Smart Nature";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;

    private boolean lastStatusNormal = false;  // Bandera para el último estado normal
    private boolean isNotificationScheduled = false;  // Bandera para evitar mostrar notificación repetida



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

        // Inicializa Firestore
        firestore = FirebaseFirestore.getInstance();


        // Configura un cronómetro (Handler)
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                requestNotificationPermission();
                checkAndShowNotification();

                // Repetir la tarea cada 10 segundos (ajusta el tiempo según tus necesidades)
                handler.postDelayed(this, 10000);
            }
        };

        // Inicia la ejecución de la tarea en el cronómetro
        handler.post(runnable);


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

    private void getFarolaSensorValues(String routeId, HomeFragment.SensorDataCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.i(TAG, "Ruta ID: " + routeId);

        db.collection("Rutas")
                .document(routeId)
                .collection("Farolas")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Long> sensorResults = new ArrayList<>(Arrays.asList(0L, 0L, 0L, 0L));
                        // Índices: 0 = Emergencias, 1 = Gas, 2 = Humedad, 3 = Temperatura

                        int farolaCount = 0;

                        for (QueryDocumentSnapshot farolaDoc : task.getResult()) {
                            Long emergencias = farolaDoc.getLong("emergencias");
                            Long gas = farolaDoc.getLong("gas");
                            Long humedad = farolaDoc.getLong("humedad");
                            Long temperatura = farolaDoc.getLong("temperatura");

                            // Sumar valores (verifica nulos)
                            sensorResults.set(0, sensorResults.get(0) + (emergencias != null ? emergencias : 0)); // Emergencias
                            if (gas != null && gas == 1) sensorResults.set(1, 1L); // Gas
                            sensorResults.set(2, sensorResults.get(2) + (humedad != null ? humedad : 0)); // Humedad
                            sensorResults.set(3, sensorResults.get(3) + (temperatura != null ? temperatura : 0)); // Temperatura

                            farolaCount++;
                        }

                        // Calcular promedios para humedad y temperatura
                        if (farolaCount > 0) {
                            sensorResults.set(2, sensorResults.get(2) / farolaCount); // Promedio Humedad
                            sensorResults.set(3, sensorResults.get(3) / farolaCount); // Promedio Temperatura
                        }

                        // Mostrar resultados en Logcat
                        Log.i(TAG, "Resultados de Sensores:");
                        Log.i(TAG, "Emergencias: " + sensorResults.get(0));
                        Log.i(TAG, "Gas: " + sensorResults.get(1));
                        Log.i(TAG, "Humedad (promedio): " + sensorResults.get(2));
                        Log.i(TAG, "Temperatura (promedio): " + sensorResults.get(3));

                        // Llamar al callback con los resultados
                        callback.onSensorDataReady(sensorResults);
                    } else {
                        Log.w(TAG, "Error al obtener farolas de la ruta " + routeId, task.getException());
                    }
                });
    }



    //NOTIFICACIONES Y POPUPS


    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void checkAndShowNotification() {
        String routeId = "Ruta_1";

        getFarolaSensorValues(routeId, sensorResults -> {
            Long gas = sensorResults.get(1);
            Long emergencia = sensorResults.get(0);

            if (gas != 0 || emergencia != 0) {
                lastStatusNormal = false;
                if (!isNotificationScheduled) {
                    String routeName = "Ruta 1";
                    String message = gas != 0 ? "Fuego Detectado en " + routeName :
                            "Emergencia Detectada en " + routeName;

                    int alertColor = gas != 0 ? Color.RED : Color.RED; // Siempre rojo para alertas
                    showNotification(message, gas != 0 ? 1 : 2);
                    showPopup(message, gas != 0 ? 0 : 200, alertColor);

                    isNotificationScheduled = true;
                    new Handler().postDelayed(() -> isNotificationScheduled = false, 20000);
                }
            } else {
                if (!lastStatusNormal) {
                    String message = "Todo en orden en la Ruta 1";
                    int alertColor = Color.GREEN; // Verde para estado normal
                    showPopup(message, 0, alertColor);
                    lastStatusNormal = true;
                }
            }

        });
    }

    private void showNotification(String message, int notificationId) {
        createNotificationChannel();

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)  // Icono de alerta
                .setContentTitle("Alerta")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(notificationId, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Canal de alertas";
            String description = "Canal para alertas de seguridad";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showPopup(String message, int yOffset, int alertColor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View customView = inflater.inflate(R.layout.popup_alert, null);

        TextView messageView = customView.findViewById(R.id.message);
        ImageButton closeButton = customView.findViewById(R.id.close_button);
        View alertBackground = customView.findViewById(R.id.alert_background);

        messageView.setText(message);

        // Cambia el color de fondo según el parámetro alertColor
        alertBackground.setBackgroundColor(alertColor);

        // Crea el diálogo primero
        AlertDialog dialog = builder.setView(customView).create();

        // Configura el botón de cierre
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Establece la posición y muestra el diálogo
        if (dialog.getWindow() != null) {
            dialog.getWindow().setGravity(Gravity.CENTER);
            dialog.getWindow().getAttributes().y = yOffset;
        }

        dialog.show();
    }


}
