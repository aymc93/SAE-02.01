package com.example.sae2.controller;

import com.example.sae2.modele.ennemis.ModeleEnnemi;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.net.URL;

public class ControllerTour {

    private static final int      DIR_BAS    = 0;
    private static final int      DIR_DROITE = 1;
    private static final int      DIR_HAUT   = 2;
    private static final int      DIR_GAUCHE = 3;
    private static final String[] DIRECTIONS = {"bas", "droite", "haut", "gauche"};

    // ── Nœuds injectés depuis tour.fxml ────────────────────────────────────
    @FXML private Circle    portee;
    @FXML private ImageView spriteView;

    // ── Images ─────────────────────────────────────────────────────────────
    private Image   appear1, appear2;
    private Image[] idle      = new Image[4];
    private Image[] idleBlink = new Image[4];

    // ── État ────────────────────────────────────────────────────────────────
    private ModeleEnnemi ennemi;
    private double       centreTourX;
    private double       centreTourY;
    private double       porteePixels;

    private int     directionActuelle = DIR_BAS;
    private boolean enApparition      = true;
    private boolean enClignotement    = false;
    private boolean ennemiDetecte     = false;
    private Timeline timelineEnCours;

    // ── Initialisation FXML ─────────────────────────────────────────────────

    @FXML
    public void initialize() {
        appear1 = chargerImage("appear1.png");
        appear2 = chargerImage("appear2.png");
        for (int i = 0; i < DIRECTIONS.length; i++) {
            idle[i]      = chargerImage("idle_" + DIRECTIONS[i] + ".png");
            idleBlink[i] = chargerImage("idle_" + DIRECTIONS[i] + "_blink.png");
        }
    }

    /**
     * Appelé par GameController après le chargement du FXML.
     * @param cx          centre de la tour en coordonnées absolues (paneJeu)
     * @param cy          centre de la tour en coordonnées absolues (paneJeu)
     * @param rayon       portée en pixels (3 × tailleTuile)
     * @param ennemi      modèle de l'ennemi à suivre
     */
    public void configurer(double cx, double cy, double rayon, ModeleEnnemi ennemi) {
        this.centreTourX = cx;
        this.centreTourY = cy;
        this.porteePixels = rayon;
        this.ennemi = ennemi;
        jouerApparition();
        demarrerTracking();
    }

    // ── Chargement d'images ─────────────────────────────────────────────────

    private Image chargerImage(String nom) {
        String chemin = "/com/example/sae2/images/SimpleTower/" + nom;
        URL url = getClass().getResource(chemin);
        if (url == null) {
            System.err.println("[ControllerTour] Image introuvable : " + chemin);
            return null;
        }
        return new Image(url.toExternalForm());
    }

    // ── Animation d'apparition ──────────────────────────────────────────────

    private void jouerApparition() {
        enApparition = true;
        spriteView.setImage(appear1);

        new Timeline(
            new KeyFrame(Duration.millis(150), e -> spriteView.setImage(appear2)),
            new KeyFrame(Duration.millis(300), e -> {
                enApparition = false;
                planifierIdle();
            })
        ).play();
    }

    // ── Boucle idle / blink ─────────────────────────────────────────────────

    private void planifierIdle() {
        enClignotement = false;
        spriteView.setImage(idle[directionActuelle]);

        double dureeMs = 2000 + Math.random() * 3000;

        timelineEnCours = new Timeline(
            new KeyFrame(Duration.millis(dureeMs), e -> jouerBlink())
        );
        timelineEnCours.play();
    }

    private void jouerBlink() {
        enClignotement = true;
        spriteView.setImage(idleBlink[directionActuelle]);

        timelineEnCours = new Timeline(
            new KeyFrame(Duration.millis(150), e -> planifierIdle())
        );
        timelineEnCours.play();
    }

    // ── Tracking ennemi (chaque frame) ──────────────────────────────────────

    private void demarrerTracking() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (enApparition) return;

                double dx = (ennemi.getX() + 32) - centreTourX;
                double dy = (ennemi.getY() + 32) - centreTourY;
                boolean enPortee = Math.sqrt(dx * dx + dy * dy) <= porteePixels;

                if (enPortee) {
                    if (!ennemiDetecte) {
                        ennemiDetecte = true;
                        setClassePortee("portee-alerte");
                    }
                    if (!enClignotement) {
                        int dir = calculerDirection(dx, dy);
                        if (dir != directionActuelle) {
                            directionActuelle = dir;
                            spriteView.setImage(idle[directionActuelle]);
                        }
                    }
                } else {
                    if (ennemiDetecte) {
                        ennemiDetecte = false;
                        setClassePortee("portee-libre");
                    }
                }
            }
        }.start();
    }

    /** Remplace la classe CSS de portée sans dupliquer. */
    private void setClassePortee(String nouvelleClasse) {
        portee.getStyleClass().removeAll("portee-libre", "portee-alerte");
        portee.getStyleClass().add(nouvelleClasse);
    }

    private int calculerDirection(double dx, double dy) {
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        if (angle >= -45  && angle <  45)  return DIR_DROITE;
        if (angle >=  45  && angle < 135)  return DIR_BAS;
        if (angle >= -135 && angle < -45)  return DIR_HAUT;
        return DIR_GAUCHE;
    }
}
