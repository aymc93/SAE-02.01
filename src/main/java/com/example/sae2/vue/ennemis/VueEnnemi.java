package com.example.sae2.vue.ennemis;

import com.example.sae2.modele.ennemis.ModeleEnnemi;
import javafx.scene.shape.Rectangle;

public class VueEnnemi {

    private final ModeleEnnemi modele;
    private final Rectangle rectangle;

    public VueEnnemi(ModeleEnnemi modele, Rectangle rectangle) {
        this.modele    = modele;
        this.rectangle = rectangle;
        actualiser();
    }

    public void actualiser() {
        rectangle.setLayoutX(modele.getX());
        rectangle.setLayoutY(modele.getY());
    }
}
