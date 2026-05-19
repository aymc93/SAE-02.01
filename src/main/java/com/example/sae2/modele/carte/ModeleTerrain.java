package com.example.sae2.modele.carte;

import java.util.ArrayList;
import java.util.List;

public class ModeleTerrain {

    public static final int TAILLE_TUILE = 64;
    private List<ModeleObstacle> modeleObstacles;

    private int niveauActuel = 1;

    private int ligneEntree;
    private int colonneEntree;
    private int ligneSortie;
    private int colonneSortie;

    private ModeleTypeTuile[][] grille;
    private int[][] variations; // sauvegarde l'aléatoire de chaque case
    private int nbLignes;
    private int nbColonnes;

    private static final int[][] CARTE = {
            { 50, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 51 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 , 0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 , 0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 , 0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 , 0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 , 0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 , 0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 , 0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 , 0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 , 0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 , 0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 , 0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 , 0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 , 0,  0,  0,  0,  0,  0,  0, 41 },
            { 53, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 52 },
    };

    public ModeleTerrain() {
        nbLignes   = CARTE.length;
        nbColonnes = CARTE[0].length;
        grille     = new ModeleTypeTuile[nbLignes][nbColonnes];
        variations = new int[nbLignes][nbColonnes];
        modeleObstacles = new ArrayList<>();

        setNiveau(1);
        chargerCarte();
        placerPortes();
        placerObstacles();
    }

    public void setNiveau(int niveau) {
        this.niveauActuel = niveau;
        placerPortes();
        placerObstacles();
    }

    public int getNiveauActuel() {
        return niveauActuel;
    }

    private void chargerCarte() {
        for (int l = 0; l < nbLignes; l++) {
            for (int c = 0; c < nbColonnes; c++) {
                grille[l][c] = ModeleTypeTuile.fromInt(CARTE[l][c]);
                // NOUVEAU : On génère un nombre aléatoire (ex: 0 à 999) qu'on stocke
                variations[l][c] = (int) (Math.random() * 1000);
            }
        }
    }

    // Permet à la vue de récupérer le chiffre aléatoire fixe de cette case
    public int getVariation(int ligne, int colonne) {
        return variations[ligne][colonne];
    }

    private void placerPortes() {
        colonneEntree = 0;
        colonneSortie = nbColonnes - 1;
        int min = 1;
        int max = nbLignes - 2;
        ligneEntree = (int) (Math.random() * (max - min + 1)) + min;
        ligneSortie = (int) (Math.random() * (max - min + 1)) + min;
    }

    public int getLigneEntree() { return ligneEntree; }
    public int getColonneEntree() { return colonneEntree; }
    public int getLigneSortie() { return ligneSortie; }
    public int getColonneSortie() { return colonneSortie; }
    public ModeleTypeTuile getTuile(int ligne, int colonne) { return grille[ligne][colonne]; }
    public int getNbLignes()   { return nbLignes;   }
    public int getNbColonnes() { return nbColonnes; }

    private void placerObstacles() {
        modeleObstacles.clear();

        if (niveauActuel <= 10) {
            modeleObstacles.add(new ModeleObstacle(5, 5, ModeleTypeObstacle.BARIL));
            modeleObstacles.add(new ModeleObstacle(4, 5, ModeleTypeObstacle.BARIL));
            modeleObstacles.add(new ModeleObstacle(3, 5, ModeleTypeObstacle.BARIL));
            modeleObstacles.add(new ModeleObstacle(2, 5, ModeleTypeObstacle.BARIL));
            modeleObstacles.add(new ModeleObstacle(1, 5, ModeleTypeObstacle.BARIL));
            modeleObstacles.add(new ModeleObstacle(6, 5, ModeleTypeObstacle.BARIL));
            modeleObstacles.add(new ModeleObstacle(7, 5, ModeleTypeObstacle.BARIL));
            modeleObstacles.add(new ModeleObstacle(8, 5, ModeleTypeObstacle.BARIL));
            modeleObstacles.add(new ModeleObstacle(9, 5, ModeleTypeObstacle.BARIL));
            modeleObstacles.add(new ModeleObstacle(12, 5, ModeleTypeObstacle.BARIL));
            modeleObstacles.add(new ModeleObstacle(13, 5, ModeleTypeObstacle.BARIL));

            modeleObstacles.add(new ModeleObstacle(1, 9, ModeleTypeObstacle.BARIL));
            modeleObstacles.add(new ModeleObstacle(2, 9, ModeleTypeObstacle.BARIL));

            modeleObstacles.add(new ModeleObstacle(10, 6, ModeleTypeObstacle.CAILLOU));
            modeleObstacles.add(new ModeleObstacle(12, 6, ModeleTypeObstacle.CAILLOU));
            modeleObstacles.add(new ModeleObstacle(12, 7, ModeleTypeObstacle.CAILLOU));
            modeleObstacles.add(new ModeleObstacle(11, 8, ModeleTypeObstacle.CAILLOU));
            modeleObstacles.add(new ModeleObstacle(10, 8, ModeleTypeObstacle.CAILLOU));
            modeleObstacles.add(new ModeleObstacle(9, 8, ModeleTypeObstacle.CAILLOU));
            modeleObstacles.add(new ModeleObstacle(8, 7, ModeleTypeObstacle.CAILLOU));
            modeleObstacles.add(new ModeleObstacle(7, 7, ModeleTypeObstacle.CAILLOU));
            modeleObstacles.add(new ModeleObstacle(6, 7, ModeleTypeObstacle.CAILLOU));
            modeleObstacles.add(new ModeleObstacle(5, 7, ModeleTypeObstacle.CAILLOU));
            modeleObstacles.add(new ModeleObstacle(4, 7, ModeleTypeObstacle.CAILLOU));
            modeleObstacles.add(new ModeleObstacle(3, 7, ModeleTypeObstacle.CAILLOU));
            modeleObstacles.add(new ModeleObstacle(2, 7, ModeleTypeObstacle.CAILLOU));

        } else {
            // Regroupement pour les niveaux > 10 (vu que c'était pareil dans ton code)
            modeleObstacles.add(new ModeleObstacle(7, 13, ModeleTypeObstacle.BARIL));
            modeleObstacles.add(new ModeleObstacle(12, 2, ModeleTypeObstacle.CAILLOU));
        }
    }

    public List<ModeleObstacle> getObstacles() {
        return modeleObstacles;
    }

    public String getDossierSprites() {
        int tranche = (niveauActuel - 1) / 10; // 0 pour 1-10, 1 pour 11-20, etc.
        int debut = tranche * 10 + 1;
        int fin = debut + 9;
        return "Level" + debut + "-" + fin;
    }
}