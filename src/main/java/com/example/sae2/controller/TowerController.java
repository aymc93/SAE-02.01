package com.example.sae2.controller;

import com.example.sae2.modele.Environnement;
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
    private final Environnement env;
    private final EnnemiController ennemiController;

    private final Map<String, EtatTour> tours = new HashMap<>();
    private AnimationTimer boucle;

    public TowerController(GameController main, EnnemiController ennemiController) {
        this.main = main;
        this.env = main.environnement;
        this.ennemiController = ennemiController;
        demarrerBoucle();
    }

    private void demarrerBoucle() {
        boucle = new AnimationTimer() {
            @Override
            public void handle(long now) {
                for (EtatTour etat : tours.values()) {
                    if (etat.enApparition) continue;
                    Environnement.OrdreTir ordre = env.fairePasserUnTourTour(etat.modele, now);
                    if (ordre == null) {
                        if (etat.ennemiDetecte) {
                            etat.ennemiDetecte = false;
                            etat.vue.setAlerte(false);
                        }
                        continue;
                    }
                    if (!etat.ennemiDetecte) {
                        etat.ennemiDetecte = true;
                        etat.vue.setAlerte(true);
                    }
                    etat.vue.setDirection(ordre.direction);
                    if (ordre.doitTirer) {
                        tirerProjectile(etat, ordre.cible);
                    }
                }
            }
        };
        boucle.start();
    }

    private void tirerProjectile(EtatTour etat, ModeleEnnemi cible) {
        ModeleProjectile projectile = env.creerProjectile(etat.modele, cible);
        etat.vue.afficherProjectile(projectile);
    }

    public boolean tenterPoserTour(int col, int ligne) {
        if (!env.peutPoserTour(col, ligne)) return false;
        String cle = ligne + "," + col;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/sae2/tour.fxml"));
            Group tourNode = loader.load();
            tourNode.setMouseTransparent(false);
            tourNode.setLayoutX(col * main.T);
            tourNode.setLayoutY(ligne * main.T);
            main.paneTours.getChildren().add(tourNode);

            Circle porteeShape = (Circle) tourNode.lookup("#portee");
            ImageView spriteView = (ImageView) tourNode.lookup("#spriteView");

            ModeleTour modeleTour = new ModeleTour(col, ligne);
            VueTour vueTour = new VueTour(spriteView, porteeShape, main.paneTours);

            EtatTour etat = new EtatTour(modeleTour, vueTour);
            vueTour.initialiser(TypeCarte.SIMPLE_TOWER.getDossier(), () -> etat.enApparition = false);

            tours.put(cle, etat);
            env.ajouterTour(col, ligne, modeleTour);   // l'Env connaît la tour
            ajouterDragAndDrop(tourNode, spriteView, etat, cle);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void ajouterDragAndDrop(Group tourNode, ImageView spriteView, EtatTour etat, String cle) {
        spriteView.setStyle("-fx-cursor: hand;");

        spriteView.setOnMousePressed(event -> {
            if (main.carteSelectionnee != null && main.carteSelectionnee.estPouvoir()) {
                boolean succes = tenterAppliquerPouvoir(etat.modele.getColonne(), etat.modele.getLigne(), main.carteSelectionnee);
                if (succes) {
                    TypeCarte type = main.carteSelectionnee;
                    main.deselectionner();
                    main.modeleDeck.utiliserCarte(type);
                    main.vueDeck.masquerCarte(type);
                }
                event.consume();
                return;
            }
            tourNode.toFront();
            tourNode.getProperties().put("initX", tourNode.getLayoutX());
            tourNode.getProperties().put("initY", tourNode.getLayoutY());
            tourNode.getProperties().put("mouseStartX", event.getSceneX());
            tourNode.getProperties().put("mouseStartY", event.getSceneY());
            event.consume();
        });
        spriteView.setOnMouseDragged(event -> {
            if (tourNode.getProperties().get("mouseStartX") == null) return;
            if (main.carteSelectionnee != null && main.carteSelectionnee.estPouvoir()) return;

            double deltaX = event.getSceneX() - (double) tourNode.getProperties().get("mouseStartX");
            double deltaY = event.getSceneY() - (double) tourNode.getProperties().get("mouseStartY");
            tourNode.setLayoutX((double) tourNode.getProperties().get("initX") + deltaX);
            tourNode.setLayoutY((double) tourNode.getProperties().get("initY") + deltaY);
            event.consume();
        });
        spriteView.setOnMouseReleased(event -> {
            if (tourNode.getProperties().get("initX") == null) return;
            if (main.carteSelectionnee != null && main.carteSelectionnee.estPouvoir()) return;

            if (event.getSceneY() > main.paneJeu.getHeight()) {
                vendreTour(cle, etat, tourNode);
            } else {
                tourNode.setLayoutX((double) tourNode.getProperties().get("initX"));
                tourNode.setLayoutY((double) tourNode.getProperties().get("initY"));
            }
            tourNode.getProperties().remove("mouseStartX");
            tourNode.getProperties().remove("mouseStartY");
            tourNode.getProperties().remove("initX");
            tourNode.getProperties().remove("initY");
            event.consume();
        });
    }

    private void vendreTour(String cle, EtatTour etat, Group tourNode) {
        main.paneTours.getChildren().remove(tourNode);
        tours.remove(cle);
        env.vendreTour(etat.modele.getColonne(), etat.modele.getLigne());
        main.vueArgent.actualiser(main.modeleJoueur.getArgent());
    }

    public boolean tenterAppliquerPouvoir(int col, int ligne, TypeCarte pouvoir) {
        EtatTour etat = tours.get(ligne + "," + col);
        if (etat == null) return false;
        if (!env.appliquerPouvoirTour(col, ligne, pouvoir)) return false;
        etat.enApparition = true;
        etat.vue.changerDossier(pouvoir.getDossier(), () -> etat.enApparition = false);
        return true;
    }


    public void arreter() {
        if (boucle != null) boucle.stop();
    }
}
