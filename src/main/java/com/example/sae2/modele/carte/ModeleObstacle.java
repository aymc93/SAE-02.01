package com.example.sae2.modele.carte;

public class ModeleObstacle {
    private int ligne;
    private int colonne;
    private ModeleTypeObstacle type;

    public ModeleObstacle(int ligne, int colonne, ModeleTypeObstacle type) {
        this.ligne = ligne;
        this.colonne = colonne;
        this.type = type;
    }

    public int getLigne() { return ligne; }
    public int getColonne() { return colonne; }
    public ModeleTypeObstacle getType() { return type; }
}