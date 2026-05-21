package com.example.sae2.vue.carte;

import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.modele.carte.ModeleTypeObstacle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;

public class VueObstacles {

    private static final int T = ModeleTerrain.TAILLE_TUILE;

    private final Image imageBaril;
    private final Image imageCaillou;
    private final Image imagePorte;

    public VueObstacles(ModeleTerrain modele, TilePane tilePane) {
        String dossier = modele.getDossierSprites();

        imageBaril   = chargerImage("/com/example/sae2/images/" + dossier + "/Obstacles/obstacle.png");
        imageCaillou = chargerImage("/com/example/sae2/images/" + dossier + "/Obstacles/obstacle2.png");
        imagePorte   = chargerImage("/com/example/sae2/images/" + dossier + "/Obstacles/door.png");

        dessiner(modele, tilePane);
    }

    private void dessiner(ModeleTerrain modele, TilePane tilePane) {
        int cols = modele.getNbColonnes();
        ModeleTypeObstacle[][] grille = modele.getObstaclesGrille();

        // Obstacles
        for (int l = 0; l < modele.getNbLignes(); l++) {
            for (int c = 0; c < cols; c++) {
                ModeleTypeObstacle type = grille[l][c];
                if (type != null) {
                    Image img = (type == ModeleTypeObstacle.BARIL) ? imageBaril : imageCaillou;
                    tilePane.getChildren().set(l * cols + c, creerImageView(img, 0));
                }
            }
        }

        // Portes
        int indexEntree = modele.getLigneEntree() * cols + modele.getColonneEntree();
        int indexSortie = modele.getLigneSortie() * cols + modele.getColonneSortie();
        tilePane.getChildren().set(indexEntree, creerImageView(imagePorte, 270));
        tilePane.getChildren().set(indexSortie, creerImageView(imagePorte, 90));
    }

    private ImageView creerImageView(Image img, int angle) {
        if (img == null || img.isError()) return new ImageView();
        ImageView iv = new ImageView(img);
        iv.setFitWidth(T);
        iv.setFitHeight(T);
        iv.setPreserveRatio(false);
        if (angle != 0) iv.setRotate(angle);
        return iv;
    }

    private Image chargerImage(String chemin) {
        try {
            var stream = getClass().getResourceAsStream(chemin);
            if (stream != null) return new Image(stream);
        } catch (Exception e) {}
        return null;
    }
}
