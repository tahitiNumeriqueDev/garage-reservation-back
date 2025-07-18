package com.garage.reservation.model;

public enum TypeVehicule {
    AUTO("Auto"),
    MOTO("Moto");

    private final String libelle;

    TypeVehicule(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
} 