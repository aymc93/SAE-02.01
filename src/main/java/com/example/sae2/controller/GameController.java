package com.example.sae2.controller;

import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.modele.Environnement;
import com.example.sae2.modele.deck.ModeleDeck;
import com.example.sae2.modele.deck.TypeCarte;
import com.example.sae2.modele.joueur.ModeleJoueur;
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
    @FXML private ShopController boutiqueController;
    public ModeleTerrain modele;
    public int T;
    public ModeleDeck modeleDeck;
    public ModeleJoueur modeleJoueur;
    public Environnement environnement;
    public VueDeck vueDeck;
    public VueArgent vueArgent;
    public VueCoeurs vueCoeurs;
    private TerrainController terrainController;
    private WaveController waveController;
    private EnnemiController ennemiController;
    public TowerController towerController;
    public TypeCarte carteSelectionnee = null;
    private ImageView carteSelectionneeView = null;

    @FXML
    public void initialize() {
        terrainController = new TerrainController(this, gridTerrain, tilePaneEntites, hoverCase);
        modele = terrainController.getModele();
        T = terrainController.getT();
        modeleDeck = new ModeleDeck();
        vueDeck = new VueDeck(modeleDeck, groupePouvoirs, groupeTours, this::selectionnerCarte);
        modeleJoueur = new ModeleJoueur();
        vueArgent = new VueArgent(paneArgent);
        vueArgent.actualiser(modeleJoueur.getArgent());
        vueCoeurs = new VueCoeurs(paneCoeurs);
        environnement = new Environnement(modele, modeleJoueur, modeleDeck, T);

        if (boutiqueController != null) {
            boutiqueController.init(this);
        }
        waveController = new WaveController(this);
        ennemiController = new EnnemiController(this, waveController);
        waveController.setEnnemiController(ennemiController);
        towerController = new TowerController(this, ennemiController);
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
        terrainController.cacherHover();
    }

    @FXML
    private void actualiserHover(MouseEvent event) {
        if (carteSelectionnee == null) {
            terrainController.cacherHover();
            return;
        }
        terrainController.actualiserHover(event.getX(), event.getY());
    }

    @FXML
    private void cacherHover() {
        terrainController.cacherHover();
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
        if (waveController != null) waveController.arreter();
        if (towerController != null) towerController.arreter();
        if (ennemiController != null) ennemiController.arreterTout();
    }

    public TilePane getGridTerrain() {
        return gridTerrain;
    }

    public void traiterDropObstacle(ImageView iv, int oldCol, int oldLigne) {
        int newCol = (int) ((iv.getLayoutX() + T / 2.0) / T);
        int newLigne = (int) ((iv.getLayoutY() + T / 2.0) / T);
        boolean deplacementReussi = environnement.tenterDeplacerObstacle(oldLigne, oldCol, newLigne, newCol);

        if (deplacementReussi) {
            iv.setLayoutX(newCol * T);
            iv.setLayoutY(newLigne * T);
            iv.getProperties().put("oldCol", newCol);
            iv.getProperties().put("oldLigne", newLigne);
        } else {
            iv.setLayoutX(oldCol * T);
            iv.setLayoutY(oldLigne * T);
        }
    }

    public void changerNiveauMap(int nouveauNiveau) {
        if (terrainController != null) {
            terrainController.changerNiveauMap(nouveauNiveau, tilePaneEntites);
        }
    }
}