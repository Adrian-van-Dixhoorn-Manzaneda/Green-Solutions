package com.example.greensolutions;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private ImageView imageView;
    private Spinner spinner;
    private Button actionButton;
    private int[] imageResIds = {R.drawable.poste1, R.drawable.poste2, R.drawable.poste3};
    private String[] imageOptions = {"Ruta_1", "luigi2", "luigi3"};
    //private String[] imageOptions = new String[10];
    private boolean isImageFixed = false;
    private TextView welcomeTextView;
    private int selectedPosition = 0;
    private static final String TAG = "HomeFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Inicialización de vistas
        spinner = rootView.findViewById(R.id.spinner);
        imageView = rootView.findViewById(R.id.imageView);
        actionButton = rootView.findViewById(R.id.selectButton);
        welcomeTextView = rootView.findViewById(R.id.tv_welcome);

        //updateImageOptionsWithRouteIds(imageOptions);
        // Cargar nombre del usuario desde Firebase
        loadUserNameFromFirebase();





        // Configuración del Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, imageOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Manejo de selección en el Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                updateImageOptionsWithRouteIds();

                if (!isImageFixed) {
                    imageView.setImageResource(imageResIds[position]);
                    selectedPosition = position;

                    // Obtener la ruta seleccionada del spinner
                    String selectedRouteId = imageOptions[position];

                    getFarolaSensorValues(selectedRouteId, sensorResults -> {
                        // Este bloque se ejecutará cuando los datos estén listos
                        Log.i(TAG, "Resultados recibidos desde el callback:");
                        Log.i(TAG, "Emergencias: " + sensorResults.get(0));
                        Log.i(TAG, "Gas: " + sensorResults.get(1));
                        Log.i(TAG, "Humedad (promedio): " + sensorResults.get(2));
                        Log.i(TAG, "Temperatura (promedio): " + sensorResults.get(3));


                        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_weather);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


                        // Datos para RecyclerView
                        List<RutasData> rutasData = new ArrayList<>();
                        rutasData.add(new RutasData("Emergencia", sensorResults.get(0).toString(), R.drawable.humidity));
                        rutasData.add(new RutasData("Gas", sensorResults.get(1).toString(), R.drawable.precipitation));
                        rutasData.add(new RutasData("Humedad", sensorResults.get(2).toString()+"km/h", R.drawable.wind));
                        rutasData.add(new RutasData("Temperatura", sensorResults.get(3).toString()+"%", R.drawable.precipitation));

                        RutasAdapter adapter2 = new RutasAdapter(rutasData);
                        recyclerView.setAdapter(adapter2);


                    });
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });











        // Manejo del botón
        actionButton.setOnClickListener(v -> {
            if (!isImageFixed) {
                // Fija la imagen seleccionada y cambia el botón a "Quitar"
                isImageFixed = true;
                spinner.setEnabled(false);
                actionButton.setText("Quitar");

                // Redirigir al fragmento de Rutas
                Fragment farolasFragment = new FarolasFragment();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.framelayout, farolasFragment)
                        .addToBackStack(null) // Opcional: para permitir volver atrás
                        .commit();
            } else {
                // Restaura el estado inicial
                isImageFixed = false;
                spinner.setEnabled(true);
                actionButton.setText("Seleccionar");
                spinner.setSelection(0); // Resetea el Spinner a la primera opción
                imageView.setImageResource(R.drawable.ic_launcher_foreground); // Imagen por defecto
            }
        });



        return rootView;
    }


    /**
     * Obtiene el nombre del usuario autenticado con Google usando FirebaseAuth.
     * @return El nombre del usuario autenticado con Google, o null si no está disponible.
     */
    private String getGoogleUserName() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            Log.d(TAG, "Usuario autenticado con Google: " + firebaseAuth.getCurrentUser().getDisplayName());
            return firebaseAuth.getCurrentUser().getDisplayName(); // Nombre del usuario de Google
        }
        Log.d(TAG, "No hay un usuario autenticado con Google");
        return null;
    }

    /**
     * Carga el nombre del usuario desde Firebase usando su UID.
     */
    private void loadUserNameFromFirebase() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            String userUID = firebaseAuth.getCurrentUser().getUid();
            DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                    .getReference("usuarios").child(userUID);

            // Asignar nombre predeterminado desde Google
            String googleUserName = getGoogleUserName();
            welcomeTextView.setText("Bienvenido, " + (googleUserName != null ? googleUserName : "Usuario Desconocido"));

            // Consultar Firebase para el nombre del usuario
            databaseRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot != null && snapshot.child("nombre").exists()) {
                        String userName = snapshot.child("nombre").getValue(String.class);
                        if (userName != null) {
                            welcomeTextView.setText("Bienvenido, " + userName);
                        }
                    }
                } else {
                    Log.e(TAG, "Error al consultar Firebase: " + task.getException().getMessage());
                }
            });
        } else {
            welcomeTextView.setText("Por favor, inicia sesión para continuar");
        }
    }




    private void updateImageOptionsWithRouteIds() {
        // Obtener instancia de Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Consultar la colección de rutas
        db.collection("Rutas")  // Suponiendo que las rutas están en la colección "Rutas"
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            // Lista para almacenar los IDs de las rutas
                            List<String> routeIds = new ArrayList<>();

                            // Recorrer los documentos de la colección "Rutas"
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                // Obtener el ID de cada ruta (el ID está asociado al documento)
                                String routeId = document.getId();
                                // Añadir el ID al listado
                                routeIds.add(routeId);
                            }

                            // Ahora actualizar imageOptions con los primeros 3 IDs (si hay suficientes)
                            for (int i = 0; i < imageOptions.length; i++) {
                                if (i < routeIds.size()) {
                                    // Reemplazar en imageOptions con los IDs de las rutas
                                    imageOptions[i] = routeIds.get(i);
                                }
                            }

                            // Aquí imageOptions está actualizado con los IDs de las rutas.
                            Log.i(TAG, "imageOptions actualizado con los IDs de las rutas: " + Arrays.toString(imageOptions));
                        }
                    } else {
                        Log.w(TAG, "Error al obtener las rutas", task.getException());
                    }
                });
    }


    public interface SensorDataCallback {
        void onSensorDataReady(List<Long> sensorResults);
    }

    private void getFarolaSensorValues(String routeId, SensorDataCallback callback) {
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
                            Long emergencias = farolaDoc.getLong("Emergencias");
                            Long gas = farolaDoc.getLong("Gas");
                            Long humedad = farolaDoc.getLong("Humedad");
                            Long temperatura = farolaDoc.getLong("Temperatura");

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







}

