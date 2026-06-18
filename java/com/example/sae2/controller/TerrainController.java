package com.example.sae2.controller;

import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.vue.carte.VueHover;
import com.example.sae2.vue.carte.VueObstacles;
import com.example.sae2.vue.carte.VueTerrain;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;

public class TerrainController {

    private final GameController main;
    private final ModeleTerrain modele;
    private final VueTerrain vueTerrain;
    private final VueObstacles vueObstacles;
    private final VueHover vueHover;

    public final int T;

    public TerrainController(GameController main, TilePane gridTerrain, Pane tilePaneEntites, ImageView hoverCase) {
        this.main = main;

        // Initialisation du modèle
        this.modele = new ModeleTerrain();
        this.modele.setNiveau(1);
        this.T = ModeleTerrain.TAILLE_TUILE;

        // On ajuste la taille des fenêtres de jeu
        main.paneJeu.setPrefSize(modele.getNbColonnes() * T, modele.getNbLignes() * T);
        main.paneTours.setPrefSize(modele.getNbColonnes() * T, modele.getNbLignes() * T);

        // Initialisation des Vues liées au terrain
        this.vueTerrain = new VueTerrain(modele, gridTerrain);
        this.vueObstacles = new VueObstacles(main, modele, tilePaneEntites);
        this.vueHover = new VueHover(hoverCase, T);
    }

    public ModeleTerrain getModele() { return modele; }
    public VueTerrain getVueTerrain() { return vueTerrain; }
    public VueObstacles getVueObstacles() { return vueObstacles; }
    public int getT() { return T; }

    // --- Gestion du survol (Hover) ---
    public void actualiserHover(double x, double y) {
        vueHover.actualiser(x, y);
    }

    public void cacherHover() {
        vueHover.cacher();
    }

    public void changerNiveauMap(int nouveauNiveau, Pane tilePaneEntites) {
        // 1. On met à jour la logique
        modele.setNiveau(nouveauNiveau);

        // 2. On met à jour les visuels
        vueTerrain.actualiserNiveau(modele, (TilePane) main.getGridTerrain());
        vueObstacles.actualiserNiveau(modele, tilePaneEntites);
    }
}