package com.example.sae2.vue.carte;

import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.modele.carte.ModeleTypeObstacle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class VueObstacles {

    private static final int T = ModeleTerrain.TAILLE_TUILE;

    private final Image imageBaril;
    private final Image imageCaillou;
    private final Image imagePorte;

    public VueObstacles(ModeleTerrain modele, Pane pane) {
        String dossier = modele.getDossierSprites();

        imageBaril   = chargerImage("/com/example/sae2/images/" + dossier + "/Obstacles/obstacle.png");
        imageCaillou = chargerImage("/com/example/sae2/images/" + dossier + "/Obstacles/obstacle2.png");
        imagePorte   = chargerImage("/com/example/sae2/images/" + dossier + "/Obstacles/door.png");

        dessiner(modele, pane);
    }

    private void dessiner(ModeleTerrain modele, Pane pane) {
        int cols = modele.getNbColonnes();
        Image img;
        ModeleTypeObstacle[][] grille = modele.getObstaclesGrille();

        // Obstacles
        for (int l = 0; l < modele.getNbLignes(); l++) {
            for (int c = 0; c < cols; c++) {
                ModeleTypeObstacle type = grille[l][c];
                if (type != null) {
                    if (type == ModeleTypeObstacle.BARIL) {
                        img = imageBaril;
                    } else {
                        img = imageCaillou;
                    }
                    ImageView iv = creerImageView(img, 0);
                    iv.setLayoutX((double) c * T);
                    iv.setLayoutY((double) l * T);
                    pane.getChildren().set(l * cols + c, iv);
                }
            }
        }

        // Portes
        int indexEntree = modele.getLigneEntree() * cols + modele.getColonneEntree();
        int indexSortie = modele.getLigneSortie() * cols + modele.getColonneSortie();

        ImageView ivEntree = creerImageView(imagePorte, 270);
        ivEntree.setLayoutX((double) modele.getColonneEntree() * T);
        ivEntree.setLayoutY((double) modele.getLigneEntree() * T);
        pane.getChildren().set(indexEntree, ivEntree);

        ImageView ivSortie = creerImageView(imagePorte, 90);
        ivSortie.setLayoutX((double) modele.getColonneSortie() * T);
        ivSortie.setLayoutY((double) modele.getLigneSortie() * T);
        pane.getChildren().set(indexSortie, ivSortie);
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
