package com.example.sae2.controller;

import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.modele.ennemis.BFS;
import com.example.sae2.modele.ennemis.ModeleEnnemi;
import com.example.sae2.vue.ennemis.VueEnnemi;
import javafx.animation.AnimationTimer;

import java.util.List;

public class ControllerEnnemis {

    private static final int T = ModeleTerrain.TAILLE_TUILE;
    private static final int TAILLE_ENNEMI = 64;
    private static final int OFFSET = (T - TAILLE_ENNEMI) / 2;

    private final ModeleEnnemi modele;
    private final VueEnnemi vue;
    private final List<int[]> chemin;
    private final Runnable onMort;

    private int  indexChemin = 0;
    private long tempsPrecedent = -1;

    public ControllerEnnemis(ModeleEnnemi modele, VueEnnemi vue, ModeleTerrain terrain, Runnable onMort) {
        this.modele = modele;
        this.vue = vue;
        this.onMort = onMort;

        this.chemin = BFS.calculerChemin(
                terrain.getLigneEntree(),  terrain.getColonneEntree(),
                terrain.getLigneSortie(),  terrain.getColonneSortie(),
                terrain.getGrilleBloquee()
        );

        if (chemin.isEmpty()) {
            System.err.println("[ControllerEnnemis] Aucun chemin trouvé !");
        }
    }

    private static double caseEnPixelX(int colonne) { return colonne * T + OFFSET; }
    private static double caseEnPixelY(int ligne) { return ligne * T + OFFSET; }

    // Démarre l'animation de déplacement de l'ennemi le long du chemin calculé
    public void demarrer() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (tempsPrecedent < 0) { tempsPrecedent = now; return; }

                double delta = (now - tempsPrecedent) / 1000000000.0;
                tempsPrecedent = now;

                if (modele.estMort()) {
                    vue.supprimer();
                    onMort.run();
                    stop();
                    return;
                }

                if (chemin.isEmpty() || indexChemin >= chemin.size() - 1) {
                    vue.actualiser();
                    stop();
                    return;
                }

                int[] prochaine = chemin.get(indexChemin + 1);
                double cibleX   = caseEnPixelX(prochaine[1]);
                double cibleY   = caseEnPixelY(prochaine[0]);

                if (modele.deplacerVers(cibleX, cibleY, delta)) {
                    indexChemin++;
                }

                vue.actualiser();
            }
        }.start();
    }
}
