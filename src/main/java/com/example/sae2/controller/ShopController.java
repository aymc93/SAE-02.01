package com.example.sae2.controller;

import com.example.sae2.modele.deck.TypeCarte;
import com.example.sae2.vue.boutique.VueBoutique;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ShopController {

    @FXML private VBox interfaceBoutique;
    @FXML private Label labelPrixBooster;
    @FXML private VBox interfaceChoixBooster;
    @FXML private ImageView choix1View;
    @FXML private ImageView choix2View;
    private GameController mainController;
    private VueBoutique vueBoutique;
    private TypeCarte carteChoix1;
    private TypeCarte carteChoix2;

    public void init(GameController mainController) {
        this.mainController = mainController;
        this.vueBoutique = new VueBoutique(
                mainController.paneJeu, interfaceBoutique, interfaceChoixBooster,
                labelPrixBooster, mainController.environnement.getPrixBooster(),
                choix1View, choix2View);
    }

    @FXML
    public void ouvrirBoutique() {
        vueBoutique.ouvrirBoutique();
    }

    @FXML
    public void fermerBoutique() {
        vueBoutique.fermerBoutique();
    }

    @FXML
    private void traiterAchatBooster() {
        TypeCarte[] choix = mainController.environnement.acheterBooster();
        if (choix != null) {
            mainController.vueArgent.actualiser(mainController.modeleJoueur.getArgent());
            carteChoix1 = choix[0];
            carteChoix2 = choix[1];
            vueBoutique.afficherChoix(carteChoix1, carteChoix2);
        }
    }

    private void validerChoix(TypeCarte carteChoisie) {
        if (mainController.environnement.validerChoixCarte(carteChoisie)) {
            mainController.vueDeck.ajouterCarteView(carteChoisie);
            vueBoutique.fermerChoix();
        }
    }

    @FXML private void clicChoix1() { validerChoix(carteChoix1); }
    @FXML private void clicChoix2() { validerChoix(carteChoix2); }
}
