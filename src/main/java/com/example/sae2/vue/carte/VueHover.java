package com.example.sae2.vue.carte;

import javafx.scene.image.ImageView;

public class VueHover {

    private final ImageView hoverCase;
    private final int taille;

    public VueHover(ImageView hoverCase, int taille) {
        this.hoverCase = hoverCase;
        this.taille    = taille;
    }

    /* Déplace l'indicateur sur la case survolée et le rend visible. */
    public void actualiser(double mouseX, double mouseY) {
        int col   = (int) (mouseX / taille);
        int ligne = (int) (mouseY / taille);
        hoverCase.setLayoutX(col   * taille);
        hoverCase.setLayoutY(ligne * taille);
        hoverCase.setVisible(true);
    }

    public void cacher() {
        hoverCase.setVisible(false);
    }
}
