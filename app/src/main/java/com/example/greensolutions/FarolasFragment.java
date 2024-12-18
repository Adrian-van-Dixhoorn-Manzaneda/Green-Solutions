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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FarolasFragment extends Fragment {

    private ImageView imageView;
    private Spinner spinner;
    private Button actionButton;
    private int[] imageResIds = {R.drawable.poste1, R.drawable.poste2, R.drawable.poste3};
    private String[] imageOptions = {"Farola 1", "Farola 2", "Farola 3"};
    private boolean isImageFixed = false;
    private int selectedPosition = 0;
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
        spinner = rootView.findViewById(R.id.spinner);
        imageView = rootView.findViewById(R.id.imageView);
        actionButton = rootView.findViewById(R.id.selectButton);

        // RecyclerView for displaying sensor data
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_weather);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Update spinner options with Farola IDs
        updateImageOptionsWithFarolasIds();

        // Configure Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, imageOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Handle Spinner Selection
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isImageFixed) {
                    imageView.setImageResource(imageResIds[position]);
                    selectedPosition = position;

                    // Fetch sensor data for the selected Farola
                    String selectedFarolaId = imageOptions[position];
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

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
    private void updateImageOptionsWithFarolasIds() {
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
                            List<String> farolaIds = new ArrayList<>();
                            for (DocumentSnapshot farolaDoc : querySnapshot.getDocuments()) {
                                farolaIds.add(farolaDoc.getId());
                            }

                            // Update spinner options
                            imageOptions = farolaIds.toArray(new String[0]);
                            Log.i(TAG, "Farola IDs updated: " + Arrays.toString(imageOptions));

                            // Refresh the spinner with updated options
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, imageOptions);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(adapter);
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
