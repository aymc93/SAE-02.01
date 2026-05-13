package com.example.sae2.modele.autres;

import java.util.ArrayList;
import java.util.List;

public class ModeleTerrain {

    public enum TypeCase { HERBE, PIERRE }

    public static final int NB_COLS = 20;
    public static final int NB_Ligne = 15;

    private final TypeCase[][] grille;

    // Chemins
    private final List<int[]> cheminPrincipal;
    private final List<int[]> cheminSecondaire;



    public ModeleTerrain() {

        grille = new TypeCase[NB_Ligne][NB_COLS];

        cheminPrincipal = new ArrayList<>();
        cheminSecondaire = new ArrayList<>();

        initialiser();
    }



    private void initialiser() {

        // Remplit toute la grille avec de l’herbe
        for (int ligne = 0; ligne < NB_Ligne; ligne++) {

            for (int colonne = 0; colonne < NB_COLS; colonne++) {

                grille[ligne][colonne] = TypeCase.HERBE;
            }
        }

        // Construction des chemins
        construireCheminPrincipal();
        construireCheminSecondaire();

        // Applique le chemin principal
        for (int[] point : cheminPrincipal) {

            grille[point[1]][point[0]] = TypeCase.PIERRE;
        }

        // Applique le chemin secondaire
        for (int[] point : cheminSecondaire) {

            grille[point[1]][point[0]] = TypeCase.PIERRE;
        }
    }



    /**
     * Chemin principal
     */
    private void construireCheminPrincipal() {

        // Entrée
        ajouterSegmentHorizontal(cheminPrincipal, 0, 4, 7);

        // Monte
        ajouterSegmentVertical(cheminPrincipal, 4, 7, 3);

        // Va à droite
        ajouterSegmentHorizontal(cheminPrincipal, 4, 14, 3);

        // Redescend
        ajouterSegmentVertical(cheminPrincipal, 14, 3, 7);

        // Sortie
        ajouterSegmentHorizontal(cheminPrincipal, 14, 19, 7);
    }



    /**
     * Chemin secondaire
     */
    private void construireCheminSecondaire() {

        // Descend depuis l'intersection gauche
        ajouterSegmentVertical(cheminSecondaire, 4, 7, 11);

        // Va à droite
        ajouterSegmentHorizontal(cheminSecondaire, 4, 14, 11);

        // Remonte vers l'intersection droite
        ajouterSegmentVertical(cheminSecondaire, 14, 11, 7);
    }



    /**
     * Ajoute un segment horizontal
     */
    private void ajouterSegmentHorizontal(List<int[]> chemin,int colonneDepart,int colonneArrivee, int ligne) {

        int direction;

        if (colonneArrivee >= colonneDepart) {
            direction = 1;
        } else {
            direction = -1;
        }

        for (int colonne = colonneDepart;
             colonne != colonneArrivee + direction;
             colonne += direction) {

            ajouterPoint(chemin, colonne, ligne);
        }
    }



    /**
     * Ajoute un segment vertical
     */
    private void ajouterSegmentVertical(List<int[]> chemin, int colonne, int ligneDepart, int ligneArrivee) {

        int direction;

        if (ligneArrivee >= ligneDepart) {
            direction = 1;
        } else {
            direction = -1;
        }

        for (int ligne = ligneDepart;
             ligne != ligneArrivee + direction;
             ligne += direction) {

            ajouterPoint(chemin, colonne, ligne);
        }
    }



    /**
     * Ajoute un point dans le chemin
     */
    private void ajouterPoint(List<int[]> chemin, int colonne, int ligne) {

        if (!chemin.isEmpty()) {

            int[] dernierPoint = chemin.get(chemin.size() - 1);

            if (dernierPoint[0] == colonne &&
                    dernierPoint[1] == ligne) {

                return;
            }
        }

        chemin.add(new int[]{colonne, ligne});
    }



    // Accesseurs

    public TypeCase getCase(int ligne, int colonne) {
        return grille[ligne][colonne];
    }

    public int getNbLignes() {
        return NB_Ligne;
    }

    public int getNbCols() {
        return NB_COLS;
    }

    public List<int[]> getCheminPrincipal() {
        return cheminPrincipal;
    }

    public List<int[]> getCheminSecondaire() {
        return cheminSecondaire;
    }
}