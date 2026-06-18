package com.example.sae2.vue.boutique;

import com.example.sae2.modele.Environnement;
import com.example.sae2.modele.deck.TypeCarte;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class VueBoutique {

    private static final String DOSSIER_CARTES = "/com/example/sae2/images/Cartes/";

    private final Pane paneJeu;
    private final VBox interfaceBoutique;
    private final VBox interfaceChoixBooster;
    private final ImageView choix1View;
    private final ImageView choix2View;

    public VueBoutique(Pane paneJeu, VBox interfaceBoutique, VBox interfaceChoixBooster,
                       Label labelPrixBooster, int prixBooster,
                       ImageView choix1View, ImageView choix2View) {
        this.paneJeu = paneJeu;
        this.interfaceBoutique = interfaceBoutique;
        this.interfaceChoixBooster = interfaceChoixBooster;
        this.choix1View = choix1View;
        this.choix2View = choix2View;
        labelPrixBooster.setText("Prix du booster : " + prixBooster + " pièces");
    }

    private void centrer(Region noeud) {
        noeud.setLayoutX((paneJeu.getWidth()  - noeud.getPrefWidth())  / 2);
        noeud.setLayoutY((paneJeu.getHeight() - noeud.getPrefHeight()) / 2);
    }

    public void ouvrirBoutique() {
        centrer(interfaceBoutique);
        interfaceBoutique.setVisible(true);
    }

    public void fermerBoutique() {
        interfaceBoutique.setVisible(false);
    }

    public void afficherChoix(TypeCarte carte1, TypeCarte carte2) {
        choix1View.setImage(chargerImageCarte(carte1));
        choix2View.setImage(chargerImageCarte(carte2));
        fermerBoutique();
        centrer(interfaceChoixBooster);
        interfaceChoixBooster.setVisible(true);
    }

    public void fermerChoix() {
        interfaceChoixBooster.setVisible(false);
    }

    private Image chargerImageCarte(TypeCarte carte) {
        return new Image(getClass()
                .getResource(DOSSIER_CARTES + Environnement.getNomImageCarte(carte))
                .toExternalForm());
    }
}
