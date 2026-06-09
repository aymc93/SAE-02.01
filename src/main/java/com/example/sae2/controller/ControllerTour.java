package com.example.sae2.controller;

import com.example.sae2.modele.deck.TypeCarte;
import com.example.sae2.modele.ennemis.ModeleEnnemi;
import com.example.sae2.modele.tours.ModeleTour;
import com.example.sae2.modele.tours.ModeleProjectile;
import com.example.sae2.vue.tours.VueTour;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

public class ControllerTour {

    private static final long DELAI_TIR = 800000000; // 0.8 secondes en nanosecondes

    @FXML private Group rootTour;
    @FXML private Circle portee;
    @FXML private ImageView spriteView;

    private ModeleTour modele;
    private VueTour vue;
    private ModeleEnnemi ennemi;

    private double centreTourX;
    private double centreTourY;
    private double porteePixels;

    private boolean enApparition  = true;
    private boolean ennemiDetecte = false;
    private long dernierTir = 0;

    public void configurer(int col, int ligne, double cx, double cy, double rayon, ModeleEnnemi ennemi) {
        this.centreTourX = cx;
        this.centreTourY = cy;
        this.porteePixels = rayon;
        this.ennemi = ennemi;

        this.modele = new ModeleTour(col, ligne);
        this.vue = new VueTour(spriteView, portee, (Pane) rootTour.getParent());
        this.vue.initialiser(TypeCarte.SIMPLE_TOWER.getDossier(), () -> enApparition = false);

        demarrerTracking();
    }

    public void appliquerPouvoir(TypeCarte type) {
        modele.appliquerPouvoir(type);
        enApparition = true;
        vue.changerDossier(type.getDossier(), () -> enApparition = false);
    }


    // Boucle de suivi
    private void demarrerTracking() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (enApparition) return;
                if (ennemi == null || ennemi.estMort()) {
                    if (ennemiDetecte) {
                        ennemiDetecte = false;
                        vue.setAlerte(false);
                    }
                    return;
                }

                double dx = (ennemi.getX() + 32) - centreTourX;
                double dy = (ennemi.getY() + 32) - centreTourY;
                boolean enPortee = Math.sqrt(dx * dx + dy * dy) <= porteePixels;

                if (enPortee) {
                    if (!ennemiDetecte) {
                        ennemiDetecte = true;
                        vue.setAlerte(true);
                    }
                    vue.setDirection(ModeleTour.calculerDirection(dx, dy));
                    if (now - dernierTir >= DELAI_TIR) {
                        tirerProjectile();
                        dernierTir = now;
                    }
                } else {
                    if (ennemiDetecte) {
                        ennemiDetecte = false;
                        vue.setAlerte(false);
                    }
                }
            }
        }.start();
    }

    // Création du projectile
    private void tirerProjectile() {
        System.out.println("[Tour] Tir  →  dégâts=" + modele.getDegats()
                + "  vitesse=" + (int) modele.getVitesseProjectile() + " px/s");
        ModeleProjectile projectile = new ModeleProjectile(
                centreTourX, centreTourY, ennemi,
                modele.getDegats(), modele.getVitesseProjectile());
        vue.afficherProjectile(projectile);
    }
}
