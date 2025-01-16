package com.example.greensolutions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PerfilFragment extends Fragment {

    private FirebaseFirestore db;
    private String userEmail; // Email from SharedPreferences
    private String documentId; // ID of the user's Firestore document

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        // Retrieve the logged-in user's email
        if (getActivity() != null) {
            userEmail = getActivity()
                    .getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
                    .getString("userEmail", null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        // Configuración existente para correo/contraseña
        EditText editTextName = view.findViewById(R.id.editTextText);
        EditText editTextEmail = view.findViewById(R.id.editTextText2);
        EditText editTextPassword = view.findViewById(R.id.editTextText3);

        fetchUserDocument(editTextName, editTextEmail, editTextPassword);

        setupEditButtons(editTextName, view.findViewById(R.id.imageButton), view.findViewById(R.id.btnAccept1), view.findViewById(R.id.btnCancel1));
        setupEditButtons(editTextEmail, view.findViewById(R.id.imageButton2), view.findViewById(R.id.btnAccept2), view.findViewById(R.id.btnCancel2));
        setupEditButtons(editTextPassword, view.findViewById(R.id.imageButton4), view.findViewById(R.id.btnAccept3), view.findViewById(R.id.btnCancel3));

        Button botonLogout = view.findViewById(R.id.botoncerrarsesion);
        botonLogout.setOnClickListener(this::logout);

        // Obtener SharedPreferences
        boolean isGoogleSignIn = requireActivity()
                .getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
                .getBoolean("isGoogleSignIn", false);

        // Mostrar mensaje si el usuario inició sesión mediante Google
        if (isGoogleSignIn) {
            // Hacer invisibles todos los elementos excepto el botón de cerrar sesión
            view.findViewById(R.id.editTextText).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.editTextText2).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.editTextText3).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.imageButton).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.imageButton2).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.imageButton4).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.btnAccept1).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.btnAccept2).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.btnAccept3).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.btnCancel1).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.btnCancel2).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.btnCancel3).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.textView7).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.textView9).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.textView10).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.textView8).setVisibility(View.INVISIBLE);

            view.findViewById(R.id.textView12).setVisibility(View.VISIBLE);

            botonLogout.setVisibility(View.VISIBLE);

            return view;
        }

        return view;
    }



    private void fetchUserDocument(EditText nameField, EditText emailField, EditText passwordField) {
        // Query Firestore to find the document for the logged-in user
        db.collection("usuarios")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        documentId = document.getId(); // Save the document ID for updates

                        // Set data to EditText fields
                        nameField.setText(document.getString("nombre"));
                        emailField.setText(document.getString("email"));
                        passwordField.setText(document.getString("password"));

                        // Save initial values for cancel functionality
                        nameField.setTag(document.getString("nombre"));
                        emailField.setTag(document.getString("email"));
                        passwordField.setTag(document.getString("password"));
                    } else {
                        Toast.makeText(getContext(), "No se encontraron datos del usuario.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupEditButtons(EditText editText, ImageButton editButton, ImageButton btnAccept, ImageButton btnCancel) {
        editButton.setOnClickListener(view -> {
            setEditTextEnabled(editText, true);
            editButton.setVisibility(View.GONE);
            btnAccept.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        });

        btnAccept.setOnClickListener(view -> {
            saveUserData(editText);
            setEditTextEnabled(editText, false);
            editButton.setVisibility(View.VISIBLE);
            btnAccept.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
        });

        btnCancel.setOnClickListener(view -> {
            editText.setText(editText.getTag().toString());
            setEditTextEnabled(editText, false);
            editButton.setVisibility(View.VISIBLE);
            btnAccept.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
        });
    }

    private void saveUserData(EditText editText) {
        if (documentId == null) {
            Toast.makeText(getContext(), "Error: Documento no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        String fieldName = "";
        if (editText.getId() == R.id.editTextText) fieldName = "nombre";
        else if (editText.getId() == R.id.editTextText2) fieldName = "email";
        else if (editText.getId() == R.id.editTextText3) fieldName = "password";

        String newValue = editText.getText().toString();

        db.collection("usuarios").document(documentId)
                .update(fieldName, newValue)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Actualizado correctamente", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setEditTextEnabled(EditText editText, boolean enabled) {
        editText.setEnabled(enabled);
        editText.setFocusable(enabled);
        editText.setFocusableInTouchMode(enabled);
    }

    public void logout(View view) {
        requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
                .edit()
                .remove("userEmail")
                .apply();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}