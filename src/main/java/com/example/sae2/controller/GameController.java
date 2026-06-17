package com.example.sae2.controller;

import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.modele.deck.ModeleDeck;
import com.example.sae2.modele.deck.TypeCarte;
import com.example.sae2.modele.joueur.ModeleJoueur;
import com.example.sae2.vue.carte.VueHover;
import com.example.sae2.vue.carte.VueObstacles;
import com.example.sae2.vue.carte.VueTerrain;
import com.example.sae2.vue.deck.VueArgent;
import com.example.sae2.vue.deck.VueCoeurs;
import com.example.sae2.vue.deck.VueDeck;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;

public class GameController {

    @FXML public Pane paneJeu;
    @FXML private TilePane gridTerrain;
    @FXML private Pane tilePaneEntites;
    @FXML public Pane paneTours;
    @FXML public Pane paneEnnemis;
    @FXML private ImageView hoverCase;
    @FXML private HBox groupePouvoirs;
    @FXML private HBox groupeTours;
    @FXML private HBox paneArgent;
    @FXML private HBox paneCoeurs;

    // Injection automatique du contrôleur de la boutique par JavaFX
    @FXML private ShopController boutiqueController;

    public ModeleTerrain modele;
    public ModeleDeck modeleDeck;
    public ModeleJoueur modeleJoueur;
    public int T;

    public VueDeck vueDeck;
    private VueHover vueHover;
    public VueArgent vueArgent;
    public VueCoeurs vueCoeurs;

    private VueTerrain vueTerrain;

    private WaveController waveController;
    private TowerController towerController;

    public TypeCarte carteSelectionnee = null;
    private ImageView carteSelectionneeView = null;

    @FXML
    public void initialize() {
        modele = new ModeleTerrain();
        modele.setNiveau(5);
        T = ModeleTerrain.TAILLE_TUILE;

        paneJeu.setPrefSize(modele.getNbColonnes() * T, modele.getNbLignes() * T);
        paneTours.setPrefSize(modele.getNbColonnes() * T, modele.getNbLignes() * T);

        vueTerrain = new VueTerrain(modele, gridTerrain);
        new VueObstacles(modele, tilePaneEntites);
        vueHover = new VueHover(hoverCase, T);

        modeleDeck = new ModeleDeck();
        vueDeck = new VueDeck(modeleDeck, groupePouvoirs, groupeTours, this::selectionnerCarte);

        modeleJoueur = new ModeleJoueur();
        vueArgent = new VueArgent(paneArgent);
        vueArgent.actualiser(modeleJoueur.getArgent());
        vueCoeurs = new VueCoeurs(paneCoeurs);

        // Liaison avec le ShopController créé par le FXML
        if (boutiqueController != null) {
            boutiqueController.init(this);
        }

        waveController = new WaveController(this);
        towerController = new TowerController(this, waveController);

        waveController.demarrerVague();
    }

    @FXML
    private void ouvrirBoutiqueDirecte() {
        boutiqueController.ouvrirBoutique();
    }

    private void selectionnerCarte(TypeCarte type, ImageView vue) {
        if (carteSelectionneeView == vue) {
            deselectionner();
        } else {
            deselectionner();
            carteSelectionnee = type;
            carteSelectionneeView = vue;
            vueDeck.selectionner(vue);
        }
    }

    public void deselectionner() {
        vueDeck.deselectionner();
        carteSelectionneeView = null;
        carteSelectionnee = null;
        vueHover.cacher();
    }

    @FXML
    private void actualiserHover(MouseEvent event) {
        if (carteSelectionnee == null) { vueHover.cacher(); return; }
        vueHover.actualiser(event.getX(), event.getY());
    }

    @FXML
    private void cacherHover(MouseEvent event) {
        vueHover.cacher();
    }

    @FXML
    private void placerTour(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY || carteSelectionnee == null) return;

        int col = (int) (event.getX() / T);
        int ligne = (int) (event.getY() / T);

        System.out.println("Clic détecté -> Ligne : " + ligne + " | Col : " + col + " | Constructible : " + modele.estConstructible(ligne, col));

        if (ligne < 0 || ligne >= modele.getNbLignes() || col < 0 || col >= modele.getNbColonnes()) return;

        TypeCarte type = carteSelectionnee;
        boolean succes = type.estPouvoir()
                ? towerController.tenterAppliquerPouvoir(col, ligne, type)
                : towerController.tenterPoserTour(col, ligne);

        if (succes) {
            deselectionner();
            modeleDeck.utiliserCarte(type);
            vueDeck.masquerCarte(type);
        }
    }

    public void arreterTout() {
        waveController.arreter();
        towerController.arreter();
    }

    public VueTerrain getVueTerrain() {
        return vueTerrain;
    }

    public TilePane getGridTerrain() {
        return gridTerrain;
    }
}