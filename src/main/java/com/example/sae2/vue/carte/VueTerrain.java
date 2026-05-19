package com.example.sae2.vue.autres.carte;

import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.modele.carte.ModeleTypeTuile;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class VueTerrain extends Canvas {

    private static final int T = ModeleTerrain.TAILLE_TUILE;

    private final Image[] imagesSolSimple;
    private final Image[] imagesMurBord;
    private final Image[] imagesMurCoin;

    public VueTerrain(ModeleTerrain terrain) {
        super(terrain.getNbColonnes() * T, terrain.getNbLignes() * T);

        String dossier = terrain.getDossierSprites();

        imagesSolSimple = chargerVariations("/com/example/sae2/images/" + dossier + "/Sols/Sol");
        imagesMurBord   = chargerVariations("/com/example/sae2/images/" + dossier + "/Murs/Mur_Bord");
        imagesMurCoin   = chargerVariations("/com/example/sae2/images/" + dossier + "/Murs/Mur_Coin");

        dessiner(terrain);
    }

    private void dessiner(ModeleTerrain terrain) {
        GraphicsContext gc = getGraphicsContext2D();

        for (int l = 0; l < terrain.getNbLignes(); l++) {
            for (int c = 0; c < terrain.getNbColonnes(); c++) {

                int x = c * T;
                int y = l * T;
                ModeleTypeTuile tuile = terrain.getTuile(l, c);

                Image imageADessiner = null;

                if (tuile.isSolSimple()) {
                    imageADessiner = getImageAleatoire(imagesSolSimple);
                } else if (tuile.isMurBord()) {
                    // NOUVEAU : On utilise la fonction intelligente pour le pattern des murs
                    imageADessiner = getImageMurBord(imagesMurBord, tuile, l, c, terrain);
                } else if (tuile.isMurCoin()) {
                    imageADessiner = getImageAleatoire(imagesMurCoin);
                }

                dessinerTuileAvecRotation(gc, imageADessiner, x, y, tuile.getAngle());
            }
        }
    }

    // --- FONCTIONS DE CHARGEMENT INTELLIGENT ---

    private Image[] chargerVariations(String cheminBase) {
        List<Image> listeImages = new ArrayList<>();

        Image img1 = chargerImageSilencieux(cheminBase + ".png");
        if (img1 != null) listeImages.add(img1);

        int i = 2;
        while (true) {
            Image img = chargerImageSilencieux(cheminBase + i + ".png");
            if (img != null) {
                listeImages.add(img);
                i++;
            } else {
                break;
            }
        }

        if (listeImages.isEmpty()) {
            System.out.println("[VueTerrain] Avertissement : Aucune image pour " + cheminBase);
        }

        return listeImages.toArray(new Image[0]);
    }

    private Image chargerImageSilencieux(String chemin) {
        try {
            var stream = getClass().getResourceAsStream(chemin);
            if (stream != null) {
                Image img = new Image(stream);
                if (!img.isError()) return img;
            }
        } catch (Exception e) {}
        return null;
    }

    // --- FONCTIONS POUR CHOISIR LES IMAGES ---

    private Image getImageAleatoire(Image[] images) {
        if (images == null || images.length == 0) return null;
        if (images.length == 1) return images[0];

        int index = (int) (Math.random() * images.length);
        return images[index];
    }

    /**
     * NOUVELLE MÉTHODE : Applique le pattern des coins (Image 1, puis Image 2)
     * et randomise le reste du mur au centre.
     */
    private Image getImageMurBord(Image[] images, ModeleTypeTuile tuile, int l, int c, ModeleTerrain terrain) {
        if (images == null || images.length == 0) return null;

        int distance = -1;

        // 1. Calculer à quelle distance on est du coin le plus proche
        if (tuile == ModeleTypeTuile.MUR_BORD_HAUT || tuile == ModeleTypeTuile.MUR_BORD_BAS) {
            // Pour les murs horizontaux, on regarde la distance par rapport aux colonnes des coins (0 et max)
            distance = Math.min(c, terrain.getNbColonnes() - 1 - c);
        } else if (tuile == ModeleTypeTuile.MUR_BORD_GAUCHE || tuile == ModeleTypeTuile.MUR_BORD_DROITE) {
            // Pour les murs verticaux, on regarde la distance par rapport aux lignes des coins (0 et max)
            distance = Math.min(l, terrain.getNbLignes() - 1 - l);
        }

        // 2. Appliquer le pattern selon la distance
        if (distance == 1 && images.length >= 1) {
            return images[0]; // Mur_Bord.png collé au coin
        }
        else if (distance == 2 && images.length >= 2) {
            return images[1]; // Mur_Bord2.png juste après
        }
        else {
            // Pour tous les autres murs plus loin : Random parmi le RESTE des images (index 2 et plus)
            if (images.length > 2) {
                int index = 2 + (int) (Math.random() * (images.length - 2));
                return images[index];
            } else {
                // Sécurité s'il n'y a que 2 images dans le dossier, on prend au pif
                int index = (int) (Math.random() * images.length);
                return images[index];
            }
        }
    }

    private void dessinerTuileAvecRotation(GraphicsContext gc, Image image, int x, int y, int angle) {
        if (image != null && !image.isError()) {
            if (angle == 0) {
                gc.drawImage(image, x, y, T, T);
            } else {
                gc.save();
                gc.translate(x + T / 2.0, y + T / 2.0);
                gc.rotate(angle);
                gc.drawImage(image, -T / 2.0, -T / 2.0, T, T);
                gc.restore();
            }
        } else {
            gc.fillRect(x, y, T, T);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(0.5);
            gc.strokeRect(x, y, T, T);
        }
    }
}