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
    private final EnnemiController ennemiController;

    public class EtatTour {
        final ModeleTour modele;
        final VueTour vue;
        boolean enApparition = true;
        boolean ennemiDetecte = false;
        AnimationTimer timerTracking;

        EtatTour(ModeleTour modele, VueTour vue) {
            this.modele = modele;
            this.vue = vue;
        }
        void stopper() { if (timerTracking != null) timerTracking.stop(); }
    }

    private final Map<String, EtatTour> tours = new HashMap<>();

    public TowerController(GameController main, EnnemiController ennemiController) {
        this.main = main;
        this.ennemiController = ennemiController;
    }

    public boolean tenterPoserTour(int col, int ligne) {
        if (!main.modele.estConstructible(ligne, col)) return false;
        String cle = ligne + "," + col;
        if (tours.containsKey(cle)) return false;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/sae2/tour.fxml"));
            Group tourNode = loader.load();

            // 1. ON FORCE LA TRANSPARENCE À FALSE (au cas où le FXML la bloque)
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
            demarrerTracking(etat);

            // 2. ON PASSE LE SPRITEVIEW À LA MÉTHODE POUR CIBLER L'IMAGE EXACTE
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
            // SÉCURITÉ : On vérifie que le drag a bien commencé proprement !
            if (tourNode.getProperties().get("mouseStartX") == null) return;
            if (main.carteSelectionnee != null && main.carteSelectionnee.estPouvoir()) return;

            double deltaX = event.getSceneX() - (double) tourNode.getProperties().get("mouseStartX");
            double deltaY = event.getSceneY() - (double) tourNode.getProperties().get("mouseStartY");
            tourNode.setLayoutX((double) tourNode.getProperties().get("initX") + deltaX);
            tourNode.setLayoutY((double) tourNode.getProperties().get("initY") + deltaY);
            event.consume();
        });

        spriteView.setOnMouseReleased(event -> {
            // SÉCURITÉ : On vérifie que la tour a bien été attrapée !
            if (tourNode.getProperties().get("initX") == null) return;
            if (main.carteSelectionnee != null && main.carteSelectionnee.estPouvoir()) return;

            if (event.getSceneY() > main.paneJeu.getHeight()) {
                vendreTour(cle, etat, tourNode);
            } else {
                tourNode.setLayoutX((double) tourNode.getProperties().get("initX"));
                tourNode.setLayoutY((double) tourNode.getProperties().get("initY"));
            }

            // On nettoie les propriétés pour éviter les bugs futurs
            tourNode.getProperties().remove("mouseStartX");
            tourNode.getProperties().remove("mouseStartY");
            tourNode.getProperties().remove("initX");
            tourNode.getProperties().remove("initY");

            event.consume();
        });
    }

    private void vendreTour(String cle, EtatTour etat, Group tourNode) {
        System.out.println("[Boutique] Tour vendue ! +50 pièces.");

        // 1. On arrête les calculs de cette tour
        etat.stopper();

        // 2. On retire la tour visuellement et logiquement
        main.paneTours.getChildren().remove(tourNode);
        tours.remove(cle);

        // 3. On rembourse le joueur (Ici j'ai mis 50 pièces, à toi de choisir le montant !)
        main.modeleJoueur.ajouterArgent(50);
        main.vueArgent.actualiser(main.modeleJoueur.getArgent());
    }

    private void demarrerTracking(EtatTour etat) {
        etat.timerTracking = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (etat.enApparition) return;

                ModeleEnnemi cible = etat.modele.trouverCible(ennemiController.ennemisActifs.keySet(), main.T);

                if (cible == null) {
                    if (etat.ennemiDetecte) {
                        etat.ennemiDetecte = false;
                        etat.vue.setAlerte(false);
                    }
                    return;
                }

                if (!etat.ennemiDetecte) {
                    etat.ennemiDetecte = true;
                    etat.vue.setAlerte(true);
                }

                etat.vue.setDirection(etat.modele.calculerDirectionVers(cible, main.T));

                if (etat.modele.peutTirer(now)) {
                    tirerProjectile(etat, cible);
                }
            }
        };
        etat.timerTracking.start();
    }

    private void tirerProjectile(EtatTour etat, ModeleEnnemi cible) {
        double cx = etat.modele.getColonne() * main.T + main.T / 2.0;
        double cy = etat.modele.getLigne() * main.T + main.T / 2.0;

        ModeleProjectile projectile = new ModeleProjectile(cx, cy, cible, etat.modele.getDegats(), etat.modele.getVitesseProjectile());
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

    public boolean possedeTour(int ligne, int col) {
        return tours.containsKey(ligne + "," + col);
    }

    public void arreter() {
        tours.values().forEach(EtatTour::stopper);
    }
}