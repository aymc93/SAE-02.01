package com.example.sae2;

import com.example.sae2.modele.autres.ModeleTerrain;
import com.example.sae2.vue.autres.VueTerrain;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        ModeleTerrain modele = new ModeleTerrain();
        VueTerrain vue = new VueTerrain(modele);

        int largeur = modele.getNbCols() * VueTerrain.TAILLE;
        int hauteur = modele.getNbLignes() * VueTerrain.TAILLE;

        Scene scene = new Scene(vue, largeur, hauteur);
        stage.setTitle("Flush Tower Defence");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}