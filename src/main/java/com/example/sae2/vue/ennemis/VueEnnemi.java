package com.example.sae2.vue.ennemis;

import com.example.sae2.modele.ennemis.ModeleEnnemi;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.net.URL;

public class VueEnnemi {

    private static final int TAILLE_SPRITE = 64;

    private final ModeleEnnemi modele;
    private final ImageView    imageView;
    private final Pane         pane;
    private final VueBarreVie  barreVie;

    public VueEnnemi(ModeleEnnemi modele, Pane pane, String dossierSprites) {
        this.modele    = modele;
        this.pane      = pane;
        this.imageView = new ImageView();

        imageView.setFitWidth(TAILLE_SPRITE);
        imageView.setFitHeight(TAILLE_SPRITE);
        imageView.setPreserveRatio(false);

        String chemin = "/com/example/sae2/images/" + dossierSprites + "/Ennemis/"
                + modele.getTypeEnnemi() + "/" + modele.getTypeEnnemi() + ".gif";

        URL url = getClass().getResource(chemin);
        if (url == null) {
            System.err.println("[VueEnnemi] GIF introuvable : " + chemin);
        } else {
            imageView.setImage(new Image(url.toExternalForm()));
        }

        pane.getChildren().add(imageView);
        this.barreVie = new VueBarreVie(modele, pane);
        actualiser();
    }

    /** Met à jour la position du sprite et de la barre de vie. */
    public void actualiser() {
        imageView.setLayoutX(modele.getX());
        imageView.setLayoutY(modele.getY());
        barreVie.actualiser();
    }

    /** Retire le sprite et la barre de vie du terrain. */
    public void supprimer() {
        pane.getChildren().remove(imageView);
        barreVie.supprimer();
    }
}
