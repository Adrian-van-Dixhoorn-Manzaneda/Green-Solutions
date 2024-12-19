package com.example.greensolutions;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FarolasFragment extends Fragment {

    private ViewPager2 galleryViewPager;
    private Spinner farolaSpinner;
    private Button actionButton;
    private List<String> farolaIds = new ArrayList<>();
    private List<List<Integer>> farolaImages = new ArrayList<>();
    private String routeId = null;
    private static final String TAG = "FarolasFragment";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable sensorRefreshTask;
    private final long refreshInterval = 5000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_farolas, container, false);

        // Recibir el routeId
        Bundle args = getArguments();
        if (args != null) {
            routeId = args.getString("selectedRouteId");
            Log.i(TAG, "Ruta recibida: " + routeId);
        } else {
            Log.e(TAG, "No se recibió un routeId.");
        }

        // Inicializar vistas
        galleryViewPager = rootView.findViewById(R.id.galleryViewPager);
        farolaSpinner = rootView.findViewById(R.id.farolaSpinner);
        actionButton = rootView.findViewById(R.id.selectButton);

        // RecyclerView para datos del sensor
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_weather);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Cargar IDs de farolas y configurar la UI
        updateFarolaIds(() -> {
            // Configurar Spinner con los IDs de farolas
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, farolaIds);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            farolaSpinner.setAdapter(spinnerAdapter);

            // Configurar listener para el Spinner
            farolaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Detener las actualizaciones periódicas anteriores, si existen
                    stopFetchingFarolaSensorValues();

                    // Actualizar ViewPager2 con imágenes de la farola seleccionada
                    GalleryAdapter galleryAdapter = new GalleryAdapter(getContext(), farolaImages.get(position));
                    galleryViewPager.setAdapter(galleryAdapter);

                    // Obtener datos de sensores para la farola seleccionada
                    String selectedFarolaId = farolaIds.get(position);
                    startFetchingFarolaSensorValues(routeId, selectedFarolaId, sensorResults -> {
                        // Actualizar RecyclerView con datos de sensores
                        List<WeatherData> weatherData = new ArrayList<>();

                        weatherData.add(new WeatherData("Emergencia", sensorResults.get(0).toString(), R.drawable.no_emergency));
                        weatherData.add(new WeatherData("Gas", sensorResults.get(1).toString(), R.drawable.gas));
                        weatherData.add(new WeatherData("Humedad", sensorResults.get(2).toString() + "%", R.drawable.humidity));
                        weatherData.add(new WeatherData("Temperatura", sensorResults.get(3).toString() + " ºC", R.drawable.temperatura));
                        WeatherAdapter adapter2 = new WeatherAdapter(weatherData);
                        recyclerView.setAdapter(adapter2);
                    });
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // No hacer nada si no hay selección
                }
            });
        });

        // Configurar botón
        actionButton.setText("Terminar Ruta");
        actionButton.setOnClickListener(v -> {
            // Navegar de vuelta al fragmento principal
            Fragment rutasFragment = new HomeFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.framelayout, rutasFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return rootView;
    }

    /**
     * Actualizar IDs de farolas y cargar imágenes dummy.
     */
    private void updateFarolaIds(Runnable onFarolasUpdated) {
        if (routeId == null) {
            Log.e(TAG, "Route ID is null. Cannot fetch Farola IDs.");
            return;
        }

        db.collection("Rutas")
                .document(routeId)
                .collection("Farolas")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            farolaIds.clear();
                            farolaImages.clear();
                            for (DocumentSnapshot farolaDoc : querySnapshot.getDocuments()) {
                                farolaIds.add(farolaDoc.getId());

                                // Personalizando Galería por rutas y farolas
                                switch (routeId) {
                                    case "Ruta_1":
                                        farolaImages.add(Arrays.asList(R.drawable.ruta1v1, R.drawable.parque1, R.drawable.parque2));
                                        break;
                                    case "Ruta_2":
                                        farolaImages.add(Arrays.asList(R.drawable.ruta2v1, R.drawable.parque3, R.drawable.parque4));
                                        break;
                                    default:
                                        farolaImages.add(Arrays.asList(R.drawable.ruta1v1));
                                        break;
                                }
                            }

                            Log.i(TAG, "Farola IDs actualizados: " + farolaIds);
                            onFarolasUpdated.run();
                        }
                    } else {
                        Log.w(TAG, "Error al obtener IDs de farolas", task.getException());
                    }
                });
    }

    /**
     * Obtener datos de sensores para una farola específica y refrescarlos periódicamente.
     */
    public void startFetchingFarolaSensorValues(String routeId, String farolaId, SensorDataCallback callback) {
        if (routeId == null || farolaId == null) {
            Log.e(TAG, "Route ID o Farola ID es null. No se pueden obtener datos de sensores.");
            return;
        }

        // Definir la tarea de actualización periódica
        sensorRefreshTask = new Runnable() {
            @Override
            public void run() {
                db.collection("Rutas")
                        .document(routeId)
                        .collection("Farolas")
                        .document(farolaId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                DocumentSnapshot farolaDoc = task.getResult();
                                List<Object> sensorResults = new ArrayList<>(Arrays.asList(
                                        farolaDoc.getLong("emergencias") != null ? farolaDoc.getLong("emergencias") : 0,
                                        farolaDoc.getLong("gas") != null ? farolaDoc.getLong("gas") : 0,
                                        farolaDoc.getLong("humedad") != null ? farolaDoc.getLong("humedad") : 0,
                                        farolaDoc.getDouble("temperatura") != null ? farolaDoc.getDouble("temperatura") : 0.0
                                ));

                                Log.i(TAG, "Datos de sensores obtenidos para Farola " + farolaId + ": " + sensorResults);
                                callback.onSensorDataReady(sensorResults);
                            } else {
                                Log.w(TAG, "Error al obtener datos de sensores para Farola " + farolaId, task.getException());
                            }
                        });

                // Programar la siguiente ejecución después de 5 segundos
                handler.postDelayed(this, refreshInterval);
            }
        };

        // Iniciar la tarea periódica
        handler.post(sensorRefreshTask);
    }

    /**
     * Detener la actualización periódica de los valores de sensores.
     */
    public void stopFetchingFarolaSensorValues() {
        if (sensorRefreshTask != null) {
            handler.removeCallbacks(sensorRefreshTask);
            sensorRefreshTask = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopFetchingFarolaSensorValues();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopFetchingFarolaSensorValues();
    }

    public interface SensorDataCallback {
        void onSensorDataReady(List<Object> sensorResults);
    }
}
