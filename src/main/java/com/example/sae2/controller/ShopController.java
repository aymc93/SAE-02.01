package com.example.sae2.controller;

import com.example.sae2.modele.boutique.ModeleBoutique;
import com.example.sae2.modele.deck.TypeCarte;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ShopController {

    @FXML private VBox interfaceBoutique;
    @FXML private Label labelPrixBooster;
    @FXML private VBox interfaceChoixBooster;
    @FXML private ImageView choix1View;
    @FXML private ImageView choix2View;

    private GameController mainController;
    private final ModeleBoutique modeleBoutique = new ModeleBoutique();
    private TypeCarte carteChoix1;
    private TypeCarte carteChoix2;

    // Permet au GameController de lier son instance
    public void init(GameController mainController) {
        this.mainController = mainController;
        this.labelPrixBooster.setText("Prix du booster : " + modeleBoutique.getPrixBooster() + " pièces");
    }

    @FXML
    public void ouvrirBoutique() {
        interfaceBoutique.setLayoutX((mainController.paneJeu.getWidth() - interfaceBoutique.getPrefWidth()) / 2);
        interfaceBoutique.setLayoutY((mainController.paneJeu.getHeight() - interfaceBoutique.getPrefHeight()) / 2);
        interfaceBoutique.setVisible(true);
    }

    @FXML
    public void fermerBoutique() {
        interfaceBoutique.setVisible(false);
    }

    @FXML
    private void traiterAchatBooster() {
        if (mainController.modeleDeck.estPlein()) return;

        TypeCarte[] choix = modeleBoutique.genererChoixBooster(mainController.modeleJoueur);
        if (choix != null) {
            mainController.vueArgent.actualiser(mainController.modeleJoueur.getArgent());
            carteChoix1 = choix[0];
            carteChoix2 = choix[1];

            choix1View.setImage(new Image(getClass().getResource("/com/example/sae2/images/Cartes/" + getNomImage(carteChoix1)).toExternalForm()));
            choix2View.setImage(new Image(getClass().getResource("/com/example/sae2/images/Cartes/" + getNomImage(carteChoix2)).toExternalForm()));

            fermerBoutique();
            interfaceChoixBooster.setLayoutX((mainController.paneJeu.getWidth() - interfaceChoixBooster.getPrefWidth()) / 2);
            interfaceChoixBooster.setLayoutY((mainController.paneJeu.getHeight() - interfaceChoixBooster.getPrefHeight()) / 2);
            interfaceChoixBooster.setVisible(true);
        }
    }

    private void validerChoix(TypeCarte carteChoisie) {
        if (!carteChoisie.estPouvoir() && mainController.modeleDeck.getNombreTours() >= 2) return;
        if (carteChoisie.estPouvoir() && mainController.modeleDeck.getNombrePouvoirs() >= 3) return;

        mainController.modeleDeck.ajouterCarte(carteChoisie);
        mainController.vueDeck.ajouterCarteView(carteChoisie);
        interfaceChoixBooster.setVisible(false);
    }

    @FXML private void clicChoix1() { validerChoix(carteChoix1); }
    @FXML private void clicChoix2() { validerChoix(carteChoix2); }

    private String getNomImage(TypeCarte carte) {
        return switch (carte) {
            case BLOODBAG     -> "Card-BloodBag.png";
            case BRIMSTONE    -> "Card-Brimstone.png";
            case SOYMILK      -> "Card-SoyMilk.png";
            case SIMPLE_TOWER -> "Card-tower-simple.png";
        };
    }
}