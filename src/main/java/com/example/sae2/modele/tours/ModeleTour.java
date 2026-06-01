package com.example.sae2.modele.tours;

public class ModeleTour {

    private final int colonne;
    private final int ligne;

    public ModeleTour(int colonne, int ligne) {
        this.colonne = colonne;
        this.ligne   = ligne;
    }

    public int getColonne() { return colonne; }
    public int getLigne()   { return ligne; }
}
