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

import java.util.Collection;
import java.util.function.Supplier;

public class ControllerTour {

    private static final long DELAI_TIR = 800000000; // 0.8 secondes en nanosecondes

    @FXML private Group rootTour;
    @FXML private Circle portee;
    @FXML private ImageView spriteView;

    private ModeleTour modele;
    private VueTour vue;
    private Supplier<Collection<ModeleEnnemi>> sourceEnnemis;

    private double centreTourX;
    private double centreTourY;
    private double porteePixels;

    private boolean        enApparition  = true;
    private boolean        ennemiDetecte = false;
    private long           dernierTir    = 0;
    private AnimationTimer timerTracking;

    public void configurer(int col, int ligne, double cx, double cy, double rayon,
                           Supplier<Collection<ModeleEnnemi>> sourceEnnemis) {
        this.centreTourX   = cx;
        this.centreTourY   = cy;
        this.porteePixels  = rayon;
        this.sourceEnnemis = sourceEnnemis;

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


    public void stopper() {
        if (timerTracking != null) timerTracking.stop();
    }

    // Boucle de suivi
    private void demarrerTracking() {
        timerTracking = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (enApparition) return;

                // Chercher l'ennemi le plus proche dans la portée
                ModeleEnnemi cible = null;
                double distMin = Double.MAX_VALUE;
                for (ModeleEnnemi e : sourceEnnemis.get()) {
                    if (e.estMort()) continue;
                    double dx = (e.getX() + 32) - centreTourX;
                    double dy = (e.getY() + 32) - centreTourY;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist <= porteePixels && dist < distMin) {
                        distMin = dist;
                        cible = e;
                    }
                }

                if (cible == null) {
                    if (ennemiDetecte) { ennemiDetecte = false; vue.setAlerte(false); }
                    return;
                }

                if (!ennemiDetecte) { ennemiDetecte = true; vue.setAlerte(true); }
                double dx = (cible.getX() + 32) - centreTourX;
                double dy = (cible.getY() + 32) - centreTourY;
                vue.setDirection(ModeleTour.calculerDirection(dx, dy));
                if (now - dernierTir >= DELAI_TIR) {
                    tirerProjectile(cible);
                    dernierTir = now;
                }
            }
        };
        timerTracking.start();
    }

    // Création du projectile
    private void tirerProjectile(ModeleEnnemi cible) {
        System.out.println("[Tour] Tir  →  dégâts=" + modele.getDegats()
                + "  vitesse=" + (int) modele.getVitesseProjectile() + " px/s");
        ModeleProjectile projectile = new ModeleProjectile(
                centreTourX, centreTourY, cible,
                modele.getDegats(), modele.getVitesseProjectile());
        vue.afficherProjectile(projectile);
    }
}
