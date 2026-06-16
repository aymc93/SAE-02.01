package com.example.sae2.vue.deck;

import com.example.sae2.modele.joueur.ModeleJoueur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.net.URL;

public class VueCoeurs {

    private static final double TAILLE_COEUR = 28;

    private final HBox      conteneur;
    private final ImageView[] slots = new ImageView[ModeleJoueur.VIES_MAX];
    private Image coeurPlein;
    private Image coeurVide;

    public VueCoeurs(HBox conteneur) {
        this.conteneur = conteneur;
        chargerSprites();
        construire();
    }

    private void chargerSprites() {
        URL urlPlein = getClass().getResource("/com/example/sae2/images/UI/coeur_plein.png");
        URL urlVide  = getClass().getResource("/com/example/sae2/images/UI/coeur_vide.png");
        if (urlPlein != null) coeurPlein = new Image(urlPlein.toExternalForm());
        if (urlVide  != null) coeurVide  = new Image(urlVide.toExternalForm());
    }

    private void construire() {
        for (int i = 0; i < ModeleJoueur.VIES_MAX; i++) {
            ImageView iv = new ImageView();
            iv.setFitWidth(TAILLE_COEUR);
            iv.setFitHeight(TAILLE_COEUR);
            iv.setPreserveRatio(true);
            if (coeurPlein != null) iv.setImage(coeurPlein);
            slots[i] = iv;
            conteneur.getChildren().add(iv);
        }
    }

    public void actualiser(int viesRestantes) {
        for (int i = 0; i < ModeleJoueur.VIES_MAX; i++) {
            slots[i].setImage(i < viesRestantes ? coeurPlein : coeurVide);
        }
    }
}
