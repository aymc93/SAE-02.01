package com.example.sae2.controller;

import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.modele.deck.ModeleDeck;
import com.example.sae2.modele.deck.TypeCarte;
import com.example.sae2.modele.ennemis.ModeleEnnemi;
import com.example.sae2.modele.ennemis.ModeleVague;
import com.example.sae2.modele.joueur.ModeleJoueur;
import com.example.sae2.vue.VueGameOver;
import com.example.sae2.vue.carte.VueHover;
import com.example.sae2.vue.carte.VueObstacles;
import com.example.sae2.vue.carte.VueTerrain;
import com.example.sae2.vue.deck.VueAnimPerteCoeur;
import com.example.sae2.vue.deck.VueArgent;
import com.example.sae2.vue.deck.VueCoeurs;
import com.example.sae2.vue.deck.VueDeck;
import com.example.sae2.vue.ennemis.VueEnnemi;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameController {

    @FXML private Pane paneJeu;
    @FXML private TilePane gridTerrain;
    @FXML private Pane tilePaneEntites;
    @FXML private Pane paneTours;
    @FXML private Pane paneEnnemis;
    @FXML private ImageView hoverCase;
    @FXML private HBox groupePouvoirs;
    @FXML private HBox groupeTours;
    @FXML private HBox paneArgent;
    @FXML private HBox paneCoeurs;

    // Modèles
    private ModeleTerrain modele;
    private ModeleDeck modeleDeck;
    private ModeleJoueur modeleJoueur;
    private int T;

    // Vues
    private VueDeck vueDeck;
    private VueHover vueHover;
    private VueArgent vueArgent;
    private VueCoeurs vueCoeurs;
    private VueAnimPerteCoeur vueAnimPerteCoeur;
    private VueGameOver vueGameOver;

    // Vagues
    private static final List<ModeleVague> VAGUES = List.of(
        new ModeleVague(5, "Fatty", 500, 48, 2.0)
    );
    private int indexVague              = 0;
    private int ennemisVivantsDansVague = 0;

    // Etat du jeu
    private final Map<String, ControllerTour>          toursControlleurs = new HashMap<>();
    private final Map<ModeleEnnemi, ControllerEnnemis> ennemisActifs     = new HashMap<>();
    private final List<Timeline>                       timelinesActives  = new ArrayList<>();

    // Sélection en cours
    private TypeCarte carteSelectionnee = null;
    private ImageView carteSelectionneeView = null;

    @FXML
    public void initialize() {
        modele = new ModeleTerrain();
        modele.setNiveau(45);

        T = ModeleTerrain.TAILLE_TUILE;
        int cols = modele.getNbColonnes();
        int rows = modele.getNbLignes();

        paneJeu.setPrefSize(cols * T, rows * T);
        paneTours.setPrefSize(cols * T, rows * T);

        new VueTerrain(modele, gridTerrain);
        new VueObstacles(modele, tilePaneEntites);

        vueHover = new VueHover(hoverCase, T);

        modeleDeck = new ModeleDeck();
        vueDeck = new VueDeck(modeleDeck, groupePouvoirs, groupeTours, this::selectionnerCarte);

        modeleJoueur       = new ModeleJoueur();
        vueArgent          = new VueArgent(paneArgent);
        vueCoeurs          = new VueCoeurs(paneCoeurs);
        vueAnimPerteCoeur  = new VueAnimPerteCoeur(paneJeu);
        vueGameOver        = new VueGameOver();

        demarrerVague();
    }

    // Sélection de carte

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

    private void deselectionner() {
        vueDeck.deselectionner();
        carteSelectionneeView = null;
        carteSelectionnee = null;
        vueHover.cacher();
    }

    // Gestion du hover

    @FXML
    private void actualiserHover(MouseEvent event) {
        if (carteSelectionnee == null) { vueHover.cacher(); return; }
        vueHover.actualiser(event.getX(), event.getY());
    }

    @FXML
    private void cacherHover(MouseEvent event) {
        vueHover.cacher();
    }

    // clic sur le terrain

    @FXML
    private void placerTour(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) return;
        if (carteSelectionnee == null) return;

        int col   = (int) (event.getX() / T);
        int ligne = (int) (event.getY() / T);

        if (ligne < 0 || ligne >= modele.getNbLignes()) return;
        if (col < 0 || col >= modele.getNbColonnes()) return;

        TypeCarte type = carteSelectionnee;
        boolean succes = type.estPouvoir()
                ? tenterAppliquerPouvoir(col, ligne, type)
                : tenterPoserTour(col, ligne);

        if (succes) {
            deselectionner();
            modeleDeck.utiliserCarte(type);
            vueDeck.masquerCarte(type);
        }
    }

    // Lance la vague courante : spawn des ennemis espacés dans le temps
    private void demarrerVague() {
        if (indexVague >= VAGUES.size()) {
            System.out.println("[Vagues] Toutes les vagues terminées !");
            return;
        }
        ModeleVague vague = VAGUES.get(indexVague);
        System.out.println("[Vagues] ── Vague " + (indexVague + 1) + " / " + VAGUES.size()
                + "  (" + vague.getNombreEnnemis() + " ennemis, " + vague.getPvBase() + " PV) ──");

        ennemisVivantsDansVague = vague.getNombreEnnemis();

        for (int i = 0; i < vague.getNombreEnnemis(); i++) {
            double delai = i * vague.getIntervalleSpawn();
            Timeline t = new Timeline(new KeyFrame(Duration.seconds(delai), e -> spawnerEnnemi(vague)));
            timelinesActives.add(t);
            t.play();
        }
    }

    private void spawnerEnnemi(ModeleVague vague) {
        double departX = modele.getColonneEntree() * T;
        double departY = modele.getLigneEntree() * T;

        ModeleEnnemi modEnnemi = new ModeleEnnemi(
                departX, departY, vague.getVitesse(), vague.getTypeEnnemi(), vague.getPvBase());
        VueEnnemi vueEnnemi = new VueEnnemi(modEnnemi, paneEnnemis, modele.getDossierSprites());
        ControllerEnnemis ctrl = new ControllerEnnemis(
                modEnnemi, vueEnnemi, modele,
                () -> {
                    boolean estTue = modEnnemi.estMort();
                    ennemisActifs.remove(modEnnemi);
                    if (estTue) {
                        modeleJoueur.gagnerArgentKill();
                        vueArgent.actualiser(modeleJoueur.getArgent());
                    } else {
                        modeleJoueur.perdreVie();
                        vueCoeurs.actualiser(modeleJoueur.getVies());
                        vueAnimPerteCoeur.jouer();
                        if (!modeleJoueur.estEnVie()) {
                            arreterJeu();
                            Stage stage = (Stage) paneJeu.getScene().getWindow();
                            vueGameOver.afficher(stage);
                        }
                    }
                    if (--ennemisVivantsDansVague == 0 && modeleJoueur.estEnVie()) {
                        indexVague++;
                        Timeline t = new Timeline(new KeyFrame(Duration.seconds(4), e -> demarrerVague()));
                        timelinesActives.add(t);
                        t.play();
                    }
                });

        ennemisActifs.put(modEnnemi, ctrl);
        ctrl.demarrer();
    }

    // Arrête tous les timers et timelines (game over)
    private void arreterJeu() {
        timelinesActives.forEach(Timeline::stop);
        timelinesActives.clear();
        ennemisActifs.values().forEach(ControllerEnnemis::stopper);
        toursControlleurs.values().forEach(ControllerTour::stopper);
    }

    // Pose d'une tour

    private boolean tenterPoserTour(int col, int ligne) {
        if (!modele.estConstructible(ligne, col)) return false;
        String cle = ligne + "," + col;
        if (toursControlleurs.containsKey(cle)) return false;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/sae2/tour.fxml"));
            Group tourNode = loader.load();
            ControllerTour ct = loader.getController();

            tourNode.setLayoutX(col * T);
            tourNode.setLayoutY(ligne * T);
            paneTours.getChildren().add(tourNode);

            ct.configurer(col, ligne, col * T + T / 2.0, ligne * T + T / 2.0, 3.0 * T,
                    () -> ennemisActifs.keySet());
            toursControlleurs.put(cle, ct);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Application d'un pouvoir

    private boolean tenterAppliquerPouvoir(int col, int ligne, TypeCarte pouvoir) {
        ControllerTour ct = toursControlleurs.get(ligne + "," + col);
        if (ct == null) return false;
        ct.appliquerPouvoir(pouvoir);
        return true;
    }
}
