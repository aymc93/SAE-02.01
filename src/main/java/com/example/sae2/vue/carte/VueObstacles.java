package com.example.sae2.vue.autres.carte;

import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.modele.carte.ModeleObstacle;
import com.example.sae2.modele.carte.ModeleTypeObstacle;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class VueObstacles extends Canvas {

    private static final int T = ModeleTerrain.TAILLE_TUILE;

    private final Image imageBaril;
    private final Image imageCaillou;
    private final Image imagePorteEntree;
    private final Image imagePorteSortie;

    public VueObstacles(ModeleTerrain modele) {
        super(modele.getNbColonnes() * T, modele.getNbLignes() * T);

        // On récupère le dossier dynamique (ex: Level1-10 ou Level11-20)
        String dossier = modele.getDossierSprites();

        // Les chemins deviennent dynamiques !
        imageBaril   = chargerImage("/com/example/sae2/images/" + dossier + "/Obstacles/obstacle.png");
        imageCaillou = chargerImage("/com/example/sae2/images/" + dossier + "/Obstacles/obstacle2.png");

        // Si tes portes sont dans le dossier Obstacles de chaque niveau
        imagePorteEntree = chargerImage("/com/example/sae2/images/" + dossier + "/Obstacles/door.png");
        imagePorteSortie = chargerImage("/com/example/sae2/images/" + dossier + "/Obstacles/door.png");

        dessiner(modele);
    }

    private void dessiner(ModeleTerrain modele) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        // 1. Obstacles
        for (ModeleObstacle obs : modele.getObstacles()) {
            int x = obs.getColonne() * T;
            int y = obs.getLigne() * T;
            Image img = (obs.getType() == ModeleTypeObstacle.BARIL) ? imageBaril : imageCaillou;
            if (img != null) gc.drawImage(img, x, y, T, T);
        }

        // 2. Portes (Elles apparaîtront toujours aux coordonnées calculées par le modèle)
        dessinerImageAvecRotation(gc, imagePorteEntree, modele.getColonneEntree() * T, modele.getLigneEntree() * T, 270);
        dessinerImageAvecRotation(gc, imagePorteSortie, modele.getColonneSortie() * T, modele.getLigneSortie() * T, 90);
    }

    private void dessinerImageAvecRotation(GraphicsContext gc, Image image, int x, int y, int angle) {
        if (image != null && !image.isError()) {
            gc.save();
            gc.translate(x + T / 2.0, y + T / 2.0);
            gc.rotate(angle);
            gc.drawImage(image, -T / 2.0, -T / 2.0, T, T);
            gc.restore();
        }
    }

    private Image chargerImage(String chemin) {
        try {
            var stream = getClass().getResourceAsStream(chemin);
            if (stream != null) return new Image(stream);
        } catch (Exception e) {}
        return null;
    }
}