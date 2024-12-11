package com.example.greensolutions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ImageView imageView;
    private int[] imageResIds = {R.drawable.poste1, R.drawable.poste2, R.drawable.poste3};
    private String[] imageOptions = {"Poste Central", "Poste del Norte", "Poste del Sur"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        Spinner spinner = rootView.findViewById(R.id.spinner);
        imageView = rootView.findViewById(R.id.imageView);
        Button selectButton = rootView.findViewById(R.id.selectButton);



        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_weather);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<WeatherData> weatherData = new ArrayList<>();
        weatherData.add(new WeatherData("Humedad", "65%", R.drawable.humidity));
        weatherData.add(new WeatherData("Precipitación", "10%", R.drawable.precipitation));
        weatherData.add(new WeatherData("Viento", "15 km/h", R.drawable.wind));

        WeatherAdapter adapter2 = new WeatherAdapter(weatherData);
        recyclerView.setAdapter(adapter2);



        // Configura el adaptador del Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, imageOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Maneja la selección del Spinner
        final int[] selectedPosition = {0}; // Array para manejar posición mutable
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                imageView.setImageResource(imageResIds[position]);
                selectedPosition[0] = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Maneja el clic del botón para redirigir al fragmento de la foto seleccionada
        selectButton.setOnClickListener(v -> {
            int selectedImageResId = imageResIds[selectedPosition[0]];
            String selectedImageName = imageOptions[selectedPosition[0]];

            // Navega al fragmento de la imagen seleccionada
            HomeSeleccionado fragment = HomeSeleccionado.newInstance(selectedImageResId, selectedImageName);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.framelayout, fragment) // Usa un contenedor específico
                    .addToBackStack(null)
                    .commit();
        });

        return rootView;
    }
}
