package com.example.greensolutions;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.viewpager2.widget.ViewPager2;


public class FarolasFragment extends Fragment {

    private ViewPager2 galleryViewPager;
    private Button actionButton;
    private List<Integer> imageResIds = Arrays.asList(R.drawable.poste1, R.drawable.poste2, R.drawable.poste3);
    private List<String> farolaIds = new ArrayList<>();
    private String routeId = null; // To store the passed Route ID
    private static final String TAG = "FarolasFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_farolas, container, false);

        // Retrieve the route ID passed from HomeFragment
        Bundle args = getArguments();
        if (args != null) {
            routeId = args.getString("selectedRouteId");
            Log.i(TAG, "Ruta recibida: " + routeId); // Log the received ID
        } else {
            Log.e(TAG, "No se recibió un routeId.");
        }

        // Initialize Views
        galleryViewPager = rootView.findViewById(R.id.galleryViewPager);
        actionButton = rootView.findViewById(R.id.selectButton);

        // RecyclerView for displaying sensor data
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_weather);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Update Farola IDs based on Route ID
        updateFarolaIds(() -> {
            // Configure the ViewPager2 once Farola IDs are fetched
            GalleryAdapter galleryAdapter = new GalleryAdapter(getContext(), imageResIds, farolaIds);
            galleryViewPager.setAdapter(galleryAdapter);

            // Handle page changes to fetch sensor data for the current Farola
            galleryViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    String selectedFarolaId = farolaIds.get(position);

                    // Fetch sensor data for the selected Farola
                    getFarolaSensorValues(selectedFarolaId, sensorResults -> {
                        // Update RecyclerView with sensor data
                        List<WeatherData> weatherData = new ArrayList<>();
                        weatherData.add(new WeatherData("Emergencia", sensorResults.get(0).toString(), R.drawable.humidity));
                        weatherData.add(new WeatherData("Gas", sensorResults.get(1).toString(), R.drawable.precipitation));
                        weatherData.add(new WeatherData("Humedad", sensorResults.get(2).toString() + "%", R.drawable.wind));
                        weatherData.add(new WeatherData("Temperatura", sensorResults.get(3).toString() + " ºC", R.drawable.precipitation));

                        WeatherAdapter adapter2 = new WeatherAdapter(weatherData);
                        recyclerView.setAdapter(adapter2);
                    });
                }
            });
        });

        // Configure Button
        actionButton.setText("Terminar Ruta");
        actionButton.setOnClickListener(v -> {
            // Navegar de vuelta a RutasFragment
            Fragment rutasFragment = new HomeFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.framelayout, rutasFragment)
                    .addToBackStack(null) // Optional: allows back navigation if needed
                    .commit();
        });

        return rootView;
    }

    /**
     * Fetch and update Farola IDs based on the provided Route ID.
     */
    private void updateFarolaIds(Runnable onFarolasUpdated) {
        if (routeId == null) {
            Log.e(TAG, "Route ID is null. Cannot fetch Farola IDs.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Rutas")
                .document(routeId)
                .collection("Farolas")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            farolaIds.clear();
                            for (DocumentSnapshot farolaDoc : querySnapshot.getDocuments()) {
                                farolaIds.add(farolaDoc.getId());
                            }

                            Log.i(TAG, "Farola IDs updated: " + farolaIds);
                            onFarolasUpdated.run(); // Notify that Farola IDs are updated
                        }
                    } else {
                        Log.w(TAG, "Error fetching Farola IDs", task.getException());
                    }
                });
    }

    /**
     * Fetch sensor data for the selected Farola.
     */
    private void getFarolaSensorValues(String farolaId, SensorDataCallback callback) {
        if (routeId == null || farolaId == null) {
            Log.e(TAG, "Route ID or Farola ID is null. Cannot fetch sensor data.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Rutas")
                .document(routeId)
                .collection("Farolas")
                .document(farolaId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot farolaDoc = task.getResult();
                        List<Long> sensorResults = new ArrayList<>(Arrays.asList(
                                farolaDoc.getLong("Emergencias") != null ? farolaDoc.getLong("Emergencias") : 0,
                                farolaDoc.getLong("Gas") != null ? farolaDoc.getLong("Gas") : 0,
                                farolaDoc.getLong("Humedad") != null ? farolaDoc.getLong("Humedad") : 0,
                                farolaDoc.getLong("Temperatura") != null ? farolaDoc.getLong("Temperatura") : 0
                        ));

                        Log.i(TAG, "Sensor data fetched for Farola " + farolaId + ": " + sensorResults);
                        callback.onSensorDataReady(sensorResults);
                    } else {
                        Log.w(TAG, "Error fetching sensor data for Farola " + farolaId, task.getException());
                    }
                });
    }

    public interface SensorDataCallback {
        void onSensorDataReady(List<Long> sensorResults);
    }
}

