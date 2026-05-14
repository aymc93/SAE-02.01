package com.example.sae2;

import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.vue.autres.carte.VueTerrain;
import com.example.sae2.vue.autres.carte.VueObstacles;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {

        // 1. Tu crées ton modèle (par défaut il se met au niveau 1)
        ModeleTerrain modele = new ModeleTerrain();

        modele.setNiveau(45);


        // 2. Tu crées tes vues (elles vont lire que le niveau est 15 et prendre le bon dossier)
        VueTerrain   vueTerrain   = new VueTerrain(modele);
        VueObstacles vueObstacles = new VueObstacles(modele);

        int largeur = modele.getNbColonnes() * ModeleTerrain.TAILLE_TUILE;
        int hauteur = modele.getNbLignes()   * ModeleTerrain.TAILLE_TUILE;

        StackPane root = new StackPane(vueTerrain, vueObstacles);
        Scene scene = new Scene(root, largeur, hauteur);

        stage.setTitle("Tower Defence – Salle intérieure");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}