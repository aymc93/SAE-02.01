package com.example.sae2.vue.tours;

import com.example.sae2.modele.ennemis.ModeleEnnemi;
import com.example.sae2.modele.tours.ModeleTour;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.util.Duration;

import java.net.URL;
import java.util.Random;

public class VueTour {

    // Indices dans DIRECTIONS / idle[]
    private static final int DIR_BAS    = 0;
    private static final int DIR_DROITE = 1;
    private static final int DIR_HAUT   = 2;
    private static final int DIR_GAUCHE = 3;
    private static final String[] DIRECTIONS = {"bas", "droite", "haut", "gauche"};

    private static final int    PORTEE_BLOCS = 3;
    private static final Random RNG          = new Random();

    private final ImageView    imageView;
    private final Image        appear1;
    private final Image        appear2;
    private final Image[]      idle      = new Image[4];
    private final Image[]      idleBlink = new Image[4];

    private final double       centreTourX;
    private final double       centreTourY;
    private final double       porteePixels;
    private final ModeleEnnemi ennemi;

    private final Circle portee;

    private int     directionActuelle    = 0;
    private boolean enApparition         = true;
    private boolean enClignotement       = false;
    private boolean directionVerrouillee = false;
    private boolean ennemiDetecte        = false;
    private Timeline timelineEnCours;

    public VueTour(ModeleTour modele, Pane pane, int tailleTuile, ModeleEnnemi ennemi) {
        this.ennemi       = ennemi;
        this.porteePixels = PORTEE_BLOCS * tailleTuile;

        centreTourX = modele.getColonne() * tailleTuile + tailleTuile / 2.0;
        centreTourY = modele.getLigne()   * tailleTuile + tailleTuile / 2.0;

        // Cercle de portée (ajouté en premier → rendu sous le sprite)
        portee = new Circle(centreTourX, centreTourY, porteePixels);
        portee.setStrokeType(StrokeType.INSIDE);
        portee.setStrokeWidth(1.5);
        setCouleurPortee(false);

        // Sprite de la tour
        imageView = new ImageView();
        imageView.setFitWidth(tailleTuile);
        imageView.setFitHeight(tailleTuile);
        imageView.setPreserveRatio(false);
        imageView.setLayoutX(modele.getColonne() * tailleTuile);
        imageView.setLayoutY(modele.getLigne()   * tailleTuile);

        appear1 = chargerImage("appear1.png");
        appear2 = chargerImage("appear2.png");
        for (int i = 0; i < DIRECTIONS.length; i++) {
            idle[i]      = chargerImage("idle_" + DIRECTIONS[i] + ".png");
            idleBlink[i] = chargerImage("idle_" + DIRECTIONS[i] + "_blink.png");
        }

        pane.getChildren().addAll(portee, imageView);
        jouerApparition();
        demarrerTracking();
    }

    // -------------------------------------------------------------------------
    // Chargement d'image
    // -------------------------------------------------------------------------

    private Image chargerImage(String nom) {
        String chemin = "/com/example/sae2/images/SimpleTower/" + nom;
        URL url = getClass().getResource(chemin);
        if (url == null) {
            System.err.println("[VueTour] Image introuvable : " + chemin);
            return null;
        }
        return new Image(url.toExternalForm());
    }

    // -------------------------------------------------------------------------
    // Animation d'apparition
    // -------------------------------------------------------------------------

    private void jouerApparition() {
        enApparition = true;
        imageView.setImage(appear1);

        Timeline apparition = new Timeline(
            new KeyFrame(Duration.millis(150), e -> imageView.setImage(appear2)),
            new KeyFrame(Duration.millis(300), e -> {
                enApparition = false;
                planifierIdle();
            })
        );
        apparition.play();
    }

    // -------------------------------------------------------------------------
    // Boucle idle / blink
    // -------------------------------------------------------------------------

    private void planifierIdle() {
        enClignotement = false;
        imageView.setImage(idle[directionActuelle]);

        double dureeMs = 2000 + RNG.nextInt(3000); // 2 à 5 s

        timelineEnCours = new Timeline(
            new KeyFrame(Duration.millis(dureeMs), e -> jouerBlink())
        );
        timelineEnCours.play();
    }

    private void jouerBlink() {
        enClignotement = true;
        imageView.setImage(idleBlink[directionActuelle]);

        timelineEnCours = new Timeline(
            new KeyFrame(Duration.millis(150), e -> planifierIdle())
        );
        timelineEnCours.play();
    }

    // -------------------------------------------------------------------------
    // Suivi de l'ennemi chaque frame
    // -------------------------------------------------------------------------

    private void demarrerTracking() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (enApparition) return;

                double ennemiCX = ennemi.getX() + 32;
                double ennemiCY = ennemi.getY() + 32;

                double dx       = ennemiCX - centreTourX;
                double dy       = ennemiCY - centreTourY;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance <= porteePixels) {
                    if (!ennemiDetecte) {
                        ennemiDetecte = true;
                        setCouleurPortee(true);
                    }
                    directionVerrouillee = true;

                    // Ne pas changer l'image pendant un clignotement
                    if (!enClignotement) {
                        int nouvelleDir = calculerDirection(dx, dy);
                        if (nouvelleDir != directionActuelle) {
                            directionActuelle = nouvelleDir;
                            imageView.setImage(idle[directionActuelle]);
                        }
                    }
                } else {
                    if (ennemiDetecte) {
                        ennemiDetecte = false;
                        setCouleurPortee(false);
                    }
                    directionVerrouillee = false;
                }
            }
        }.start();
    }

    private void setCouleurPortee(boolean danger) {
        if (danger) {
            portee.setFill(Color.rgb(220, 30, 30, 0.15));
            portee.setStroke(Color.rgb(220, 30, 30, 0.65));
        } else {
            portee.setFill(Color.rgb(30, 200, 30, 0.12));
            portee.setStroke(Color.rgb(30, 200, 30, 0.60));
        }
    }

    private int calculerDirection(double dx, double dy) {
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        if (angle >= -45  && angle <  45)  return DIR_DROITE;
        if (angle >=  45  && angle < 135)  return DIR_BAS;
        if (angle >= -135 && angle < -45)  return DIR_HAUT;
        return DIR_GAUCHE;
    }
}
