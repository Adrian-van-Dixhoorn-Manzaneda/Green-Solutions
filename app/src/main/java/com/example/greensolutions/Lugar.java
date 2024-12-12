package com.example.greensolutions;

public class Lugar {
    private String foto;

    public Lugar() {

    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    @Override
    public String toString() {
        return "Lugar{" +
                ", foto='" + foto +
                '}';
    }

}