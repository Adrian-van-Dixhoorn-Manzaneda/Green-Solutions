package com.example.greensolutions;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class PublicarFragment extends Fragment {

    private Lugar lugar;
    private ImageView foto;
    private Uri uriUltimaFoto;

    ActivityResultLauncher<Intent> galeriaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        requireContext().getContentResolver().takePersistableUriPermission(uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        lugar.setFoto(uri.toString());
                        uriUltimaFoto = uri; // Actualiza uriUltimaFoto con la URI de la galería
                        ponerFoto(foto, uri.toString());
                    } else {
                        Toast.makeText(requireContext(),
                                "Foto no cargada", Toast.LENGTH_LONG).show();
                    }
                }
            });

    ActivityResultLauncher<Intent> tomarFotoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && uriUltimaFoto != null) {
                        // Muestra la foto en el ImageView
                        ponerFoto(foto, uriUltimaFoto.toString());
                    } else {
                        Toast.makeText(requireContext(), "Foto cancelada o no capturada", Toast.LENGTH_LONG).show();
                        uriUltimaFoto = null; // Reinicia uriUltimaFoto si no se capturó la foto
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_publicar, container, false);
        foto = view.findViewById(R.id.foto);
        lugar = new Lugar();

        view.findViewById(R.id.galeria).setOnClickListener(this::fotoDeGaleria);
        view.findViewById(R.id.camara).setOnClickListener(this::tomarFoto);
        view.findViewById(R.id.publicar).setOnClickListener(this::publicarFoto);

        return view;
    }

    public void fotoDeGaleria(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        galeriaLauncher.launch(intent);
    }

    public void tomarFoto(View view) {
        try {
            File file = File.createTempFile(
                    "img_" + (System.currentTimeMillis() / 1000), ".jpg",
                    requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES));

            if (Build.VERSION.SDK_INT >= 24) {
                uriUltimaFoto = FileProvider.getUriForFile(
                        requireContext(), "com.example.greensolutions.fileProvider", file);
            } else {
                uriUltimaFoto = Uri.fromFile(file);
            }

            if (uriUltimaFoto == null) {
                Toast.makeText(requireContext(), "Error al crear URI para la foto", Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriUltimaFoto);
            tomarFotoLauncher.launch(intent);

        } catch (IOException ex) {
            Toast.makeText(requireContext(), "Error al crear fichero de imagen", Toast.LENGTH_LONG).show();
        }
    }

    protected void ponerFoto(ImageView imageView, String uri) {
        if (uri != null && !uri.isEmpty() && !uri.equals("null")) {
            Glide.with(this)
                    .load(Uri.parse(uri))
                    .placeholder(R.drawable.gas)
                    .error(R.drawable.baseline_close_24)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.emergency);
        }
    }

    public void publicarFoto(View view) {
        TextView notificacion = getView().findViewById(R.id.notificacion);

        // Valida si se seleccionó o capturó una foto
        if (uriUltimaFoto == null && (lugar.getFoto() == null || lugar.getFoto().isEmpty())) {
            Toast.makeText(requireContext(), "No se ha seleccionado ni capturado una foto", Toast.LENGTH_LONG).show();
            return;
        }

        // Muestra un mensaje de carga
        notificacion.setVisibility(View.VISIBLE);
        notificacion.setText("Subiendo foto, por favor espera...");

        // Referencia a Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String nombreCarpeta = "Farola_1"; // Cambia según la carpeta que desees
        String nombreArchivo = "img_" + System.currentTimeMillis() + ".jpg";
        StorageReference fotoRef = storageRef.child(nombreCarpeta + "/" + nombreArchivo);

        // Determina la URI a subir
        Uri uriParaSubir = uriUltimaFoto != null ? uriUltimaFoto : Uri.parse(lugar.getFoto());

        // Subir el archivo
        fotoRef.putFile(uriParaSubir)
                .addOnSuccessListener(taskSnapshot -> {
                    notificacion.setText("Foto publicada con éxito.");
                    Toast.makeText(requireContext(), "Foto subida correctamente", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    notificacion.setText("Error al subir la foto.");
                    Toast.makeText(requireContext(), "Error al subir la foto: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}

