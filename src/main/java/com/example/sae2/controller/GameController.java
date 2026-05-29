package com.example.sae2.controller;

import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.modele.ennemis.ModeleEnnemi;
import com.example.sae2.vue.carte.VueObstacles;
import com.example.sae2.vue.carte.VueTerrain;
import com.example.sae2.vue.ennemis.VueEnnemi;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GameController {

    @FXML private Pane      paneJeu;
    @FXML private TilePane  gridTerrain;
    @FXML private Pane      tilePaneEntites;
    @FXML private Pane      paneTours;
    @FXML private ImageView imageViewEnnemi;

    private ModeleTerrain modele;
    private ModeleEnnemi  modeleEnnemi;
    private int           T;
    private final Set<String> toursPlacees = new HashSet<>();

    @FXML
    public void initialize() {
        modele = new ModeleTerrain();
        modele.setNiveau(45);

        int cols = modele.getNbColonnes();
        int rows = modele.getNbLignes();
        T = ModeleTerrain.TAILLE_TUILE;

        paneJeu.setPrefSize(cols * T, rows * T);

        gridTerrain.setPrefTileWidth(T);
        gridTerrain.setPrefTileHeight(T);
        gridTerrain.setPrefColumns(cols);
        gridTerrain.setPrefSize(cols * T, rows * T);
        new VueTerrain(modele, gridTerrain);

        tilePaneEntites.setPrefSize(cols * T, rows * T);
        for (int l = 0; l < rows; l++) {
            for (int c = 0; c < cols; c++) {
                Region cellule = new Region();
                cellule.setLayoutX(c * T);
                cellule.setLayoutY(l * T);
                cellule.setPrefSize(T, T);
                tilePaneEntites.getChildren().add(cellule);
            }
        }
        new VueObstacles(modele, tilePaneEntites);

        paneTours.setPrefSize(cols * T, rows * T);

        double departX = modele.getColonneEntree() * T;
        double departY = modele.getLigneEntree()   * T;

        modeleEnnemi = new ModeleEnnemi(departX, departY, 48, "Fatty");
        VueEnnemi vueEnnemi = new VueEnnemi(modeleEnnemi, imageViewEnnemi, modele.getDossierSprites());
        new ControllerEnnemis(modeleEnnemi, vueEnnemi, modele).demarrer();
    }

    /** Géré en FXML via onMouseClicked="#placerTour" sur paneTours. */
    @FXML
    private void placerTour(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) return;

        int col   = (int) (event.getX() / T);
        int ligne = (int) (event.getY() / T);

        if (ligne < 0 || ligne >= modele.getNbLignes()) return;
        if (col  < 0 || col   >= modele.getNbColonnes()) return;
        if (!estConstructible(ligne, col)) return;

        String cle = ligne + "," + col;
        if (toursPlacees.contains(cle)) return;
        toursPlacees.add(cle);

        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/sae2/tour.fxml")
            );
            Group tourNode = loader.load();
            ControllerTour ct = loader.getController();

            tourNode.setLayoutX(col   * T);
            tourNode.setLayoutY(ligne * T);

            // Centre absolu dans paneJeu pour le calcul de distance
            ct.configurer(col * T + T / 2.0, ligne * T + T / 2.0, 3.0 * T, modeleEnnemi);

            paneTours.getChildren().add(tourNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean estConstructible(int ligne, int colonne) {
        if (!modele.getTuile(ligne, colonne).isSolSimple()) return false;
        if (modele.getObstaclesGrille()[ligne][colonne] != null) return false;
        return true;
    }
}
