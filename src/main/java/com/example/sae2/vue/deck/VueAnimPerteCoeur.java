package com.example.sae2.vue.deck;

import javafx.animation.FadeTransition;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;

public class VueAnimPerteCoeur {

    private static final String CHEMIN_IMAGE = "/com/example/sae2/images/UI/AnimePerteCoeur.png";
    private static final double DUREE_FADE   = 1.2;

    private final Pane  paneJeu;
    private final Image image;

    public VueAnimPerteCoeur(Pane paneJeu) {
        this.paneJeu = paneJeu;
        URL url = getClass().getResource(CHEMIN_IMAGE);
        this.image = (url != null) ? new Image(url.toExternalForm()) : null;
    }

    public void jouer() {
        double w = paneJeu.getWidth()  > 0 ? paneJeu.getWidth()  : paneJeu.getPrefWidth();
        double h = paneJeu.getHeight() > 0 ? paneJeu.getHeight() : paneJeu.getPrefHeight();

        ImageView iv = new ImageView();
        iv.setFitWidth(w);
        iv.setFitHeight(h);
        iv.setPreserveRatio(false);
        if (image != null) iv.setImage(image);

        // Voile rouge semi-transparent par-dessus l'image
        Rectangle rouge = new Rectangle(w, h, Color.rgb(200, 0, 0, 0.40));

        Group overlay = new Group(iv, rouge);
        overlay.setMouseTransparent(true);
        paneJeu.getChildren().add(overlay);

        FadeTransition fade = new FadeTransition(Duration.seconds(DUREE_FADE), overlay);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> paneJeu.getChildren().remove(overlay));
        fade.play();
    }
}
