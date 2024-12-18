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

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FarolasFragment extends Fragment {

    private ImageView imageView;
    private Spinner spinner;
    private Button actionButton;
    private int[] imageResIds = {R.drawable.poste1, R.drawable.poste2, R.drawable.poste3};
    private String[] imageOptions = {"Farola Central", "Farola del Norte", "Farola del Sur"};
    private boolean isImageFixed = false;
    private int selectedPosition = 0;
    private TextView welcomeTextView;

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

        // Cargar nombre del usuario desde Firebase
        loadUserNameFromFirebase();

        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_weather);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Datos para RecyclerView
        List<WeatherData> weatherData = new ArrayList<>();
        weatherData.add(new WeatherData("Emergencia", "1", R.drawable.humidity));
        weatherData.add(new WeatherData("Gas", "0", R.drawable.precipitation));
        weatherData.add(new WeatherData("Humedad", "15%", R.drawable.wind));
        weatherData.add(new WeatherData("Temperatura", "10 ºC", R.drawable.precipitation));

        WeatherAdapter adapter2 = new WeatherAdapter(weatherData);
        recyclerView.setAdapter(adapter2);

        // Configuración del Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, imageOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Manejo de selección en el Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isImageFixed) {
                    imageView.setImageResource(imageResIds[position]);
                    selectedPosition = position;
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


}

