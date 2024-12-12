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
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        Uri uri = result.getData().getData();
                        requireContext().getContentResolver().takePersistableUriPermission(uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        lugar.setFoto(uri.toString());
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
                        ponerFoto(foto, uriUltimaFoto.toString());
                    } else {
                        Toast.makeText(requireContext(),
                                "Foto cancelada", Toast.LENGTH_LONG).show();
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

    public void actualizaVistas() {
        ponerFoto(foto, lugar.getFoto());
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
                        requireContext(), "es.upv.lfuster.greensolutions.fileProvider", file);
            } else {
                uriUltimaFoto = Uri.fromFile(file);
            }
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriUltimaFoto);
            tomarFotoLauncher.launch(intent);
        } catch (IOException ex) {
            Toast.makeText(requireContext(), "Error al crear fichero de imagen",
                    Toast.LENGTH_LONG).show();
        }
    }

    protected void ponerFoto(ImageView imageView, String uri) {
        if (uri != null && !uri.isEmpty() && !uri.equals("null")) {
            imageView.setImageURI(Uri.parse(uri));
        } else {
            imageView.setImageBitmap(null);
        }
    }

    // Aquí hay que poner el fragment de la galeria de fotos publicadas
    public void publicarFoto(View view) {
        if (uriUltimaFoto != null) {
            Intent intent = new Intent(requireContext(), RutasFragment.class);
            intent.putExtra("fotoUri", uriUltimaFoto.toString());
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "No se ha seleccionado una foto", Toast.LENGTH_LONG).show();
        }
    }
}