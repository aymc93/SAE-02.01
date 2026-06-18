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

    // --- Accès rapides pour les autres contrôleurs ---
    public ModeleTerrain modele;
    public int T;
    // -------------------------------------------------

    public ModeleDeck modeleDeck;
    public ModeleJoueur modeleJoueur;

    /** Coeur logique du jeu (le Modèle d'orchestration). */
    public Environnement environnement;

    public VueDeck vueDeck;
    public VueArgent vueArgent;
    public VueCoeurs vueCoeurs;

    // --- Nos 4 Contrôleurs du jeu ---
    private TerrainController terrainController;
    private WaveController waveController;
    private EnnemiController ennemiController;
    public TowerController towerController;

    public TypeCarte carteSelectionnee = null;
    private ImageView carteSelectionneeView = null;

    @FXML
    public void initialize() {
        // 1. Initialisation du Terrain
        terrainController = new TerrainController(this, gridTerrain, tilePaneEntites, hoverCase);

        // Raccourcis pour ne pas casser tes autres contrôleurs
        modele = terrainController.getModele();
        T = terrainController.getT();

        // 2. Initialisation du Deck et Joueur
        modeleDeck = new ModeleDeck();
        vueDeck = new VueDeck(modeleDeck, groupePouvoirs, groupeTours, this::selectionnerCarte);

        modeleJoueur = new ModeleJoueur();
        vueArgent = new VueArgent(paneArgent);
        vueArgent.actualiser(modeleJoueur.getArgent());
        vueCoeurs = new VueCoeurs(paneCoeurs);

        if (boutiqueController != null) {
            boutiqueController.init(this);
        }

        // 2.5. Création du coeur logique : l'Environnement orchestre tout le jeu.
        environnement = new Environnement(modele, modeleJoueur, modeleDeck, T);

        // 3. Initialisation des Vagues, Ennemis et Tours
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
        terrainController.cacherHover(); // On passe par le TerrainController
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
    private void cacherHover(MouseEvent event) {
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

    // Utilisé par le WaveController pour changer de niveau
    public VueTerrain getVueTerrain() {
        return terrainController.getVueTerrain();
    }

    public TilePane getGridTerrain() {
        return gridTerrain;
    }

    /** Appelé par la Vue quand le joueur relâche un obstacle avec sa souris */
    public void traiterDropObstacle(ImageView iv, int oldCol, int oldLigne) {
        // On calcule la case d'arrivée en fonction du centre de l'image
        int newCol = (int) ((iv.getLayoutX() + T / 2.0) / T);
        int newLigne = (int) ((iv.getLayoutY() + T / 2.0) / T);

        boolean deplacementReussi = false;

        // Si le joueur l'a vraiment déplacé sur une autre case
        if (newCol != oldCol || newLigne != oldLigne) {
            // On vérifie qu'il n'y a pas de tour ici
            boolean aUneTour = towerController != null && towerController.possedeTour(newLigne, newCol);

            if (!aUneTour) {
                deplacementReussi = modele.deplacerObstacle(oldLigne, oldCol, newLigne, newCol);
            }
        }

        if (deplacementReussi) {
            // Le déplacement est validé : on centre l'image sur la case
            iv.setLayoutX(newCol * T);
            iv.setLayoutY(newLigne * T);

            // On ordonne aux monstres de recalculer leur chemin pour esquiver !
            if (ennemiController != null) {
                ennemiController.recalculerChemins();
            }

            // On mémorise la nouvelle position
            iv.getProperties().put("oldCol", newCol);
            iv.getProperties().put("oldLigne", newLigne);
        } else {
            // Annulé : l'obstacle retourne automatiquement à sa place d'origine
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