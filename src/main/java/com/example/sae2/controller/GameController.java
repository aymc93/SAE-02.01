package com.example.sae2.controller;

import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.modele.ennemis.ModeleEnnemi;
import com.example.sae2.vue.carte.VueObstacles;
import com.example.sae2.vue.carte.VueTerrain;
import com.example.sae2.vue.ennemis.VueEnnemi;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.shape.Rectangle;

public class GameController {

    @FXML private Pane paneJeu;
    @FXML private GridPane gridTerrain;
    @FXML private TilePane tilePaneEntites;
    @FXML private Rectangle rectEnnemi;

    @FXML
    public void initialize() {
        ModeleTerrain modele = new ModeleTerrain();
        modele.setNiveau(45);

        int cols = modele.getNbColonnes();
        int rows = modele.getNbLignes();
        int T    = ModeleTerrain.TAILLE_TUILE;

        paneJeu.setPrefSize(cols * T, rows * T);

        // Terrain
       new VueTerrain(modele, gridTerrain);

        // Obstacles dans le TilePane
        tilePaneEntites.setPrefTileWidth(T);
        tilePaneEntites.setPrefTileHeight(T);
        tilePaneEntites.setPrefColumns(cols);
        tilePaneEntites.setPrefSize(cols * T, rows * T);
        for (int i = 0; i < rows * cols; i++) {
            Region cellule = new Region();
            cellule.setPrefSize(T, T);
            tilePaneEntites.getChildren().add(cellule);
        }
        new VueObstacles(modele, tilePaneEntites);

        // Ennemi : spawn à la porte d'entrée, cible la porte de sortie
        double departX  = modele.getColonneEntree() * T;
        double departY  = modele.getLigneEntree() * T;
        double arriveeX = modele.getColonneSortie() * T;
        double arriveeY = modele.getLigneSortie() * T;

        ModeleEnnemi modeleEnnemi = new ModeleEnnemi(departX, departY, arriveeX, arriveeY, 128);
        VueEnnemi vueEnnemi = new VueEnnemi(modeleEnnemi, rectEnnemi);

        // Game loop
        new AnimationTimer() {
            long tempsPrecedent = -1;

            @Override
            public void handle(long now) {
                if (tempsPrecedent < 0) { tempsPrecedent = now; return; }

                double delta = (now - tempsPrecedent) / 1000000000.0;
                tempsPrecedent = now;

                modeleEnnemi.deplacer(delta);
                vueEnnemi.actualiser();

                if (modeleEnnemi.estArrive()) stop();
            }
        }.start();
    }
}
