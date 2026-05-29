package com.example.sae2.controller;

import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.modele.ennemis.BFS;
import com.example.sae2.modele.ennemis.ModeleEnnemi;
import com.example.sae2.vue.ennemis.VueEnnemi;
import javafx.animation.AnimationTimer;

import java.util.List;

/**
 * Controleur dedie aux ennemis.
 * Responsabilites :
 *  - construire la grille de collision a partir du terrain
 *  - calculer le chemin via BFS
 *  - deplacer l'ennemi case par case (centre sur chaque case 64x64)
 *  - avancer l'animation au bon rythme (DUREE_FRAME_NS)
 *  - demander a la vue de se rafraichir a chaque frame
 */
public class ControllerEnnemis {

    /** Taille d'une case en pixels. */
    private static final int T = ModeleTerrain.TAILLE_TUILE;   // 64

    /** Taille du sprite ennemi en pixels. */
    private static final int TAILLE_ENNEMI = 64;

    /** Decalage pour centrer le sprite 32x32 dans une case 64x64. */
    private static final int OFFSET = (T - TAILLE_ENNEMI) / 2; // 16

    private final ModeleEnnemi modele;
    private final VueEnnemi    vue;
    private final List<int[]>  chemin;
    private int  indexChemin    = 0;
    private long tempsPrecedent = -1;

    public ControllerEnnemis(ModeleEnnemi modele, VueEnnemi vue, ModeleTerrain terrain) {
        this.modele = modele;
        this.vue    = vue;

        boolean[][] grilleBloquee = construireGrilleBloquee(terrain);
        this.chemin = BFS.calculerChemin(
                terrain.getLigneEntree(),  terrain.getColonneEntree(),
                terrain.getLigneSortie(),  terrain.getColonneSortie(),
                grilleBloquee
        );

        if (chemin.isEmpty()) {
            System.err.println("[ControllerEnnemis] Aucun chemin trouve !");
        }
    }

    // -------------------------------------------------------------------------
    // Grille de collision
    // -------------------------------------------------------------------------

    private boolean[][] construireGrilleBloquee(ModeleTerrain terrain) {
        int rows = terrain.getNbLignes();
        int cols = terrain.getNbColonnes();
        boolean[][] bloquee = new boolean[rows][cols];

        for (int l = 0; l < rows; l++) {
            for (int c = 0; c < cols; c++) {
                boolean estMur      = !terrain.getTuile(l, c).isSolSimple();
                boolean estObstacle = terrain.getObstaclesGrille()[l][c] != null;
                boolean estEntree   = (l == terrain.getLigneEntree()  && c == terrain.getColonneEntree());
                boolean estSortie   = (l == terrain.getLigneSortie()  && c == terrain.getColonneSortie());

                bloquee[l][c] = (estMur || estObstacle) && !estEntree && !estSortie;
            }
        }
        return bloquee;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static double caseEnPixelX(int colonne) { return colonne * T + OFFSET; }
    private static double caseEnPixelY(int ligne)   { return ligne   * T + OFFSET; }

    // -------------------------------------------------------------------------
    // Boucle de jeu
    // -------------------------------------------------------------------------

    public void demarrer() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {

                // --- Initialisation au premier appel ---
                if (tempsPrecedent < 0) { tempsPrecedent = now; return; }

                double delta = (now - tempsPrecedent) / 1000000000.0;
                tempsPrecedent = now;

                // --- Deplacement : fin de chemin ---
                if (chemin.isEmpty() || indexChemin >= chemin.size() - 1) {
                    vue.actualiser();
                    stop();
                    return;
                }

                // --- Deplacement : vers le centre de la case suivante ---
                int[] prochaineCellule = chemin.get(indexChemin + 1);
                double cibleX = caseEnPixelX(prochaineCellule[1]);
                double cibleY = caseEnPixelY(prochaineCellule[0]);

                boolean arrive = modele.deplacerVers(cibleX, cibleY, delta);
                if (arrive) indexChemin++;

                vue.actualiser();
            }
        }.start();
    }
}
