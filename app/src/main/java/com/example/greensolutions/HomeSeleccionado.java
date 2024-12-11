package com.example.greensolutions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HomeSeleccionado extends Fragment {

    private static final String ARG_IMAGE_RES_ID = "imageResId";
    private static final String ARG_IMAGE_NAME = "imageName";
    private RecyclerView recyclerView;
    private PosteAdapter posteAdapter;
    private List<PosteSensors> sensorList;

    public static HomeSeleccionado newInstance(int imageResId, String imageName) {
        HomeSeleccionado fragment = new HomeSeleccionado();
        Bundle args = new Bundle();
        args.putInt(ARG_IMAGE_RES_ID, imageResId);
        args.putString(ARG_IMAGE_NAME, imageName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_seleccionado, container, false);

        ImageView selectedImageView = rootView.findViewById(R.id.selectedImageView);
        TextView imageNameTextView = rootView.findViewById(R.id.imageNameTextView);
        Button quitarButton = rootView.findViewById(R.id.quitarButton);
/**
        // Initialize RecyclerView just like in HomeFragment
        recyclerView = rootView.findViewById(R.id.recyclerSensors);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Initialize the list of sensors
        sensorList = new ArrayList<>();
        sensorList.add(new PosteSensors("Sensor 1", "Description of sensor 1", R.drawable.humidity));
        sensorList.add(new PosteSensors("Sensor 2", "Description of sensor 2", R.drawable.precipitation));
        sensorList.add(new PosteSensors("Sensor 3", "Description of sensor 3", R.drawable.wind));

        // Set the adapter for RecyclerView
        posteAdapter = new PosteAdapter(sensorList);
        recyclerView.setAdapter(posteAdapter);
 **/
        // Set the image and name for the selected item
        if (getArguments() != null) {
            int imageResId = getArguments().getInt(ARG_IMAGE_RES_ID);
            String imageName = getArguments().getString(ARG_IMAGE_NAME);
            selectedImageView.setImageResource(imageResId);
            imageNameTextView.setText(imageName);
        }

        // Handle the "QUITAR" button click
        quitarButton.setOnClickListener(v -> {
            HomeFragment homeFragment = new HomeFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.nav_home, homeFragment); // Replace with the correct container ID
            transaction.addToBackStack(null); // Optionally add to back stack
            transaction.commit();
        });

        return rootView;
    }
}
