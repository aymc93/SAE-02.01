package com.example.sae2.controller;

import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.modele.deck.ModeleDeck;
import com.example.sae2.modele.deck.TypeCarte;
import com.example.sae2.modele.ennemis.ModeleEnnemi;
import com.example.sae2.vue.carte.VueObstacles;
import com.example.sae2.vue.carte.VueTerrain;
import com.example.sae2.vue.deck.VueDeck;
import com.example.sae2.vue.ennemis.VueEnnemi;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameController {

    // ── Nœuds FXML ───────────────────────────────────────────────────────────
    @FXML private Pane      paneJeu;
    @FXML private TilePane  gridTerrain;
    @FXML private Pane      tilePaneEntites;
    @FXML private Pane      paneTours;
    @FXML private Pane      paneEnnemis;
    @FXML private ImageView hoverCase;

    // Conteneurs du deck (remplis par VueDeck)
    @FXML private HBox groupePouvoirs;
    @FXML private HBox groupeTours;

    // ── Modèle ────────────────────────────────────────────────────────────────
    private ModeleTerrain modele;
    private ModeleDeck    modeleDeck;
    private int           T;

    // ── Vue du deck ───────────────────────────────────────────────────────────
    private VueDeck vueDeck;

    // clé "ligne,col" → contrôleur de la tour posée sur cette case
    private final Map<String, ControllerTour> toursControlleurs = new HashMap<>();

    // Liste des ennemis actifs sur le terrain (modèle → contrôleur)
    private final Map<ModeleEnnemi, ControllerEnnemis> ennemisActifs = new HashMap<>();

    // Référence au dernier ennemi spawné (pour le ciblage des tours)
    private ModeleEnnemi modeleEnnemiActuel = null;

    // ── Sélection en cours ────────────────────────────────────────────────────
    private TypeCarte  carteSelectionnee     = null;
    private ImageView  carteSelectionneeView = null;

    // =========================================================================
    // Initialisation
    // =========================================================================

    @FXML
    public void initialize() {
        modele = new ModeleTerrain();
        modele.setNiveau(5);

        int cols = modele.getNbColonnes();
        int rows = modele.getNbLignes();
        T = ModeleTerrain.TAILLE_TUILE;

        paneJeu.setPrefSize(cols * T, rows * T);

        gridTerrain.setPrefTileWidth(T);
        gridTerrain.setPrefTileHeight(T);
        gridTerrain.setPrefColumns(cols);
        gridTerrain.setPrefSize(cols * T, rows * T);
        new VueTerrain(modele, gridTerrain);

        new VueObstacles(modele, tilePaneEntites);

        paneTours.setPrefSize(cols * T, rows * T);

        spawnerEnnemi();

        // Deck : modèle + vue liés
        modeleDeck = new ModeleDeck();
        vueDeck    = new VueDeck(modeleDeck, groupePouvoirs, groupeTours, this::selectionnerCarte);
    }

    // =========================================================================
    // Sélection de carte (callback depuis VueDeck)
    // =========================================================================

    private void selectionnerCarte(TypeCarte type, ImageView vue) {
        if (carteSelectionneeView == vue) {
            // Clic sur la carte déjà sélectionnée → désélectionner
            deselectionner();
        } else {
            deselectionner();
            carteSelectionnee     = type;
            carteSelectionneeView = vue;
            vue.getStyleClass().add("carte-selectionnee");
        }
    }

    private void deselectionner() {
        if (carteSelectionneeView != null) {
            carteSelectionneeView.getStyleClass().remove("carte-selectionnee");
            carteSelectionneeView = null;
        }
        carteSelectionnee = null;
        hoverCase.setVisible(false);
    }

    // =========================================================================
    // Indicateur de survol (hover_case snap-to-grid)
    // =========================================================================

    @FXML
    private void actualiserHover(MouseEvent event) {
        if (carteSelectionnee == null) {
            hoverCase.setVisible(false);
            return;
        }
        int col   = (int) (event.getX() / T);
        int ligne = (int) (event.getY() / T);
        hoverCase.setLayoutX(col   * T);
        hoverCase.setLayoutY(ligne * T);
        hoverCase.setVisible(true);
    }

    @FXML
    private void cacherHover(MouseEvent event) {
        hoverCase.setVisible(false);
    }

    // =========================================================================
    // Clic sur le terrain
    // =========================================================================

    @FXML
    private void placerTour(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) return;
        if (carteSelectionnee == null) return;

        int col   = (int) (event.getX() / T);
        int ligne = (int) (event.getY() / T);

        if (ligne < 0 || ligne >= modele.getNbLignes())   return;
        if (col  < 0 || col   >= modele.getNbColonnes()) return;

        TypeCarte type = carteSelectionnee;
        boolean succes;

        if (type.estPouvoir()) {
            succes = tenterAppliquerPouvoir(col, ligne, type);
        } else {
            succes = tenterPoserTour(col, ligne);
        }

        // La carte n'est consommée que si le placement a réussi
        if (succes) {
            deselectionner();
            modeleDeck.utiliserCarte(type);
            vueDeck.masquerCarte(type);
        }
    }

    // =========================================================================
    // Spawn d'un ennemi
    // =========================================================================

    private void spawnerEnnemi() {
        double departX = modele.getColonneEntree() * T;
        double departY = modele.getLigneEntree()   * T;

        ModeleEnnemi modEnnemi = new ModeleEnnemi(departX, departY, 48, "Fatty");
        VueEnnemi    vueEnnemi = new VueEnnemi(modEnnemi, paneEnnemis, modele.getDossierSprites());

        ControllerEnnemis ctrl = new ControllerEnnemis(
            modEnnemi, vueEnnemi, modele,
            () -> ennemisActifs.remove(modEnnemi)   // onMort : retire de la map
        );

        ennemisActifs.put(modEnnemi, ctrl);
        modeleEnnemiActuel = modEnnemi;
        ctrl.demarrer();
    }

    // ── Pose d'une tour ───────────────────────────────────────────────────────

    private boolean tenterPoserTour(int col, int ligne) {
        if (!estConstructible(ligne, col)) return false;
        String cle = ligne + "," + col;
        if (toursControlleurs.containsKey(cle)) return false;

        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/sae2/tour.fxml")
            );
            Group          tourNode = loader.load();
            ControllerTour ct       = loader.getController();

            tourNode.setLayoutX(col   * T);
            tourNode.setLayoutY(ligne * T);
            paneTours.getChildren().add(tourNode);

            ct.configurer(col * T + T / 2.0, ligne * T + T / 2.0, 3.0 * T, modeleEnnemiActuel);
            toursControlleurs.put(cle, ct);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Application d'un pouvoir ──────────────────────────────────────────────

    private boolean tenterAppliquerPouvoir(int col, int ligne, TypeCarte pouvoir) {
        String cle = ligne + "," + col;
        ControllerTour ct = toursControlleurs.get(cle);
        if (ct == null) return false; // pas de tour sur cette case

        ct.appliquerPouvoir(pouvoir);
        return true;
    }

    // ── Vérification de constructibilité ─────────────────────────────────────

    private boolean estConstructible(int ligne, int colonne) {
        if (!modele.getTuile(ligne, colonne).isSolSimple()) return false;
        if (modele.getObstaclesGrille()[ligne][colonne] != null) return false;
        return true;
    }
}
