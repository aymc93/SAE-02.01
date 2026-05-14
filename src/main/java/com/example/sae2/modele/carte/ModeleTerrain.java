package com.example.sae2.modele.carte;

import java.util.ArrayList;
import java.util.List;

public class ModeleTerrain {

    public static final int TAILLE_TUILE = 64;
    private List<ModeleObstacle> modeleObstacles;


    private int niveauActuel = 1;
    private String dossierSprites = "Level1-10"; // Valeur par défaut


    private int ligneEntree;
    private int colonneEntree;
    private int ligneSortie;
    private int colonneSortie;

    private ModeleTypeTuile[][] grille;
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
        modeleObstacles = new ArrayList<>();

        setNiveau(1); // On initialise le jeu au niveau 1
        chargerCarte();
        placerPortes();
        placerObstacles();
    }

    //Change le niveau et met à jour le dossier des sprites

    public void setNiveau(int niveau) {
        this.niveauActuel = niveau;

        int debutDizaine = (((niveau - 1) / 10) * 10) + 1;
        int finDizaine = debutDizaine + 9;
        this.dossierSprites = "Level" + debutDizaine + "-" + finDizaine;

        placerPortes();
        placerObstacles();
    }

    public int getNiveauActuel() {
        return niveauActuel;
    }

    public String getDossierSprites() {
        return dossierSprites;
    }

    private void chargerCarte() {
        for (int l = 0; l < nbLignes; l++) {
            for (int c = 0; c < nbColonnes; c++) {
                grille[l][c] = ModeleTypeTuile.fromInt(CARTE[l][c]);
            }
        }
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
        modeleObstacles.clear(); // On vide la liste avant de remplir selon le niveau

        if (niveauActuel <= 10) {
            // PLACEMENT POUR LES NIVEAUX 1 À 10
            modeleObstacles.add(new ModeleObstacle(5, 5, ModeleTypeObstacle.BARIL));
            modeleObstacles.add(new ModeleObstacle(5, 6, ModeleTypeObstacle.CAILLOU));
        }
        else if (niveauActuel <= 20) {
            modeleObstacles.add(new ModeleObstacle(2, 10, ModeleTypeObstacle.BARIL));
            modeleObstacles.add(new ModeleObstacle(8, 3, ModeleTypeObstacle.CAILLOU));
        }
        else if (niveauActuel <= 30) {
            modeleObstacles.add(new ModeleObstacle(7, 13, ModeleTypeObstacle.BARIL));
            modeleObstacles.add(new ModeleObstacle(12, 2, ModeleTypeObstacle.CAILLOU));
        }
        else if (niveauActuel <= 40) {
            modeleObstacles.add(new ModeleObstacle(7, 13, ModeleTypeObstacle.BARIL));
            modeleObstacles.add(new ModeleObstacle(12, 2, ModeleTypeObstacle.CAILLOU));
        }
        else if (niveauActuel <= 50) {
            modeleObstacles.add(new ModeleObstacle(7, 13, ModeleTypeObstacle.BARIL));
            modeleObstacles.add(new ModeleObstacle(12, 2, ModeleTypeObstacle.CAILLOU));
        }
    }

    public List<ModeleObstacle> getObstacles() {
        return modeleObstacles;
    }
}