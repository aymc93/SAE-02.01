package com.example.sae2.controller;

import com.example.sae2.modele.deck.TypeCarte;
import com.example.sae2.modele.ennemis.ModeleEnnemi;
import com.example.sae2.modele.tours.ModeleProjectile;
import com.example.sae2.modele.tours.ModeleTour;
import com.example.sae2.vue.tours.VueTour;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.image.ImageView;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TowerController {

    private final GameController main;
    private final WaveController wave;
    private static final long DELAI_TIR = 800000000L;

    private class EtatTour {
        final ModeleTour modele;
        final VueTour vue;
        final double cx, cy, porteePixels;
        boolean enApparition = true;
        boolean ennemiDetecte = false;
        long dernierTir = 0;
        AnimationTimer timerTracking;

        EtatTour(ModeleTour modele, VueTour vue, double cx, double cy, double porteePixels) {
            this.modele = modele;
            this.vue = vue;
            this.cx = cx;
            this.cy = cy;
            this.porteePixels = porteePixels;
        }
        void stopper() { if (timerTracking != null) timerTracking.stop(); }
    }

    private final Map<String, EtatTour> tours = new HashMap<>();

    public TowerController(GameController main, WaveController wave) {
        this.main = main;
        this.wave = wave;
    }

    public boolean tenterPoserTour(int col, int ligne) {
        if (!main.modele.estConstructible(ligne, col)) return false;
        String cle = ligne + "," + col;
        if (tours.containsKey(cle)) return false;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/sae2/tour.fxml"));
            Group tourNode = loader.load();

            tourNode.setLayoutX(col * main.T);
            tourNode.setLayoutY(ligne * main.T);
            main.paneTours.getChildren().add(tourNode);

            Circle porteeShape = (Circle) tourNode.lookup("#portee");
            ImageView spriteView = (ImageView) tourNode.lookup("#spriteView");

            double cx = col * main.T + main.T / 2.0;
            double cy = ligne * main.T + main.T / 2.0;
            double portee = 3.0 * main.T;

            ModeleTour modeleTour = new ModeleTour(col, ligne);
            VueTour vueTour = new VueTour(spriteView, porteeShape, main.paneTours);

            EtatTour etat = new EtatTour(modeleTour, vueTour, cx, cy, portee);
            vueTour.initialiser(TypeCarte.SIMPLE_TOWER.getDossier(), () -> etat.enApparition = false);

            tours.put(cle, etat);
            demarrerTracking(etat);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void demarrerTracking(EtatTour etat) {
        etat.timerTracking = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (etat.enApparition) return;

                ModeleEnnemi cible = null;
                double distMin = Double.MAX_VALUE;
                for (ModeleEnnemi e : wave.ennemisActifs.keySet()) {
                    if (e.estMort()) continue;
                    double dx = (e.getX() + 32) - etat.cx;
                    double dy = (e.getY() + 32) - etat.cy;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist <= etat.porteePixels && dist < distMin) {
                        distMin = dist;
                        cible = e;
                    }
                }

                if (cible == null) {
                    if (etat.ennemiDetecte) { etat.ennemiDetecte = false; etat.vue.setAlerte(false); }
                    return;
                }

                if (!etat.ennemiDetecte) { etat.ennemiDetecte = true; etat.vue.setAlerte(true); }

                double dx = (cible.getX() + 32) - etat.cx;
                double dy = (cible.getY() + 32) - etat.cy;
                etat.vue.setDirection(ModeleTour.calculerDirection(dx, dy));

                if (now - etat.dernierTir >= DELAI_TIR) {
                    tirerProjectile(etat, cible);
                    etat.dernierTir = now;
                }
            }
        };
        etat.timerTracking.start();
    }

    private void tirerProjectile(EtatTour etat, ModeleEnnemi cible) {
        ModeleProjectile projectile = new ModeleProjectile(etat.cx, etat.cy, cible, etat.modele.getDegats(), etat.modele.getVitesseProjectile());
        etat.vue.afficherProjectile(projectile);
    }

    public boolean tenterAppliquerPouvoir(int col, int ligne, TypeCarte pouvoir) {
        EtatTour etat = tours.get(ligne + "," + col);
        if (etat == null) return false;
        etat.modele.appliquerPouvoir(pouvoir);
        etat.enApparition = true;
        etat.vue.changerDossier(pouvoir.getDossier(), () -> etat.enApparition = false);
        return true;
    }

    public void arreter() {
        tours.values().forEach(EtatTour::stopper);
    }
}