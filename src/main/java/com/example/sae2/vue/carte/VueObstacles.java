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
        ModeleTypeObstacle[][] grille = modele.getObstaclesGrille();

        for (int l = 0; l < modele.getNbLignes(); l++) {
            for (int c = 0; c < modele.getNbColonnes(); c++) {
                ModeleTypeObstacle type = grille[l][c];
                if (type == null) continue;

                Image img = (type == ModeleTypeObstacle.BARIL) ? imageBaril : imageCaillou;
                ImageView iv = creerImageView(img, 0);
                iv.setLayoutX((double) c * T);
                iv.setLayoutY((double) l * T);
                pane.getChildren().add(iv);
            }
        }

        ajouterPorte(pane, modele.getColonneEntree(), modele.getLigneEntree(), 270);
        ajouterPorte(pane, modele.getColonneSortie(), modele.getLigneSortie(), 90);
    }

    private void ajouterPorte(Pane pane, int col, int ligne, int angle) {
        ImageView iv = creerImageView(imagePorte, angle);
        iv.setLayoutX((double) col * T);
        iv.setLayoutY((double) ligne * T);
        pane.getChildren().add(iv);
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
