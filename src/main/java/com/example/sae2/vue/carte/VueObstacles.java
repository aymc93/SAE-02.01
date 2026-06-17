package com.example.sae2.vue.carte;

import com.example.sae2.controller.GameController;
import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.modele.carte.ModeleTypeObstacle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class VueObstacles {

    private static final int T = ModeleTerrain.TAILLE_TUILE;

    private final GameController mainController;
    private Image imageBaril;
    private Image imageCaillou;
    private Image imagePorte;

    public VueObstacles(GameController mainController, ModeleTerrain modele, Pane pane) {
        this.mainController = mainController;
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

                // Mémorisation de la case actuelle dans les propriétés de l'image
                iv.getProperties().put("oldCol", c);
                iv.getProperties().put("oldLigne", l);

                // On transforme l'image en objet déplaçable !
                iv.setStyle("-fx-cursor: hand;");
                ajouterDragAndDrop(iv);

                pane.getChildren().add(iv);
            }
        }

        ajouterPorte(pane, modele.getColonneEntree(), modele.getLigneEntree(), 270);
        ajouterPorte(pane, modele.getColonneSortie(), modele.getLigneSortie(), 90);
    }

    private void ajouterDragAndDrop(ImageView iv) {
        // Quand on clique sur l'obstacle
        iv.setOnMousePressed(event -> {
            iv.toFront(); // L'image passe au-dessus des autres pour être bien visible
            iv.getProperties().put("initX", iv.getLayoutX());
            iv.getProperties().put("initY", iv.getLayoutY());
            iv.getProperties().put("mouseStartX", event.getSceneX());
            iv.getProperties().put("mouseStartY", event.getSceneY());
            event.consume(); // Empêche de poser une tour en cliquant sur un obstacle
        });

        // Quand on bouge la souris en restant cliqué
        iv.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - (double) iv.getProperties().get("mouseStartX");
            double deltaY = event.getSceneY() - (double) iv.getProperties().get("mouseStartY");
            iv.setLayoutX((double) iv.getProperties().get("initX") + deltaX);
            iv.setLayoutY((double) iv.getProperties().get("initY") + deltaY);
            event.consume();
        });

        // Quand on relâche le clic gauche
        iv.setOnMouseReleased(event -> {
            int oldC = (int) iv.getProperties().get("oldCol");
            int oldL = (int) iv.getProperties().get("oldLigne");

            // On demande au Contrôleur principal de vérifier la case d'arrivée
            mainController.traiterDropObstacle(iv, oldC, oldL);
            event.consume();
        });
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


    public void actualiserNiveau(ModeleTerrain modele, Pane pane) {
        // On supprime les anciens obstacles
        pane.getChildren().clear();

        // On recharge les nouvelles images
        String dossier = modele.getDossierSprites();
        // (Note: supprime le "final" sur imageBaril, imageCaillou et imagePorte en haut de ta classe si ça fait une erreur ici !)
        imageBaril   = chargerImage("/com/example/sae2/images/" + dossier + "/Obstacles/obstacle.png");
        imageCaillou = chargerImage("/com/example/sae2/images/" + dossier + "/Obstacles/obstacle2.png");
        imagePorte   = chargerImage("/com/example/sae2/images/" + dossier + "/Obstacles/door.png");

        // On redessine tout
        dessiner(modele, pane);
    }
}