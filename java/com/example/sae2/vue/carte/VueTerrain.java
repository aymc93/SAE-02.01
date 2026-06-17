package com.example.sae2.vue.carte;

import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.modele.carte.TypeTuile;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;

import java.util.ArrayList;
import java.util.List;

public class VueTerrain {

    private static final int T = ModeleTerrain.TAILLE_TUILE;

    // Retrait du 'final' pour pouvoir écraser les tableaux d'images lors du changement de niveau
    private Image[] imagesSolSimple;
    private Image[] imagesMurBord;
    private Image[] imagesMurCoin;

    public VueTerrain(ModeleTerrain terrain, TilePane grille) {
        // Configuration de base du TilePane
        int cols = terrain.getNbColonnes();
        int rows = terrain.getNbLignes();
        grille.setPrefTileWidth(T);
        grille.setPrefTileHeight(T);
        grille.setPrefColumns(cols);
        grille.setPrefSize(cols * T, rows * T);

        // Chargement visuel initial (Niveau 1)
        actualiserNiveau(terrain, grille);
    }

    /**
     * CORRECTION : Cette méthode vide la grille actuelle, recharge les images
     * depuis le nouveau dossier de sprites et reconstruit la carte visuellement.
     */
    public void actualiserNiveau(ModeleTerrain terrain, TilePane grille) {
        // 1. On nettoie tout ce qui est actuellement affiché à l'écran
        grille.getChildren().clear();

        // 2. On récupère le nouveau dossier de sprites mis à jour
        String dossier = terrain.getDossierSprites();
        System.out.println("[VueTerrain] Chargement visuel des sprites depuis le dossier : " + dossier);

        // 3. On recharge les bonnes images en mémoire
        imagesSolSimple = chargerImages("/com/example/sae2/images/" + dossier + "/Sols/Sol");
        imagesMurBord   = chargerImages("/com/example/sae2/images/" + dossier + "/Murs/Mur_Bord");
        imagesMurCoin   = chargerImages("/com/example/sae2/images/" + dossier + "/Murs/Mur_Coin");

        // 4. On ré-affiche toutes les tuiles
        peupler(terrain, grille);
    }

    private void peupler(ModeleTerrain terrain, TilePane grille) {
        for (int l = 0; l < terrain.getNbLignes(); l++) {
            for (int c = 0; c < terrain.getNbColonnes(); c++) {
                TypeTuile tuile = terrain.getTuile(l, c);

                ImageView iv = new ImageView(selectionnerImage(tuile, l, c, terrain));
                iv.setFitWidth(T);
                iv.setFitHeight(T);
                iv.setPreserveRatio(false);
                iv.setRotate(tuile.getAngle());

                grille.getChildren().add(iv);
            }
        }
    }

    private Image selectionnerImage(TypeTuile tuile, int l, int c, ModeleTerrain terrain) {
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
        var initialStream = getClass().getResourceAsStream(cheminBase + ".png");
        if (initialStream != null) {
            liste.add(new Image(initialStream));
        } else {
            System.out.println("[Alerte VueTerrain] Impossible de trouver l'image de base : " + cheminBase + ".png");
            return new Image[0];
        }

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
        if (images == null || images.length == 0) return null;
        if (images.length == 1) return images[0];
        return images[(int) (Math.random() * images.length)];
    }

    private Image imageMurBord(Image[] images, TypeTuile tuile, int l, int c, ModeleTerrain terrain) {
        if (images == null || images.length == 0) return null;
        int distance = -1;
        if (tuile == TypeTuile.MUR_BORD_HAUT || tuile == TypeTuile.MUR_BORD_BAS) {
            distance = Math.min(c, terrain.getNbColonnes() - 1 - c);
        } else if (tuile == TypeTuile.MUR_BORD_GAUCHE || tuile == TypeTuile.MUR_BORD_DROITE) {
            distance = Math.min(l, terrain.getNbLignes() - 1 - l);
        }
        if (distance == 1) return images[0];
        if (distance == 2 && images.length >= 2) return images[1];
        if (images.length > 2) return images[2 + (int) (Math.random() * (images.length - 2))];
        return images[(int) (Math.random() * images.length)];
    }
}