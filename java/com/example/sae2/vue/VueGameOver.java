package com.example.sae2.vue;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;

public class VueGameOver {

    public void afficher(Stage stageCourant) {
        URL url = getClass().getResource("/com/example/sae2/images/UI/dead.png");
        if (url == null) {
            System.err.println("[VueGameOver] Image dead.png introuvable.");
            return;
        }

        Image image = new Image(url.toExternalForm());

        double largeur = (image.getWidth()  > 0 ? image.getWidth()  : 600) / 3.0;
        double hauteur = (image.getHeight() > 0 ? image.getHeight() : 600) / 3.0;

        ImageView iv = new ImageView(image);
        iv.setFitWidth(largeur);
        iv.setFitHeight(hauteur);
        iv.setPreserveRatio(false);

        StackPane root = new StackPane(iv);

        Stage stageGameOver = new Stage();
        stageGameOver.setTitle("Game Over");
        stageGameOver.setScene(new Scene(root, largeur, hauteur));
        stageGameOver.setResizable(false);
        stageGameOver.show();

        stageCourant.close();
    }
}
