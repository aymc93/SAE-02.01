package com.example.sae2.vue.ennemis;

import com.example.sae2.modele.ennemis.ModeleEnnemi;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.net.URL;

public class VueBarreVie {

    private static final int PV_PAR_COEUR = 50;
    private static final double TAILLE_COEUR = 6;
    private static final double ESPACEMENT = 1;
    private static final double OFFSET_Y = 10; // pixels au-dessus du sprite ennemi

    private final ModeleEnnemi modele;
    private final Pane pane;
    private final Group barre;
    private final ImageView[] slots;
    private final Image coeurPlein;
    private final Image coeurVide;

    private int pvPrecedent = -1;

    public VueBarreVie(ModeleEnnemi modele, Pane pane) {
        this.modele = modele;
        this.pane = pane;

        URL urlPlein = getClass().getResource("/com/example/sae2/images/UI/coeur_plein.png");
        URL urlVide = getClass().getResource("/com/example/sae2/images/UI/coeur_vide.png");
        coeurPlein = (urlPlein != null) ? new Image(urlPlein.toExternalForm()) : null;
        coeurVide = (urlVide  != null) ? new Image(urlVide.toExternalForm()) : null;

        int nbCoeurs = modele.getPvMax() / PV_PAR_COEUR;
        slots = new ImageView[nbCoeurs];
        barre = new Group();

        for (int i = 0; i < nbCoeurs; i++) {
            ImageView iv = new ImageView();
            iv.setFitWidth(TAILLE_COEUR);
            iv.setFitHeight(TAILLE_COEUR);
            iv.setPreserveRatio(false);
            if (coeurPlein != null) iv.setImage(coeurPlein);
            iv.setLayoutX(i * (TAILLE_COEUR + ESPACEMENT));
            slots[i] = iv;
            barre.getChildren().add(iv);
        }
        pane.getChildren().add(barre);
        actualiser();
    }

    public void actualiser() {
        int pv = modele.getPv();

        if (pv != pvPrecedent) {
            int coeursPleinCourants = (int) Math.ceil((double) pv / PV_PAR_COEUR);
            for (int i = 0; i < slots.length; i++) {
                slots[i].setImage(i < coeursPleinCourants ? coeurPlein : coeurVide);
            }
            pvPrecedent = pv;
        }

        double largeurBarre = slots.length * (TAILLE_COEUR + ESPACEMENT) - ESPACEMENT;
        barre.setLayoutX(modele.getX() + 32 - largeurBarre / 2.0);
        barre.setLayoutY(modele.getY() - TAILLE_COEUR - OFFSET_Y);
    }

    public void supprimer() {
        pane.getChildren().remove(barre);
    }
}
