package com.example.sae2.vue.tours;

import com.example.sae2.modele.tours.ModeleProjectile;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.net.URL;

public class VueTour {

    private static final String[] DIRECTIONS      = {"bas", "droite", "haut", "gauche"};
    private static final String   BASE_IMAGES     = "/com/example/sae2/images/";
    private static final String   DOSSIER_DEFAUT  = BASE_IMAGES + "SimpleTower/";

    private final ImageView spriteView;
    private final Circle    portee;
    private final Pane      parentPane;

    private final Image[] idle      = new Image[4];
    private final Image[] idleBlink = new Image[4];

    private int      directionActuelle = 0;
    private boolean  enClignotement   = false;
    private Timeline timelineEnCours  = null;

    public VueTour(ImageView spriteView, Circle portee, Pane parentPane) {
        this.spriteView = spriteView;
        this.portee     = portee;
        this.parentPane = parentPane;
    }

    /** Charge les sprites du dossier initial et joue l'animation d'apparition. */
    public void initialiser(String dossier, Runnable onFin) {
        String base = BASE_IMAGES + dossier + "/";
        chargerSprites(base);
        jouerApparitionDepuis(base, onFin);
    }

    /** Change les sprites après application d'un pouvoir et rejoue l'apparition. */
    public void changerDossier(String dossier, Runnable onFin) {
        String base = BASE_IMAGES + dossier + "/";
        chargerSprites(base);
        if (timelineEnCours != null) { timelineEnCours.stop(); timelineEnCours = null; }
        enClignotement = false;
        jouerApparitionDepuis(base, onFin);
    }

    /** Met à jour le sprite de direction si elle a changé. */
    public void setDirection(int direction) {
        if (direction == directionActuelle) return;
        directionActuelle = direction;
        if (!enClignotement) spriteView.setImage(idle[directionActuelle]);
    }

    /** Passe le cercle de portée en mode alerte ou libre. */
    public void setAlerte(boolean alerte) {
        portee.getStyleClass().removeAll("portee-libre", "portee-alerte");
        portee.getStyleClass().add(alerte ? "portee-alerte" : "portee-libre");
    }

    /** Crée le nœud visuel du projectile et l'anime jusqu'à l'impact. */
    public void afficherProjectile(ModeleProjectile projectile) {
        Image     spriteProj = chargerImage(DOSSIER_DEFAUT + "projectile.png");
        ImageView vueSprite  = null;
        Circle    boule      = null;

        if (spriteProj != null) {
            vueSprite = new ImageView(spriteProj);
            vueSprite.setFitWidth(20);
            vueSprite.setFitHeight(20);
            vueSprite.setLayoutX(projectile.getX() - 10);
            vueSprite.setLayoutY(projectile.getY() - 10);
            parentPane.getChildren().add(vueSprite);
        } else {
            boule = new Circle(6);
            boule.setFill(Color.LIMEGREEN);
            boule.setLayoutX(projectile.getX());
            boule.setLayoutY(projectile.getY());
            parentPane.getChildren().add(boule);
        }

        final ImageView finalVue   = vueSprite;
        final Circle    finalBoule = boule;

        new AnimationTimer() {
            private long tempsPrecedent = -1;
            @Override
            public void handle(long now) {
                if (tempsPrecedent < 0) { tempsPrecedent = now; return; }
                double delta = (now - tempsPrecedent) / 1_000_000_000.0;
                tempsPrecedent = now;

                boolean touche = projectile.avancer(delta);
                if (finalVue != null) {
                    finalVue.setLayoutX(projectile.getX() - 10);
                    finalVue.setLayoutY(projectile.getY() - 10);
                } else {
                    finalBoule.setLayoutX(projectile.getX());
                    finalBoule.setLayoutY(projectile.getY());
                }
                if (touche) {
                    if (finalVue  != null) parentPane.getChildren().remove(finalVue);
                    else                   parentPane.getChildren().remove(finalBoule);
                    stop();
                }
            }
        }.start();
    }

    // ── Privé ─────────────────────────────────────────────────────────────────

    private void chargerSprites(String base) {
        for (int i = 0; i < DIRECTIONS.length; i++) {
            idle[i]      = chargerImage(base + "idle_" + DIRECTIONS[i] + ".png");
            idleBlink[i] = chargerImage(base + "idle_" + DIRECTIONS[i] + "_blink.png");
        }
    }

    private Image chargerImage(String cheminAbsolu) {
        URL url = getClass().getResource(cheminAbsolu);
        if (url == null) {
            System.err.println("[VueTour] Image introuvable : " + cheminAbsolu);
            return null;
        }
        return new Image(url.toExternalForm());
    }

    private void jouerApparitionDepuis(String base, Runnable onFin) {
        Image a1 = chargerImage(base + "appear1.png");
        Image a2 = chargerImage(base + "appear2.png");
        spriteView.setImage(a1);
        new Timeline(
            new KeyFrame(Duration.millis(150), e -> spriteView.setImage(a2)),
            new KeyFrame(Duration.millis(300), e -> {
                planifierIdle();
                if (onFin != null) onFin.run();
            })
        ).play();
    }

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
}
