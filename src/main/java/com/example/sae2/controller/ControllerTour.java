package com.example.sae2.controller;

import com.example.sae2.modele.deck.TypeCarte;
import com.example.sae2.modele.ennemis.ModeleEnnemi;
import com.example.sae2.modele.tours.ModeleProjectile;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.net.URL;

public class ControllerTour {

    private static final int DIR_BAS    = 0;
    private static final int DIR_DROITE = 1;
    private static final int DIR_HAUT   = 2;
    private static final int DIR_GAUCHE = 3;

    private static final String[] DIRECTIONS = {"bas", "droite", "haut", "gauche"};
    private static final long     DELAI_TIR  = 800_000_000L;

    private static final String BASE_IMAGES       = "/com/example/sae2/images/";
    private static final String DOSSIER_TOUR_BASE = BASE_IMAGES + "SimpleTower/";

    @FXML private Group     rootTour;
    @FXML private Circle    portee;
    @FXML private ImageView spriteView;

    private final Image[] idle      = new Image[4];
    private final Image[] idleBlink = new Image[4];

    // Stats du type de tour actuel (SimpleTower par défaut)
    private int    degatsActuels = TypeCarte.SIMPLE_TOWER.getDegats();
    private double vitesseProj   = TypeCarte.SIMPLE_TOWER.getVitesseProjectile();

    private ModeleEnnemi ennemi;
    private Pane         parentPane;

    private double centreTourX;
    private double centreTourY;
    private double porteePixels;

    private int     directionActuelle = DIR_BAS;
    private boolean enApparition      = true;
    private boolean enClignotement    = false;
    private boolean ennemiDetecte     = false;

    private long     dernierTir      = 0;
    private Timeline timelineEnCours = null;

    // =========================================================================
    // Initialisation FXML : charge les sprites SimpleTower et joue l'apparition
    // =========================================================================

    @FXML
    public void initialize() {
        chargerSprites(DOSSIER_TOUR_BASE);
        jouerApparitionDepuis(DOSSIER_TOUR_BASE);
    }

    public void configurer(double cx, double cy, double rayon, ModeleEnnemi ennemi) {
        this.centreTourX  = cx;
        this.centreTourY  = cy;
        this.porteePixels = rayon;
        this.ennemi       = ennemi;
        this.parentPane   = (Pane) rootTour.getParent();
        demarrerTracking();
    }

    // =========================================================================
    // Application d'un pouvoir : change les sprites et rejoue l'apparition
    // =========================================================================

    public void appliquerPouvoir(TypeCarte type) {
        // Mettre à jour les stats du projectile
        degatsActuels = type.getDegats();
        vitesseProj   = type.getVitesseProjectile();

        String base = BASE_IMAGES + type.getDossier() + "/";

        // Mettre à jour les tableaux idle/idleBlink avec les sprites du pouvoir
        chargerSprites(base);

        // Arrêter toute animation en cours et réinitialiser l'état
        if (timelineEnCours != null) {
            timelineEnCours.stop();
            timelineEnCours = null;
        }
        enClignotement = false;

        // Rejouer l'animation d'apparition avec les nouveaux sprites
        jouerApparitionDepuis(base);
    }

    // =========================================================================
    // Chargement des sprites
    // =========================================================================

    /** Charge idle et idleBlink depuis le dossier donné (chemin absolu classpath). */
    private void chargerSprites(String base) {
        for (int i = 0; i < DIRECTIONS.length; i++) {
            idle[i]      = chargerImage(base + "idle_" + DIRECTIONS[i] + ".png");
            idleBlink[i] = chargerImage(base + "idle_" + DIRECTIONS[i] + "_blink.png");
        }
    }

    /** Charge une image depuis son chemin absolu dans le classpath. */
    private Image chargerImage(String cheminAbsolu) {
        URL url = getClass().getResource(cheminAbsolu);
        if (url == null) {
            System.err.println("[ControllerTour] Image introuvable : " + cheminAbsolu);
            return null;
        }
        return new Image(url.toExternalForm());
    }

    // =========================================================================
    // Animation d'apparition
    // =========================================================================

    private void jouerApparitionDepuis(String base) {
        enApparition = true;
        Image a1 = chargerImage(base + "appear1.png");
        Image a2 = chargerImage(base + "appear2.png");

        spriteView.setImage(a1);
        new Timeline(
            new KeyFrame(Duration.millis(150), e -> spriteView.setImage(a2)),
            new KeyFrame(Duration.millis(300), e -> {
                enApparition = false;
                planifierIdle();
            })
        ).play();
    }

    // =========================================================================
    // Boucle idle / blink (cycle aléatoire)
    // =========================================================================

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

    // =========================================================================
    // Suivi de l'ennemi (AnimationTimer)
    // =========================================================================

    private void demarrerTracking() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (enApparition) return;
                if (ennemi == null || ennemi.estMort()) {
                    // Ennemi mort ou absent : repasser en mode libre et ne pas tirer
                    if (ennemiDetecte) {
                        ennemiDetecte = false;
                        setClassePortee("portee-libre");
                    }
                    return;
                }

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
                    if (now - dernierTir >= DELAI_TIR) {
                        tirerProjectile();
                        dernierTir = now;
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

    // =========================================================================
    // Tir de projectile
    // =========================================================================

    private void tirerProjectile() {
        if (parentPane == null) {
            parentPane = (Pane) rootTour.getParent();
        }
        if (parentPane == null) return;

        System.out.println("[Tour] Tir  →  dégâts=" + degatsActuels
                + "  vitesse=" + (int) vitesseProj + " px/s");
        ModeleProjectile projectile = new ModeleProjectile(
                centreTourX, centreTourY, ennemi, degatsActuels, vitesseProj);
        Image            spriteProj     = chargerImage(DOSSIER_TOUR_BASE + "projectile.png");

        ImageView vueSprite = null;
        Circle    boule     = null;

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

        ImageView finalVueSprite = vueSprite;
        Circle    finalBoule     = boule;

        new AnimationTimer() {
            private long tempsPrecedent = -1;
            @Override
            public void handle(long now) {
                if (tempsPrecedent < 0) { tempsPrecedent = now; return; }
                double delta = (now - tempsPrecedent) / 1_000_000_000.0;
                tempsPrecedent = now;

                boolean touche = projectile.avancer(delta);

                if (finalVueSprite != null) {
                    finalVueSprite.setLayoutX(projectile.getX() - 10);
                    finalVueSprite.setLayoutY(projectile.getY() - 10);
                } else {
                    finalBoule.setLayoutX(projectile.getX());
                    finalBoule.setLayoutY(projectile.getY());
                }

                if (touche) {
                    if (finalVueSprite != null) parentPane.getChildren().remove(finalVueSprite);
                    else                        parentPane.getChildren().remove(finalBoule);
                    stop();
                }
            }
        }.start();
    }

    // =========================================================================
    // Utilitaires
    // =========================================================================

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
