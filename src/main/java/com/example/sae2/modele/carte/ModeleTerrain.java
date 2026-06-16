package com.example.sae2.modele.carte;

public class ModeleTerrain {

    public static final int TAILLE_TUILE = 64;
    private ModeleTypeObstacle[][] obstaclesGrille;

    private int niveauActuel = 1;

    private int ligneEntree;
    private int colonneEntree;
    private int ligneSortie;
    private int colonneSortie;

    private TypeTuile[][] grille;
    private int[][] variations;
    private int nbLignes;
    private int nbColonnes;

        private static final int[][] CARTE = {
            { 50, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 51 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 41 },
            { 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 41 },
            { 53, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 52 },
    };

    private static final int[][] OBSTACLES = {
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0 },
            { 0, 0, 0, 0, 0, 1, 0, 2, 0, 1, 0, 0 },
            { 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 1, 0, 2, 1, 1, 1, 0 },
            { 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    };

    public ModeleTerrain() {
        nbLignes   = CARTE.length;
        nbColonnes = CARTE[0].length;
        grille = new TypeTuile[nbLignes][nbColonnes];
        variations = new int[nbLignes][nbColonnes];

        setNiveau(niveauActuel);
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
                grille[l][c] = TypeTuile.fromInt(CARTE[l][c]);
                variations[l][c] = (int) (Math.random() * 1000);
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
    public TypeTuile getTuile(int ligne, int colonne) { return grille[ligne][colonne]; }
    public int getNbLignes() { return nbLignes; }
    public int getNbColonnes() { return nbColonnes; }

    private void placerObstacles() {
        obstaclesGrille = new ModeleTypeObstacle[nbLignes][nbColonnes];
        for (int l = 0; l < nbLignes; l++) {
            for (int c = 0; c < nbColonnes; c++) {
                obstaclesGrille[l][c] = ModeleTypeObstacle.fromInt(OBSTACLES[l][c]);
            }
        }
    }

    public ModeleTypeObstacle[][] getObstaclesGrille() {
        return obstaclesGrille;
    }

    public String getDossierSprites() {
        int tranche = (niveauActuel - 1) / 10;
        int debut = tranche * 10 + 1;
        int fin = debut + 9;
        return "Level" + debut + "-" + fin;
    }

    /** Retourne vrai si on peut poser une tour sur cette case. */
    public boolean estConstructible(int ligne, int colonne) {
        return getTuile(ligne, colonne).isSolSimple()
                && obstaclesGrille[ligne][colonne] == null;
    }

    /** Retourne la grille des cases bloquées (murs, obstacles), utilisée par le BFS. */
    public boolean[][] getGrilleBloquee() {
        boolean[][] bloquee = new boolean[nbLignes][nbColonnes];
        for (int l = 0; l < nbLignes; l++) {
            for (int c = 0; c < nbColonnes; c++) {
                boolean estMur      = !getTuile(l, c).isSolSimple();
                boolean estObstacle = obstaclesGrille[l][c] != null;
                boolean estEntree   = l == ligneEntree  && c == colonneEntree;
                boolean estSortie   = l == ligneSortie  && c == colonneSortie;
                bloquee[l][c] = (estMur || estObstacle) && !estEntree && !estSortie;
            }
        }
        return bloquee;
    }
}