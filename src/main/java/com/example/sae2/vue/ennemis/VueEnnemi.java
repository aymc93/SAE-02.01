package com.example.sae2.vue.ennemis;

import com.example.sae2.modele.ennemis.ModeleEnnemi;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;

public class VueEnnemi {

    private static final int TAILLE_SPRITE = 64;

    private final ModeleEnnemi modele;
    private final ImageView    imageView;

    public VueEnnemi(ModeleEnnemi modele, ImageView imageView, String dossierSprites) {
        this.modele    = modele;
        this.imageView = imageView;

        String chemin = "/com/example/sae2/images/" + "Level1-10/Ennemis/" + modele.getTypeEnnemi()
                + "/" + modele.getTypeEnnemi() + ".gif";

        URL url = getClass().getResource(chemin);
        if (url == null) {
            System.err.println("[VueEnnemi] GIF introuvable : " + chemin);
        } else {
            imageView.setImage(new Image(url.toExternalForm()));
        }

        imageView.setFitWidth(TAILLE_SPRITE);
        imageView.setFitHeight(TAILLE_SPRITE);
        imageView.setPreserveRatio(false);

        actualiser();
    }

    /**
     * Met a jour uniquement la position.
     * L'animation du GIF est geree en interne par JavaFX.
     */
    public void actualiser() {
        imageView.setLayoutX(modele.getX());
        imageView.setLayoutY(modele.getY());
    }
}
