package com.example.sae2.vue.carte;

import com.example.sae2.modele.carte.ModeleTerrain;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

public class VueTerrain {

    private static final int T = ModeleTerrain.TAILLE_TUILE;

    private final Image[] imagesSolSimple;
    private final Image[] imagesMurBord;
    private final Image[] imagesMurCoin;

    public VueTerrain(ModeleTerrain terrain, GridPane grille) {
        String dossier = terrain.getDossierSprites();

        imagesSolSimple = chargerImages("/com/example/sae2/images/" + dossier + "/Sols/Sol");
        imagesMurBord = chargerImages("/com/example/sae2/images/" + dossier + "/Murs/Mur_Bord");
        imagesMurCoin = chargerImages("/com/example/sae2/images/" + dossier + "/Murs/Mur_Coin");

        peupler(terrain, grille);
    }

    private void peupler(ModeleTerrain terrain, GridPane grille) {
        for (int l = 0; l < terrain.getNbLignes(); l++) {
            for (int c = 0; c < terrain.getNbColonnes(); c++) {
                VueTypeTuile tuile = terrain.getTuile(l, c);

                ImageView iv = new ImageView(selectionnerImage(tuile, l, c, terrain));
                iv.setFitWidth(T);
                iv.setFitHeight(T);
                iv.setPreserveRatio(false);
                iv.setRotate(tuile.getAngle());

                grille.add(iv, c, l);
            }
        }
    }

    private Image selectionnerImage(VueTypeTuile tuile, int l, int c, ModeleTerrain terrain) {
        if (tuile.isSolSimple()) {
            return imageAleatoire(imagesSolSimple);
        } else if (tuile.isMurBord()) {
            return imageMurBord(imagesMurBord, tuile, l, c, terrain);
        } else {
            return imageAleatoire(imagesMurCoin);
        }
    }

    private Image[] chargerImages(String cheminBase) {
        List<Image> liste = new ArrayList<>();
        liste.add(new Image(getClass().getResourceAsStream(cheminBase + ".png")));
        int i = 2;
        while (true) {
            var stream = getClass().getResourceAsStream(cheminBase + i + ".png");
            if (stream == null) break;
            liste.add(new Image(stream));
            i++;
        }
        return liste.toArray(new Image[0]);
    }

    private Image imageAleatoire(Image[] images) {
        if (images.length == 1) return images[0];
        return images[(int) (Math.random() * images.length)];
    }

    private Image imageMurBord(Image[] images, VueTypeTuile tuile, int l, int c, ModeleTerrain terrain) {
        int distance = -1;
        if (tuile == VueTypeTuile.MUR_BORD_HAUT || tuile == VueTypeTuile.MUR_BORD_BAS) {
            distance = Math.min(c, terrain.getNbColonnes() - 1 - c);
        } else if (tuile == VueTypeTuile.MUR_BORD_GAUCHE || tuile == VueTypeTuile.MUR_BORD_DROITE) {
            distance = Math.min(l, terrain.getNbLignes() - 1 - l);
        }
        if (distance == 1) return images[0];
        if (distance == 2 && images.length >= 2) return images[1];
        if (images.length > 2) return images[2 + (int) (Math.random() * (images.length - 2))];
        return images[(int) (Math.random() * images.length)];
    }
}
